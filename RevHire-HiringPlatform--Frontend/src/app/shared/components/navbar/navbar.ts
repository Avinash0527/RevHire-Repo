import { Component, inject, signal, computed } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NgClass, NgFor, NgIf, DatePipe, AsyncPipe } from '@angular/common';
import { SearchService } from '../../../core/services/search.service';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap, of, tap } from 'rxjs';
import { Subject } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, NgClass, NgFor, NgIf, DatePipe, FormsModule, AsyncPipe],
    templateUrl: './navbar.html',
    styleUrl: './navbar.css'
})
export class NavbarComponent {
    auth = inject(AuthService);
    router = inject(Router);
    themeService = inject(ThemeService);
    notifService = inject(NotificationService);
    searchService = inject(SearchService);

    menuOpen = signal(false);
    notifMenuOpen = signal(false);
    mobileOpen = signal(false);
    searchExpanded = signal(false);
    searchQuery = signal('');
    searchResults = signal<any[]>([]);
    isSearching = signal(false);

    private searchSubject = new Subject<string>();

    firstName = computed(() => {
        const name = this.auth.currentUser()?.name;
        return name ? name.split(' ')[0] : 'User';
    });

    recentNotifications = computed(() => {
        return this.notifService.notifications().slice(0, 5);
    });

    constructor() {
        this.searchSubject.pipe(
            takeUntilDestroyed(),
            debounceTime(300),
            distinctUntilChanged(),
            tap(q => {
                if (!q.trim()) {
                    this.searchResults.set([]);
                    this.isSearching.set(false);
                } else {
                    this.isSearching.set(true);
                }
            }),
            switchMap(query => {
                if (!query.trim()) return of([]);
                return this.shouldSearchJobs()
                    ? this.searchService.searchJobs(query)
                    : this.searchService.searchSeekers(query);
            })
        ).subscribe({
            next: (results) => {
                this.searchResults.set(results);
                this.isSearching.set(false);
            },
            error: () => this.isSearching.set(false)
        });
    }

    private shouldSearchJobs(): boolean {
        return !this.auth.isEmployer() && !this.auth.isAdmin();
    }

    toggleSearch(): void {
        this.searchExpanded.set(!this.searchExpanded());
        if (!this.searchExpanded()) {
            this.searchQuery.set('');
            this.searchResults.set([]);
        }
    }

    submitSearch(): void {
        const query = this.searchQuery().trim();
        if (!query) return;

        this.searchExpanded.set(false);
        this.searchResults.set([]);
        this.router.navigate(['/jobs'], { queryParams: { keyword: query } });
    }

    onSearchButtonClick(): void {
        if (this.searchExpanded() && this.searchQuery().trim()) {
            this.submitSearch();
            return;
        }
        this.toggleSearch();
    }

    onSearchInput(query: string): void {
        this.searchQuery.set(query);
        this.searchSubject.next(query);
    }

    navigateSearchResult(result: any): void {
        this.searchExpanded.set(false);
        this.searchQuery.set('');
        this.searchResults.set([]);

        if (this.shouldSearchJobs()) {
            this.router.navigate(['/jobs', result.id]);
        } else {
            this.router.navigate(['/employer/seeker', result.id]);
        }
    }

    toggleNotifMenu(event: Event): void {
        event.stopPropagation();
        this.notifMenuOpen.set(!this.notifMenuOpen());
        if (this.notifMenuOpen()) this.menuOpen.set(false);
    }

    markAsReadAndNavigate(notif: any): void {
        this.notifMenuOpen.set(false);
        if (!notif.isRead) {
            this.notifService.markRead(notif.id).subscribe();
        }
        // Redirection logic if notif has a link, otherwise do nothing or stay on same page
        if (notif.link) {
            this.router.navigateByUrl(notif.link);
        }
    }

    viewAll(): void {
        this.notifMenuOpen.set(false);
        const url = this.auth.isSeeker() ? '/seeker/notifications' : '/employer/notifications';
        this.router.navigateByUrl(url);
    }
}
