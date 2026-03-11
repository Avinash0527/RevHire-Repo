import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';
import { NgClass } from '@angular/common';
import { finalize } from 'rxjs/operators';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, NgClass],
    templateUrl: './login.html',
    styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);

    loginForm = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
    });
    submitted = false; loading = false; showPwd = false; errorMsg = '';
    forgotLoading = false;
    forgotMsg = '';

    features = [
        { icon: 'bi-briefcase', text: 'Access 50,000+ job listings' },
        { icon: 'bi-shield-check', text: 'Verified employer profiles' },
        { icon: 'bi-lightning', text: 'Apply in one click' },
        { icon: 'bi-bar-chart', text: 'Track your applications' },
    ];

    get f() { return this.loginForm.controls; }

    ngOnInit(): void { }



    onSubmit(): void {
        this.submitted = true; this.errorMsg = '';
        if (this.loginForm.invalid) return;
        this.loading = true; this.ls.start();
        this.auth.login(this.loginForm.value as any).subscribe({
            next: (res) => {
                this.ls.stop(); this.loading = false;
                this.toast.success(`Welcome back, ${res.name}!`);
                if (res.role === 'JOB_SEEKER') {
                    this.router.navigateByUrl('/');
                } else {
                    this.router.navigateByUrl('/');
                }
            },
            error: (err) => {
                this.ls.stop(); this.loading = false;
                if (err.status === 401) {
                    this.errorMsg = 'Incorrect credentials. Try again with valid credentials.';
                } else {
                    this.errorMsg = err.error?.message || err.message || 'Login failed. Please try again.';
                }
            }
        });
    }

    onForgotPassword(): void {
        const emailControl = this.f['email'];
        emailControl.markAsTouched();
        this.forgotMsg = '';

        if (emailControl.invalid || !emailControl.value) {
            this.toast.error('Enter a valid email address first.');
            return;
        }

        this.forgotLoading = true;

        this.auth.forgotPassword(emailControl.value).pipe(
            finalize(() => {
                this.forgotLoading = false;
            })
        ).subscribe({
            next: (res: any) => {
                this.forgotMsg = typeof res === 'string'
                    ? res
                    : (res?.message || 'If an account exists with that email, reset instructions were sent.');
                this.toast.success('Password reset request sent.');
                this.router.navigate(['/reset-password'], {
                    queryParams: { email: emailControl.value }
                });
            },
            error: (err: any) => {
                let msg = 'Failed to process forgot password request.';
                if (err.status === 0) {
                    msg = 'Cannot reach server right now. Please try again in a moment.';
                } else if (err.name === 'TimeoutError') {
                    msg = 'Request timed out while contacting server. Please try again.';
                } else if (typeof err.error === 'string') {
                    msg = err.error;
                } else if (err.error?.message) {
                    msg = err.error.message;
                } else if (err.message) {
                    msg = err.message;
                }
                this.toast.error(msg);
            }
        });
    }
}
