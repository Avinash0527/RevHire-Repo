import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ApplicationService } from '../../../core/services/application.service';
import { JobService } from '../../../core/services/job.service';
import { SeekerService } from '../../../core/services/seeker.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Application } from '../../../core/models/application.model';
import { Job } from '../../../core/models/job.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';

@Component({
    selector: 'app-seeker-dashboard',
    standalone: true,
    imports: [RouterLink, NgClass, StatusBadgeComponent],
    templateUrl: './seeker-dashboard.html',
    styleUrl: './seeker-dashboard.css'
})
export class SeekerDashboardComponent implements OnInit {
    auth = inject(AuthService);
    private appService = inject(ApplicationService);
    private jobService = inject(JobService);
    private notifService = inject(NotificationService);
    private seekerService = inject(SeekerService);

    recentApps = signal<Application[]>([]);
    recommendedJobs = signal<Job[]>([]);
    stats = signal<any[]>([]);

    firstName = computed(() => {
        const user = this.auth.currentUser();
        const name = user?.name;
        return name ? name.split(' ')[0] : (user?.email?.split('@')[0] || 'there');
    });

    profileCompletion = signal(75);

    completionTasks = signal<any[]>([]);

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (!user) return;

        this.appService.getMyApplications().subscribe(apps => {
            this.recentApps.set(apps.slice(0, 4));

            // Derive some stats from real applications
            const reviewing = apps.filter(a => a.status === 'REVIEWING').length;
            const accepted = apps.filter(a => a.status === 'SELECTED').length;
            const shortlisted = apps.filter(a => a.status === 'SHORTLISTED').length;
            const rejected = apps.filter(a => a.status === 'REJECTED').length;

            this.stats.set([
                { label: 'Applications', value: apps.length, icon: 'bi-send', bg: 'rgba(79,70,229,0.1)', color: '#4F46E5' },
                { label: 'Under Review', value: reviewing, icon: 'bi-eye', bg: 'rgba(251,191,36,0.1)', color: '#d97706' },
                { label: 'Shortlisted', value: shortlisted, icon: 'bi-star', bg: 'rgba(139,92,246,0.1)', color: '#7c3aed' },
                { label: 'Accepted', value: accepted, icon: 'bi-check-circle', bg: 'rgba(16,185,129,0.1)', color: '#10B981' },
                { label: 'Rejected', value: rejected, icon: 'bi-x-circle', bg: 'rgba(239,68,68,0.08)', color: '#ef4444' },
            ]);
        });

        this.jobService.getAllJobs().subscribe(jobs => {
            this.recommendedJobs.set(jobs.slice(0, 4));
        });

        // Load Real Profile Completion Progress
        this.seekerService.getProfile().subscribe((profile: any) => {
            this.profileCompletion.set(profile.completionPercentage || 0);
            this.completionTasks.set([
                { label: 'Upload your resume', link: '/seeker/resume-builder', done: profile.resumeUploaded },
                { label: 'Add work experience', link: '/seeker/profile', done: profile.experienceSet },
                { label: 'Add education', link: '/seeker/profile', done: profile.educationSet },
                { label: 'Add skills', link: '/seeker/profile', done: profile.skillsSet },
                { label: 'Write bio/summary', link: '/seeker/profile', done: profile.profileSummarySet },
            ]);
        });
    }
}
