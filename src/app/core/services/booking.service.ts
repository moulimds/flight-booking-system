import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { BookingRequest, BookingResponse, CancellationPreviewResponse, CancellationRequest } from '../models/booking.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private gatewayUrl = 'http://localhost:8080/api/bookings';

  private getAuthHeaders(): HttpHeaders {
    const user = this.authService.currentUserValue;
    let headers = new HttpHeaders();
    if (user?.userId) {
      headers = headers.set('X-User-Id', String(user.userId));
    }
    if (user?.role) {
      headers = headers.set('X-User-Role', user.role);
    }
    if (user?.email) {
      headers = headers.set('X-User-Email', user.email);
    }
    return headers;
  }

  public createBooking(req: BookingRequest): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.gatewayUrl}`, req, { headers: this.getAuthHeaders() });
  }

  public getBookingById(id: number): Observable<BookingResponse> {
    return this.http.get<BookingResponse>(`${this.gatewayUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  public getBookingByReference(reference: string): Observable<BookingResponse> {
    return this.http.get<BookingResponse>(`${this.gatewayUrl}/reference/${reference}`, { headers: this.getAuthHeaders() });
  }

  public getMyBookings(): Observable<BookingResponse[]> {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) {
      return of([]);
    }
    return this.http.get<BookingResponse[]>(`${this.gatewayUrl}/user/${user.userId}`, { headers: this.getAuthHeaders() });
  }

  public getBookingsByUser(userId: number): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.gatewayUrl}/user/${userId}`, { headers: this.getAuthHeaders() });
  }

  public getAllBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.gatewayUrl}`, { headers: this.getAuthHeaders() });
  }

  // Exact Refund Policy Preview
  public getCancellationPreview(idOrRef: string): Observable<CancellationPreviewResponse> {
    return this.http.get<CancellationPreviewResponse>(`${this.gatewayUrl}/${idOrRef}/cancellation-preview`, { headers: this.getAuthHeaders() });
  }

  // Customer Submits Cancellation & Automatic Bank Refund
  public requestCancellation(idOrRef: string, reqOrReason?: CancellationRequest | string): Observable<BookingResponse> {
    let payload: CancellationRequest = {};
    if (typeof reqOrReason === 'string') {
      payload = { reason: reqOrReason };
    } else if (reqOrReason) {
      payload = reqOrReason;
    }
    return this.http.post<BookingResponse>(`${this.gatewayUrl}/${idOrRef}/request-cancellation`, payload, { headers: this.getAuthHeaders() });
  }

  // Support Team Pending Cancellations
  public getPendingSupportCancellations(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.gatewayUrl}/cancellations/pending-support`, { headers: this.getAuthHeaders() });
  }

  // Support Team Approves or Rejects Cancellation
  public reviewCancellationBySupport(idOrRef: string, decision: 'APPROVE' | 'REJECT', remarks?: string): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.gatewayUrl}/${idOrRef}/support-review`, { decision, remarks }, { headers: this.getAuthHeaders() });
  }

  // Admin Pending Refund Approvals
  public getPendingAdminRefunds(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.gatewayUrl}/refunds/pending-admin`, { headers: this.getAuthHeaders() });
  }

  // Admin Approves or Rejects Refund
  public reviewRefundByAdmin(idOrRef: string, decision: 'APPROVE' | 'REJECT', remarks?: string): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.gatewayUrl}/${idOrRef}/admin-review`, { decision, remarks }, { headers: this.getAuthHeaders() });
  }

  // Confirm booking
  public confirmBooking(idOrRef: string | number, paymentId = 1): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.gatewayUrl}/${idOrRef}/confirm?paymentId=${paymentId}`, {}, { headers: this.getAuthHeaders() });
  }

  // Reschedule booking
  public rescheduleBooking(idOrRef: string | number, request: any): Observable<BookingResponse> {
    return this.http.post<BookingResponse>(`${this.gatewayUrl}/${idOrRef}/reschedule`, request, { headers: this.getAuthHeaders() });
  }

  // Legacy / Direct cancellation fallback
  public cancelBooking(bookingReference: string): Observable<any> {
    return this.http.post<any>(`${this.gatewayUrl}/${bookingReference}/cancel`, {}, { headers: this.getAuthHeaders() });
  }
}
