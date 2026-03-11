import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user.model';

export const roleGuard: CanActivateFn = (route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const user = auth.currentUser();
    const requiredRoles = route.data?.['roles'] as string[];

    if (!user) {
        router.navigate(['/login']);
        return false;
    }

    if (!requiredRoles || requiredRoles.length === 0) {
        return true;
    }

    if (!requiredRoles.includes(user.role)) {
        router.navigate(['/401']);
        return false;
    }

    return true;
};
