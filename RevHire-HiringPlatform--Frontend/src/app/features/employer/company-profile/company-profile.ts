import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { CompanyService } from '../../../core/services/company.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Router } from '@angular/router';
import { LoadingService } from '../../../core/services/loading.service';
import { Company } from '../../../core/models/company.model';

@Component({
    selector: 'app-company-profile',
    standalone: true,
    imports: [ReactiveFormsModule, NgClass],
    templateUrl: './company-profile.html',
    styleUrl: './company-profile.css'
})
export class CompanyProfileComponent implements OnInit {
    private companyService = inject(CompanyService);
    private authService = inject(AuthService);
    private fb = inject(FormBuilder);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);
    private router = inject(Router);

    company = signal<Company | null>(null);
    saving = signal(false);
    selectedTab = signal<'PROFILE' | 'TEAM' | 'SETTINGS' | 'EMPLOYEE_PROFILE'>('PROFILE');
    avatarPreview = signal<string | null>(null);

    companyForm = this.fb.group({
        name: ['', Validators.required],
        description: ['', Validators.required],
        industry: ['', Validators.required],
        website: ['', Validators.required],
        location: [''],
        size: [''],
        userName: ['', Validators.required],
        userEmail: [{ value: '', disabled: true }],
        userPhone: ['', Validators.pattern(/^\d{10}$/)]
    });

    get f() { return this.companyForm.controls; }

    ngOnInit(): void {
        const user = this.authService.currentUser();
        this.avatarPreview.set(user?.avatar || null);

        this.companyService.getCompany().subscribe(company => {
            if (company) {
                this.company.set(company);
                const resolvedEmail = this.resolveEmail(company, user?.email || '');
                const persistedSize = this.getPersistedCompanySize(company.id);
                this.companyForm.patchValue({
                    ...company,
                    size: (company.size as any)?.toString?.() || persistedSize || '',
                    userEmail: resolvedEmail,
                    userName: company.userName || user?.name || '',
                    userPhone: company.userPhone || user?.phone || ''
                });
                if (company.logo) {
                    this.avatarPreview.set(company.logo);
                    this.authService.updateCurrentUser({ avatar: company.logo });
                }
                if (this.companyForm.valid) {
                    this.authService.updateProfileStatus(true);
                }
            } else {
                // Pre-fill from auth if profile not found
                if (user) {
                    this.companyForm.patchValue({
                        name: user.name,
                        userName: user.name,
                        userEmail: user.email
                    });
                }
            }
        });
    }

    onAvatarSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            this.toast.error('Please select a valid image file.');
            input.value = '';
            return;
        }

        if (file.size > 2 * 1024 * 1024) {
            this.toast.error('Image size exceeds 2MB limit');
            input.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            const dataUrl = typeof reader.result === 'string' ? reader.result : null;
            if (!dataUrl) {
                this.toast.error('Failed to read selected image.');
                return;
            }

            this.avatarPreview.set(dataUrl);
            this.authService.updateCurrentUser({ avatar: dataUrl });
            this.toast.success('Profile image updated.');
        };
        reader.onerror = () => {
            this.toast.error('Failed to load selected image.');
        };
        reader.readAsDataURL(file);
        input.value = '';
    }

    removeProfileImage(): void {
        if (!this.avatarPreview()) return;
        this.avatarPreview.set(null);
        this.authService.updateCurrentUser({ avatar: undefined });
        this.toast.success('Profile image removed.');
    }

    saveProfile(): void {
        const tab = this.selectedTab();
        const requiredControls = tab === 'EMPLOYEE_PROFILE'
            ? (['userName', 'userPhone'] as const)
            : (['name', 'description', 'industry', 'website'] as const);

        const invalid = requiredControls.some((key) => {
            const control = this.f[key];
            control.markAsTouched();
            return control.invalid;
        });

        if (invalid) {
            return;
        }
        this.saving.set(true);
        this.ls.start();

        const raw = this.companyForm.getRawValue();
        const email = (raw.userEmail || this.resolveEmail(this.company(), this.authService.currentUser()?.email || '')).trim();

        // Send only backend-relevant fields to avoid 400 from unknown/invalid properties.
        const payload: Partial<Company> = {
            ...(this.company()?.id ? { id: this.company()!.id } : {}),
            name: (raw.name || '').trim(),
            description: (raw.description || '').trim(),
            industry: (raw.industry || '').trim(),
            website: (raw.website || '').trim(),
            location: (raw.location || '').trim(),
            size: (raw.size || '').trim(),
            userName: (raw.userName || '').trim(),
            userPhone: (raw.userPhone || '').trim() || undefined,
            email,
            phone: (raw.userPhone || '').trim() || undefined,
            logo: this.avatarPreview() || undefined
        };

        const request$ = this.company()?.id
            ? this.companyService.updateCompanyById(this.company()!.id, payload as Company)
            : this.companyService.updateCompany(payload as Company);

        request$.subscribe({
            next: (updated) => {
                this.ls.stop();
                this.saving.set(false);
                this.company.set(updated);
                const resolvedEmail = this.resolveEmail(updated, email);
                const effectiveSize = ((updated.size as any)?.toString?.() || (payload.size as string) || '').trim();
                this.companyForm.patchValue({
                    ...updated,
                    size: effectiveSize,
                    userEmail: resolvedEmail,
                    userName: updated.userName || payload.userName || '',
                    userPhone: updated.userPhone || payload.userPhone || ''
                });
                if (updated.id && effectiveSize) {
                    this.persistCompanySize(updated.id, effectiveSize);
                }
                this.authService.updateCurrentUser({
                    email: resolvedEmail,
                    name: (payload.userName || this.authService.currentUser()?.name || '').toString(),
                    phone: (payload.userPhone || this.authService.currentUser()?.phone || undefined)?.toString()
                });
                const message = this.selectedTab() === 'EMPLOYEE_PROFILE'
                    ? 'Employee profile updated!'
                    : 'Company profile updated!';
                this.toast.success(message);

                // Update auth status to indicate profile is complete
                this.authService.updateProfileStatus(true);
            },
            error: (err) => {
                this.ls.stop();
                this.saving.set(false);
                const msg = (typeof err?.error === 'string' ? err.error : err?.error?.message) || err?.message || 'Failed to update profile';
                this.toast.error(msg);
            }
        });
    }

    private resolveEmail(company: Partial<Company> | null, fallback: string): string {
        return (company?.userEmail || company?.email || fallback || '').trim();
    }

    private getCompanySizeStorageKey(companyId: number): string {
        return `revhire_company_size_${companyId}`;
    }

    private persistCompanySize(companyId: number, size: string): void {
        localStorage.setItem(this.getCompanySizeStorageKey(companyId), size);
    }

    private getPersistedCompanySize(companyId?: number): string {
        if (!companyId) return '';
        return localStorage.getItem(this.getCompanySizeStorageKey(companyId)) || '';
    }
}
