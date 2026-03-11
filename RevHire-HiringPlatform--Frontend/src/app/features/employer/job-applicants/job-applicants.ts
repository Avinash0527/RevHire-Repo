import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgClass, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { SavedResumeService } from '../../../core/services/saved-resume.service';
import { Application, ApplicationStatus } from '../../../core/models/application.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { SeekerService } from '../../../core/services/seeker.service';
import { ToastService } from '../../../core/services/toast.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
    selector: 'app-job-applicants',
    standalone: true,
    imports: [RouterLink, NgClass, StatusBadgeComponent, FormsModule, DatePipe, PaginationComponent],
    templateUrl: './job-applicants.html',
    styleUrl: './job-applicants.css'
})
export class JobApplicantsComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private appService = inject(ApplicationService);
    private auth = inject(AuthService);
    private savedResumeService = inject(SavedResumeService);
    private seekerService = inject(SeekerService);
    private toast = inject(ToastService);

    loading = signal(true);
    applicants = signal<Application[]>([]);
    statusFilter = signal('ALL');
    currentJobId = signal<number | null>(null);
    savedResumeIds = signal<Set<string>>(new Set());

    // Pagination
    currentPage = signal(1);
    readonly pageSize = 10;

    pagedApplicants = computed(() => {
        const apps = this.filteredApplicants();
        const start = (this.currentPage() - 1) * this.pageSize;
        return apps.slice(start, start + this.pageSize);
    });

    constructor() {
        // Reset pagination when filters change
        import('@angular/core').then(m => {
            m.effect(() => {
                this.statusFilter();
                this.searchTerm();
                this.skillFilter();
                this.expFilters();
                this.eduFilters();
                this.appliedAfter();
                this.currentPage.set(1);
            }, { allowSignalWrites: true });
        });
    }

    // Advanced Filters
    searchTerm = signal('');
    skillFilter = signal('');
    expFilters = signal<string[]>([]);
    eduFilters = signal<string[]>([]);
    appliedAfter = signal('');
    showFilters = signal(false);

    experienceOptions = [
        'Entry Level (0 - 2 yrs)',
        'Mid Level (3 - 5 yrs)',
        'Senior Level (6 - 9 yrs)',
        'Lead / Manager (10+ yrs)'
    ];
    educationOptions = [
        'High School',
        'Associate Degree',
        "Bachelor's Degree",
        "Master's Degree",
        'PhD / Doctorate'
    ];

    showExpMenu = signal(false);
    showEduMenu = signal(false);

    // Bulk update
    selectedIds = signal<Set<number>>(new Set());
    bulkStatus: string = '';
    filteredApplicants = computed(() => {
        const search = this.searchTerm().toLowerCase().trim();
        const skill = this.skillFilter().toLowerCase().trim();
        const selectedExp = this.expFilters();
        const selectedEdu = this.eduFilters();
        const status = this.statusFilter();
        const date = this.appliedAfter();
        const jobId = this.currentJobId();

        return this.applicants().filter(a => {
            // Job Filter
            if (jobId && a.jobId !== jobId) return false;

            // Status Filter
            if (status !== 'ALL' && a.status !== status) return false;

            // Search (Name/Email)
            if (search && !(a.jobSeekerName.toLowerCase().includes(search) || a.jobSeekerEmail.toLowerCase().includes(search))) return false;

            // Skill Filter
            if (skill && !a.jobSeekerSkills?.toLowerCase().includes(skill)) return false;

            // Experience Filter
            if (selectedExp.length > 0) {
                const text = (a.jobSeekerExperience || '').toLowerCase();
                const years = Number((a.jobSeekerExperience || '').match(/\d+/)?.[0] || '0');
                const matchesExp = selectedExp.some(exp => {
                    if (exp === 'Entry Level (0 - 2 yrs)') return years <= 2 || text.includes('entry');
                    if (exp === 'Mid Level (3 - 5 yrs)') return (years >= 3 && years <= 5) || text.includes('mid');
                    if (exp === 'Senior Level (6 - 9 yrs)') return (years >= 6 && years <= 9) || text.includes('senior');
                    if (exp === 'Lead / Manager (10+ yrs)') return years >= 10 || text.includes('lead') || text.includes('manager');
                    return false;
                });
                if (!matchesExp) return false;
            }

            // Education Filter
            if (selectedEdu.length > 0) {
                const text = a.jobSeekerEducation?.toLowerCase() || '';
                if (!selectedEdu.some(edu => text.includes(edu.toLowerCase()))) return false;
            }

            // Date Filter
            if (date) {
                const afterDate = new Date(date);
                if (!a.appliedAt || new Date(a.appliedAt) < afterDate) return false;
            }

            return true;
        });
    });

    allSelected = computed(() =>
        this.filteredApplicants().length > 0 &&
        this.filteredApplicants().every((a: Application) => this.selectedIds().has(a.id))
    );

    // Notes panel toggle
    openNotes = signal<Set<number>>(new Set());

    refresh(): void {
        this.loading.set(true);
        const user = this.auth.currentUser();
        if (user) {
            this.appService.getApplicationsByEmployer(user.id).subscribe({
                next: (apps) => {
                    this.applicants.set(apps);
                    this.loading.set(false);
                },
                error: () => this.loading.set(false)
            });
        }
    }


    ngOnInit(): void {
        const user = this.auth.currentUser();
        this.route.queryParams.subscribe(params => {
            this.currentJobId.set(params['jobId'] ? Number(params['jobId']) : null);
            if (user) {
                this.refresh();

                // Load saved resumes to check status
                this.savedResumeService.getSavedResumes().subscribe(resumes => {
                    this.savedResumeIds.set(new Set(
                        resumes
                            .filter(r => r?.jobId != null)
                            .map(r => this.savedKey(r.id, r.jobId))
                    ));
                });
            }
        });
    }

    toggleExp(val: string): void {
        this.expFilters.update(curr =>
            curr.includes(val) ? curr.filter(v => v !== val) : [...curr, val]
        );
    }

    toggleEdu(val: string): void {
        this.eduFilters.update(curr =>
            curr.includes(val) ? curr.filter(v => v !== val) : [...curr, val]
        );
    }

    resetFilters(): void {
        this.searchTerm.set('');
        this.skillFilter.set('');
        this.expFilters.set([]);
        this.eduFilters.set([]);
        this.appliedAfter.set('');
        this.statusFilter.set('ALL');
        this.showExpMenu.set(false);
        this.showEduMenu.set(false);
    }

    updateStatus(appId: number, status: ApplicationStatus | string): void {
        const normalizedStatus = String(status).toUpperCase() as ApplicationStatus;
        const oldStatus = this.applicants().find(a => a.id === appId)?.status;

        // Optimistic UI Update
        this.applicants.update(list => {
            return list.map(a => a.id === appId ? { ...a, status: normalizedStatus } : a);
        });
        this.toast.success(`Status updated to ${normalizedStatus}`);

        this.appService.updateStatus(appId, normalizedStatus).subscribe({
            next: () => {
                // Already updated optimistically
            },
            error: (err) => {
                // Rollback on error
                this.applicants.update(list => {
                    return list.map(a => a.id === appId ? { ...a, status: oldStatus as any } : a);
                });

                const msg =
                    (typeof err?.error === 'string' && err.error) ||
                    err?.error?.message ||
                    err?.message ||
                    'Failed to update status. Please try again.';
                this.toast.error(msg);
            }
        });
    }

    downloadResume(app: Application): void {
        this.appService.downloadResume(app.id).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `Resume_${app.jobSeekerName.replace(/\s+/g, '_')}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            },
            error: (err: any) => {
                const msg =
                    (typeof err?.error === 'string' && err.error) ||
                    err?.error?.message ||
                    err?.message ||
                    'Could not download resume. No file found.';
                this.toast.error(msg);
            }
        });
    }

    // Bulk selection
    toggleSelect(id: number): void {
        this.selectedIds.update(s => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });
    }
    toggleSelectAll(): void {
        if (this.allSelected()) {
            this.selectedIds.set(new Set());
        } else {
            this.selectedIds.set(new Set(this.filteredApplicants().map((a: Application) => a.id)));
        }
    }
    clearSelection(): void { this.selectedIds.set(new Set()); this.bulkStatus = ''; }

    applyBulkStatus(): void {
        if (!this.bulkStatus) return;
        const status = String(this.bulkStatus).toUpperCase() as ApplicationStatus;
        const selectedSet = this.selectedIds();

        const validIds = Array.from(selectedSet).filter((id: number) => {
            const app = this.applicants().find((a: Application) => a.id === id);
            return app && app.status !== 'WITHDRAWN';
        });

        if (validIds.length === 0) {
            this.toast.warning('No valid applications selected for update.');
            this.clearSelection();
            return;
        }

        // Store old statuses for rollback
        const originalApplicants = [...this.applicants()];

        // Optimistic UI Update
        this.applicants.update(list => {
            return list.map(a => validIds.includes(a.id) ? { ...a, status: status } : a);
        });
        this.toast.success(`${validIds.length} application(s) updated to ${status}`);
        this.clearSelection();

        this.appService.updateBulkStatus(validIds, status).subscribe({
            next: () => {
                // Already updated optimistically
            },
            error: (err) => {
                // Rollback
                this.applicants.set(originalApplicants);

                const msg =
                    (typeof err?.error === 'string' && err.error) ||
                    err?.error?.message ||
                    err?.message ||
                    'Failed to update bulk status';
                this.toast.error(msg);
            }
        });
    }

    // Notes
    toggleNotes(appId: number): void {
        this.openNotes.update(s => { const n = new Set(s); n.has(appId) ? n.delete(appId) : n.add(appId); return n; });
    }
    saveNote(app: Application, note: string): void {
        this.appService.addNote(app.id, note).subscribe(() => {
            app.notes = note;
            this.toast.success('Note saved');
        });
    }

    // Saved Resumes
    isResumeSaved(seekerId: number, jobId: number): boolean {
        return this.savedResumeIds().has(this.savedKey(seekerId, jobId));
    }

    toggleSaveResume(app: Application): void {
        const seekerId = app.jobSeekerId;
        const key = this.savedKey(seekerId, app.jobId);
        const exists = this.savedResumeIds().has(key);

        // Optimistic Update
        this.savedResumeIds.update(set => {
            const n = new Set(set);
            exists ? n.delete(key) : n.add(key);
            return n;
        });

        if (exists) {
            this.savedResumeService.unsaveResume(seekerId, app.jobId).subscribe({
                next: () => this.toast.success(`Removed from saved.`),
                error: (err) => {
                    const msg = err?.error?.message || err?.message || 'Failed to unsave';
                    this.toast.error(msg);
                    // Rollback
                    this.savedResumeIds.update(set => { const n = new Set(set); n.add(key); return n; });
                }
            });
        } else {
            this.savedResumeService.saveResume(seekerId, app.jobId).subscribe({
                next: () => this.toast.success(`Resume saved!`),
                error: (err) => {
                    const msg = err?.error?.message || err?.message || 'Failed to save';
                    this.toast.error(msg);
                    // Rollback
                    this.savedResumeIds.update(set => { const n = new Set(set); n.delete(key); return n; });
                }
            });
        }
    }

    private savedKey(seekerId: number, jobId: number): string {
        return `${seekerId}-${jobId}`;
    }
}
