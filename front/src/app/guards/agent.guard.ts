import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs/operators';

export const agentGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  return authService.currentUser$.pipe(
    map(user => {
      if (user?.role === 'AGENT') {
        return true;
      }
      router.navigate(['/client/dashboard']);
      return false;
    })
  );
};
