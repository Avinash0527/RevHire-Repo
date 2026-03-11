import { Component, inject, OnInit, signal, computed, effect } from '@angular/core';
import { NgClass, DatePipe } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
    selector: 'app-employer-notifications',
    standalone: true,
    imports: [NgClass, DatePipe, PaginationComponent],
    templateUrl: './employer-notifications.html',
    styleUrl: './employer-notifications.css'
})
export class EmployerNotificationsComponent implements OnInit {
    notifService = inject(NotificationService);
    private auth = inject(AuthService);

    showUnreadOnly = signal(false);
    currentPage = signal(1);
    readonly pageSize = 10;

    constructor() {
        effect(() => {
            this.showUnreadOnly();
            this.currentPage.set(1);
        }, { allowSignalWrites: true });
    }

    filteredNotifications = computed(() => {
        const notifs = this.notifService.notifications();
        if (this.showUnreadOnly()) {
            return notifs.filter(n => !n.isRead);
        }
        return notifs;
    });

    pagedNotifications = computed(() => {
        const notifs = this.filteredNotifications();
        const start = (this.currentPage() - 1) * this.pageSize;
        return notifs.slice(start, start + this.pageSize);
    });

    ngOnInit(): void {
        // Notifications are now loaded proactively by NotificationService effect
    }

    getIcon(type: string): string {
        switch (type) {
            case 'SUCCESS': return 'bi-check-circle-fill';
            case 'WARNING': return 'bi-exclamation-triangle-fill';
            case 'ERROR': return 'bi-x-circle-fill';
            default: return 'bi-info-circle-fill';
        }
    }

    markRead(id: number): void {
        this.notifService.markRead(id).subscribe();
    }

    markAllRead(): void {
        this.notifService.markAllRead().subscribe();
    }
}
