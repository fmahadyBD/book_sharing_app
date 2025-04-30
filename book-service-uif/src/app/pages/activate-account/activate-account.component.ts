import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CodeInputModule } from 'angular-code-input'; // Import CodeInputModule
import { FormsModule } from '@angular/forms';
import { AuthenticationService } from '../../services/services/authentication.service';

@Component({
  selector: 'app-activate-account',
  templateUrl: './activate-account.component.html',
  styleUrls: ['./activate-account.component.scss'],
  standalone: true, // Make this component standalone
  imports: [CommonModule, FormsModule, CodeInputModule] // Import needed modules here
})
export class ActivateAccountComponent {

  message = '';
  isOkay = true;
  submitted = false;

  constructor(
    private router: Router,
    private authService: AuthenticationService
  ) {}

  private confirmAccount(token: string) {
    this.authService.confirm({ token }).subscribe({
      next: () => {
        this.message = 'Your account has been successfully activated.\nNow you can proceed to login';
        this.submitted = true;
      },
      error: () => {
        this.message = 'Token has been expired or invalid';
        this.submitted = true;
        this.isOkay = false;
      }
    });
  }

  redirectToLogin() {
    this.router.navigate(['login']);
  }

  // Accept event of type `any`, and extract the token from event.detail if needed
  onCodeCompleted(event: any) {
    const token = event; // If event emits string directly
    this.confirmAccount(token);
  }
}
