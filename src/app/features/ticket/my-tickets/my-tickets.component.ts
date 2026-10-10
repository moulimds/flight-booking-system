import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BookingService } from '../../../core/services/booking.service';
import { FlightService } from '../../../core/services/flight.service';
import { SeatService } from '../../../core/services/seat.service';
import { BookingResponse, CancellationPreviewResponse } from '../../../core/models/booking.models';
import { Flight } from '../../../core/models/flight.models';
import { Seat } from '../../../core/models/seat.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { StatusBadgeDirective } from '../../../shared/directives/status-badge.directive';
import { ToastService } from '../../../core/services/toast.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-my-tickets',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, CurrencyFormatterPipe, StatusBadgeDirective],
  templateUrl: './my-tickets.component.html',
  styleUrl: './my-tickets.component.css'
})
export class MyTicketsComponent implements OnInit {
  private bookingService = inject(BookingService);
  private flightService = inject(FlightService);
  private seatService = inject(SeatService);
  private notificationService = inject(NotificationService);
  private toastService = inject(ToastService);

  public bookings: BookingResponse[] = [];
  public isLoading = false;

  // Cancellation Modal & Preview State
  public selectedBooking: BookingResponse | null = null;
  public cancellationPreview: CancellationPreviewResponse | null = null;
  public cancellationReason = '';
  public isPreviewLoading = false;
  public isCancelling = false;
  public showModal = false;

  // Bank Details Form for Direct Refund Deposit
  public bankDetails = {
    transferMode: 'BANK' as 'BANK' | 'UPI',
    accountHolderName: '',
    bankName: 'HDFC Bank',
    accountNumber: '',
    confirmAccountNumber: '',
    ifscCode: 'HDFC0001234',
    upiId: ''
  };

  public bankNameOptions: string[] = [
    'HDFC Bank',
    'State Bank of India',
    'ICICI Bank',
    'Axis Bank',
    'Kotak Mahindra Bank',
    'Punjab National Bank',
    'Bank of Baroda',
    'Canara Bank',
    'IndusInd Bank',
    'Other Bank'
  ];

  // Instant Refund Success Receipt State
  public refundReceipt: {
    bookingReference: string;
    passengerName: string;
    refundAmount: number;
    refundPercentage: number;
    bankName: string;
    accountMasked: string;
    ifscCode: string;
    transactionRef: string;
    timestamp: string;
  } | null = null;
  public showRefundReceipt = false;

  // ============================================================
  // RESCHEDULE WORKFLOW STATE
  // ============================================================
  public showRescheduleModal = false;
  public rescheduleBookingData: BookingResponse | null = null;
  public rescheduleStep: 1 | 2 | 3 | 4 = 1; // 1: Search & Pick Flight, 2: Pick Seat, 3: Review & Payment, 4: Success
  public isRescheduleLoading = false;
  public isSubmittingReschedule = false;

  // Step 1: Search Alternative Flights
  public allFlights: Flight[] = [];
  public availableAlternativeFlights: Flight[] = [];
  public selectedNewFlight: Flight | null = null;
  public rescheduleSearchOrigin = '';
  public rescheduleSearchDest = '';
  public rescheduleSearchDate = '';

  // Step 2: Pick Seat
  public flightSeats: Seat[] = [];
  public selectedSeats: { [passengerIdx: number]: string } = {}; // { 0: '12A', 1: '12B' }
  public activePassengerIdx = 0;
  public seatRows: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15];
  public seatColsLeft: string[] = ['A', 'B', 'C'];
  public seatColsRight: string[] = ['D', 'E', 'F'];
  public occupiedSeatsSet: Set<string> = new Set(['1B', '2E', '3A', '4C', '7B', '9F', '11D', '14A']);

  public get reschedulePassengersList(): any[] {
    if (this.rescheduleBookingData?.passengers && this.rescheduleBookingData.passengers.length > 0) {
      return this.rescheduleBookingData.passengers;
    }
    return [{
      fullName: this.rescheduleBookingData?.passengerName || 'Primary Passenger',
      seatNumber: this.rescheduleBookingData?.seatNumber || '12A'
    }];
  }

  // Step 3: Payment
  public readonly rescheduleFee = 500; // Flat nominal reschedule charge in ₹
  public fareDifference = 0;
  public totalRescheduleAmount = 500;
  public paymentMethod: 'CARD' | 'UPI' | 'NETBANKING' = 'CARD';
  public paymentForm = {
    cardHolder: 'John Doe',
    cardNumber: '4532 •••• •••• 8821',
    expiry: '12/28',
    cvv: '921',
    upiId: 'passenger@okaxis',
    bank: 'HDFC Bank'
  };

  // Step 4: Rescheduled Result
  public rescheduledResult: BookingResponse | null = null;

  ngOnInit(): void {
    this.loadBookings();
  }

  public loadBookings(): void {
    this.isLoading = true;
    this.bookingService.getMyBookings().subscribe({
      next: (data) => {
        this.bookings = data || [];
        this.isLoading = false;
      },
      error: () => {
        this.bookings = [];
        this.isLoading = false;
      }
    });
  }

  // ============================================================
  // CANCELLATION & AUTOMATIC BANK REFUND METHODS
  // ============================================================
  public openCancellationModal(booking: BookingResponse): void {
    this.selectedBooking = booking;
    this.cancellationPreview = null;
    this.cancellationReason = '';
    this.showRefundReceipt = false;
    this.refundReceipt = null;

    // Pre-populate bank details with passenger info
    this.bankDetails = {
      transferMode: 'BANK',
      accountHolderName: booking.passengerName || 'Primary Passenger',
      bankName: 'HDFC Bank',
      accountNumber: '',
      confirmAccountNumber: '',
      ifscCode: 'HDFC0001234',
      upiId: (booking.passengerEmail ? booking.passengerEmail.split('@')[0] : 'user') + '@okhdfcbank'
    };

    this.showModal = true;
    this.isPreviewLoading = true;

    this.bookingService.getCancellationPreview(booking.bookingReference).subscribe({
      next: (preview) => {
        this.cancellationPreview = preview;
        this.isPreviewLoading = false;
      },
      error: (err) => {
        this.isPreviewLoading = false;
        this.toastService.error('Error', err?.error?.message || 'Unable to retrieve refund preview from server.');
      }
    });
  }

  public closeModal(): void {
    this.showModal = false;
    this.selectedBooking = null;
    this.cancellationPreview = null;
    this.showRefundReceipt = false;
    this.refundReceipt = null;
  }

  public submitCancellationRequest(): void {
    if (!this.selectedBooking || !this.cancellationPreview) return;

    // Validate Bank Account Details
    if (this.bankDetails.transferMode === 'BANK') {
      if (!this.bankDetails.accountHolderName.trim()) {
        this.toastService.warning('Required Field', 'Please provide the Bank Account Holder Name.');
        return;
      }
      if (!this.bankDetails.bankName.trim()) {
        this.toastService.warning('Required Field', 'Please select or provide your Bank Name.');
        return;
      }
      if (!this.bankDetails.accountNumber.trim() || this.bankDetails.accountNumber.trim().length < 6) {
        this.toastService.warning('Validation Error', 'Please enter a valid Bank Account Number (at least 6 digits).');
        return;
      }
      if (this.bankDetails.accountNumber.trim() !== this.bankDetails.confirmAccountNumber.trim()) {
        this.toastService.warning('Mismatch Error', 'Bank Account Number and Confirm Account Number do not match.');
        return;
      }
      if (!this.bankDetails.ifscCode.trim() || this.bankDetails.ifscCode.trim().length < 4) {
        this.toastService.warning('Validation Error', 'Please enter a valid IFSC Code (e.g. HDFC0001234).');
        return;
      }
    } else {
      if (!this.bankDetails.upiId.trim() || !this.bankDetails.upiId.includes('@')) {
        this.toastService.warning('Validation Error', 'Please enter a valid UPI ID (e.g. username@okhdfcbank).');
        return;
      }
    }

    this.isCancelling = true;

    const payload = {
      reason: this.cancellationReason || 'Customer requested automatic reservation cancellation & bank refund',
      accountHolderName: this.bankDetails.accountHolderName.trim(),
      bankName: this.bankDetails.transferMode === 'BANK' ? this.bankDetails.bankName.trim() : 'UPI Transfer',
      accountNumber: this.bankDetails.transferMode === 'BANK' ? this.bankDetails.accountNumber.trim() : this.bankDetails.upiId.trim(),
      ifscCode: this.bankDetails.transferMode === 'BANK' ? this.bankDetails.ifscCode.trim().toUpperCase() : 'UPI0001',
      upiId: this.bankDetails.upiId.trim()
    };

    this.bookingService.requestCancellation(this.selectedBooking.bookingReference, payload).subscribe({
      next: (res) => {
        this.isCancelling = false;

        const refundAmt = res.calculatedRefundAmount ?? this.cancellationPreview?.estimatedRefundAmount ?? 0;
        const refundPct = res.refundPercentage ?? this.cancellationPreview?.refundPercentage ?? 0;
        const masked = this.bankDetails.transferMode === 'BANK'
          ? '•••• ' + this.bankDetails.accountNumber.trim().slice(-4)
          : this.bankDetails.upiId.trim();

        // Populate authoritative receipt
        this.refundReceipt = {
          bookingReference: res.bookingReference,
          passengerName: res.passengerName || this.bankDetails.accountHolderName,
          refundAmount: Number(refundAmt),
          refundPercentage: Number(refundPct),
          bankName: this.bankDetails.transferMode === 'BANK' ? this.bankDetails.bankName : 'UPI Virtual Payment',
          accountMasked: masked,
          ifscCode: this.bankDetails.transferMode === 'BANK' ? this.bankDetails.ifscCode.toUpperCase() : 'UPI-INSTANT',
          transactionRef: res.refundTransactionRef || ('NEFT-REF-' + Date.now().toString().slice(-7)),
          timestamp: new Date().toLocaleString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
          })
        };

        this.showRefundReceipt = true;

        // Dispatch Refund Notification Email to sindhusakthi41@gmail.com
        this.notificationService.sendRefundApproved({
          recipient: 'sindhusakthi41@gmail.com',
          bookingReference: res.bookingReference,
          passengerName: res.passengerName || this.bankDetails.accountHolderName,
          refundAmount: Number(refundAmt),
          refundTransactionRef: this.refundReceipt.transactionRef,
          approvalRemarks: `Automatic refund credited to ${this.refundReceipt.bankName} (${this.refundReceipt.accountMasked})`
        }).subscribe();

        this.toastService.success(
          'Refund Credited to Bank Account',
          `₹${refundAmt.toLocaleString('en-IN')} has been automatically deposited to your ${this.refundReceipt.bankName} account.`
        );

        // Reload bookings so the list updates immediately
        this.loadBookings();
      },
      error: (err) => {
        this.isCancelling = false;
        this.toastService.error('Cancellation Failed', err?.error?.message || 'Could not process automatic cancellation and refund.');
      }
    });
  }

  // ============================================================
  // RESCHEDULE METHODS
  // ============================================================
  public openRescheduleModal(booking: BookingResponse): void {
    this.rescheduleBookingData = booking;
    this.rescheduleStep = 1;
    this.selectedNewFlight = null;
    this.selectedSeats = {};
    this.activePassengerIdx = 0;
    this.rescheduledResult = null;
    this.rescheduleSearchOrigin = booking.source || 'DEL';
    this.rescheduleSearchDest = booking.destination || 'BOM';

    // Set search date to tomorrow or current departure date
    const today = new Date().toISOString().split('T')[0];
    this.rescheduleSearchDate = booking.departureDate || today;

    this.showRescheduleModal = true;
    this.searchAlternativeFlights();
  }

  public closeRescheduleModal(): void {
    this.showRescheduleModal = false;
    this.rescheduleBookingData = null;
    this.selectedNewFlight = null;
    this.selectedSeats = {};
    this.rescheduleStep = 1;
  }

  public searchAlternativeFlights(): void {
    this.isRescheduleLoading = true;
    this.flightService.getAllFlights().subscribe({
      next: (flights) => {
        this.allFlights = flights || [];
        this.filterAlternativeFlights();
        this.isRescheduleLoading = false;
      },
      error: () => {
        this.availableAlternativeFlights = [];
        this.isRescheduleLoading = false;
      }
    });
  }

  public filterAlternativeFlights(): void {
    if (!this.rescheduleBookingData) return;
    const currentFlightId = this.rescheduleBookingData.flightId;
    const origin = (this.rescheduleSearchOrigin || '').trim().toUpperCase();
    const dest = (this.rescheduleSearchDest || '').trim().toUpperCase();
    const targetDate = this.rescheduleSearchDate;

    // Filter out current flight and match route / availability
    let matches = this.allFlights.filter(f => {
      if (f.id === currentFlightId) return false;
      const fOrig = (f.source || f.origin || '').toUpperCase();
      const fDest = (f.destination || '').toUpperCase();

      const routeMatches = (!origin || fOrig === origin) && (!dest || fDest === dest);
      return routeMatches;
    });

    // If date is selected, sort or prioritize that date, otherwise show all available on route
    if (targetDate) {
      matches.sort((a, b) => {
        if (a.departureDate === targetDate && b.departureDate !== targetDate) return -1;
        if (a.departureDate !== targetDate && b.departureDate === targetDate) return 1;
        return 0;
      });
    }

    // Fallback: If no exact route flights found, show scheduled active flights
    if (matches.length === 0) {
      matches = this.allFlights.filter(f => f.id !== currentFlightId);
    }

    this.availableAlternativeFlights = matches;
  }

  public selectNewFlight(f: Flight): void {
    this.selectedNewFlight = f;
    this.calculateRescheduleFares();
    this.loadSeatsForNewFlight(f.id);
    this.rescheduleStep = 2; // Advance to seat selection
  }

  public calculateRescheduleFares(): void {
    if (!this.selectedNewFlight || !this.rescheduleBookingData) return;
    const originalFare = Number(this.rescheduleBookingData.fareAmount) || 5000;
    const newFare = Number(this.selectedNewFlight.basePrice) || 5000;

    // Difference is strictly extra fare if new flight costs more
    this.fareDifference = Math.max(0, newFare - originalFare);
    this.totalRescheduleAmount = this.fareDifference + this.rescheduleFee;
  }

  public loadSeatsForNewFlight(flightId: number): void {
    this.occupiedSeatsSet = new Set(['1B', '2E', '3A', '4C', '7B', '9F', '11D', '14A']);
    // Pre-seed initial default seat selections for all passengers
    this.selectedSeats = {};
    const passengersCount = this.rescheduleBookingData?.passengers?.length || 1;
    const defaultCols = ['A', 'B', 'C', 'D', 'E', 'F'];
    for (let i = 0; i < passengersCount; i++) {
      const row = 12 + i;
      const col = defaultCols[i % defaultCols.length];
      this.selectedSeats[i] = `${row}${col}`;
    }

    this.seatService.getSeatsForFlight(flightId).subscribe({
      next: (seats) => {
        if (seats && seats.length > 0) {
          this.flightSeats = seats;
          this.occupiedSeatsSet.clear();
          seats.filter(s => s.status !== 'AVAILABLE').forEach(s => this.occupiedSeatsSet.add(s.seatNumber));
        }
      },
      error: () => {
        // Fallback grid will use default occupied seats
      }
    });
  }

  public isSeatOccupied(seatNum: string): boolean {
    return this.occupiedSeatsSet.has(seatNum);
  }

  public isSeatSelectedByAnyPassenger(seatNum: string): boolean {
    return Object.values(this.selectedSeats).includes(seatNum);
  }

  public isSeatSelectedByCurrentPassenger(seatNum: string): boolean {
    return this.selectedSeats[this.activePassengerIdx] === seatNum;
  }

  public pickSeat(seatNum: string): void {
    if (this.isSeatOccupied(seatNum)) {
      this.toastService.warning('Seat Occupied', `Seat ${seatNum} is already booked. Please choose another seat.`);
      return;
    }

    // Check if another passenger in same booking already selected this seat
    for (const [idx, s] of Object.entries(this.selectedSeats)) {
      if (Number(idx) !== this.activePassengerIdx && s === seatNum) {
        this.toastService.warning('Seat Already Selected', `Seat ${seatNum} is already selected for Passenger #${Number(idx) + 1}.`);
        return;
      }
    }

    this.selectedSeats[this.activePassengerIdx] = seatNum;

    // Auto-advance to next passenger if more exist
    const totalPassengers = this.rescheduleBookingData?.passengers?.length || 1;
    if (this.activePassengerIdx < totalPassengers - 1) {
      this.activePassengerIdx++;
    }
  }

  public areAllSeatsPicked(): boolean {
    const totalPassengers = this.rescheduleBookingData?.passengers?.length || 1;
    for (let i = 0; i < totalPassengers; i++) {
      if (!this.selectedSeats[i]) return false;
    }
    return true;
  }

  public proceedToPayment(): void {
    if (!this.areAllSeatsPicked()) {
      this.toastService.warning('Pick Seats', 'Please select a seat for all passengers before proceeding.');
      return;
    }
    this.rescheduleStep = 3;
  }

  public submitReschedulePayment(): void {
    if (!this.rescheduleBookingData || !this.selectedNewFlight) return;
    this.isSubmittingReschedule = true;

    const primarySeat = this.selectedSeats[0] || '14A';
    const passengersPayload = (this.rescheduleBookingData.passengers || []).map((p, idx) => ({
      passengerId: p.id,
      fullName: p.fullName,
      seatNumber: this.selectedSeats[idx] || primarySeat
    }));

    const payload = {
      newFlightId: this.selectedNewFlight.id,
      newSeatNumber: primarySeat,
      additionalFarePaid: this.totalRescheduleAmount,
      paymentMethod: this.paymentMethod,
      passengers: passengersPayload
    };

    this.bookingService.rescheduleBooking(this.rescheduleBookingData.id, payload).subscribe({
      next: (res) => {
        this.isSubmittingReschedule = false;
        this.rescheduledResult = res;
        this.rescheduleStep = 4; // Success screen

        // Dispatch Rescheduled Flight Itinerary Email + PDF to sindhusakthi41@gmail.com
        this.notificationService.sendFlightRescheduled({
          recipient: 'sindhusakthi41@gmail.com',
          bookingReference: res.bookingReference,
          passengerName: res.passengerName || 'Sindhu Sakthi',
          flightNumber: this.selectedNewFlight?.flightNumber || 'SW-Rescheduled',
          source: this.selectedNewFlight?.source || 'DEL',
          destination: this.selectedNewFlight?.destination || 'BOM',
          oldDepartureTime: this.rescheduleBookingData?.departureDate + ' ' + ((this.rescheduleBookingData as any)?.departureTime || '10:00 AM'),
          newDepartureTime: (this.selectedNewFlight?.departureDate || 'Updated Date') + ' ' + (this.selectedNewFlight?.departureTime || '02:30 PM')
        }).subscribe();

        this.toastService.success(
          'Flight Rescheduled',
          `PNR ${res.bookingReference} successfully rescheduled. Updated itinerary PDF sent to email.`
        );
        this.loadBookings(); // Refresh main list
      },
      error: (err) => {
        this.isSubmittingReschedule = false;
        this.toastService.error('Reschedule Failed', err?.error?.message || 'Could not complete flight rescheduling.');
      }
    });
  }
}
