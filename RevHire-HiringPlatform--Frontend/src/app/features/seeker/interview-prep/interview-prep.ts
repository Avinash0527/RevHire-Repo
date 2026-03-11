import { Component, OnInit, inject, signal } from '@angular/core';
import { NgClass, NgFor, NgIf, DatePipe, AsyncPipe, NgTemplateOutlet, TitleCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { InterviewPrepService, InterviewQuestion } from '../../../core/services/interview-prep.service';
import { ApplicationService } from '../../../core/services/application.service';

@Component({
    selector: 'app-interview-prep',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, NgClass, NgFor, NgIf, DatePipe, FormsModule, AsyncPipe, NgTemplateOutlet, TitleCasePipe],
    templateUrl: './interview-prep.html',
    styleUrl: './interview-prep.css'
})
export class InterviewPrepComponent implements OnInit {
    private prepService = inject(InterviewPrepService);
    private route = inject(ActivatedRoute);
    private appService = inject(ApplicationService);

    roles = signal<string[]>([]);
    skills = signal<string[]>([]);
    techRoles: string[] = [
        'DevOps Engineer',
        'Cyber Security Analyst',
        'Project Manager',
        'UI/UX Designer',
        'Game Developer',
        'Networking Engineer',
        'AI/ML Engineer'
    ];
    designRoles: string[] = [
        'Product Designer',
        'UX Designer',
        'UI Designer',
        'UX Researcher',
        'UX Writer',
        'Graphics Designer',
        'Brand Designer'
    ];
    financeRoles: string[] = [
        'Finance Analyst',
        'Quantitative Analyst',
        'Banking Financial Service',
        'Financial Advisor',
        'Accounting Manager'
    ];
    marketingRoles: string[] = [
        'Marketing Manager',
        'Digital Marketing Manager',
        'Content Marketer',
        'Social Media Manager',
        'Market Research'
    ];
    healthCareRoles: string[] = [
        'Medical AI Deployment',
        'Medical Coding',
        'Healthcare Administrator',
        'Healthcare Data Analyst'
    ];
    operationsRoles: string[] = [
        'IT Operation Manager',
        'Operation Engineering',
        'System Administrator',
        'Database Administrator',
        'Cloud Architect',
        'Technical Support Specialist'
    ];
    salesRoles: string[] = [
        'Sales Development Representative',
        'Business Development Representative',
        'Account Executive',
        'Solution Consultant'
    ];
    additionalSkills: string[] = [
        // Technology
        'Java', 'Python', 'TypeScript', 'JavaScript', 'Angular', 'React', 'Node.js', 'Spring Boot',
        'DevOps', 'Linux', 'Cloud Computing', 'AWS', 'Azure',
        'Cyber Security', 'Network Security', 'AI/ML',
        'Data Structures', 'System Design', 'SQL', 'NoSQL',

        // Design
        'Figma', 'UI Design', 'UX Design',
        'Adobe XD', 'Photoshop', 'Branding', 'Graphic Design',

        // Finance
        'Financial Analysis', 'Excel', 'Risk Analysis',
        'Accounting', 'Budgeting', 'Investment Analysis',

        // Marketing
        'Digital Marketing', 'SEO', 'SEM', 'Content Marketing',

        // Healthcare
        'Medical Coding', 'Healthcare Analytics', 'Healthcare Administration',

        // Operations
        'IT Operations', 'Cloud Architecture',
        'Technical Support',

        // Sales
        'CRM'
    ];

    selectedType = signal<'ROLE' | 'SKILL' | 'HR'>('ROLE');
    selectedCategory = signal<string>('');

    // Grouping questions by difficulty
    basicQuestions = signal<InterviewQuestion[]>([]);
    moderateQuestions = signal<InterviewQuestion[]>([]);
    advancedQuestions = signal<InterviewQuestion[]>([]);

    questions = signal<InterviewQuestion[]>([]); // Keep for backward compatibility or simple lists
    loading = signal<boolean>(true);

    // Track user's applied roles
    appliedRoles = signal<string[]>([]);

    ngOnInit() {
        this.loadFilterOptions();
        this.loadAppliedRoles();

        // Check if a role or skill was passed in the URL (e.g. from My Applications)
        this.route.queryParams.subscribe(params => {
            const roleParam = params['role'];
            const skillParam = params['skill'];
            const hrParam = params['hr'];

            if (roleParam) {
                this.selectedType.set('ROLE');
                this.selectedCategory.set(roleParam);
            } else if (skillParam) {
                this.selectedType.set('SKILL');
                this.selectedCategory.set(skillParam);
            } else if (hrParam) {
                this.selectedType.set('HR');
                this.selectedCategory.set('HR Interview');
            }

            // If we have an initial selection, fetch the questions
            if (this.selectedCategory() || this.selectedType() === 'HR') {
                // Use a small timeout to let the options load first so the dropdown matches
                setTimeout(() => this.fetchQuestions(), 100);
            }
        });
    }

    loadFilterOptions() {
        this.prepService.getAvailableRoles().subscribe(roles => this.roles.set(roles));
        this.prepService.getAvailableSkills().subscribe(skills => this.skills.set(skills));
    }

    technologyRoles(): string[] {
        const excluded = new Set([
            ...this.designRoles.map(r => r.toLowerCase()),
            ...this.financeRoles.map(r => r.toLowerCase()),
            ...this.marketingRoles.map(r => r.toLowerCase()),
            ...this.healthCareRoles.map(r => r.toLowerCase()),
            ...this.operationsRoles.map(r => r.toLowerCase()),
            ...this.salesRoles.map(r => r.toLowerCase())
        ]);
        const merged = [...this.techRoles, ...this.roles().filter(r => !excluded.has(r.toLowerCase()))];
        const seen = new Set<string>();
        return merged.filter(role => {
            const key = role.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    designDisplayRoles(): string[] {
        const merged = [...this.designRoles];
        const seen = new Set<string>();
        return merged.filter(role => {
            const key = role.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    financeDisplayRoles(): string[] {
        const merged = [...this.financeRoles];
        const seen = new Set<string>();
        return merged.filter(role => {
            const key = role.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    marketingDisplayRoles(): string[] {
        const merged = [...this.marketingRoles];
        const seen = new Set<string>();
        return merged.filter(role => {
            const key = role.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    healthCareDisplayRoles(): string[] {
        const merged = [...this.healthCareRoles];
        const seen = new Set<string>();
        return merged.filter(role => {
            const key = role.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    operationsDisplayRoles(): string[] {
        const merged = [...this.operationsRoles];
        const seen = new Set<string>();
        return merged.filter(role => {
            const key = role.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    salesDisplayRoles(): string[] {
        const merged = [...this.salesRoles];
        const seen = new Set<string>();
        return merged.filter(role => {
            const key = role.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    displaySkills(): string[] {
        const merged = [...this.additionalSkills, ...this.skills()];
        const seen = new Set<string>();
        return merged.filter(skill => {
            const key = skill.toLowerCase();
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
        });
    }

    loadAppliedRoles() {
        this.appService.getMyApplications().subscribe({
            next: (apps) => {
                // Extract unique job titles from applications
                const titles = apps.map(app => app.jobTitle);
                const uniqueTitles = [...new Set(titles)];
                this.appliedRoles.set(uniqueTitles);
            },
            error: (err) => {
                console.error('Failed to load user applications', err);
            }
        });
    }

    onTypeChange(type: 'ROLE' | 'SKILL' | 'HR') {
        this.selectedType.set(type);
        if (type === 'HR') {
            this.selectedCategory.set('HR Interview');
            this.fetchQuestions();
        } else {
            this.selectedCategory.set(''); // Reset category when switching type
            this.questions.set([]);
            this.basicQuestions.set([]);
            this.moderateQuestions.set([]);
            this.advancedQuestions.set([]);
        }
    }

    fetchQuestions() {
        if (!this.selectedCategory() && this.selectedType() !== 'HR') return;

        this.loading.set(true);

        const obs$ = this.selectedType() === 'ROLE'
            ? this.prepService.getQuestionsByRole(this.selectedCategory())
            : this.selectedType() === 'SKILL'
                ? this.prepService.getQuestionsBySkill(this.selectedCategory())
                : this.prepService.getHRQuestions();

        obs$.subscribe({
            next: (data) => {
                // If it's a role, let's do a soft-match incase the DB job title doesn't exactly match the JSON keys
                if (this.selectedType() === 'ROLE' && data.length === 0) {
                    this.prepService.getInterviewData().subscribe(fullData => {
                        const target = this.selectedCategory().toLowerCase();
                        let foundQuestions: InterviewQuestion[] = [];

                        Object.keys(fullData.roles).forEach(key => {
                            if (target.includes(key.toLowerCase()) || key.toLowerCase().includes(target)) {
                                foundQuestions = [...foundQuestions, ...fullData.roles[key]];
                            }
                        });

                        if (foundQuestions.length > 0) {
                            this.processQuestions(foundQuestions);
                        } else {
                            this.clearQuestions();
                            this.loading.set(false);
                        }
                    });
                    return;
                }

                this.processQuestions(data);
            },
            error: (err) => {
                console.error('Failed to load questions', err);
                this.clearQuestions();
                this.loading.set(false);
            }
        });
    }

    private clearQuestions() {
        this.questions.set([]);
        this.basicQuestions.set([]);
        this.moderateQuestions.set([]);
        this.advancedQuestions.set([]);
    }

    private processQuestions(data: InterviewQuestion[]) {
        // Map over data and add an answer hint to legacy objects missing the new param
        const annotatedData = data.map(q => ({
            ...q,
            answer: q.answer || "This is a comprehensive, mock answer designed to help you prepare. In a real scenario, you would expand on the core concepts mentioned in the hint and provide practical examples from your experience.",
            show: false
        }));

        this.questions.set(annotatedData);

        // Categorize by difficulty
        this.basicQuestions.set(annotatedData.filter(q => q.difficulty === 'BASIC'));
        this.moderateQuestions.set(annotatedData.filter(q => q.difficulty === 'MODERATE'));
        this.advancedQuestions.set(annotatedData.filter(q => q.difficulty === 'ADVANCED'));

        this.loading.set(false);
    }
}
