import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
    private _isDark = signal<boolean>(false);
    readonly isDark = this._isDark.asReadonly();

    constructor() {
        const stored = localStorage.getItem('revhire_theme');
        const dark = stored === 'dark';
        this._isDark.set(dark);
        this.applyTheme(dark);
    }

    toggle(): void {
        const newVal = !this._isDark();
        this._isDark.set(newVal);
        this.applyTheme(newVal);
        localStorage.setItem('revhire_theme', newVal ? 'dark' : 'light');
    }

    private applyTheme(dark: boolean): void {
        const root = document.documentElement;
        if (dark) {
            root.classList.add('dark');
        } else {
            root.classList.remove('dark');
        }
    }
}
