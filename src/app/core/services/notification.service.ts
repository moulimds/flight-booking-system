import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';

export interface NotificationEventPayload {
  eventType?: string;
  recipient?: string;
  email?: string;
  passengerEmail?: string;
  passengerName?: string;
  fullName?: string;
  bookingReference?: string;
  flightNumber?: string;
  source?: string;
  destination?: string;
  seatNumber?: string;
  fareAmount?: number;
  amount?: number;
  refundPercentage?: number;
  refundAmount?: number;
  refundTransactionRef?: string;
  approvalRemarks?: string;
  reason?: string;
  paymentReference?: string;
  departureTime?: string;
  oldDepartureTime?: string;
  newDepartureTime?: string;
  delayMinutes?: number;
  delayReason?: string;
  resetToken?: string;
  passengers?: any[];
  userId?: number;
  bookingId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/notifications';
  private directUrl = 'http://localhost:8089/api/notifications';
  private defaultEmail = 'sindhusakthi41@gmail.com';

  private preparePayload(payload: NotificationEventPayload): NotificationEventPayload {
    return {
      ...payload,
      recipient: payload.recipient || payload.email || this.defaultEmail,
      email: payload.email || payload.recipient || this.defaultEmail
    };
  }

  public sendBookingConfirmed(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'BOOKING_CONFIRMED' });
    return this.http.post(`${this.apiUrl}/booking-confirmed`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/booking-confirmed`, data)),
      catchError(err => {
        console.warn('Booking confirmed notification error:', err);
        return of(null);
      })
    );
  }

  public sendTaxInvoice(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'TAX_INVOICE' });
    return this.http.post(`${this.apiUrl}/invoice`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/invoice`, data)),
      catchError(err => {
        console.warn('Invoice notification error:', err);
        return of(null);
      })
    );
  }

  public sendBookingCancelled(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'BOOKING_CANCELLED' });
    return this.http.post(`${this.apiUrl}/booking-cancelled`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/booking-cancelled`, data)),
      catchError(err => {
        console.warn('Booking cancelled notification error:', err);
        return of(null);
      })
    );
  }

  public sendRefundRequested(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'REFUND_REQUESTED' });
    return this.http.post(`${this.apiUrl}/refund-requested`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/refund-requested`, data)),
      catchError(err => {
        console.warn('Refund requested notification error:', err);
        return of(null);
      })
    );
  }

  public sendRefundApproved(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'REFUND_APPROVED' });
    return this.http.post(`${this.apiUrl}/refund-approved`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/refund-approved`, data)),
      catchError(err => {
        console.warn('Refund approved notification error:', err);
        return of(null);
      })
    );
  }

  public sendRefundDenied(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'REFUND_DENIED' });
    return this.http.post(`${this.apiUrl}/refund-denied`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/refund-denied`, data)),
      catchError(err => {
        console.warn('Refund denied notification error:', err);
        return of(null);
      })
    );
  }

  public sendFlightRescheduled(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'FLIGHT_RESCHEDULED' });
    return this.http.post(`${this.apiUrl}/flight-rescheduled`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/flight-rescheduled`, data)),
      catchError(err => {
        console.warn('Flight rescheduled notification error:', err);
        return of(null);
      })
    );
  }

  public sendFlightDelayed(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'FLIGHT_DELAYED' });
    return this.http.post(`${this.apiUrl}/flight-delayed`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/flight-delayed`, data)),
      catchError(err => {
        console.warn('Flight delayed notification error:', err);
        return of(null);
      })
    );
  }

  public sendUserRegistered(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'USER_REGISTERED' });
    return this.http.post(`${this.apiUrl}/user-registered`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/user-registered`, data)),
      catchError(err => {
        console.warn('User registered notification error:', err);
        return of(null);
      })
    );
  }

  public sendPasswordResetOtp(payload: NotificationEventPayload): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'PASSWORD_RESET_REQUESTED' });
    return this.http.post(`${this.apiUrl}/password-reset`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/password-reset`, data)),
      catchError(err => {
        console.warn('Password reset notification error:', err);
        return of(null);
      })
    );
  }

  public sendOperationsReport(payload: any): Observable<any> {
    const data = this.preparePayload({ ...payload, eventType: 'OPERATIONS_REPORT' });
    return this.http.post(`${this.apiUrl}/operations-report`, data).pipe(
      catchError(() => this.http.post(`${this.directUrl}/operations-report`, data)),
      catchError(err => {
        console.warn('Operations report email notification error:', err);
        return of(null);
      })
    );
  }

  public downloadOperationsReportPdf(payload: any): Observable<Blob> {
    return this.http.post(`${this.directUrl}/operations-report/download`, payload, {
      responseType: 'blob'
    }).pipe(
      catchError(() => this.http.post(`${this.apiUrl}/operations-report/download`, payload, {
        responseType: 'blob'
      }))
    );
  }
}
