import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';
import { UserRole } from '../../../core/models/user.model';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, NgClass],
    templateUrl: './register.html',
    styleUrl: './register.css'
})
export class RegisterComponent {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private router = inject(Router);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);

    selectedRole = signal<UserRole>('JOB_SEEKER');
    verificationStep = signal<'EMAIL' | 'OTP' | 'DETAILS'>('EMAIL');
    otpCode = ''; emailVerifiedToken = '';
    submitted = false; loading = false; showPwd = false; errorMsg = '';

    perks = ['Free forever for job seekers', '1-click apply to jobs', 'Resume builder included', 'Job alerts to your inbox', 'Track all your applications'];

    registerForm = this.fb.group({
        name: ['', [Validators.required, Validators.minLength(2)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
        companyName: [''],
        terms: [false, Validators.requiredTrue],
    }, {
        validators: (g) => {
            const role = this.selectedRole();
            const company = g.get('companyName');
            if (role === 'EMPLOYER' && !company?.value) {
                company?.setErrors({ required: true });
            } else {
                company?.setErrors(null);
            }
            return g.get('password')?.value !== g.get('confirmPassword')?.value ? { mismatch: true } : null;
        }
    });

    get f() { return this.registerForm.controls; }

    onSendOtp(): void {
        const email = this.f['email'].value;
        if (!email || this.f['email'].invalid) {
            this.f['email'].markAsTouched();
            return;
        }
        this.loading = true; this.ls.start();
        this.auth.sendOtp(email).subscribe({
            next: () => {
                this.loading = false; this.ls.stop();
                this.verificationStep.set('OTP');
                this.toast.success('OTP sent to your email');
            },
            error: (err) => {
                this.loading = false; this.ls.stop();
                this.errorMsg = err.error || 'Failed to send OTP';
            }
        });
    }

    onVerifyOtp(otp: string): void {
        if (!otp || otp.length < 6) return;
        this.loading = true; this.ls.start();
        this.auth.verifyOtp(this.f['email'].value!, otp).subscribe({
            next: () => {
                this.loading = false; this.ls.stop();
                this.verificationStep.set('DETAILS');
                this.toast.success('Email verified successfully');
            },
            error: (err) => {
                this.loading = false; this.ls.stop();
                this.errorMsg = err.error || 'Invalid OTP';
            }
        });
    }

    onSubmit(): void {
        this.submitted = true; this.errorMsg = '';
        if (this.registerForm.invalid) return;
        this.loading = true; this.ls.start();
        this.auth.register({
            name: this.f['name'].value!,
            email: this.f['email'].value!,
            password: this.f['password'].value!,
            role: this.selectedRole(),
            companyName: this.selectedRole() === 'EMPLOYER' ? (this.f['companyName'].value ?? undefined) : undefined
        }).subscribe({
            next: (res) => {
                // Silent login to enable the "complete profile" flow
                this.auth.login({
                    email: this.f['email'].value!,
                    password: this.f['password'].value!
                }).subscribe({
                    next: () => {
                        this.ls.stop(); this.loading = false;
                        this.toast.success('Account created! Let\'s complete your profile.');
                        const profileRoute = res.role === 'JOB_SEEKER' ? '/seeker/profile' : '/employer/company';
                        this.router.navigate([profileRoute]);
                    },
                    error: () => {
                        this.ls.stop(); this.loading = false;
                        this.router.navigate(['/login']);
                    }
                });
            },
            error: (err) => {
                this.ls.stop();
                this.loading = false;
                // Spring backend sends the message as plain text or JSON
                this.errorMsg = (typeof err.error === 'string' ? err.error : err.error?.message) || err.message;
            }
        });
    }
}
