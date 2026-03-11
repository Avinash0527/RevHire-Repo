import { Injectable, signal } from '@angular/core';

export interface Toast {
    id: number;
    message: string;
    type: 'success' | 'error' | 'info' | 'warning';
    icon?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
    private _toasts = signal<Toast[]>([]);
    readonly toasts = this._toasts.asReadonly();
    private nextId = 1;

    show(message: string, type: Toast['type'] = 'info', duration = 3500): void {
        const icons = { success: 'bi-check-circle-fill', error: 'bi-x-circle-fill', info: 'bi-info-circle-fill', warning: 'bi-exclamation-triangle-fill' };
        const toast: Toast = { id: this.nextId++, message, type, icon: icons[type] };
        this._toasts.update(t => [...t, toast]);
        setTimeout(() => this.remove(toast.id), duration);
    }

    success(msg: string) { this.show(msg, 'success'); }
    error(msg: string) { this.show(msg, 'error'); }
    info(msg: string) { this.show(msg, 'info'); }
    warning(msg: string) { this.show(msg, 'warning'); }

    remove(id: number): void {
        this._toasts.update(t => t.filter(toast => toast.id !== id));
    }
}
