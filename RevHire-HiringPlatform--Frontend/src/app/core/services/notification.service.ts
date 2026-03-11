import { Injectable, signal, computed, inject, Injector } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Notification } from '../models/notification.model';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class NotificationService {
    private http = inject(HttpClient);
    private authService = inject(AuthService);
    private readonly API_URL = `${environment.apiUrl}/notifications`;

    private _notifications = signal<Notification[]>([]);
    private _loading = signal(false);
    readonly notifications = this._notifications.asReadonly();
    readonly unreadCount = computed(() => this._notifications().filter(n => !n.isRead).length);
    readonly loading = this._loading.asReadonly();

    private pollingInterval: any;
    private injector = inject(Injector);

    constructor() {
        // Proactively load notifications when user logs in
        import('@angular/core').then(m => {
            m.effect(() => {
                const user = this.authService.currentUser();
                if (user) {
                    this.loadForUser().subscribe();
                    this.startPolling();
                } else {
                    this.stopPolling();
                    this._notifications.set([]);
                }
            }, { allowSignalWrites: true, injector: this.injector });
        });
    }

    private startPolling(): void {
        if (this.pollingInterval) return;
        this.pollingInterval = setInterval(() => {
            this.loadForUser().subscribe();
        }, 20000); // Poll every 20 seconds
    }

    private stopPolling(): void {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
        }
    }

    loadForUser(): Observable<Notification[]> {
        this._loading.set(true);
        console.log('Loading notifications from:', this.API_URL);
        return this.http.get<Notification[]>(this.API_URL).pipe(
            tap({
                next: (ns) => {
                    console.log('Notifications received:', ns);
                    // Standardize isRead property (handle backend 'read' vs frontend 'isRead')
                    const normalized = (ns || []).map(n => ({
                        ...n,
                        isRead: n.isRead !== undefined ? n.isRead : (n as any).read
                    }));
                    const sorted = [...normalized].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
                    this._notifications.set(sorted);
                    this._loading.set(false);
                },
                error: (err) => {
                    console.error('Error loading notifications:', err);
                    this._loading.set(false);
                }
            })
        );
    }

    markRead(id: number): Observable<void> {
        console.log('Marking notification read id:', id);
        // Optimistic update: Update the signal immediately
        this._notifications.update(ns => ns.map(n => n.id === id ? { ...n, isRead: true } : n));

        return this.http.put<void>(`${this.API_URL}/${id}/read`, {}).pipe(
            tap({
                error: (err) => {
                    console.error('Failed to mark as read in backend, reverting...', err);
                    // Revert UI if needed, but for Read flag we can usually stay optimistic
                }
            })
        );
    }

    markAllRead(): Observable<void> {
        return this.http.put<void>(`${this.API_URL}/mark-all-read`, {}).pipe(
            tap(() => {
                this._notifications.update(ns => ns.map(n => ({ ...n, isRead: true })));
            })
        );
    }

    deleteNotification(id: number): Observable<any> {
        return this.http.delete(`${this.API_URL}/${id}`, { responseType: 'text' }).pipe(
            tap(() => {
                this._notifications.update(ns => ns.filter(n => n.id !== id));
            })
        );
    }

    deleteAllNotifications(): Observable<any> {
        return this.http.delete(`${this.API_URL}/all`, { responseType: 'text' }).pipe(
            tap(() => this._notifications.set([]))
        );
    }

    getNotificationById(id: number): Observable<Notification> {
        return this.http.get<Notification>(`${this.API_URL}/${id}`);
    }
}
