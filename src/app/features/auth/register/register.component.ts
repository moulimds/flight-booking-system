import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { GoogleAuthService } from '../../../core/services/google-auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private googleAuthService = inject(GoogleAuthService);
  private notificationService = inject(NotificationService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  public isLoading = false;
  public isGoogleLoading = false;

  public registerForm: FormGroup = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  public onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    const val = this.registerForm.value;

    this.authService.register({
      fullName: val.fullName,
      email: val.email,
      password: val.password,
      role: 'CUSTOMER'
    }).subscribe({
      next: (res) => {
        this.isLoading = false;

        // Dispatch Welcome Kit Email + Membership Pass PDF to sindhusakthi41@gmail.com
        this.notificationService.sendUserRegistered({
          fullName: res.fullName || val.fullName,
          email: res.email || val.email,
          recipient: 'sindhusakthi41@gmail.com'
        }).subscribe();

        this.toastService.success('Registration Complete', `Welcome to SkyWings, ${res.fullName || res.email}! Welcome kit sent to email.`);
        this.navigateUser(res.role);
      },
      error: (err) => {
        this.isLoading = false;
        const msg = err.error?.message || 'Registration failed. Please verify your details.';
        this.toastService.error('Registration Error', msg);
      }
    });
  }

  public async signInWithGoogle(): Promise<void> {
    this.isGoogleLoading = true;
    try {
      const googleProfile = await this.googleAuthService.triggerGoogleSignIn();
      this.authService.loginWithGoogle({
        email: googleProfile.email,
        name: googleProfile.name,
        token: googleProfile.token,
        photoUrl: googleProfile.photoUrl
      }).subscribe({
        next: (res) => {
          this.isGoogleLoading = false;
          this.toastService.success('Google Authentication Successful', `Welcome to SkyWings, ${res.fullName || res.email}!`);
          this.navigateUser(res.role);
        },
        error: (err) => {
          this.isGoogleLoading = false;
          const msg = err?.error?.message || 'Google Sign-In could not be completed. Please try again.';
          this.toastService.error('Google Sign-In Error', msg);
        }
      });
    } catch (err: any) {
      this.isGoogleLoading = false;
      if (err?.message && !err.message.includes('cancelled')) {
        this.toastService.error('Google Sign-In', err.message);
      }
    }
  }

  private navigateUser(role: string | null | undefined): void {
    const r = role || '';
    if (r === 'ADMIN' || r === 'ROLE_ADMIN') {
      this.router.navigate(['/admin/dashboard']);
    } else if (r === 'SUPPORT' || r === 'ROLE_SUPPORT') {
      this.router.navigate(['/support/console']);
    } else if (r === 'AIRPORT_ADMIN' || r === 'ROLE_AIRPORT_ADMIN') {
      this.router.navigate(['/airport/console']);
    } else if (r === 'AIRCRAFT_COMPANY_ADMIN' || r === 'ROLE_AIRCRAFT_COMPANY_ADMIN') {
      this.router.navigate(['/aircraft/console']);
    } else {
      this.router.navigate(['/flights/dashboard']);
    }
  }
}


