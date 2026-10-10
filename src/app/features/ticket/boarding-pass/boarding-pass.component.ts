import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { BookingService } from '../../../core/services/booking.service';
import { FlightService } from '../../../core/services/flight.service';
import { BookingResponse } from '../../../core/models/booking.models';
import { Flight } from '../../../core/models/flight.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-boarding-pass',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyFormatterPipe],
  templateUrl: './boarding-pass.component.html',
  styleUrl: './boarding-pass.component.css'
})
export class BoardingPassComponent implements OnInit {
  private bookingService = inject(BookingService);
  private flightService = inject(FlightService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);

  public bookingReference = '';
  public booking: BookingResponse | null = null;
  public flight: Flight | null = null;
  public isLoading = false;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.bookingReference = params['reference'] || '';
      if (this.bookingReference) {
        this.loadBookingDetails();
      }
    });
  }

  private loadBookingDetails(): void {
    this.isLoading = true;
    this.bookingService.getBookingByReference(this.bookingReference).subscribe({
      next: (b) => {
        this.booking = b;
        if (b && b.flightId) {
          this.flightService.getFlightById(b.flightId).subscribe({
            next: (f) => {
              this.flight = f;
              this.isLoading = false;
            },
            error: () => this.isLoading = false
          });
        } else {
          this.isLoading = false;
        }
      },
      error: () => {
        this.isLoading = false;
        this.toastService.error('Not Found', 'Could not locate booking reference.');
      }
    });
  }

  public downloadPass(): void {
    this.toastService.success('Boarding Pass Ready', 'Digital boarding pass opened for download/printing.');
    window.print();
  }
}

