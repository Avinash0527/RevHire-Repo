import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { Job } from '../../../core/models/job.model';
import { ToastService } from '../../../core/services/toast.service';
import { JobCardComponent } from '../../../shared/components/job-card/job-card';

@Component({
    selector: 'app-saved-jobs',
    standalone: true,
    imports: [RouterLink, NgClass, JobCardComponent],
    templateUrl: './saved-jobs.html',
    styleUrl: './saved-jobs.css'
})
export class SavedJobsComponent implements OnInit {
    private jobService = inject(JobService);
    private toast = inject(ToastService);

    loading = signal(true);
    savedJobs = signal<Job[]>([]);

    ngOnInit(): void {
        this.loadSavedJobs();
    }

    loadSavedJobs(): void {
        this.loading.set(true);
        this.jobService.getSavedJobs().subscribe(jobs => {
            this.savedJobs.set(jobs);
            this.loading.set(false);
        });
    }

    onSaveToggle(jobId: number): void {
        this.jobService.toggleSave(jobId).subscribe({
            next: (isSaved) => {
                if (!isSaved) {
                    this.savedJobs.update(jobs => jobs.filter(j => j.id !== jobId));
                    this.toast.success('Job removed from saved list');
                }
            },
            error: (err) => {
                this.toast.error(err.error || 'Failed to update bookmark');
            }
        });
    }
}
