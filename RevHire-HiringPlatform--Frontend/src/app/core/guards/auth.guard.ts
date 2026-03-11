import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authGuard: CanActivateFn = (route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const toast = inject(ToastService);

    if (auth.isAuthenticated()) {
        const user = auth.currentUser();
        // If seeker profile is incomplete, only allow access to profile page
        if (user?.role === 'JOB_SEEKER' && !user.isProfileComplete && !state.url.includes('/seeker/profile')) {
            toast.warning('Please complete your profile first');
            router.navigate(['/seeker/profile']);
            return false;
        }

        // If employer profile is incomplete, only allow access to company profile page
        if (user?.role === 'EMPLOYER' && !user.isProfileComplete && !state.url.includes('/employer/company')) {
            toast.warning('Please complete your company profile first');
            router.navigate(['/employer/company']);
            return false;
        }

        return true;
    }

    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
};

export const authChildGuard: CanActivateFn = (route, state) => {
    return authGuard(route, state);
};

export const guestGuard: CanActivateFn = (route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (auth.isAuthenticated()) {
        // Special case: allow non-admins to access admin-login for switching
        if (state.url.includes('/admin-login') && !auth.isAdmin()) {
            return true;
        }
        router.navigate([auth.getDashboardRoute()]);
        return false;
    }

    return true;
};
