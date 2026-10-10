import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../../core/services/profile.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserProfile } from '../../../core/models/profile.models';
import { AuthResponse } from '../../../core/models/auth.models';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-profile-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-view.component.html',
  styleUrl: './profile-view.component.css'
})
export class ProfileViewComponent implements OnInit {
  private fb = inject(FormBuilder);
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  public profile: UserProfile | null = null;
  public currentUser: AuthResponse | null = null;
  public isSaving = false;

  public profileForm: FormGroup = this.fb.group({
    fullName: ['', Validators.required],
    phoneNumber: ['', Validators.required],
    city: [''],
    country: [''],
    passportNumber: ['', Validators.required],
    passportExpiryDate: ['', Validators.required],
    passportIssuingCountry: ['', Validators.required],
    nationality: ['', Validators.required]
  });

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    this.profileService.getMyProfile().subscribe({
      next: (p) => {
        this.profile = p;
        if (p) {
          this.profileForm.patchValue({
            fullName: p.fullName || this.currentUser?.fullName || '',
            phoneNumber: p.phoneNumber || '',
            city: p.city || '',
            country: p.country || '',
            passportNumber: p.passportNumber || '',
            passportExpiryDate: p.passportExpiryDate || '',
            passportIssuingCountry: p.passportIssuingCountry || '',
            nationality: p.nationality || ''
          });
        }
      },
      error: () => {
        if (this.currentUser) {
          this.profileForm.patchValue({
            fullName: this.currentUser.fullName || '',
            phoneNumber: (this.currentUser as any)?.phone || ''
          });
        }
      }
    });
  }

  public getInitials(): string {
    const name = this.profile?.fullName || this.currentUser?.fullName || 'User';
    return name
      .split(' ')
      .map(part => part[0])
      .filter(Boolean)
      .slice(0, 2)
      .join('')
      .toUpperCase() || 'U';
  }

  public onSaveProfile(): void {
    if (this.profileForm.invalid) return;

    this.isSaving = true;
    const val = this.profileForm.value;

    this.profileService.updateProfile({
      fullName: val.fullName,
      phoneNumber: val.phoneNumber,
      city: val.city,
      country: val.country
    }).subscribe({
      next: (updatedProfile) => {
        this.profile = updatedProfile;
        this.profileService.updatePassport({
          passportNumber: val.passportNumber,
          passportExpiryDate: val.passportExpiryDate,
          passportIssuingCountry: val.passportIssuingCountry,
          nationality: val.nationality
        }).subscribe({
          next: (passportProfile) => {
            this.profile = passportProfile;
            this.isSaving = false;
            this.toastService.success('Profile Saved', 'Customer details and passport updated successfully.');
          },
          error: (err) => {
            this.isSaving = false;
            this.toastService.error('Passport Error', err?.error?.message || 'Failed to update passport info.');
          }
        });
      },
      error: (err) => {
        this.isSaving = false;
        this.toastService.error('Profile Error', err?.error?.message || 'Failed to update contact info.');
      }
    });
  }
}
