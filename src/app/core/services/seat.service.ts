import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Seat, SeatHoldRequest, SeatBookRequest } from '../models/seat.models';

@Injectable({
  providedIn: 'root'
})
export class SeatService {
  private http = inject(HttpClient);
  private gatewayUrl = 'http://localhost:8080/api/seats';

  public getSeatsForFlight(flightId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.gatewayUrl}/flight/${flightId}`);
  }

  public getAvailableSeatsByFlight(flightId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.gatewayUrl}/flight/${flightId}/available`);
  }

  public holdSeat(seatId: number, req?: SeatHoldRequest): Observable<Seat> {
    return this.http.post<Seat>(`${this.gatewayUrl}/${seatId}/hold`, req || {});
  }

  public releaseSeat(seatId: number): Observable<Seat> {
    return this.http.post<Seat>(`${this.gatewayUrl}/${seatId}/release`, {});
  }

  public bookSeat(seatId: number, req: SeatBookRequest): Observable<Seat> {
    return this.http.post<Seat>(`${this.gatewayUrl}/${seatId}/book`, req);
  }

  public markCheckedIn(flightId: number, seatNumber: string): Observable<void> {
    return this.http.patch<void>(`${this.gatewayUrl}/flight/${flightId}/seat/${seatNumber}/checkin`, {});
  }

  public createBatchSeats(req: { flightId: number; rows: number }): Observable<Seat[]> {
    return this.http.post<Seat[]>(`${this.gatewayUrl}/batch`, req);
  }

  public createSeat(req: Partial<Seat>): Observable<Seat> {
    return this.http.post<Seat>(`${this.gatewayUrl}`, req);
  }

  public configureCabinSeats(req: {
    flightId: number;
    businessSeats: number;
    premiumEconomySeats: number;
    economySeats: number;
    columnsPerRow?: number;
    clearExisting?: boolean;
  }): Observable<Seat[]> {
    return this.http.post<Seat[]>(`${this.gatewayUrl}/configure-cabin`, req);
  }
}

