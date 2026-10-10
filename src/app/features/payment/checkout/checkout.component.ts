import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PaymentService } from '../../../core/services/payment.service';
import { BookingService } from '../../../core/services/booking.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { BookingResponse } from '../../../core/models/booking.models';
import { getMealByCode, getMealDisplayName } from '../../../core/models/meal.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule, CurrencyFormatterPipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {
  private fb = inject(FormBuilder);
  private paymentService = inject(PaymentService);
  private bookingService = inject(BookingService);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  public getMealByCode = getMealByCode;
  public getMealDisplayName = getMealDisplayName;

  public bookingId = 0;
  public bookingReference = '';
  public allBookingReferences = '';
  public tripType = 'ONE_WAY';
  public amount = 0;
  public booking: BookingResponse | null = null;
  public selectedMethod = 'UPI'; // Default to UPI for convenience as requested
  public upiId = 'user@okhdfcbank';
  public selectedBank = 'HDFC Bank';
  public isPaying = false;
  public isLoadingBooking = false;

  public availableBanks = [
    { id: 'HDFC', name: 'HDFC Bank', code: 'HDFC' },
    { id: 'ICICI', name: 'ICICI Bank', code: 'ICICI' },
    { id: 'SBI', name: 'State Bank of India', code: 'SBI' },
    { id: 'AXIS', name: 'Axis Bank', code: 'UTIB' },
    { id: 'KOTAK', name: 'Kotak Mahindra', code: 'KKBK' },
    { id: 'PNB', name: 'Punjab National Bank', code: 'PUNB' }
  ];

  public cardForm: FormGroup = this.fb.group({
    cardholderName: ['Sindhu Sakthi', Validators.required],
    cardNumber: ['4532 8901 2345 8921', [Validators.required, Validators.minLength(12)]],
    cardExpiry: ['12/28', Validators.required],
    cardCvv: ['888', [Validators.required, Validators.minLength(3)]]
  });

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['bookingId']) this.bookingId = +params['bookingId'];
      if (params['bookingReference']) this.bookingReference = params['bookingReference'];
      if (params['allBookingReferences']) this.allBookingReferences = params['allBookingReferences'];
      if (params['tripType']) this.tripType = params['tripType'];
      if (params['amount']) this.amount = +params['amount'];

      if (this.bookingReference) {
        this.fetchBookingDetails(this.bookingReference);
      }
    });
  }

  private fetchBookingDetails(ref: string): void {
    this.isLoadingBooking = true;
    this.bookingService.getBookingByReference(ref).subscribe({
      next: (b) => {
        this.booking = b;
        this.isLoadingBooking = false;
        if (b.id) this.bookingId = b.id;
        if (b.fareAmount && (!this.amount || this.amount === 0)) this.amount = b.fareAmount;
      },
      error: () => {
        this.isLoadingBooking = false;
      }
    });
  }

  public selectBank(bankName: string): void {
    this.selectedBank = bankName;
  }

  public onPay(): void {
    if (this.selectedMethod === 'CARD' && this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
      this.toastService.warning('Card Details Required', 'Please enter valid payment card details.');
      return;
    }

    if (this.selectedMethod === 'UPI' && (!this.upiId || !this.upiId.includes('@'))) {
      this.toastService.warning('Invalid UPI ID', 'Please enter a valid UPI address (e.g., yourname@okhdfcbank).');
      return;
    }

    this.isPaying = true;
    const user = this.authService.currentUserValue;

    this.paymentService.processPayment({
      bookingId: this.bookingId,
      userId: user?.userId || 1,
      amount: this.amount,
      currency: 'INR',
      paymentMethod: this.selectedMethod,
      cardNumber: this.selectedMethod === 'CARD' ? this.cardForm.value.cardNumber : undefined,
      cvv: this.selectedMethod === 'CARD' ? this.cardForm.value.cardCvv : undefined,
      expiryDate: this.selectedMethod === 'CARD' ? this.cardForm.value.cardExpiry : undefined
    }).subscribe({
      next: (res) => {
        this.isPaying = false;

        // Confirm all flight legs if round-trip or multi-city
        const refs = this.allBookingReferences
          ? this.allBookingReferences.split(',').map(s => s.trim()).filter(Boolean)
          : [this.bookingReference];

        refs.forEach(ref => {
          this.bookingService.confirmBooking(ref, res.id || 1).subscribe({ error: () => {} });
        });

        // Dispatch Confirmation Email + E-Ticket PDF and Tax Invoice PDF
        const passengerName = this.booking?.passengerName || user?.fullName || 'Valued Passenger';
        const passengerEmail = this.booking?.passengerEmail || user?.email || '';

        this.notificationService.sendBookingConfirmed({
          recipient: passengerEmail,
          bookingReference: this.bookingReference,
          passengerName: passengerName,
          flightNumber: this.booking?.flightNumber || 'SW-101',
          source: this.booking?.source || 'DEL',
          destination: this.booking?.destination || 'BOM',
          seatNumber: this.booking?.seatNumber || 'Allocated',
          fareAmount: this.amount,
          passengers: this.booking?.passengers || [],
          departureTime: '12 Oct 2026, 10:00 AM'
        }).subscribe();

        this.notificationService.sendTaxInvoice({
          recipient: passengerEmail,
          bookingReference: this.bookingReference,
          passengerName: passengerName,
          flightNumber: this.booking?.flightNumber || 'SW-101',
          fareAmount: this.amount
        }).subscribe();

        this.toastService.success(
          'Payment & Booking Confirmed!',
          `Transaction: ${res.paymentReference || 'TXN-OK'} • E-Ticket & Tax Invoice PDF sent to ${passengerEmail}`
        );

        if (this.allBookingReferences && this.allBookingReferences.includes(',')) {
          this.router.navigate(['/tickets/my-tickets']);
        } else {
          this.router.navigate(['/tickets/boarding-pass', this.bookingReference]);
        }
      },
      error: (err) => {
        this.isPaying = false;
        const msg = err.error?.message || 'Payment authorization failed. Please try again.';
        this.toastService.error('Payment Error', msg);
      }
    });
  }
}
