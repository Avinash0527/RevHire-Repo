import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { NgClass, NgOptimizedImage } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { JobService } from '../../core/services/job.service';
import { Job } from '../../core/models/job.model';
import { JobCardComponent } from '../../shared/components/job-card/job-card';
import { QuoteCarouselComponent } from '../../shared/components/quote-carousel/quote-carousel.component';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-home',
    standalone: true,
    imports: [RouterLink, NgClass, FormsModule, JobCardComponent, QuoteCarouselComponent, NgOptimizedImage],
    templateUrl: './home.html',
    styleUrl: './home.css'
})
export class HomeComponent implements OnInit {
    private jobService = inject(JobService);
    private router = inject(Router);
    private toastService = inject(ToastService);
    private authService = inject(AuthService);

    searchKeyword = '';
    searchLocation = '';
    loading = signal(true);
    recentJobs = signal<Job[]>([]);
    fallbackOpportunities = [
        { company: 'Google', role: 'Frontend Engineer', location: 'Bangalore, India', type: 'Full Time' },
        { company: 'Microsoft', role: 'Backend Developer', location: 'Hyderabad, India', type: 'Full Time' },
        { company: 'Amazon', role: 'Data Analyst', location: 'Chennai, India', type: 'Hybrid' },
        { company: 'Adobe', role: 'UI/UX Designer', location: 'Remote', type: 'Remote' },
        { company: 'Flipkart', role: 'Product Manager', location: 'Bangalore, India', type: 'Full Time' },
        { company: 'Zoho', role: 'Software Engineer', location: 'Chennai, India', type: 'On-site' }
    ];

    popularTags = ['Frontend', 'Backend', 'Product', 'Design', 'Data'];
    stats = [
        { value: '24k+', label: 'Live Jobs' },
        { value: '10k+', label: 'Companies' },
        { value: '120k+', label: 'Candidates' }
    ]; categories = [
        { icon: '\uD83D\uDCBB', name: 'Technology', value: 'TECHNOLOGY', count: 1240 },
        { icon: '\uD83C\uDFA8', name: 'Design', value: 'DESIGN', count: 340 },
        { icon: '\uD83D\uDCCA', name: 'Finance', value: 'FINANCE', count: 520 },
        { icon: '\uD83C\uDFE5', name: 'Healthcare', value: 'HEALTHCARE', count: 680 },
        { icon: '\uD83D\uDCE3', name: 'Marketing', value: 'MARKETING', count: 290 },
        { icon: '\u2699\uFE0F', name: 'Operations', value: 'OPERATIONS', count: 420 },
        { icon: '\uD83D\uDED2', name: 'Sales', value: 'SALES', count: 380 },
    ];

    ngOnInit(): void {
        if (this.authService.isAuthenticated()) {
            this.router.navigateByUrl(this.authService.getDashboardRoute());
            return;
        }

        this.jobService.getAllJobs().subscribe({
            next: (jobs) => {
                this.recentJobs.set((jobs || []).slice(0, 6));
                this.loading.set(false);
            },
            error: () => {
                this.recentJobs.set([]);
                this.loading.set(false);
            }
        });
    }

    search(): void {
        this.router.navigate(['/jobs'], {
            queryParams: { keyword: this.searchKeyword, location: this.searchLocation }
        });
    }

    searchTag(tag: string): void {
        this.router.navigate(['/jobs'], { queryParams: { keyword: tag } });
    }

    onSaveToggle(jobId: number): void {
        if (!this.authService.isAuthenticated()) {
            this.router.navigate(['/login'], { queryParams: { returnUrl: '/' } });
            return;
        }

        this.jobService.toggleSave(jobId).subscribe({
            next: (isSaved) => {
                this.toastService.success(isSaved ? 'Job saved to your bookmarks' : 'Job removed from bookmarks');
            },
            error: (err) => {
                this.toastService.error(err.error || 'Failed to update bookmark. Please login.');
            }
        });
    }
}

