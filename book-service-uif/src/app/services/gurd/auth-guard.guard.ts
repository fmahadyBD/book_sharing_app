import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../token/token.service';

export const authGuardGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if(tokenService.isTokenNotValid()){
    router.navigate(['login']);
    return false;
  }
  return true;
};
