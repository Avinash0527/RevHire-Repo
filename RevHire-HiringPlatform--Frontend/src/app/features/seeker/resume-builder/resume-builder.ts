import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { NgClass, CommonModule } from '@angular/common';
import { ResumeService } from '../../../core/services/resume.service';
import { ToastService } from '../../../core/services/toast.service';
import { LoadingService } from '../../../core/services/loading.service';
import { Resume } from '../../../core/models/resume.model';
import { SeekerService } from '../../../core/services/seeker.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-resume-builder',
    standalone: true,
    imports: [ReactiveFormsModule, NgClass, CommonModule],
    templateUrl: './resume-builder.html',
    styleUrl: './resume-builder.css'
})
export class ResumeBuilderComponent implements OnInit {
    private fb = inject(FormBuilder);
    private resumeService = inject(ResumeService);
    private toast = inject(ToastService);
    private ls = inject(LoadingService);
    private seekerService = inject(SeekerService);
    private authService = inject(AuthService);

    saving = signal(false);
    zoomScale = signal(1);
    previewModalOpen = signal(false);
    expandedSections = signal<Set<string>>(new Set(['personal', 'experience', 'education', 'skills', 'projects', 'certificates']));
    private readonly websitePattern = /^(https?:\/\/)[^\s$.?#].[^\s]*$/i;
    private readonly yearPattern = /^(19|20)\d{2}$/;
    private readonly strictEmailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    private readonly phonePattern = /^\d{10}$/;
    private readonly githubPattern = /^https?:\/\/(www\.)?github\.com(\/.*)?$/i;

    resumeForm = this.fb.group({
        fullName: ['', Validators.required],
        title: [''],
        email: ['', [Validators.required, Validators.pattern(this.strictEmailPattern)]],
        phone: ['', Validators.pattern(this.phonePattern)],
        location: [''],
        website: ['', Validators.pattern(this.websitePattern)],
        summary: [''],
        experience: this.fb.array([]),
        education: this.fb.array([]),
        skills: this.fb.array([]),
        projects: this.fb.array([]),
        certifications: this.fb.array([])
    });

    get experienceArr() { return this.resumeForm.get('experience') as FormArray; }
    get educationArr() { return this.resumeForm.get('education') as FormArray; }
    get skillsArr() { return this.resumeForm.get('skills') as FormArray; }
    get projectsArr() { return this.resumeForm.get('projects') as FormArray; }
    get certificationsArr() { return this.resumeForm.get('certifications') as FormArray; }

    private createExperienceGroup(exp: any = {}) {
        return this.fb.group({
            company: [exp.company || ''],
            position: [exp.position || ''],
            startDate: [exp.startDate || ''],
            endDate: [exp.endDate || ''],
            description: [exp.description || '']
        });
    }

    private createEducationGroup(edu: any = {}) {
        return this.fb.group({
            school: [edu.school || ''],
            degree: [edu.degree || ''],
            field: [edu.field || ''],
            endDate: [edu.endDate || '', Validators.pattern(this.yearPattern)]
        });
    }

    private createProjectGroup(project: any = {}) {
        return this.fb.group({
            name: [project.name || ''],
            description: [project.description || ''],
            link: [project.link || '', Validators.pattern(this.githubPattern)],
            technologies: [project.technologies || []]
        });
    }

    ngOnInit(): void {
        this.resumeService.getResume().subscribe(resume => {
            // Check if resume has actual content. Backend might return empty object {}
            const hasResume = resume && (resume.fullName || resume.summary || (resume as any).objective);

            if (hasResume) {
                const objective = (resume as any).objective || resume.summary || '';
                let summaryValue = objective;
                let builderData: any = null;

                // Extract metadata if present
                const metaStart = objective.indexOf('[BUILDER_METADATA]');
                const metaEnd = objective.indexOf('[/BUILDER_METADATA]');

                if (metaStart !== -1 && metaEnd !== -1) {
                    try {
                        const jsonStr = objective.substring(metaStart + 18, metaEnd);
                        builderData = JSON.parse(jsonStr);
                        summaryValue = objective.substring(0, metaStart).trim();
                    } catch (e) {
                        console.error('Failed to parse resume metadata', e);
                    }
                }

                this.resumeForm.patchValue({
                    fullName: resume.fullName,
                    title: resume.title,
                    email: resume.email,
                    phone: resume.phone,
                    location: resume.location || builderData?.location || '',
                    website: resume.website || builderData?.website || '',
                    summary: summaryValue
                });

                // Prioritize builderData for arrays if it exists
                if (builderData) {
                    if (builderData.experience?.length) {
                        this.experienceArr.clear();
                        builderData.experience.forEach((exp: any) => this.experienceArr.push(this.createExperienceGroup(exp)));
                    }
                    if (builderData.education?.length) {
                        this.educationArr.clear();
                        builderData.education.forEach((edu: any) => this.educationArr.push(this.createEducationGroup(edu)));
                    }
                    if (builderData.skills?.length) {
                        this.skillsArr.clear();
                        builderData.skills.forEach((skill: any) => this.skillsArr.push(this.fb.group(skill)));
                    }
                    if (builderData.projects?.length) {
                        this.projectsArr.clear();
                        builderData.projects.forEach((p: any) => this.projectsArr.push(this.createProjectGroup(p)));
                    }
                    if (builderData.certifications?.length) {
                        this.certificationsArr.clear();
                        builderData.certifications.forEach((c: any) => this.certificationsArr.push(this.fb.group(c)));
                    }
                } else {
                    // Fallback to legacy structure
                    if (resume.experiences?.length) {
                        resume.experiences.forEach((exp: any) => this.experienceArr.push(this.createExperienceGroup(exp)));
                    }
                    if (resume.education?.length) {
                        resume.education.forEach(edu => this.educationArr.push(this.createEducationGroup(edu)));
                    }
                    if (resume.skills?.length) {
                        resume.skills.forEach(skill => this.skillsArr.push(this.fb.group(skill)));
                    }
                    if (resume.projects?.length) {
                        resume.projects.forEach(p => this.projectsArr.push(this.createProjectGroup(p)));
                    }
                    if (resume.certifications?.length) {
                        resume.certifications.forEach(c => this.certificationsArr.push(this.fb.group(c)));
                    }
                }
            }

            // Pre-populate name and email from AuthService if not already present
            const user = this.authService.currentUser();
            if (user) {
                this.resumeForm.patchValue({
                    fullName: this.resumeForm.get('fullName')?.value || user.name,
                    email: this.resumeForm.get('email')?.value || user.email
                });
            }

            // Always fetch profile to pre-fill missing basic info or if resume is new
            this.seekerService.getProfile().subscribe(profile => {
                if (profile) {
                    this.resumeForm.patchValue({
                        fullName: this.resumeForm.get('fullName')?.value || profile.name,
                        email: this.resumeForm.get('email')?.value || profile.email,
                        phone: this.resumeForm.get('phone')?.value || profile.phone,
                        location: this.resumeForm.get('location')?.value || profile.location,
                        summary: this.resumeForm.get('summary')?.value || profile.summary || profile.headline
                    });

                    // If no skills in resume yet, import from profile
                    if (this.skillsArr.length === 0 && profile.skillsList) {
                        profile.skillsList.forEach((s: any) => {
                            this.skillsArr.push(this.fb.group({
                                name: [s.name],
                                level: [s.level || 'INTERMEDIATE']
                            }));
                        });
                    }
                }

                // Add empty sections only if still empty
                if (this.experienceArr.length === 0) this.addExperience();
                if (this.educationArr.length === 0) this.addEducation();
                if (this.skillsArr.length === 0) this.addSkill();
                if (this.projectsArr.length === 0) this.addProject();
                if (this.certificationsArr.length === 0) this.addCertification();
            });
        });
    }

    addExperience() {
        this.experienceArr.push(this.createExperienceGroup());
    }

    removeExperience(index: number) { this.experienceArr.removeAt(index); }

    isExperiencePresent(index: number): boolean {
        const val = this.experienceArr.at(index)?.get('endDate')?.value;
        return String(val || '').toLowerCase() === 'present';
    }

    setExperiencePresent(index: number): void {
        const ctrl = this.experienceArr.at(index)?.get('endDate');
        if (!ctrl) return;
        ctrl.setValue('Present');
        ctrl.markAsTouched();
        ctrl.markAsDirty();
    }

    useExperienceEndDate(index: number): void {
        const ctrl = this.experienceArr.at(index)?.get('endDate');
        if (!ctrl) return;
        if (this.isExperiencePresent(index)) {
            ctrl.setValue('');
        }
        ctrl.markAsTouched();
        ctrl.markAsDirty();
    }

    addEducation() {
        this.educationArr.push(this.createEducationGroup());
    }

    removeEducation(index: number) { this.educationArr.removeAt(index); }

    addSkill() {
        this.skillsArr.push(this.fb.group({
            name: [''], level: ['INTERMEDIATE']
        }));
    }

    removeSkill(index: number) { this.skillsArr.removeAt(index); }

    addProject() {
        this.projectsArr.push(this.createProjectGroup());
    }
    removeProject(index: number) { this.projectsArr.removeAt(index); }

    addCertification() {
        this.certificationsArr.push(this.fb.group({
            name: [''], issuer: [''], date: ['']
        }));
    }
    removeCertification(index: number) { this.certificationsArr.removeAt(index); }

    toggleSection(section: string) {
        const updated = new Set(this.expandedSections());
        if (updated.has(section)) updated.delete(section);
        else updated.add(section);
        this.expandedSections.set(updated);
    }

    isExpanded(section: string): boolean {
        return this.expandedSections().has(section);
    }

    zoomIn() { this.zoomScale.update(s => Math.min(s + 0.1, 1.5)); }
    zoomOut() { this.zoomScale.update(s => Math.max(s - 0.1, 0.5)); }
    openPreviewModal() { this.previewModalOpen.set(true); }
    closePreviewModal() { this.previewModalOpen.set(false); }

    saveResume(): void {
        if (this.resumeForm.invalid) {
            this.resumeForm.markAllAsTouched();
            return;
        }
        this.saving.set(true);
        this.ls.start();

        const val = this.resumeForm.value;

        // metadata injection: embed the full form state into the objective field
        const metadata = `\n\n[BUILDER_METADATA]${JSON.stringify(val)}[/BUILDER_METADATA]`;

        // Flatten data for the backend ResumeText model
        const resumeTextPayload = {
            fullName: val.fullName || '',
            title: val.title || '',
            email: val.email || '',
            phone: val.phone || '',
            location: val.location || '',
            website: val.website || '',
            objective: (val.summary || '') + metadata,
            skills: val.skills?.map((s: any) => s.name).join(', '),
            experience: val.experience?.map((e: any) =>
                `${e.position} at ${e.company} (${e.startDate} - ${e.endDate})\n${e.description}`
            ).join('\n\n'),
            education: val.education?.map((e: any) =>
                `${e.degree} in ${e.field} from ${e.school} (End: ${e.endDate})`
            ).join('\n\n'),
            projects: val.projects?.map((p: any) =>
                `${p.name}: ${p.description} (Tech: ${p.technologies?.join(', ') || ''})`
            ).join('\n\n'),
            certifications: val.certifications?.map((c: any) =>
                `${c.name} by ${c.issuer} (${c.date})`
            ).join('\n\n')
        };

        // Note: Using resumeService.saveResume which calls POST /api/seeker/profile/resume-text
        this.resumeService.saveResume(resumeTextPayload as any).subscribe({
            next: () => {
                this.ls.stop();
                this.saving.set(false);
                this.toast.success('Resume saved successfully!');
            },
            error: (err) => {
                this.ls.stop();
                this.saving.set(false);
                this.toast.error('Failed to save resume: ' + (err.error || 'Server error'));
            }
        });
    }

    downloadResume(): void {
        const previewEl = document.querySelector('.resume-sheet .resume-view') as HTMLElement | null;
        if (!previewEl) {
            this.toast.error('Resume preview not found.');
            return;
        }

        const popup = window.open('', '_blank', 'width=1000,height=900');
        if (!popup) {
            this.toast.error('Popup blocked. Allow popups and try again.');
            return;
        }

        const styleTags = Array.from(document.querySelectorAll('style, link[rel="stylesheet"]'))
            .map((el) => el.outerHTML)
            .join('\n');

        const safeName = (this.resumeForm.get('fullName')?.value || 'Resume').replace(/\s+/g, '_');
        const resumeHtml = previewEl.outerHTML;

        popup.document.open();
        popup.document.write(`
            <html>
                <head>
                    <title>${safeName}.pdf</title>
                    ${styleTags}
                    <style>
                        body { margin: 0; background: #ffffff; }
                        .resume-sheet {
                            background: #ffffff !important;
                            padding: 0 !important;
                            overflow: visible !important;
                            height: auto !important;
                            display: block !important;
                        }
                        .resume-view {
                            transform: none !important;
                            transform-origin: top left !important;
                            box-shadow: none !important;
                            border-radius: 0 !important;
                            width: 210mm !important;
                            max-width: 210mm !important;
                            margin: 0 auto !important;
                            min-height: auto !important;
                        }
                        @page { size: A4; margin: 10mm; }
                    </style>
                </head>
                <body>
                    <div class="resume-sheet">${resumeHtml}</div>
                </body>
            </html>
        `);
        popup.document.close();

        // Open browser print dialog so user can Save as PDF locally.
        setTimeout(() => {
            popup.focus();
            popup.print();
        }, 300);
    }
}
