import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
    private _count = 0;
    private _isLoading = signal(false);
    readonly isLoading = this._isLoading.asReadonly();

    start(): void {
        this._count++;
        this._isLoading.set(true);
    }

    stop(): void {
        this._count = Math.max(0, this._count - 1);
        if (this._count === 0) this._isLoading.set(false);
    }

    stopAll(): void {
        this._count = 0;
        this._isLoading.set(false);
    }
}
