import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs/operators';

export const clientGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  return authService.currentUser$.pipe(
    map(user => {
      if (user?.role === 'CLIENT') {
        return true;
      }
      router.navigate(['/agent/dashboard']);
      return false;
    })
  );
};
