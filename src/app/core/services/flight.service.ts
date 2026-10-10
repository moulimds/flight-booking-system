import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Flight, FlightSearchCriteria, FareDetail, FlightSearchResult, Aircraft, MealOption, WeeklySchedule } from '../models/flight.models';

@Injectable({
  providedIn: 'root'
})
export class FlightService {
  private http = inject(HttpClient);
  private gatewayUrl = 'http://localhost:8080/api';

  public getAllFlights(): Observable<Flight[]> {
    return this.http.get<Flight[]>(`${this.gatewayUrl}/flights`);
  }

  public getFlightById(id: number): Observable<Flight> {
    return this.http.get<Flight>(`${this.gatewayUrl}/flights/${id}`);
  }

  public getFlightByNumber(flightNumber: string): Observable<Flight> {
    return this.http.get<Flight>(`${this.gatewayUrl}/flights/number/${flightNumber}`);
  }

  public searchFlights(criteria: FlightSearchCriteria): Observable<Flight[]> {
    let params = new HttpParams();
    const origin = criteria.source || criteria.origin || '';
    const dest = criteria.destination || '';
    const date = criteria.date || criteria.departureDate || '';

    if (origin) params = params.set('source', origin);
    if (dest) params = params.set('destination', dest);
    if (date) params = params.set('date', date);

    return this.http.get<Flight[]>(`${this.gatewayUrl}/flights/search`, { params });
  }

  public searchAggregateFlights(criteria: FlightSearchCriteria): Observable<FlightSearchResult[]> {
    let params = new HttpParams();
    const origin = criteria.source || criteria.origin || '';
    const dest = criteria.destination || '';
    const date = criteria.date || criteria.departureDate || '';

    if (origin) params = params.set('source', origin);
    if (dest) params = params.set('destination', dest);
    if (date) params = params.set('date', date);

    return this.http.get<FlightSearchResult[]>(`${this.gatewayUrl}/search/flights`, { params });
  }

  public createFlight(flight: Partial<Flight>): Observable<Flight> {
    return this.http.post<Flight>(`${this.gatewayUrl}/flights`, flight);
  }

  public updateFlight(id: number, flight: Partial<Flight>): Observable<Flight> {
    return this.http.put<Flight>(`${this.gatewayUrl}/flights/${id}`, flight);
  }

  public deleteFlight(id: number): Observable<void> {
    return this.http.delete<void>(`${this.gatewayUrl}/flights/${id}`);
  }

  public getFaresForFlight(flightId: number): Observable<FareDetail[]> {
    return this.http.get<FareDetail[]>(`${this.gatewayUrl}/fares/flight/${flightId}`);
  }

  public getAllFares(): Observable<FareDetail[]> {
    return this.http.get<FareDetail[]>(`${this.gatewayUrl}/fares`);
  }

  // --- Aircraft Management API ---
  public getAllAircraft(): Observable<Aircraft[]> {
    return this.http.get<Aircraft[]>(`${this.gatewayUrl}/flights/aircraft`);
  }

  public getAircraftById(id: number): Observable<Aircraft> {
    return this.http.get<Aircraft>(`${this.gatewayUrl}/flights/aircraft/${id}`);
  }

  public createAircraft(aircraft: Partial<Aircraft>): Observable<Aircraft> {
    return this.http.post<Aircraft>(`${this.gatewayUrl}/flights/aircraft`, aircraft);
  }

  public updateAircraft(id: number, aircraft: Partial<Aircraft>): Observable<Aircraft> {
    return this.http.put<Aircraft>(`${this.gatewayUrl}/flights/aircraft/${id}`, aircraft);
  }

  public deleteAircraft(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.gatewayUrl}/flights/aircraft/${id}`);
  }

  public updateAircraftStatus(id: number, status: string): Observable<Aircraft> {
    return this.http.patch<Aircraft>(`${this.gatewayUrl}/flights/aircraft/${id}/status`, { status });
  }

  // --- Meal Options Management API ---
  public getAllMealOptions(): Observable<MealOption[]> {
    return this.http.get<MealOption[]>(`${this.gatewayUrl}/flights/meals`);
  }

  public getMealsByFlight(flightId: number): Observable<MealOption[]> {
    return this.http.get<MealOption[]>(`${this.gatewayUrl}/flights/meals/flight/${flightId}`);
  }

  public getMealsByAircraft(aircraftId: number): Observable<MealOption[]> {
    return this.http.get<MealOption[]>(`${this.gatewayUrl}/flights/meals/aircraft/${aircraftId}`);
  }

  public createMealOption(meal: Partial<MealOption>): Observable<MealOption> {
    return this.http.post<MealOption>(`${this.gatewayUrl}/flights/meals`, meal);
  }

  public updateMealOption(id: number, meal: Partial<MealOption>): Observable<MealOption> {
    return this.http.put<MealOption>(`${this.gatewayUrl}/flights/meals/${id}`, meal);
  }

  public toggleMealAvailability(id: number, available?: boolean): Observable<MealOption> {
    return this.http.patch<MealOption>(`${this.gatewayUrl}/flights/meals/${id}/availability`, available !== undefined ? { available } : {});
  }

  public deleteMealOption(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.gatewayUrl}/flights/meals/${id}`);
  }

  // --- Weekly Flight Schedules API ---
  public getAllSchedules(): Observable<WeeklySchedule[]> {
    return this.http.get<WeeklySchedule[]>(`${this.gatewayUrl}/flights/schedules`);
  }

  public getScheduleById(id: number): Observable<WeeklySchedule> {
    return this.http.get<WeeklySchedule>(`${this.gatewayUrl}/flights/schedules/${id}`);
  }

  public createSchedule(schedule: Partial<WeeklySchedule>): Observable<WeeklySchedule> {
    return this.http.post<WeeklySchedule>(`${this.gatewayUrl}/flights/schedules`, schedule);
  }

  public updateSchedule(id: number, schedule: Partial<WeeklySchedule>): Observable<WeeklySchedule> {
    return this.http.put<WeeklySchedule>(`${this.gatewayUrl}/flights/schedules/${id}`, schedule);
  }

  public generateScheduleFlights(id: number): Observable<{ scheduleId: number; generatedFlightsCount: number; message: string }> {
    return this.http.post<{ scheduleId: number; generatedFlightsCount: number; message: string }>(`${this.gatewayUrl}/flights/schedules/${id}/generate`, {});
  }

  public updateScheduleStatus(id: number, status: string): Observable<WeeklySchedule> {
    return this.http.patch<WeeklySchedule>(`${this.gatewayUrl}/flights/schedules/${id}/status`, { status });
  }

  public deleteSchedule(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.gatewayUrl}/flights/schedules/${id}`);
  }
}
