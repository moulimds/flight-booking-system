import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BookingService } from '../../../core/services/booking.service';
import { FlightService } from '../../../core/services/flight.service';
import { BookingResponse } from '../../../core/models/booking.models';
import { Flight } from '../../../core/models/flight.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { StatusBadgeDirective } from '../../../shared/directives/status-badge.directive';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-support-console',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, CurrencyFormatterPipe, StatusBadgeDirective],
  templateUrl: './support-console.component.html'
})
export class SupportConsoleComponent implements OnInit {
  private bookingService = inject(BookingService);
  private flightService = inject(FlightService);
  private toastService = inject(ToastService);

  public searchTerm = '';
  public allBookings: BookingResponse[] = [];
  public filteredBookings: BookingResponse[] = [];
  public flights: Flight[] = [];
  public isLoading = false;

  public get confirmedCount(): number {
    return this.allBookings.filter(b => b.bookingStatus === 'CONFIRMED').length;
  }

  ngOnInit(): void {
    this.loadData();
  }

  public loadData(): void {
    this.isLoading = true;

    // Load All Bookings
    this.bookingService.getAllBookings().subscribe({
      next: (b) => {
        this.allBookings = b || [];
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.toastService.error('Error', 'Unable to fetch passenger bookings.');
      }
    });

    this.flightService.getAllFlights().subscribe({
      next: (f) => this.flights = f || [],
      error: () => {}
    });
  }

  public applyFilter(): void {
    const term = (this.searchTerm || '').trim().toLowerCase();
    if (!term) {
      this.filteredBookings = [...this.allBookings];
      return;
    }
    this.filteredBookings = this.allBookings.filter(b =>
      (b.bookingReference && b.bookingReference.toLowerCase().includes(term)) ||
      (b.passengerName && b.passengerName.toLowerCase().includes(term)) ||
      (b.passengerEmail && b.passengerEmail.toLowerCase().includes(term))
    );
  }
}
