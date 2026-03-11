import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);

    // Skip token for public auth endpoints
    const publicAuthEndpoints = ['/login', '/register', '/forgot-password', '/reset-password', '/refresh'];
    const isPublicAuth = req.url.includes('/api/auth/') && publicAuthEndpoints.some(endpoint => req.url.endsWith(endpoint));

    if (isPublicAuth) {
        return next(req);
    }

    const token = localStorage.getItem('revhire_token');

    if (token && token !== 'null' && token !== 'undefined') {
        const cloned = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
        return next(cloned);
    }

    return next(req);
};
