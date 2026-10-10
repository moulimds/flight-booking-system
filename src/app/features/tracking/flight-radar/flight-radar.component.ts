import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { TrackingService } from '../../../core/services/tracking.service';
import { FlightService } from '../../../core/services/flight.service';
import { FlightTracking } from '../../../core/models/tracking.models';
import { StatusBadgeDirective } from '../../../shared/directives/status-badge.directive';

@Component({
  selector: 'app-flight-radar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, StatusBadgeDirective],
  templateUrl: './flight-radar.component.html',
  styleUrl: './flight-radar.component.css'
})
export class FlightRadarComponent implements OnInit, OnDestroy {
  private trackingService = inject(TrackingService);
  private flightService = inject(FlightService);
  private route = inject(ActivatedRoute);

  public flightNumber = 'AI-101';
  public activeFlights: FlightTracking[] = [];
  public tracking: FlightTracking | null = null;
  public isLoading = false;
  private trackingSub?: Subscription;

  ngOnInit(): void {
    this.flightService.getAllFlights().subscribe({
      next: (flights) => {
        if (flights && flights.length > 0) {
          this.activeFlights = flights.map(f => ({
            id: f.id,
            flightId: f.id,
            flightNumber: f.flightNumber,
            airline: f.airline,
            source: f.source || f.origin || 'DEL',
            destination: f.destination || 'BOM',
            departureAirport: f.source || f.origin || 'DEL',
            arrivalAirport: f.destination || 'BOM',
            departureTime: f.departureTime || '10:00',
            arrivalTime: f.arrivalTime || '12:00',
            status: f.status || 'SCHEDULED',
            latitude: 28.6139,
            longitude: 77.2090,
            altitude: 35000,
            speed: 850,
            heading: 210,
            progressPercentage: 45,
            gate: f.gate || 'Gate 4',
            terminal: f.terminal || 'T3',
            lastUpdated: new Date().toISOString()
          }));
        }

        this.route.queryParams.subscribe(params => {
          if (params['flightNumber']) {
            this.flightNumber = params['flightNumber'];
          } else if (this.activeFlights.length > 0) {
            this.flightNumber = this.activeFlights[0].flightNumber;
          }
          if (this.flightNumber) {
            this.startTracking();
          }
        });
      },
      error: () => {
        this.activeFlights = [];
      }
    });
  }

  public startTracking(): void {
    if (!this.flightNumber) return;
    if (this.trackingSub) this.trackingSub.unsubscribe();
    this.isLoading = true;

    this.trackingSub = this.trackingService.getLiveStream(this.flightNumber).subscribe({
      next: (data) => {
        this.tracking = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  public selectActiveFlight(fn: string): void {
    this.flightNumber = fn;
    this.startTracking();
  }

  public onSearch(): void {
    this.startTracking();
  }

  ngOnDestroy(): void {
    if (this.trackingSub) this.trackingSub.unsubscribe();
  }
}

