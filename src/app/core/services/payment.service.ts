import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentRequest, PaymentResponse } from '../models/booking.models';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private http = inject(HttpClient);
  private gatewayUrl = 'http://localhost:8080/api/payments';

  public processPayment(req: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.gatewayUrl}/process`, req);
  }

  public getPaymentByBookingId(bookingId: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.gatewayUrl}/booking/${bookingId}`);
  }

  public getPaymentByReference(reference: string): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.gatewayUrl}/reference/${reference}`);
  }
}

