import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormArray, FormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { NgClass } from '@angular/common';
import { JobService } from '../../../core/services/job.service';
import { CompanyService } from '../../../core/services/company.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-post-job',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, NgClass, FormsModule],
    templateUrl: './post-job.html',
    styleUrl: './post-job.css'
})
export class PostJobComponent implements OnInit {
    private fb = inject(FormBuilder);
    private jobService = inject(JobService);
    private companyService = inject(CompanyService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);
    auth = inject(AuthService);

    /** Rejects today and past dates */
    private futureDateValidator = (ctrl: AbstractControl): ValidationErrors | null => {
        if (!ctrl.value) return null;
        if (this.isEditMode()) return null; // Allow historical dates when editing
        const selected = new Date(ctrl.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return selected > today ? null : { futureDate: true };
    };

    currentStep = signal(1);
    posting = signal(false);
    agreed = false;
    submitError = signal('');
    isEditMode = signal(false);
    jobId = signal<number | null>(null);
    companyDisplayName = signal('Your Company');

    jobForm = this.fb.group({
        title: ['', Validators.required],
        description: ['', Validators.required],
        category: ['TECHNOLOGY', Validators.required],
        jobType: ['FULL_TIME', Validators.required],
        location: ['', Validators.required],
        salaryMin: [null as number | null],
        salaryMax: [null as number | null],
        experienceLevel: ['MID', Validators.required],
        deadline: ['', [Validators.required, this.futureDateValidator]],
        education: [''],
        openings: [1, [Validators.required, Validators.min(1)]],
        status: ['ACTIVE'],
        skills: ['', Validators.required],
        responsibilities: this.fb.array([this.fb.control('', Validators.required)]),
        requirements: this.fb.array([this.fb.control('', Validators.required)])
    });

    preventDecimals(event: KeyboardEvent): void {
        if (event.key === '.' || event.key === 'e' || event.key === '-' || event.key === '+') {
            event.preventDefault();
        }
    }

    onSubmit(): void {
        const id = this.route.snapshot.params['id'];
        if (id) {
            this.isEditMode.set(true);
            this.jobId.set(+id);
            this.loadJobData(+id);
        }
    }

    ngOnInit(): void {
        const authCompany = this.auth.currentUser()?.companyName?.trim();
        if (authCompany) this.companyDisplayName.set(authCompany);

        this.companyService.getCompany().subscribe({
            next: (company) => {
                const name = company?.name?.trim();
                if (name) this.companyDisplayName.set(name);
            },
            error: () => { }
        });

        const id = this.route.snapshot.params['id'];
        if (id) {
            this.isEditMode.set(true);
            this.jobId.set(+id);
            this.loadJobData(+id);
        }
    }

    private loadJobData(id: number): void {
        this.ls.start();
        this.jobService.getJobById(id).subscribe({
            next: (job) => {
                this.ls.stop();

                // Parse salary
                let sMin = null;
                let sMax = null;
                if (job.salary && job.salary.includes('-')) {
                    const parts = job.salary.split('-').map(s => s.replace(/[^0-9]/g, '').trim());
                    sMin = parts[0] ? parseInt(parts[0]) : null;
                    sMax = parts[1] ? parseInt(parts[1]) : null;
                }

                // Determine experienceLevel from experienceYears
                let expLevel = 'MID';
                if (job.experienceYears <= 2) expLevel = 'ENTRY';
                else if (job.experienceYears >= 10) expLevel = 'LEAD';
                else if (job.experienceYears >= 6) expLevel = 'SENIOR';

                this.jobForm.patchValue({
                    title: job.title,
                    description: job.description,
                    category: (job as any).category || 'TECHNOLOGY',
                    jobType: job.jobType,
                    location: job.location,
                    salaryMin: sMin,
                    salaryMax: sMax,
                    experienceLevel: expLevel,
                    deadline: job.deadline,
                    education: job.education || '',
                    openings: job.openings || 1,
                    status: job.status || 'ACTIVE',
                    skills: job.skills || ''
                });

                this.respArr.clear();
                if (job.responsibilities) {
                    job.responsibilities.split('\n').filter(r => !!r.trim()).forEach(r => {
                        this.respArr.push(this.fb.control(r.trim(), Validators.required));
                    });
                }
                if (this.respArr.length === 0) this.respArr.push(this.fb.control('', Validators.required));

                this.reqArr.clear();
                if (job.requirements) {
                    job.requirements.split('\n').filter(r => !!r.trim()).forEach(r => {
                        this.reqArr.push(this.fb.control(r.trim(), Validators.required));
                    });
                }
                if (this.reqArr.length === 0) this.reqArr.push(this.fb.control('', Validators.required));

                this.agreed = true; // Pre-checked for edit
            },
            error: (err) => {
                this.ls.stop();
                this.toast.error('Failed to load job details');
                this.router.navigate(['/employer/jobs']);
            }
        });
    }

    get respArr() { return this.jobForm.get('responsibilities') as FormArray; }
    get reqArr() { return this.jobForm.get('requirements') as FormArray; }

    nextStep() {
        if (this.currentStep() === 1) {
            const step1Fields = ['title', 'category', 'jobType', 'experienceLevel', 'location', 'deadline', 'openings'];
            let isValid = true;
            step1Fields.forEach(field => {
                const ctrl = this.jobForm.get(field);
                if (ctrl?.invalid) {
                    ctrl.markAsTouched();
                    isValid = false;
                }
            });
            if (!isValid) return;
        } else if (this.currentStep() === 2) {
            const step2Fields = ['description', 'skills'];
            let isValid = true;
            step2Fields.forEach(field => {
                const ctrl = this.jobForm.get(field);
                if (ctrl?.invalid) {
                    ctrl.markAsTouched();
                    isValid = false;
                }
            });
            if (this.respArr.invalid) { this.respArr.markAllAsTouched(); isValid = false; }
            if (this.reqArr.invalid) { this.reqArr.markAllAsTouched(); isValid = false; }
            if (!isValid) return;
        }

        if (this.currentStep() < 3) this.currentStep.update(s => s + 1);
    }
    prevStep() { if (this.currentStep() > 1) this.currentStep.update(s => s - 1); }

    addResp() { this.respArr.push(this.fb.control('')); }
    removeResp(i: number) { this.respArr.removeAt(i); }

    addReq() { this.reqArr.push(this.fb.control('')); }
    removeReq(i: number) { this.reqArr.removeAt(i); }

    submitJob() {
        this.submitError.set('');
        if (this.jobForm.invalid) {
            this.jobForm.markAllAsTouched();
            this.submitError.set('Please fill in all required fields before posting.');
            return;
        }

        this.posting.set(true);
        this.ls.start();

        const formValue = this.jobForm.value;

        let expYears = 0;
        switch (formValue.experienceLevel) {
            case 'ENTRY': expYears = 2; break;
            case 'MID': expYears = 5; break;
            case 'SENIOR': expYears = 9; break;
            case 'LEAD': expYears = 10; break;
        }

        // Skills (tags) go into `skills` — the backend now uses a dedicated field
        const skillsCSV = typeof formValue.skills === 'string' ? formValue.skills : '';

        const fullDescription = formValue.description || '';
        const respText = formValue.responsibilities?.filter((r: any) => !!(r?.trim())).join('\n') || '';
        const reqText = formValue.requirements?.filter((r: any) => !!(r?.trim())).join('\n') || '';

        const jobData: any = {
            title: formValue.title,
            description: fullDescription,
            responsibilities: respText,
            requirements: reqText,
            location: formValue.location,
            jobType: formValue.jobType,
            deadline: formValue.deadline,
            experienceYears: expYears,
            education: formValue.education || null,
            openings: formValue.openings ?? 1,
            skills: skillsCSV,
            category: formValue.category,
            salary: (formValue.salaryMin && formValue.salaryMax) ? `₹${formValue.salaryMin} - ₹${formValue.salaryMax}` : 'Competitive',
            status: formValue.status || 'ACTIVE'
        };

        const user = this.auth.currentUser();
        if (!user) return;

        const request = this.isEditMode() && this.jobId()
            ? this.jobService.updateJob(this.jobId()!, jobData)
            : this.jobService.postJob(jobData);

        request.subscribe({
            next: (newJob) => {
                this.ls.stop();
                this.posting.set(false);
                this.toast.success(this.isEditMode() ? 'Job updated successfully!' : 'Job posted successfully!');
                this.router.navigate(['/employer/jobs']);
            },
            error: (err) => {
                this.ls.stop();
                this.posting.set(false);
                this.submitError.set(err?.error?.message || `Failed to ${this.isEditMode() ? 'update' : 'post'} job. Please try again.`);
            }
        });
    }
}
