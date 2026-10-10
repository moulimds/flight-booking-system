import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, startWith, switchMap } from 'rxjs';
import { FlightTracking } from '../models/tracking.models';

@Injectable({
  providedIn: 'root'
})
export class TrackingService {
  private http = inject(HttpClient);
  private gatewayUrl = 'http://localhost:8080/api/flight-tracking';

  public getActiveFlights(): Observable<FlightTracking[]> {
    return this.http.get<FlightTracking[]>(`${this.gatewayUrl}/active`);
  }

  public getActiveTracking(): Observable<FlightTracking[]> {
    return this.getActiveFlights();
  }

  public getFlightTracking(flightNumber: string): Observable<FlightTracking> {
    return this.http.get<FlightTracking>(`${this.gatewayUrl}/flight-number/${flightNumber}`);
  }

  public getFlightTrackingById(flightId: number): Observable<FlightTracking> {
    return this.http.get<FlightTracking>(`${this.gatewayUrl}/flight/${flightId}`);
  }

  public getLiveStream(flightNumber: string): Observable<FlightTracking> {
    return interval(4000).pipe(
      startWith(0),
      switchMap(() => this.getFlightTracking(flightNumber))
    );
  }
}

