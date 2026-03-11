import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, NgClass],
    templateUrl: './reset-password.html',
    styleUrl: './reset-password.css'
})
export class ResetPasswordComponent implements OnInit {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);

    submitted = false;
    loading = false;
    showPwd = false;
    showConfirmPwd = false;
    errorMsg = '';
    token = '';
    email = '';
    otpSubmitted = false;
    otpStep = true;

    form = this.fb.group({
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required]
    });

    get f() { return this.form.controls; }

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParamMap.get('token') || '';
        this.email = this.route.snapshot.queryParamMap.get('email') || '';
        // Backward compatibility: if reset link token is present, skip OTP step.
        this.otpStep = !this.token;
    }

    onVerifyOtp(rawOtp: string): void {
        this.otpSubmitted = true;
        this.errorMsg = '';
        const enteredOtp = (rawOtp || '').trim();
        if (enteredOtp.length !== 6) {
            this.errorMsg = 'Enter a valid 6-digit OTP.';
            return;
        }
        this.token = enteredOtp;
        this.otpStep = false;
    }

    onSubmit(): void {
        this.submitted = true;
        this.errorMsg = '';

        if (this.otpStep) {
            this.errorMsg = 'Please verify OTP first.';
            return;
        }
        if (!this.token) {
            this.errorMsg = 'Invalid or missing reset token.';
            return;
        }
        if (this.form.invalid) return;
        if (this.f['newPassword'].value !== this.f['confirmPassword'].value) {
            this.errorMsg = 'Passwords do not match.';
            return;
        }

        this.loading = true;
        this.ls.start();
        this.auth.resetPassword({
            token: this.token,
            newPassword: this.f['newPassword'].value!
        }).subscribe({
            next: (msg) => {
                this.ls.stop();
                this.loading = false;
                this.toast.success(msg || 'Password reset successful. Please sign in.');
                this.router.navigate(['/login']);
            },
            error: (err: any) => {
                this.ls.stop();
                this.loading = false;
                this.errorMsg = (typeof err.error === 'string' ? err.error : err.error?.message) || err.message || 'Failed to reset password.';
            }
        });
    }
}
