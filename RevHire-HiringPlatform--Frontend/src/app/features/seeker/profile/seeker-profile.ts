import { Component, inject, signal, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';
import { SeekerService } from '../../../core/services/seeker.service';

@Component({
    selector: 'app-seeker-profile',
    standalone: true,
    imports: [ReactiveFormsModule, NgClass],
    templateUrl: './seeker-profile.html',
    styleUrl: './seeker-profile.css'
})
export class SeekerProfileComponent implements OnInit {
    auth = inject(AuthService);
    private fb = inject(FormBuilder);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);
    private seekerService = inject(SeekerService);

    saving = signal(false);
    activeTab = signal<'personal' | 'security' | 'notifications' | 'delete'>('personal');
    skills = signal<string[]>([]);
    dragActive = signal(false);
    uploadProgress = signal(0);
    uploadedFile = signal<string | null>(null);
    extractedText = signal<string | null>(null);
    showDeleteModal = signal(false);
    avatarPreview = signal<string | null>(null);
    resumeUploadedAt = signal<number | null>(null);
    showOldPassword = signal(false);
    showNewPassword = signal(false);
    showConfirmPassword = signal(false);

    profileForm = this.fb.group({
        name: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        phone: ['', Validators.pattern(/^\d{10}$/)],
        location: [''],
        bio: ['']
    });

    securityForm = this.fb.group({
        oldPassword: ['', Validators.required],
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required]
    }, { validators: this.passwordsMatchValidator });

    get f() { return this.profileForm.controls; }
    get sf() { return this.securityForm.controls; }

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (user) {
            this.avatarPreview.set(user.avatar || null);
            this.restoreResumeState();
            this.profileForm.patchValue({
                name: user.name,
                email: user.email,
                phone: user.phone || '',
                location: user.location || ''
            });

            this.seekerService.getProfile().subscribe({
                next: (profile) => {
                    let bio = profile.summary || profile.headline || '';
                    if (bio.includes('[BUILDER_METADATA]')) {
                        bio = bio.split('[BUILDER_METADATA]')[0].trim();
                    }
                    this.profileForm.patchValue({
                        email: profile.email || user.email,
                        bio: bio,
                        location: profile.location || '',
                        phone: profile.phone || ''
                    });

                    if (profile.profileImage) {
                        this.avatarPreview.set(profile.profileImage);
                        this.auth.updateCurrentUser({ avatar: profile.profileImage });
                    }

                    if (profile.skillsList) {
                        this.skills.set(profile.skillsList.map((s: any) => s.name));
                    }
                    this.auth.updateProfileStatus(!!(profile.phone || profile.location));
                },
                error: (err) => {
                    if (err.status !== 404) console.error('Failed to load profile', err);
                }
            });
        }
    }

    saveProfile(): void {
        if (this.profileForm.invalid) {
            this.profileForm.markAllAsTouched();
            return;
        }
        this.saving.set(true);
        this.ls.start();

        const formData = this.profileForm.getRawValue();
        const payload = {
            email: formData.email || undefined,
            phone: formData.phone || undefined,
            location: formData.location || undefined,
            summary: formData.bio || undefined,
            headline: formData.bio || undefined,
            profileImage: this.avatarPreview() || undefined
        };

        this.seekerService.updateProfile(payload).subscribe({
            next: (res) => {
                this.ls.stop();
                this.saving.set(false);
                this.auth.updateProfileStatus(true);
                this.auth.updateCurrentUser({
                    email: formData.email || '',
                    phone: formData.phone || '',
                    location: formData.location || '',
                    avatar: this.avatarPreview() || undefined
                });
                this.toast.success('Profile updated successfully!');
            },
            error: (err: any) => {
                this.ls.stop();
                this.saving.set(false);
                this.toast.error(err.error?.message || err.message || 'Failed to update profile');
            }
        });
    }

    changePassword(): void {
        if (this.securityForm.invalid) {
            this.securityForm.markAllAsTouched();
            return;
        }
        this.saving.set(true);
        this.ls.start();

        const payload = {
            oldPassword: this.sf['oldPassword'].value!,
            newPassword: this.sf['newPassword'].value!
        };

        this.auth.updatePassword(payload).subscribe({
            next: () => {
                this.ls.stop();
                this.saving.set(false);
                this.securityForm.reset();
                this.toast.success('Password updated successfully!');
            },
            error: (err: any) => {
                this.ls.stop();
                this.saving.set(false);
                this.toast.error(err.error?.message || err.message || 'Failed to update password');
            }
        });
    }

    isPasswordMismatch(): boolean {
        return !!this.securityForm.errors?.['mismatch'] &&
            (this.sf['confirmPassword'].touched || this.sf['confirmPassword'].dirty);
    }

    private passwordsMatchValidator(control: AbstractControl) {
        const newPassword = control.get('newPassword')?.value;
        const confirmPassword = control.get('confirmPassword')?.value;
        if (!newPassword || !confirmPassword) return null;
        return newPassword === confirmPassword ? null : { mismatch: true };
    }

    deleteAccount(): void { this.showDeleteModal.set(true); }

    confirmDelete(): void {
        this.showDeleteModal.set(false);
        this.ls.start();
        this.seekerService.deleteAccount().subscribe({
            next: () => {
                this.ls.stop();
                this.toast.success('Account deleted successfully.');
                this.auth.logout();
            },
            error: (err: any) => {
                this.ls.stop();
                this.toast.error(err.error?.message || err.message || 'Failed to delete account');
            }
        });
    }

    closeDeleteModal(): void { this.showDeleteModal.set(false); }

    addSkill(input: HTMLInputElement): void {
        const val = input.value.trim();
        if (val && !this.skills().includes(val)) {
            const updatedSkills = [...this.skills(), val];
            this.skills.set(updatedSkills);
            input.value = '';
            this.seekerService.updateResumeText({ skills: updatedSkills.join(',') }).subscribe({
                next: () => this.toast.success(`Skill "${val}" added.`),
                error: () => {
                    this.toast.error('Failed to save skill');
                    this.skills.update(s => s.filter(x => x !== val));
                }
            });
        }
    }

    removeSkill(skill: string): void {
        const originalSkills = [...this.skills()];
        const updatedSkills = this.skills().filter(x => x !== skill);
        this.skills.set(updatedSkills);
        this.seekerService.updateResumeText({ skills: updatedSkills.join(',') }).subscribe({
            next: () => { },
            error: () => {
                this.toast.error('Failed to remove skill');
                this.skills.set(originalSkills);
            }
        });
    }

    handleDrop(event: DragEvent): void {
        const file = event.dataTransfer?.files[0];
        if (file) this.uploadFile(file);
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (file) this.uploadFile(file);
        input.value = '';
    }

    onAvatarSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (!file) return;

        if (!file.type.startsWith('image/') || file.size > 2 * 1024 * 1024) {
            this.toast.error('Invalid image or size exceeds 2MB');
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            const dataUrl = reader.result as string;
            this.avatarPreview.set(dataUrl);
            this.auth.updateCurrentUser({ avatar: dataUrl });
            this.toast.success('Local preview updated. Save profile to persist.');
        };
        reader.readAsDataURL(file);
        input.value = '';
    }

    removeProfileImage(): void {
        this.avatarPreview.set(null);
        this.auth.updateCurrentUser({ avatar: undefined });
        this.toast.success('Remove profile image? Save to persist changes.');
    }

    uploadFile(file: File): void {
        if (file.size > 2 * 1024 * 1024) {
            this.toast.error('File size exceeds 2MB limit');
            return;
        }
        this.uploadProgress.set(10);
        this.seekerService.uploadResume(file).subscribe({
            next: (res) => {
                this.uploadProgress.set(100);
                this.uploadedFile.set(file.name);
                this.resumeUploadedAt.set(Date.now());
                this.persistResumeState(file.name);
                this.toast.success('Resume uploaded successfully!');
                if (res?.skillsList) this.skills.set(res.skillsList.map((s: any) => s.name));
                setTimeout(() => this.uploadProgress.set(0), 1000);
            },
            error: (err) => {
                this.toast.error(err.error || 'Failed to upload resume');
                this.uploadProgress.set(0);
            }
        });
    }

    removeFile(): void {
        this.uploadedFile.set(null);
        this.resumeUploadedAt.set(null);
        this.clearResumeState();
    }

    private getResumeStorageKey(): string | null {
        const userId = this.auth.currentUser()?.id;
        return userId ? `revhire_resume_upload_${userId}` : null;
    }

    private persistResumeState(fileName: string): void {
        const key = this.getResumeStorageKey();
        if (key) localStorage.setItem(key, JSON.stringify({ fileName, uploadedAt: this.resumeUploadedAt() }));
    }

    private restoreResumeState(): void {
        const key = this.getResumeStorageKey();
        if (!key) return;
        const raw = localStorage.getItem(key);
        if (raw) {
            try {
                const parsed = JSON.parse(raw);
                this.uploadedFile.set(parsed.fileName);
                this.resumeUploadedAt.set(parsed.uploadedAt || null);
            } catch { localStorage.removeItem(key); }
        }
    }

    private clearResumeState(): void {
        const key = this.getResumeStorageKey();
        if (key) localStorage.removeItem(key);
    }
}
