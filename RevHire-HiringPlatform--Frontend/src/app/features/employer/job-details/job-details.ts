import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { Job } from '../../../core/models/job.model';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-employer-job-details',
    standalone: true,
    imports: [RouterLink, NgClass],
    templateUrl: './job-details.html',
    styleUrl: './job-details.css'
})
export class EmployerJobDetailsComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private jobService = inject(JobService);
    private toast = inject(ToastService);

    loading = signal(true);
    job = signal<Job | null>(null);

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        if (!id) {
            this.loading.set(false);
            this.toast.error('Invalid job id');
            return;
        }

        this.jobService.getJobById(id).subscribe({
            next: (job) => {
                this.job.set(job);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.toast.error('Failed to load job details');
            }
        });
    }

    asList(value: string | string[] | undefined): string[] {
        if (!value) return [];
        if (Array.isArray(value)) return value.filter(Boolean);
        return value
            .split(',')
            .map((x) => x.trim())
            .filter(Boolean);
    }
}
