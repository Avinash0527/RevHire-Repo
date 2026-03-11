import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgClass, DatePipe, CommonModule } from '@angular/common';
import { CompanyService } from '../../core/services/company.service';
import { JobService } from '../../core/services/job.service';
import { Company } from '../../core/models/company.model';
import { Job } from '../../core/models/job.model';
import { JobCardComponent } from '../../shared/components/job-card/job-card';

@Component({
    selector: 'app-company-detail',
    standalone: true,
    imports: [CommonModule, RouterLink, NgClass, DatePipe, JobCardComponent],
    templateUrl: './company-detail.html',
    styleUrl: './company-detail.css'
})
export class CompanyDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private companyService = inject(CompanyService);
    private jobService = inject(JobService);

    company = signal<Company | null>(null);
    jobs = signal<Job[]>([]);

    // Derived state for open positions
    activeJobs = computed(() => {
        const allJobs = this.jobs();
        console.log('Company Jobs:', allJobs);
        return allJobs.filter(j => j.status?.toUpperCase() === 'ACTIVE');
    });
    openJobsCount = computed(() => this.activeJobs().length);
    totalPositionsCount = computed(() =>
        this.activeJobs().reduce((acc, job) => acc + (job.openings || 1), 0)
    );

    loading = signal(true);
    notFound = signal(false);

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        if (!id) {
            this.notFound.set(true);
            this.loading.set(false);
            return;
        }

        this.companyService.getCompanyById(id).subscribe({
            next: (company) => {
                this.company.set(company);
                this.fetchCompanyJobs(id);
            },
            error: () => {
                this.notFound.set(true);
                this.loading.set(false);
            }
        });
    }

    private fetchCompanyJobs(companyId: number): void {
        this.companyService.getCompanyJobs(companyId).subscribe({
            next: (jobs) => {
                this.jobs.set(jobs);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    onSaveToggle(jobId: number): void {
        // Handled by JobCardComponent internally or can be refreshed here
    }
}
