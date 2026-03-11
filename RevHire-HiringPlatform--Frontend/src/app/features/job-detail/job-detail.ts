import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NgClass, SlicePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { JobService } from '../../core/services/job.service';
import { ApplicationService } from '../../core/services/application.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { LoadingService } from '../../core/services/loading.service';
import { CompanyService } from '../../core/services/company.service';
import { SeekerService } from '../../core/services/seeker.service';
import { Job } from '../../core/models/job.model';
import { Company } from '../../core/models/company.model';

@Component({
    selector: 'app-job-detail',
    standalone: true,
    imports: [RouterLink, NgClass, ReactiveFormsModule, FormsModule, SlicePipe],
    templateUrl: './job-detail.html',
    styleUrl: './job-detail.css'
})
export class JobDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    readonly router = inject(Router);
    private jobService = inject(JobService);
    private appService = inject(ApplicationService);
    private toast = inject(ToastService);
    readonly authService = inject(AuthService);
    private ls = inject(LoadingService);
    private fb = inject(FormBuilder);
    private companyService = inject(CompanyService);
    private seekerService = inject(SeekerService);

    job = signal<Job | null>(null);
    company = signal<Company | null>(null);
    notFound = signal(false);
    showModal = signal(false);
    isSaved = signal(false);
    hasApplied = signal(false);
    applying = signal(false);
    selectedResume = signal<File | null>(null);
    dragActive = signal(false);
    applySubmitted = false;

    applyForm = this.fb.group({ coverLetter: [''] });

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.jobService.getJobById(id).subscribe(job => {
            if (!job) { this.notFound.set(true); return; }
            this.job.set(this.normalizeJobForDisplay(job));
            this.isSaved.set(this.jobService.isSaved(id));

            // Fetch company details
            this.companyService.getCompanyById(job.companyId).subscribe(company => {
                this.company.set(company);
            });

            const user = this.authService.currentUser();
            if (user) {
                this.refreshApplyStatus(id);
            }
        });
    }

    private normalizeJobForDisplay(job: Job): Job {
        return job;
    }

    private refreshApplyStatus(jobId: number): void {
        this.appService.getMyApplications().subscribe({
            next: (apps) => {
                const activeApplication = apps.find(a => a.jobId === jobId && a.status !== 'WITHDRAWN');
                this.hasApplied.set(!!activeApplication);
            },
            error: () => {
                // Fallback to old check if list endpoint fails for any reason.
                this.appService.hasApplied(jobId).subscribe(applied => this.hasApplied.set(applied));
            }
        });
    }

    openApplyModal(): void {
        if (!this.authService.isSeeker()) {
            this.router.navigate(['/login']);
            return;
        }
        this.showModal.set(true);
    }
    closeModal(): void {
        this.showModal.set(false);
        this.dragActive.set(false);
        this.selectedResume.set(null);
    }

    onResumeSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (file) this.setResumeFile(file);
        input.value = '';
    }

    onDropResume(event: DragEvent): void {
        event.preventDefault();
        this.dragActive.set(false);
        const file = event.dataTransfer?.files?.[0];
        if (file) this.setResumeFile(file);
    }

    private setResumeFile(file: File): void {
        const allowedExtensions = ['.pdf', '.doc', '.docx'];
        const fileName = file.name.toLowerCase();
        if (!allowedExtensions.some(ext => fileName.endsWith(ext))) {
            this.toast.error('Only PDF, DOC, and DOCX files are allowed.');
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            this.toast.error('File size exceeds 5MB limit.');
            return;
        }

        this.selectedResume.set(file);
    }

    submitApplication(): void {
        this.applySubmitted = true;
        if (this.applyForm.invalid) return;
        this.applying.set(true); this.ls.start();
        const coverLetter = this.applyForm.value.coverLetter || '';

        const completeSuccess = () => {
            this.ls.stop();
            this.applying.set(false);
            this.hasApplied.set(true);
            this.closeModal();
            this.toast.success('Application submitted successfully!');
        };

        const completeFailure = (msg: string) => {
            this.ls.stop();
            this.applying.set(false);
            this.toast.error(msg);
        };

        const submitApplicationRequest = (resumeFileId?: number) => {
            this.appService.applyToJob(this.job()!.id, coverLetter, resumeFileId).subscribe({
                next: () => completeSuccess(),
                error: (err) => {
                    const backendMsg =
                        (typeof err?.error === 'string' && err.error) ||
                        err?.error?.message ||
                        err?.message ||
                        'Failed to submit application.';
                    completeFailure(backendMsg);
                }
            });
        };

        const resumeFile = this.selectedResume();
        if (resumeFile) {
            this.seekerService.uploadResume(resumeFile).subscribe({
                next: (savedFile: any) => submitApplicationRequest(savedFile.id),
                error: () => {
                    this.ls.stop();
                    this.applying.set(false);
                    this.toast.error('Resume upload failed. Please try again.');
                }
            });
            return;
        }

        submitApplicationRequest();
    }

    toggleSave(): void {
        if (!this.authService.isAuthenticated()) {
            this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
            return;
        }

        this.jobService.toggleSave(this.job()!.id).subscribe(saved => {
            this.isSaved.set(saved);
            this.toast.info(saved ? 'Job saved to your list!' : 'Job removed from saved list.');
        });
    }

    formatSalaryDisplay(salaryText: string): string {
        const raw = (salaryText || '').trim();
        if (!raw) return '';

        const values = raw.match(/\d+(\.\d+)?/g)?.map(v => Number(v)) || [];
        if (values.length === 0) {
            return raw.includes('₹') ? raw : `₹${raw}`;
        }

        const annualValues = values.map(v => (v <= 200000 ? v * 12 : v));
        const lpaValues = annualValues.map(v => v / 100000);

        if (lpaValues.length >= 2) {
            return `₹${lpaValues[0].toFixed(1)} - ₹${lpaValues[1].toFixed(1)} LPA`;
        }

        return `₹${lpaValues[0].toFixed(1)} LPA`;
    }
}
