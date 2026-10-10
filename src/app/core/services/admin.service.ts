import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, catchError, of } from 'rxjs';
import { Flight } from '../models/flight.models';
import { BookingResponse } from '../models/booking.models';
import { UserResponse } from '../models/auth.models';

export interface AdminDashboardMetrics {
  totalFlights: number;
  activeFlights: number;
  scheduledFlights: number;
  cancelledFlights: number;
  totalCustomers: number;
  totalBookings: number;
  totalRevenue: number;
  pendingPayments: number;
  rescheduleRequests: number;
  cancellationRequests: number;
  confirmedBookings?: number;
  cancelledBookings?: number;
  activeUsers?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private gatewayUrl = 'http://localhost:8080/api';

  public getDashboardMetrics(): Observable<AdminDashboardMetrics> {
    return forkJoin({
      flights: this.http.get<Flight[]>(`${this.gatewayUrl}/flights`).pipe(catchError(() => of([]))),
      bookings: this.http.get<BookingResponse[]>(`${this.gatewayUrl}/bookings`).pipe(catchError(() => of([]))),
      users: this.http.get<UserResponse[]>(`${this.gatewayUrl}/auth/users`).pipe(catchError(() => of([])))
    }).pipe(
      map(({ flights, bookings, users }) => {
        const flightList = flights || [];
        const bookingList = bookings || [];
        const userList = users || [];

        // 1. Flights breakdown
        const totalFlights = flightList.length;
        const activeStatuses = ['ACTIVE', 'ON_TIME', 'BOARDING', 'IN_AIR', 'DEPARTED', 'DELAYED', 'IN_FLIGHT', 'LANDED'];
        const activeFlights = flightList.filter(f => {
          const s = (f.status || '').toUpperCase();
          return activeStatuses.includes(s) || (s !== 'CANCELLED' && s !== 'SCHEDULED' && s !== 'COMPLETED');
        }).length;
        const scheduledFlights = flightList.filter(f => (f.status || '').toUpperCase() === 'SCHEDULED').length;
        const cancelledFlights = flightList.filter(f => (f.status || '').toUpperCase() === 'CANCELLED').length;

        // 2. Customers breakdown
        let totalCustomers = userList.length;
        if (totalCustomers === 0 && bookingList.length > 0) {
          const uniqueBookers = new Set(bookingList.map(b => b.passengerEmail).filter(Boolean));
          totalCustomers = uniqueBookers.size || bookingList.length;
        }

        // 3. Bookings breakdown
        const totalBookings = bookingList.length;
        const confirmed = bookingList.filter(b => 
          b.bookingStatus === 'CONFIRMED' || 
          b.paymentStatus === 'SUCCESS' || 
          b.paymentStatus === 'PAID'
        );
        const cancelled = bookingList.filter(b => b.bookingStatus === 'CANCELLED');
        const revenue = confirmed.reduce((acc, curr) => acc + (curr.fareAmount || 0), 0);

        // 4. Pending payments
        const pendingPayments = bookingList.filter(b => 
          b.paymentStatus === 'PENDING' || 
          (b.bookingStatus === 'PENDING' && b.paymentStatus !== 'SUCCESS' && b.paymentStatus !== 'PAID')
        ).length;

        // 5. Reschedule requests
        const rescheduleRequests = bookingList.filter(b => 
          b.isRescheduled === true || 
          b.originalFlightId != null || 
          b.bookingStatus === 'RESCHEDULED' || 
          b.bookingStatus === 'RESCHEDULE_REQUESTED'
        ).length;

        // 6. Cancellation requests
        const cancellationRequests = bookingList.filter(b => 
          b.cancellationStatus === 'CANCELLATION_REQUESTED' || 
          b.cancellationStatus === 'SUPPORT_APPROVED' || 
          b.cancellationStatus === 'REFUND_PENDING_ADMIN_APPROVAL' || 
          b.bookingStatus === 'CANCELLATION_REQUESTED' ||
          b.paymentStatus === 'REFUND_PENDING_ADMIN_APPROVAL'
        ).length;

        return {
          totalFlights,
          activeFlights,
          scheduledFlights,
          cancelledFlights,
          totalCustomers,
          totalBookings,
          totalRevenue: revenue,
          pendingPayments,
          rescheduleRequests,
          cancellationRequests,
          confirmedBookings: confirmed.length,
          cancelledBookings: cancelled.length,
          activeUsers: userList.length
        };
      })
    );
  }

  public getAllBookings(): Observable<BookingResponse[]> {
    return this.http.get<BookingResponse[]>(`${this.gatewayUrl}/bookings`);
  }

  public getAllFlights(): Observable<Flight[]> {
    return this.http.get<Flight[]>(`${this.gatewayUrl}/flights`);
  }

  public getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.gatewayUrl}/auth/users`);
  }
}

