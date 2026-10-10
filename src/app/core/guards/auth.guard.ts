import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  if (authService.isAuthenticated) {
    return true;
  }

  toastService.info('Authentication Required', 'Please log in to access this page.');
  const returnUrl = state.url && !state.url.startsWith('/auth') && state.url !== '/' ? state.url : null;
  if (returnUrl) {
    router.navigate(['/auth/login'], { queryParams: { returnUrl } });
  } else {
    router.navigate(['/auth/login']);
  }
  return false;
};

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  if (authService.isAuthenticated && authService.isAdmin) {
    return true;
  }

  toastService.warning('Admin Access Required', 'You need administrator privileges to access this dashboard.');
  router.navigate(['/flights/dashboard']);
  return false;
};

export const staffGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  if (authService.isAuthenticated && authService.isStaff) {
    return true;
  }

  toastService.warning('Staff Access Required', 'You need staff privileges to access this area.');
  router.navigate(['/flights/dashboard']);
  return false;
};
