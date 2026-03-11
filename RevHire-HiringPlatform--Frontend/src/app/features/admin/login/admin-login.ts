import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';
import { NgClass } from '@angular/common';

@Component({
    selector: 'app-admin-login',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, NgClass],
    templateUrl: './admin-login.html',
    styleUrl: '../../auth/login/login.css' // Reusing auth styles
})
export class AdminLoginComponent implements OnInit {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private router = inject(Router);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);

    loginForm = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
    });
    submitted = false; loading = false; showPwd = false; errorMsg = '';

    get f() { return this.loginForm.controls; }

    ngOnInit(): void {
        if (this.auth.isAdmin()) {
            this.router.navigate(['/admin/dashboard']);
        }
    }

    onSubmit(): void {
        this.submitted = true; this.errorMsg = '';
        if (this.loginForm.invalid) return;
        this.loading = true; this.ls.start();
        this.auth.adminLogin(this.loginForm.value as any).subscribe({
            next: (res) => {
                this.ls.stop(); this.loading = false;
                this.toast.success(`Welcome back, Admin ${res.name}!`);
                this.router.navigateByUrl('/admin/dashboard');
            },
            error: (err) => {
                this.ls.stop(); this.loading = false;
                if (err.status === 401) {
                    this.errorMsg = 'Incorrect admin credentials.';
                } else {
                    this.errorMsg = 'Login failed. Please try again.';
                }
            }
        });
    }
}
