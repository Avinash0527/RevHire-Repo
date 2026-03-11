import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SeekerService } from '../../../core/services/seeker.service';
import { ToastService } from '../../../core/services/toast.service';
import { NgClass } from '@angular/common';

@Component({
    selector: 'app-seeker-profile-view',
    standalone: true,
    imports: [RouterLink, NgClass],
    templateUrl: './seeker-profile-view.html',
    styleUrl: './seeker-profile-view.css'
})
export class SeekerProfileViewComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private seekerService = inject(SeekerService);
    private toast = inject(ToastService);

    loading = signal(true);
    profile = signal<any | null>(null);
    seekerId = signal<number | null>(null);

    ngOnInit(): void {
        const seekerId = Number(this.route.snapshot.paramMap.get('id'));
        if (!seekerId) {
            this.loading.set(false);
            this.toast.error('Invalid seeker id');
            return;
        }
        this.seekerId.set(seekerId);

        this.seekerService.getProfileById(seekerId).subscribe({
            next: (res) => {
                this.profile.set(res);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.toast.error('Failed to load seeker profile');
            }
        });
    }

    skills(): string[] {
        const p = this.profile();
        if (!p) return [];
        if (Array.isArray(p.skillsList) && p.skillsList.length) {
            return p.skillsList.map((s: any) => s?.name).filter(Boolean);
        }
        if (typeof p.skills === 'string') {
            return p.skills.split(',').map((x: string) => x.trim()).filter(Boolean);
        }
        return [];
    }

    profileImage(): string | null {
        const p = this.profile();
        if (!p) return null;

        const raw = (p.avatar || p.profileImage || p.profileImageUrl || p.imageUrl || '').toString().trim();
        const fromStorage = this.seekerId() ? localStorage.getItem(`revhire_avatar_${this.seekerId()}`) : '';
        const value = (raw || fromStorage || '').toString().trim();
        if (!value) return null;

        if (value.startsWith('data:image/') || value.startsWith('http://') || value.startsWith('https://') || value.startsWith('blob:')) {
            return value;
        }

        // Support backend returning plain base64 without data URI prefix.
        return `data:image/jpeg;base64,${value}`;
    }
}
