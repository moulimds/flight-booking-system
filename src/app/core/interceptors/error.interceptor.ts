import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred. Please try again.';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = error.error.message;
      } else {
        // Server-side error
        if (error.status === 401) {
          const isAuthEndpoint = req.url.includes('/auth/login') || req.url.includes('/auth/google') || req.url.includes('/auth/register');
          if (isAuthEndpoint) {
            errorMessage = error.error?.message || 'Authentication failed. Please verify your credentials or try again.';
          } else {
            errorMessage = 'Session expired or unauthorized. Please log in.';
            authService.logout();
          }
        } else if (error.status === 403) {
          errorMessage = 'Access denied. You do not have permission for this action.';
        } else if (error.status === 404) {
          errorMessage = error.error?.message || 'Requested resource was not found.';
        } else if (error.status >= 500) {
          errorMessage = 'Server error occurred. Please try again later.';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }
      }

      if (error.status !== 0) {
        toastService.error('Request Failed', errorMessage);
      }
      return throwError(() => error);
    })
  );
};
