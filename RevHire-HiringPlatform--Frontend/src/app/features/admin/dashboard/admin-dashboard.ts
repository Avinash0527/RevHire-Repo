import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { NgClass, NgFor, NgIf, AsyncPipe, DatePipe, KeyValuePipe } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { ToastService } from '../../../core/services/toast.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [NgClass, NgFor, NgIf, AsyncPipe, DatePipe, KeyValuePipe, PaginationComponent],
    templateUrl: './admin-dashboard.html',
    styleUrl: './admin-dashboard.css'
})
export class AdminDashboardComponent implements OnInit {
    private adminService = inject(AdminService);
    private toast = inject(ToastService);

    users = signal<any[]>([]);
    metrics = signal<any>(null);
    loading = signal(true);
    activeTab = signal<'users' | 'metrics' | 'logs'>('users');
    logs = signal<any[]>([]);

    // Pagination constants
    readonly pageSize = 10;

    // User Pagination
    userPage = signal(1);
    pagedUsers = computed(() => {
        const start = (this.userPage() - 1) * this.pageSize;
        return this.users().slice(start, start + this.pageSize);
    });

    // Logs Pagination
    logPage = signal(1);
    pagedLogs = computed(() => {
        const start = (this.logPage() - 1) * this.pageSize;
        return this.logs().slice(start, start + this.pageSize);
    });

    ngOnInit(): void {
        this.loadInitialData();
    }

    loadInitialData(): void {
        this.loading.set(true);
        this.adminService.getAllUsers().subscribe({
            next: (users) => {
                this.users.set(users);
                this.loadMetrics();
            },
            error: () => {
                this.toast.error('Failed to load users');
                this.loading.set(false);
            }
        });
    }

    loadMetrics(): void {
        this.adminService.getAdminMetrics().subscribe({
            next: (data) => {
                this.metrics.set(data);
                this.loading.set(false);
            },
            error: () => {
                this.toast.error('Failed to load metrics');
                this.loading.set(false);
            }
        });
    }

    toggleUserStatus(user: any): void {
        const newStatus = user.status ? 'false' : 'true';
        this.adminService.updateUserStatus(user.id, newStatus).subscribe({
            next: () => {
                this.toast.success(`User ${user.name} status updated`);
                this.loadInitialData();
            },
            error: () => this.toast.error('Failed to update user status')
        });
    }

    deleteUser(user: any): void {
        if (confirm(`Are you sure you want to permanently delete user ${user.name}?`)) {
            this.adminService.deleteUser(user.id).subscribe({
                next: () => {
                    this.toast.success(`User ${user.name} deleted successfully`);
                    this.loadInitialData();
                },
                error: (err) => this.toast.error('Failed to delete user')
            });
        }
    }

    loadLogs(): void {
        this.adminService.getAllAuditLogs().subscribe({
            next: (logs) => this.logs.set(logs),
            error: () => this.toast.error('Failed to load audit logs')
        });
    }

    switchTab(tab: 'users' | 'metrics' | 'logs'): void {
        this.activeTab.set(tab);
        if (tab === 'logs' && this.logs().length === 0) {
            this.loadLogs();
        }
    }
}
