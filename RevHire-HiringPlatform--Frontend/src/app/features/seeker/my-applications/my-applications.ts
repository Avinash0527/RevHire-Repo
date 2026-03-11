import { Component, inject, signal, OnInit, effect, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgClass, DatePipe } from '@angular/common';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { Application } from '../../../core/models/application.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { JobService } from '../../../core/services/job.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
    selector: 'app-my-applications',
    standalone: true,
    imports: [RouterLink, NgClass, StatusBadgeComponent, DatePipe, PaginationComponent],
    templateUrl: './my-applications.html',
    styleUrl: './my-applications.css'
})
export class MyApplicationsComponent implements OnInit {
    private appService = inject(ApplicationService);
    private auth = inject(AuthService);
    private jobService = inject(JobService);
    private notifService = inject(NotificationService);

    loading = signal(true);
    applications = signal<Application[]>([]);
    activeFilter = signal<string>('ALL');
    expandedId = signal<number | null>(null);

    // Pagination
    currentPage = signal(1);
    readonly pageSize = 10;

    filteredApps = computed(() => {
        const filter = this.activeFilter();
        const apps = this.applications();
        if (filter === 'ALL') return apps;
        return apps.filter(a => a.status === filter);
    });

    pagedApps = computed(() => {
        const apps = this.filteredApps();
        const start = (this.currentPage() - 1) * this.pageSize;
        return apps.slice(start, start + this.pageSize);
    });

    filters = [
        { id: 'ALL', label: 'All Jobs' },
        { id: 'APPLIED', label: 'Applied' },
        { id: 'REVIEWING', label: 'Reviewing' },
        { id: 'SHORTLISTED', label: 'Shortlisted' },
        { id: 'SELECTED', label: 'Selected' },
        { id: 'REJECTED', label: 'Rejected' },
        { id: 'WITHDRAWN', label: 'Withdrawn' },
    ];

    showWithdrawModal = signal(false);
    isWithdrawing = signal(false);
    withdrawReason = signal('');
    selectedApp = signal<Application | null>(null);
    jobDeadlineById = signal<Record<number, string>>({});
    Math = Math;

    constructor() {
        // Auto-refresh applications when notifications change (indicates potential status update)
        effect(() => {
            const notifications = this.notifService.notifications();
            if (notifications.length > 0 && !this.loading()) {
                this.refreshApplications();
            }
        });

        effect(() => {
            this.activeFilter();
            this.currentPage.set(1);
        }, { allowSignalWrites: true });
    }

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (user) {
            this.refreshApplications();

            this.jobService.getAllJobs().subscribe({
                next: (jobs) => {
                    const map: Record<number, string> = {};
                    jobs.forEach(j => map[j.id] = j.deadline);
                    this.jobDeadlineById.set(map);
                },
                error: () => { }
            });
        }
    }

    private refreshApplications(): void {
        this.appService.getMyApplications().subscribe({
            next: (apps) => {
                this.applications.set(apps);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    getCount(id: string): number {
        if (id === 'ALL') return this.applications().length;
        return this.applications().filter(a => a.status === id).length;
    }

    toggleExpand(id: number): void {
        this.expandedId.set(this.expandedId() === id ? null : id);
    }

    openWithdrawModal(app: Application): void {
        this.selectedApp.set(app);
        this.withdrawReason.set('');
        this.showWithdrawModal.set(true);
    }

    closeWithdrawModal(): void {
        this.showWithdrawModal.set(false);
        this.selectedApp.set(null);
    }

    confirmWithdraw(): void {
        const app = this.selectedApp();
        if (!app) return;

        this.isWithdrawing.set(true);
        this.appService.withdrawApplication(app.id, this.withdrawReason()).subscribe({
            next: () => {
                this.applications.update(apps =>
                    apps.map(a => a.id === app.id ? { ...a, status: 'WITHDRAWN' as any } : a)
                );
                this.isWithdrawing.set(false);
                this.closeWithdrawModal();
            },
            error: () => {
                this.isWithdrawing.set(false);
            }
        });
    }

    private isDeadlinePassed(jobId: number): boolean {
        const deadlineRaw = this.jobDeadlineById()[jobId];
        if (!deadlineRaw) return false;

        const parsed = new Date(deadlineRaw);
        if (Number.isNaN(parsed.getTime())) return false;

        // If only a date is provided, treat deadline as end of that day.
        const hasTime = /T|\d{1,2}:\d{2}/.test(deadlineRaw);
        if (!hasTime) {
            parsed.setHours(23, 59, 59, 999);
        }

        return Date.now() > parsed.getTime();
    }

    isRegistrationClosed(app: Application): boolean {
        return this.isDeadlinePassed(app.jobId);
    }

    isWithdrawDisabled(app: Application): boolean {
        return ['SELECTED', 'REJECTED', 'WITHDRAWN'].includes(app.status) || this.isDeadlinePassed(app.jobId);
    }
}
