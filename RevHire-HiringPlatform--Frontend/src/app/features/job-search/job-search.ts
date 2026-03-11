import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgClass, TitleCasePipe } from '@angular/common';
import { JobService } from '../../core/services/job.service';
import { Job } from '../../core/models/job.model';
import { JobCardComponent } from '../../shared/components/job-card/job-card';
import { ToastService } from '../../core/services/toast.service';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';

@Component({
    selector: 'app-job-search',
    standalone: true,
    imports: [RouterLink, FormsModule, NgClass, TitleCasePipe, JobCardComponent, PaginationComponent],
    templateUrl: './job-search.html',
    styleUrl: './job-search.css'
})
export class JobSearchComponent implements OnInit {
    private jobService = inject(JobService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private toastService = inject(ToastService);

    loading = signal(true);
    allJobs = signal<Job[]>([]);
    filteredJobs = signal<Job[]>([]);

    // Pagination
    currentPage = signal(1);
    readonly pageSize = 12;

    pagedJobs = computed(() => {
        const jobs = this.filteredJobs();
        const start = (this.currentPage() - 1) * this.pageSize;
        return jobs.slice(start, start + this.pageSize);
    });

    keyword = '';
    locationFilter = '';
    selectedCategory = '';
    selectedTypes: string[] = [];
    selectedLevels: string[] = [];
    salaryMin = 0;
    sortBy = 'recent';

    jobTypes = [
        { value: 'FULL_TIME', label: 'Full Time' },
        { value: 'PART_TIME', label: 'Part Time' },
        { value: 'REMOTE', label: 'Remote' },
        { value: 'CONTRACT', label: 'Contract' },
    ];

    categories = [
        { value: 'TECHNOLOGY', label: 'Technology', icon: '\uD83D\uDCBB' },
        { value: 'DESIGN', label: 'Design', icon: '\uD83C\uDFA8' },
        { value: 'FINANCE', label: 'Finance', icon: '\uD83D\uDCCA' },
        { value: 'MARKETING', label: 'Marketing', icon: '\uD83D\uDCE3' },
        { value: 'HEALTHCARE', label: 'Healthcare', icon: '\uD83C\uDFE5' },
        { value: 'OPERATIONS', label: 'Operations', icon: '\u2699\uFE0F' },
        { value: 'SALES', label: 'Sales', icon: '\uD83D\uDED2' },
    ];

    expLevels = [
        { value: 'ENTRY', label: 'Entry Level (0 - 2 yrs)' },
        { value: 'MID', label: 'Mid Level (3 - 5 yrs)' },
        { value: 'SENIOR', label: 'Senior Level (6 - 9 yrs)' },
        { value: 'LEAD', label: 'Lead / Manager (10+ yrs)' }
    ];

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            this.keyword = params['keyword'] || '';
            this.locationFilter = params['location'] || '';
            this.selectedCategory = params['category'] || '';
            this.doSearch();
        });
    }

    doSearch(): void {
        this.loading.set(true);
        this.jobService.getAllJobs().subscribe({
            next: (jobs) => {
                const filtered = this.applyClientFilters(jobs || []);
                this.allJobs.set(filtered);
                this.filteredJobs.set(filtered);
                this.sortJobs();
                this.currentPage.set(1);
                this.loading.set(false);
            },
            error: () => {
                this.toastService.error('Failed to load jobs');
                this.loading.set(false);
            }
        });
    }

    private applyClientFilters(jobs: Job[]): Job[] {
        const keyword = this.keyword.trim().toLowerCase();
        const location = this.locationFilter.trim().toLowerCase();

        return jobs.filter(job => {
            const title = (job.title || '').toLowerCase();
            const company = (job.companyName || '').toLowerCase();
            const jobLocation = (job.location || '').toLowerCase();

            if (keyword && !(title.includes(keyword) || company.includes(keyword) || jobLocation.includes(keyword))) {
                return false;
            }

            if (location && !jobLocation.includes(location)) {
                return false;
            }

            if (this.selectedCategory) {
                const selectedCategory = this.normalizeCategory(this.selectedCategory);
                const jobCategory = this.resolveJobCategory(job);
                if (selectedCategory !== jobCategory) {
                    return false;
                }
            }

            if (this.selectedTypes.length > 0 && !this.selectedTypes.includes(job.jobType)) {
                return false;
            }

            if (this.selectedLevels.length > 0 && !this.matchesSelectedExperienceLevels(job.experienceYears || 0)) {
                return false;
            }

            if (this.salaryMin > 0) {
                const selectedAnnual = this.salaryMin * 12; // salaryMin from slider is monthly
                const salaryRange = this.extractSalaryAnnualRange(job.salary);
                // Correct logic: show job if its maximum potential salary meets or exceeds the user's minimum requirement
                if (salaryRange.max < selectedAnnual) {
                    return false;
                }
            }

            return true;
        });
    }

    private matchesSelectedExperienceLevels(years: number): boolean {
        return this.selectedLevels.some(level => {
            if (level === 'ENTRY') return years >= 0 && years <= 2;
            if (level === 'MID') return years >= 3 && years <= 5;
            if (level === 'SENIOR') return years >= 6 && years <= 9;
            if (level === 'LEAD') return years >= 10;
            return false;
        });
    }

    private extractSalaryAnnualRange(salaryText: string): { min: number; max: number } {
        if (!salaryText) return { min: 0, max: Number.MAX_SAFE_INTEGER };

        const text = salaryText.toUpperCase();
        const isLPA = text.includes('LPA');
        const isMonthly = text.includes('/MO') || text.includes('MONTH');

        const values = text.match(/\d+(\.\d+)?/g)?.map(v => Number(v)) || [];
        if (values.length === 0) return { min: 0, max: Number.MAX_SAFE_INTEGER };

        // Normalize to annual
        let normalized = values.map(v => {
            if (isLPA) return v * 100000;
            if (isMonthly) return v * 12;
            // Heuristic for raw numbers: if < 100, assume LPA, else assume absolute
            if (v < 100) return v * 100000;
            return v;
        });

        if (normalized.length === 1) {
            return { min: normalized[0], max: normalized[0] };
        }

        return {
            min: Math.min(...normalized),
            max: Math.max(...normalized)
        };
    }

    private normalizeCategory(value?: string): string {
        if (!value) return '';
        const normalized = value.toUpperCase().replace(/[^A-Z]/g, '');
        const map: Record<string, string> = {
            TECH: 'TECHNOLOGY',
            TECHNOLOGY: 'TECHNOLOGY',
            IT: 'TECHNOLOGY',
            SOFTWARE: 'TECHNOLOGY',
            DEVELOPMENT: 'TECHNOLOGY',
            ENGINEERING: 'TECHNOLOGY',
            DESIGN: 'DESIGN',
            CREATIVE: 'DESIGN',
            UIUX: 'DESIGN',
            FINANCE: 'FINANCE',
            ACCOUNTING: 'FINANCE',
            BANKING: 'FINANCE',
            MARKETING: 'MARKETING',
            ADVERTISING: 'MARKETING',
            HEALTHCARE: 'HEALTHCARE',
            MEDICAL: 'HEALTHCARE',
            OPERATIONS: 'OPERATIONS',
            LOGISTICS: 'OPERATIONS',
            EDUCATION: 'EDUCATION',
            ACADEMIA: 'EDUCATION',
            SALES: 'SALES',
            BUSINESS: 'SALES'
        };
        return map[normalized] || normalized;
    }

    private resolveJobCategory(job: Job): string {
        const direct = this.normalizeCategory(job.category);
        if (direct) return direct;

        const probe = `${job.title || ''} ${job.description || ''} ${job.skills || ''}`.toLowerCase();
        if (/\b(design|ui|ux|figma)\b/.test(probe)) return 'DESIGN';
        if (/\b(finance|account|bank|audit)\b/.test(probe)) return 'FINANCE';
        if (/\b(marketing|seo|content|brand)\b/.test(probe)) return 'MARKETING';
        if (/\b(health|medical|nurse|hospital)\b/.test(probe)) return 'HEALTHCARE';
        if (/\b(operation|supply|logistics|process)\b/.test(probe)) return 'OPERATIONS';
        if (/\b(teacher|education|trainer|curriculum)\b/.test(probe)) return 'EDUCATION';
        if (/\b(sales|business development|bdm|inside sales)\b/.test(probe)) return 'SALES';
        return 'TECHNOLOGY';
    }

    minSalaryLabel(): string {
        if (!this.salaryMin) return '\u20B90 LPA';
        const lpa = (this.salaryMin * 12) / 100000;
        return `\u20B9${lpa.toFixed(1)} LPA`;
    }

    sortJobs(): void {
        const sorted = [...this.allJobs()];
        if (this.sortBy === 'recent') {
            sorted.sort((a, b) => new Date(b.postedDate).getTime() - new Date(a.postedDate).getTime());
        } else if (this.sortBy === 'salary_high') {
            sorted.sort((a, b) => {
                const rangeA = this.extractSalaryAnnualRange(a.salary);
                const rangeB = this.extractSalaryAnnualRange(b.salary);
                return rangeB.max - rangeA.max;
            });
        } else if (this.sortBy === 'applicants') {
            sorted.sort((a, b) => (b.applicantCount || 0) - (a.applicantCount || 0));
        }
        this.filteredJobs.set(sorted);
    }

    toggleType(type: string): void {
        const idx = this.selectedTypes.indexOf(type);
        if (idx >= 0) this.selectedTypes.splice(idx, 1);
        else this.selectedTypes.push(type);
        this.doSearch();
    }

    toggleLevel(lvl: string): void {
        const idx = this.selectedLevels.indexOf(lvl);
        if (idx >= 0) this.selectedLevels.splice(idx, 1);
        else this.selectedLevels.push(lvl);
        this.doSearch();
    }

    toggleCategory(cat: string): void {
        this.selectedCategory = this.selectedCategory === cat ? '' : cat;
        this.doSearch();
    }

    clearFilters(): void {
        this.keyword = '';
        this.locationFilter = '';
        this.selectedCategory = '';
        this.selectedTypes = [];
        this.selectedLevels = [];
        this.salaryMin = 0;
        this.doSearch();
    }

    onSaveToggle(jobId: number): void {
        this.jobService.toggleSave(jobId).subscribe({
            next: (isSaved) => {
                this.toastService.success(isSaved ? 'Job bookmarked' : 'Bookmark removed');
            },
            error: (err) => {
                this.toastService.error(err.error || 'Failed to update bookmark. Please login.');
            }
        });
    }
}
