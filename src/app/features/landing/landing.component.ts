import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import { ClickOutsideDirective } from '../../shared/directives/click-outside.directive';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, ClickOutsideDirective],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent implements OnInit {
  public authService = inject(AuthService);
  public themeService = inject(ThemeService);
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  public isProfileOpen = false;

  // Booking confirmation notice from redirect query params
  public confirmedBookingReference: string | null = null;
  public confirmedBookingId: string | null = null;
  public confirmedAmount: number | null = null;

  public quickSearchForm = this.fb.group({
    origin: ['', Validators.required],
    destination: ['', Validators.required],
    departureDate: [''],
    returnDate: ['']
  });

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['bookingReference']) {
        this.confirmedBookingReference = params['bookingReference'];
      }
      if (params['bookingId']) {
        this.confirmedBookingId = params['bookingId'];
      }
      if (params['amount']) {
        this.confirmedAmount = +params['amount'];
      }
    });
  }

  public dismissBookingBanner(): void {
    this.confirmedBookingReference = null;
  }

  public onQuickSearch(): void {
    const val = this.quickSearchForm.value;
    this.router.navigate(['/flights/dashboard'], {
      queryParams: {
        origin: val.origin,
        destination: val.destination,
        departureDate: val.departureDate
      }
    });
  }

  public quickSelect(from: string, to: string): void {
    this.router.navigate(['/flights/dashboard'], {
      queryParams: {
        origin: from,
        destination: to
      }
    });
  }
}

