import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { JobService } from '../../../core/services/job.service';
import { ApplicationService } from '../../../core/services/application.service';
import { CompanyService } from '../../../core/services/company.service';
import { Job } from '../../../core/models/job.model';
import { Application } from '../../../core/models/application.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';

@Component({
    selector: 'app-employer-dashboard',
    standalone: true,
    imports: [RouterLink, NgClass, StatusBadgeComponent],
    templateUrl: './employer-dashboard.html',
    styleUrl: './employer-dashboard.css'
})
export class EmployerDashboardComponent implements OnInit {
    auth = inject(AuthService);
    private jobService = inject(JobService);
    private appService = inject(ApplicationService);
    private companyService = inject(CompanyService);

    activeJobs = signal<Job[]>([]);
    recentApplicants = signal<Application[]>([]);
    stats = signal<any[]>([]);

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (!user) return;

        // Load Jobs
        this.jobService.getJobsByEmployer(user.id).subscribe({
            next: (jobs) => this.activeJobs.set(jobs.slice(0, 3)),
            error: () => this.activeJobs.set([])
        });

        // Load Recent Applicants
        this.appService.getApplicationsByEmployer(user.id).subscribe({
            next: (apps) => this.recentApplicants.set(apps.slice(0, 4)),
            error: () => this.recentApplicants.set([])
        });

        // Load Stats from Backend
        this.companyService.getDashboardStats().subscribe({
            next: (s) => {
                this.stats.set([
                    { label: 'Active Jobs', value: s.activeJobs || 0, icon: 'bi-briefcase', bg: 'rgba(79,70,229,0.1)', color: '#4F46E5' },
                    { label: 'Total Applicants', value: s.totalApplications || 0, icon: 'bi-people', bg: 'rgba(139,92,246,0.1)', color: '#7c3aed' },
                    { label: 'Pending Reviews', value: s.pendingReviews || 0, icon: 'bi-eye', bg: 'rgba(251,191,36,0.1)', color: '#d97706' },
                    { label: 'Total Jobs', value: s.totalJobs || 0, icon: 'bi-list-task', bg: 'rgba(16,185,129,0.1)', color: '#10B981' },
                ]);
            },
            error: () => {
                this.stats.set([
                    { label: 'Active Jobs', value: 0, icon: 'bi-briefcase', bg: 'rgba(79,70,229,0.1)', color: '#4F46E5' },
                    { label: 'Total Applicants', value: 0, icon: 'bi-people', bg: 'rgba(139,92,246,0.1)', color: '#7c3aed' },
                    { label: 'Pending Reviews', value: 0, icon: 'bi-eye', bg: 'rgba(251,191,36,0.1)', color: '#d97706' },
                    { label: 'Total Jobs', value: 0, icon: 'bi-list-task', bg: 'rgba(16,185,129,0.1)', color: '#10B981' },
                ]);
            }
        });
    }
}
