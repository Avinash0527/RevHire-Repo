import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { ApplicationStatus } from '../../../core/models/application.model';

@Component({
    selector: 'app-status-badge',
    standalone: true,
    imports: [NgClass],
    templateUrl: './status-badge.html',
    styleUrl: './status-badge.css'
})
export class StatusBadgeComponent {
    @Input() status!: ApplicationStatus;

    get badgeClass(): string {
        return 'badge-' + this.status.toLowerCase();
    }

    get label(): string {
        return this.status.charAt(0) + this.status.slice(1).toLowerCase();
    }

    get iconClass(): string {
        const map: Record<ApplicationStatus, string> = {
            APPLIED: 'bi-send', REVIEWING: 'bi-eye', SHORTLISTED: 'bi-star',
            SELECTED: 'bi-check-circle', REJECTED: 'bi-x-circle', WITHDRAWN: 'bi-clock-history'
        };
        return map[this.status] || 'bi-circle';
    }
}
