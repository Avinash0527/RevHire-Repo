import { computed, Component, inject, OnInit, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { SavedResumeService } from '../../../core/services/saved-resume.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-saved-resumes',
    standalone: true,
    imports: [NgClass],
    templateUrl: './saved-resumes.html',
    styleUrl: './saved-resumes.css'
})
export class SavedResumesComponent implements OnInit {
    private auth = inject(AuthService);
    private savedResumeService = inject(SavedResumeService);
    private toast = inject(ToastService);

    loading = signal(true);
    savedList = signal<any[]>([]); // Backend returns JobSeekerProfileResponse possibly
    expandedId = signal<number | null>(null);

    categorizedResumes = computed(() => {
        const list = this.savedList();
        const groups: { [key: string]: any[] } = {};
        list.forEach(item => {
            const role = item.appliedForRole || 'Other';
            if (!groups[role]) groups[role] = [];
            groups[role].push(item);
        });
        return Object.keys(groups).sort().map(role => ({
            role,
            items: groups[role]
        }));
    });

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (user) {
            this.loadSavedResumes();
        } else {
            this.loading.set(false);
        }
    }

    private loadSavedResumes(): void {
        this.loading.set(true);
        this.savedResumeService.getSavedResumes().subscribe({
            next: (resumes) => {
                // Map backend JobSeekerProfileResponse to frontend structure
                const mapped = resumes.map(r => ({
                    ...r,
                    seekerName: r.name || 'Candidate',
                    seekerEmail: r.email || 'No email',
                    appliedForRole: r.appliedRole || r.headline || 'Applied Candidate',
                    savedAt: new Date().toLocaleDateString(),
                    resumeSnapshot: {
                        fullName: r.name,
                        title: r.headline,
                        email: r.email,
                        phone: r.phone,
                        location: r.location || 'Remote',
                        summary: r.summary,
                        skills: (r.skills || '').split(',').filter((s: string) => s.trim()).map((s: string) => ({ name: s.trim(), level: 'Intermediate' })),
                        experiences: r.experience ? [{ title: 'Experience', company: 'Previous', description: r.experience, current: false }] : [],
                        education: r.education ? [{ degree: 'Education', institution: 'Previous', endYear: '' }] : [],
                        certifications: (r.certifications || '').split(',').filter((s: string) => s.trim())
                    }
                }));
                this.savedList.set(mapped);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    toggleExpand(id: number): void {
        this.expandedId.set(this.expandedId() === id ? null : id);
    }

    removeSaved(item: any): void {
        const seekerId = item.id;
        const jobId = item.jobId;
        if (!jobId) {
            this.toast.error('Missing job context for this saved resume');
            return;
        }
        this.savedResumeService.unsaveResume(seekerId, jobId).subscribe(() => {
            this.savedList.update(list => list.filter(sr => !(sr.id === seekerId && sr.jobId === jobId)));
            this.toast.success(`Removed resume from saved.`);
        });
    }
}
