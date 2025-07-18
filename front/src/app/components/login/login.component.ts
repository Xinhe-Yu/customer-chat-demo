import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      role: ['CLIENT', Validators.required],
      identifier: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    const { role, identifier, password } = this.loginForm.value;
    const credentials = { identifier, password };

    const loginMethod = role === 'CLIENT' 
      ? this.authService.loginClient(credentials)
      : this.authService.loginAgent(credentials);

    loginMethod.subscribe({
      next: () => {
        this.isLoading = false;
        const redirectPath = role === 'CLIENT' ? '/client/dashboard' : '/agent/dashboard';
        this.router.navigate([redirectPath]);
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Login failed. Please check your credentials.', 'Close', {
          duration: 3000
        });
        console.error('Login error:', error);
      }
    });
  }

  get roleControl() { return this.loginForm.get('role'); }
  get identifierControl() { return this.loginForm.get('identifier'); }
  get passwordControl() { return this.loginForm.get('password'); }
}
