import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';

const mockRegisterResponse = { role: 'JOB_SEEKER', name: 'John Doe' };

const authServiceMock = {
    register: jasmine.createSpy('register').and.returnValue(of(mockRegisterResponse)),
    login: jasmine.createSpy('login').and.returnValue(of({ role: 'JOB_SEEKER', name: 'John Doe' })),
};

const toastServiceMock = {
    success: jasmine.createSpy('success'),
    error: jasmine.createSpy('error'),
};

const loadingServiceMock = {
    start: jasmine.createSpy('start'),
    stop: jasmine.createSpy('stop'),
};

describe('RegisterComponent', () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;

    beforeEach(async () => {
        authServiceMock.register.calls.reset();
        authServiceMock.login.calls.reset();
        toastServiceMock.success.calls.reset();
        loadingServiceMock.start.calls.reset();
        loadingServiceMock.stop.calls.reset();
        authServiceMock.register.and.returnValue(of(mockRegisterResponse));
        authServiceMock.login.and.returnValue(of({ role: 'JOB_SEEKER', name: 'John Doe' }));

        await TestBed.configureTestingModule({
            imports: [RegisterComponent, HttpClientTestingModule, RouterTestingModule],
            providers: [
                provideRouter([]),
                { provide: AuthService,   useValue: authServiceMock },
                { provide: ToastService,  useValue: toastServiceMock },
                { provide: LoadingService, useValue: loadingServiceMock },
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(RegisterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    // ── Smoke test ────────────────────────────────────────────────────────────
    it('should create', () => {
        expect(component).toBeTruthy();
    });

    // ── Initial state ─────────────────────────────────────────────────────────
    it('should initialise with JOB_SEEKER role selected', () => {
        expect(component.selectedRole()).toBe('JOB_SEEKER');
    });

    it('should initialise with an invalid form', () => {
        expect(component.registerForm.invalid).toBeTrue();
    });

    it('should have submitted = false on init', () => {
        expect(component.submitted).toBeFalse();
    });

    // ── Role toggle ───────────────────────────────────────────────────────────
    it('should switch role to EMPLOYER', () => {
        component.selectedRole.set('EMPLOYER');
        expect(component.selectedRole()).toBe('EMPLOYER');
    });

    // ── Form validation ───────────────────────────────────────────────────────
    it('should mark form invalid when required fields are empty', () => {
        component.onSubmit();
        expect(component.submitted).toBeTrue();
        expect(component.registerForm.invalid).toBeTrue();
    });

    it('should detect password mismatch', () => {
        component.registerForm.patchValue({
            name: 'John Doe',
            email: 'john@example.com',
            password: 'password123',
            confirmPassword: 'different456',
            terms: true,
        });
        component.registerForm.updateValueAndValidity();
        expect(component.registerForm.errors?.['mismatch']).toBeTrue();
    });

    it('should be valid when all fields are correct and passwords match', () => {
        component.registerForm.patchValue({
            name: 'John Doe',
            email: 'john@example.com',
            password: 'password123',
            confirmPassword: 'password123',
            terms: true,
        });
        component.registerForm.updateValueAndValidity();
        expect(component.registerForm.invalid).toBeFalse();
    });

    it('should require companyName when role is EMPLOYER', () => {
        component.selectedRole.set('EMPLOYER');
        component.registerForm.patchValue({
            name: 'Jane',
            email: 'jane@corp.com',
            password: 'password123',
            confirmPassword: 'password123',
            companyName: '',
            terms: true,
        });
        component.registerForm.updateValueAndValidity();
        expect(component.registerForm.invalid).toBeTrue();
    });

    // ── Submission ────────────────────────────────────────────────────────────
    it('should not call auth.register if form is invalid', () => {
        component.onSubmit();
        expect(authServiceMock.register).not.toHaveBeenCalled();
    });

    it('should call auth.register with correct payload on valid submit', () => {
        component.registerForm.patchValue({
            name: 'John Doe',
            email: 'john@example.com',
            password: 'password123',
            confirmPassword: 'password123',
            terms: true,
        });
        component.onSubmit();
        expect(authServiceMock.register).toHaveBeenCalledWith(jasmine.objectContaining({
            name: 'John Doe',
            email: 'john@example.com',
            password: 'password123',
            role: 'JOB_SEEKER',
        }));
    });

    it('should call loadingService.start on valid submit', () => {
        component.registerForm.patchValue({
            name: 'John Doe',
            email: 'john@example.com',
            password: 'password123',
            confirmPassword: 'password123',
            terms: true,
        });
        component.onSubmit();
        expect(loadingServiceMock.start).toHaveBeenCalled();
    });

    it('should show toast and stop loading on successful register + login', () => {
        component.registerForm.patchValue({
            name: 'John Doe',
            email: 'john@example.com',
            password: 'password123',
            confirmPassword: 'password123',
            terms: true,
        });
        component.onSubmit();
        expect(toastServiceMock.success).toHaveBeenCalledWith("Account created! Let's complete your profile.");
        expect(loadingServiceMock.stop).toHaveBeenCalled();
    });

    it('should set errorMsg on register failure', () => {
        authServiceMock.register.and.returnValue(
            throwError(() => ({ error: 'Email already in use', message: 'Email already in use' }))
        );
        component.registerForm.patchValue({
            name: 'John Doe',
            email: 'john@example.com',
            password: 'password123',
            confirmPassword: 'password123',
            terms: true,
        });
        component.onSubmit();
        expect(component.errorMsg).toBeTruthy();
    });

    // ── Password visibility toggle ────────────────────────────────────────────
    it('should toggle showPwd', () => {
        expect(component.showPwd).toBeFalse();
        component.showPwd = true;
        expect(component.showPwd).toBeTrue();
    });
});
