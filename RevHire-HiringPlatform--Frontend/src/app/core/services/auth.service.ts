import { Injectable, signal, computed } from '@angular/core';
import { Observable, tap, timeout } from 'rxjs';
import { User, LoginRequest, RegisterRequest, AuthResponse, PasswordResetRequest, PasswordUpdateRequest } from '../models/user.model';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private _currentUser = signal<User | null>(null);
    private _isLoading = signal(false);

    readonly currentUser = this._currentUser.asReadonly();
    readonly isAuthenticated = computed(() => this._currentUser() !== null);
    readonly isSeeker = computed(() => this._currentUser()?.role === 'JOB_SEEKER');
    readonly isEmployer = computed(() => this._currentUser()?.role === 'EMPLOYER');
    readonly isAdmin = computed(() => this._currentUser()?.role === 'ADMIN');
    readonly isLoading = this._isLoading.asReadonly();

    private readonly API_URL = `${environment.apiUrl}/auth`;

    constructor(private http: HttpClient, private router: Router) {
        const stored = localStorage.getItem('revhire_user');
        if (stored) {
            try { this._currentUser.set(JSON.parse(stored)); } catch { }
        }
    }

    login(req: LoginRequest): Observable<AuthResponse> {
        this._isLoading.set(true);
        return this.http.post<AuthResponse>(`${this.API_URL}/login`, req).pipe(
            tap(res => this.handleAuthResponse(res)),
            tap(() => this._isLoading.set(false))
        );
    }

    adminLogin(req: LoginRequest): Observable<AuthResponse> {
        this._isLoading.set(true);
        return this.http.post<AuthResponse>(`${this.API_URL}/admin/login`, req).pipe(
            tap(res => this.handleAuthResponse(res)),
            tap(() => this._isLoading.set(false))
        );
    }

    register(req: RegisterRequest): Observable<any> {
        this._isLoading.set(true);
        return this.http.post<any>(`${this.API_URL}/register`, req).pipe(
            tap(() => this._isLoading.set(false))
        );
    }

    sendOtp(email: string): Observable<string> {
        return this.http.post(`${this.API_URL}/send-otp`, null, {
            params: { email },
            responseType: 'text'
        });
    }

    verifyOtp(email: string, otp: string): Observable<string> {
        return this.http.post(`${this.API_URL}/verify-otp`, null, {
            params: { email, otp },
            responseType: 'text'
        });
    }

    refreshToken(): Observable<AuthResponse> {
        const token = localStorage.getItem('revhire_refresh_token');
        return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken: token }).pipe(
            tap(res => this.handleAuthResponse(res))
        );
    }

    forgotPassword(email: string): Observable<string> {
        return this.http
            .post(`${this.API_URL}/forgot-password`, { email }, { responseType: 'text' })
            .pipe(timeout(15000));
    }

    resetPassword(req: PasswordResetRequest): Observable<string> {
        return this.http.post(`${this.API_URL}/reset-password`, req, { responseType: 'text' });
    }

    updatePassword(req: PasswordUpdateRequest): Observable<string> {
        return this.http.post(`${this.API_URL}/update-password`, req, { responseType: 'text' });
    }

    private handleAuthResponse(res: AuthResponse): void {
        const persistedAvatar = this.getPersistedAvatar(res.id);
        const user: User = {
            id: res.id,
            name: res.name,
            email: res.email,
            role: res.role,
            avatar: persistedAvatar || undefined,
            createdAt: new Date().toISOString(),
            isProfileComplete: false // Default to false, components will update this
        };

        this._currentUser.set(user);
        localStorage.setItem('revhire_token', res.token);
        localStorage.setItem('revhire_refresh_token', res.refreshToken);
        localStorage.setItem('revhire_user', JSON.stringify(user));
    }

    logout(): void {
        this.http.post(`${this.API_URL}/logout`, {}).subscribe({
            next: () => this.clearSession(),
            error: () => this.clearSession()
        });
    }

    private clearSession(): void {
        this._currentUser.set(null);
        localStorage.removeItem('revhire_token');
        localStorage.removeItem('revhire_refresh_token');
        localStorage.removeItem('revhire_user');
        this.router.navigate(['/login']);
    }

    getDashboardRoute(): string {
        const user = this._currentUser();
        if (!user) return '/login';

        if (user.role === 'JOB_SEEKER' && !user.isProfileComplete) {
            return '/seeker/profile';
        }

        if (user.role === 'EMPLOYER' && !user.isProfileComplete) {
            return '/employer/company';
        }

        if (user.role === 'ADMIN') {
            return '/admin/dashboard';
        }

        return user.role === 'EMPLOYER' ? '/employer/dashboard' : '/seeker/dashboard';
    }

    updateProfileStatus(isComplete: boolean): void {
        const user = this._currentUser();
        if (user) {
            const updated = { ...user, isProfileComplete: isComplete };
            this._currentUser.set(updated);
            localStorage.setItem('revhire_user', JSON.stringify(updated));
        }
    }

    updateCurrentUser(patch: Partial<User>): void {
        const user = this._currentUser();
        if (!user) return;

        const updated: User = { ...user, ...patch };
        this._currentUser.set(updated);
        localStorage.setItem('revhire_user', JSON.stringify(updated));

        // Persist avatar per user so it survives logout/login until explicitly removed.
        if (Object.prototype.hasOwnProperty.call(patch, 'avatar')) {
            if (patch.avatar) {
                this.setPersistedAvatar(user.id, patch.avatar);
            } else {
                this.clearPersistedAvatar(user.id);
            }
        }
    }

    private getAvatarStorageKey(userId: number): string {
        return `revhire_avatar_${userId}`;
    }

    private setPersistedAvatar(userId: number, avatar: string): void {
        localStorage.setItem(this.getAvatarStorageKey(userId), avatar);
    }

    private getPersistedAvatar(userId: number): string | null {
        return localStorage.getItem(this.getAvatarStorageKey(userId));
    }

    private clearPersistedAvatar(userId: number): void {
        localStorage.removeItem(this.getAvatarStorageKey(userId));
    }
}
