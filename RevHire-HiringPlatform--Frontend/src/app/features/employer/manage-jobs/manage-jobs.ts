import { Component, inject, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { AuthService } from '../../../core/services/auth.service';
import { Job } from '../../../core/models/job.model';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-manage-jobs',
    standalone: true,
    imports: [RouterLink, NgClass],
    templateUrl: './manage-jobs.html',
    styleUrl: './manage-jobs.css'
})
export class ManageJobsComponent implements OnInit {
    private jobService = inject(JobService);
    private auth = inject(AuthService);
    private toast = inject(ToastService);
    private router = inject(Router);

    Math = Math;
    loading = signal(true);
    jobs = signal<Job[]>([]);

    showDeleteModal = signal(false);
    jobToDeleteId = signal<number | null>(null);
    isDeleting = signal(false);

    ngOnInit(): void {
        const user = this.auth.currentUser();
        if (user) {
            this.loadJobs(user.id);
        } else {
            // Fallback if signal hasn't propagated yet
            this.loading.set(false);
        }
    }

    private loadJobs(userId: number): void {
        this.loading.set(true);
        this.jobService.getJobsByEmployer(userId).subscribe({
            next: (jobs) => {
                this.jobs.set(jobs);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                // toast.error is already handled in many services but adding safety
            }
        });
    }

    deleteJob(id: number): void {
        this.jobToDeleteId.set(id);
        this.showDeleteModal.set(true);
    }

    cancelDelete(): void {
        this.showDeleteModal.set(false);
        this.jobToDeleteId.set(null);
    }

    confirmDelete(): void {
        const id = this.jobToDeleteId();
        if (id) {
            this.isDeleting.set(true);
            this.jobService.deleteJob(id).subscribe({
                next: () => {
                    this.jobs.update(list => list.filter(j => j.id !== id));
                    this.toast.success('Job deleted successfully');
                    this.cancelDelete();
                    this.isDeleting.set(false);
                },
                error: () => {
                    this.toast.error('Failed to delete job');
                    this.cancelDelete();
                    this.isDeleting.set(false);
                }
            });
        }
    }

    editJob(id: number): void {
        this.router.navigate(['/employer/edit-job', id]);
    }
}
