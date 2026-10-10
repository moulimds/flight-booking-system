import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SeatService } from '../../../core/services/seat.service';
import { FlightService } from '../../../core/services/flight.service';
import { AuthService } from '../../../core/services/auth.service';
import { Seat } from '../../../core/models/seat.models';
import { Flight } from '../../../core/models/flight.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { SeatStatusDirective } from '../../../shared/directives/seat-status.directive';
import { ToastService } from '../../../core/services/toast.service';

export interface PassengerSeatSelection {
  id: number;
  fullName: string;
  dateOfBirth: string;
  gender: string;
  travelClass: 'BUSINESS' | 'PREMIUM_ECONOMY' | 'ECONOMY';
  selectedSeat: Seat | null;
  fareAmount: number;
  passengerType?: 'ADULT' | 'CHILD' | 'INFANT';
}

export interface TripLegSelectionConfig {
  legIndex: number;
  flightId: number;
  flight: Flight | null;
  title: string;
  seats: { [passengerIdx: number]: Seat | null };
}

@Component({
  selector: 'app-seat-selection',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, CurrencyFormatterPipe, SeatStatusDirective],
  templateUrl: './seat-selection.component.html',
  styleUrl: './seat-selection.component.css'
})
export class SeatSelectionComponent implements OnInit, OnDestroy {
  private seatService = inject(SeatService);
  private flightService = inject(FlightService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  public flightId = 1;
  public flight: Flight | null = null;
  public seats: Seat[] = [];
  public isLoading = false;
  public isHolding = false;

  // Trip Mode
  public tripType: 'ONE_WAY' | 'ROUND_TRIP' | 'MULTI_CITY' = 'ONE_WAY';
  public tripLegs: TripLegSelectionConfig[] = [];
  public currentLegIndex = 0;

  // Multi-Passenger Selection (1 to 5 passengers)
  public passengerCount = 1;
  public adultsCount = 1;
  public childrenCount = 0;
  public infantsCount = 0;
  public passengers: PassengerSeatSelection[] = [];
  public activePassengerIndex = 0;

  public leftColumns: string[] = ['A', 'B'];
  public rightColumns: string[] = ['C', 'D'];

  public businessRows: number[] = [];
  public premiumEconomyRows: number[] = [];
  public economyRows: number[] = [];

  // 2-Minute Hold Timer
  public holdSecondsRemaining = 0;
  private holdTimerInterval: any = null;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.flightId = +params['id'] || 1;
      this.initPassengers();
      this.route.queryParams.subscribe(qParams => {
        if (qParams['tripType']) {
          this.tripType = qParams['tripType'];
        }
        if (qParams['adults']) {
          this.adultsCount = parseInt(qParams['adults'], 10) || 1;
        }
        if (qParams['children']) {
          this.childrenCount = parseInt(qParams['children'], 10) || 0;
        }
        if (qParams['infants']) {
          this.infantsCount = parseInt(qParams['infants'], 10) || 0;
        }

        if (qParams['adults'] || qParams['children'] || qParams['infants']) {
          this.setPassengerBreakdown(this.adultsCount, this.childrenCount, this.infantsCount);
        } else if (qParams['passengers']) {
          const count = parseInt(qParams['passengers'], 10);
          if (count >= 1 && count <= 5) {
            this.setPassengerBreakdown(count, 0, 0);
          }
        }
        if (qParams['cabin'] && qParams['cabin'] !== 'ALL') {
          const cabin = qParams['cabin'].toUpperCase();
          this.passengers.forEach(p => {
            p.travelClass = cabin as any;
          });
        }

        this.setupTripLegs(qParams);
      });
    });
  }

  ngOnDestroy(): void {
    this.clearHoldTimer();
  }

  private setupTripLegs(qParams: any): void {
    this.tripLegs = [];
    if (this.tripType === 'ROUND_TRIP') {
      const returnFlightId = +qParams['returnFlightId'] || this.flightId;
      this.tripLegs = [
        { legIndex: 0, flightId: this.flightId, flight: null, title: 'Leg 1: Outbound Flight', seats: {} },
        { legIndex: 1, flightId: returnFlightId, flight: null, title: 'Leg 2: Return Flight', seats: {} }
      ];
    } else if (this.tripType === 'MULTI_CITY') {
      const legIds = (qParams['legFlightIds'] || String(this.flightId)).split(',').map((id: string) => +id.trim());
      this.tripLegs = legIds.map((fId: number, idx: number) => ({
        legIndex: idx,
        flightId: fId,
        flight: null,
        title: `Leg ${idx + 1} Flight`,
        seats: {}
      }));
    } else {
      this.tripLegs = [
        { legIndex: 0, flightId: this.flightId, flight: null, title: 'Flight', seats: {} }
      ];
    }

    // Preload flight details for all legs
    this.tripLegs.forEach(leg => {
      this.flightService.getFlightById(leg.flightId).subscribe(f => {
        leg.flight = f;
        if (this.tripType === 'ROUND_TRIP') {
          leg.title = leg.legIndex === 0
            ? `Leg 1: Outbound (${f?.source || f?.origin || 'DEL'} → ${f?.destination || 'BOM'})`
            : `Leg 2: Return (${f?.source || f?.origin || 'BOM'} → ${f?.destination || 'DEL'})`;
        } else if (this.tripType === 'MULTI_CITY') {
          leg.title = `Leg ${leg.legIndex + 1}: ${f?.source || f?.origin || 'DEP'} → ${f?.destination || 'ARR'}`;
        }
      });
    });

    this.currentLegIndex = 0;
    this.loadFlightAndSeats();
  }

  public switchLeg(index: number): void {
    if (index < 0 || index >= this.tripLegs.length) return;
    this.currentLegIndex = index;
    this.flightId = this.tripLegs[index].flightId;
    this.loadFlightAndSeats();
    this.syncPassengerSeatsForCurrentLeg();
  }

  private syncPassengerSeatsForCurrentLeg(): void {
    const curLeg = this.tripLegs[this.currentLegIndex];
    if (!curLeg) return;
    this.passengers.forEach((p, idx) => {
      p.selectedSeat = curLeg.seats[idx] || null;
    });
  }

  public isLegComplete(legIdx: number): boolean {
    const leg = this.tripLegs[legIdx];
    if (!leg) return false;
    return this.passengers.every((_, pIdx) => !!leg.seats[pIdx]);
  }

  public get allTripLegsHaveSeats(): boolean {
    return this.tripLegs.every((_, legIdx) => this.isLegComplete(legIdx));
  }

  public initPassengers(): void {
    const user = this.authService.currentUserValue;
    this.passengers = [
      {
        id: 1,
        fullName: user?.fullName || 'Adult 1',
        dateOfBirth: '1995-05-15',
        gender: 'MALE',
        travelClass: 'ECONOMY',
        selectedSeat: null,
        fareAmount: 5000,
        passengerType: 'ADULT'
      }
    ];
    this.passengerCount = 1;
    this.adultsCount = 1;
    this.childrenCount = 0;
    this.infantsCount = 0;
    this.activePassengerIndex = 0;
  }

  public setPassengerBreakdown(adults: number, children: number, infants: number): void {
    this.adultsCount = Math.max(1, adults);
    this.childrenCount = Math.max(0, children);
    this.infantsCount = Math.max(0, infants);
    const total = Math.min(5, this.adultsCount + this.childrenCount + this.infantsCount);
    this.passengerCount = total;

    const user = this.authService.currentUserValue;
    const newPassengers: PassengerSeatSelection[] = [];
    let pId = 1;

    for (let i = 0; i < this.adultsCount && newPassengers.length < total; i++) {
      newPassengers.push({
        id: pId++,
        fullName: i === 0 && user?.fullName ? user.fullName : `Adult ${i + 1}`,
        dateOfBirth: '1995-05-15',
        gender: 'MALE',
        travelClass: 'ECONOMY',
        selectedSeat: null,
        fareAmount: 5000,
        passengerType: 'ADULT'
      });
    }

    for (let i = 0; i < this.childrenCount && newPassengers.length < total; i++) {
      newPassengers.push({
        id: pId++,
        fullName: `Child ${i + 1}`,
        dateOfBirth: '2017-06-10',
        gender: 'MALE',
        travelClass: 'ECONOMY',
        selectedSeat: null,
        fareAmount: 5000,
        passengerType: 'CHILD'
      });
    }

    for (let i = 0; i < this.infantsCount && newPassengers.length < total; i++) {
      newPassengers.push({
        id: pId++,
        fullName: `Infant ${i + 1}`,
        dateOfBirth: '2025-02-15',
        gender: 'MALE',
        travelClass: 'ECONOMY',
        selectedSeat: null,
        fareAmount: 5000,
        passengerType: 'INFANT'
      });
    }

    // Preserve previously selected classes and seats if present
    if (this.passengers && this.passengers.length > 0) {
      newPassengers.forEach((np, idx) => {
        if (this.passengers[idx]) {
          np.travelClass = this.passengers[idx].travelClass || np.travelClass;
          np.selectedSeat = this.passengers[idx].selectedSeat || null;
          np.fareAmount = this.passengers[idx].fareAmount || np.fareAmount;
        }
      });
    }

    this.passengers = newPassengers;
    if (this.activePassengerIndex >= this.passengers.length) {
      this.activePassengerIndex = 0;
    }
  }

  public setPassengerCount(count: number): void {
    this.setPassengerBreakdown(count, 0, 0);
  }

  public get currentPassenger(): PassengerSeatSelection {
    return this.passengers[this.activePassengerIndex] || this.passengers[0];
  }

  public setActivePassenger(index: number): void {
    this.activePassengerIndex = index;
  }

  public onClassChange(p: PassengerSeatSelection): void {
    const base = this.flight?.basePrice || 5000;
    if (p.travelClass === 'BUSINESS') {
      p.fareAmount = Math.round(base * 1.75);
    } else if (p.travelClass === 'PREMIUM_ECONOMY') {
      p.fareAmount = Math.round(base * 1.35);
    } else {
      p.fareAmount = base;
    }

    // If current selected seat does not match new class, reset seat
    if (p.selectedSeat && p.selectedSeat.seatClass !== p.travelClass) {
      p.selectedSeat = null;
      if (this.tripLegs[this.currentLegIndex]) {
        this.tripLegs[this.currentLegIndex].seats[this.activePassengerIndex] = null;
      }
      this.toastService.info('Seat Reset', `Please choose a ${p.travelClass} seat for ${p.fullName}`);
    }
  }

  public loadFlightAndSeats(): void {
    this.isLoading = true;
    this.flightService.getFlightById(this.flightId).subscribe({
      next: (f) => {
        this.flight = f;
        if (this.tripLegs[this.currentLegIndex]) {
          this.tripLegs[this.currentLegIndex].flight = f;
        }
        this.passengers.forEach(p => this.onClassChange(p));
      },
      error: () => {}
    });

    this.seatService.getSeatsForFlight(this.flightId).subscribe({
      next: (seats) => {
        this.seats = (seats || []).map(s => {
          if (!s.rowNumber && s.seatNumber) {
            const match = s.seatNumber.match(/^(\d+)([A-Z])$/);
            if (match) {
              s.rowNumber = parseInt(match[1], 10);
              s.columnLetter = match[2];
            }
          }
          return s;
        });

        const colSet = new Set<string>();
        this.seats.forEach(s => {
          const col = s.columnLetter || this.extractCol(s.seatNumber);
          if (col) colSet.add(col);
        });
        const allCols = Array.from(colSet).sort();
        if (allCols.length > 0) {
          const mid = Math.ceil(allCols.length / 2);
          this.leftColumns = allCols.slice(0, mid);
          this.rightColumns = allCols.slice(mid);
        }

        const allRows = Array.from(new Set(this.seats.map(s => s.rowNumber || this.extractRow(s.seatNumber)))).sort((a, b) => a - b);
        
        this.businessRows = allRows.filter(r => 
          this.seats.some(s => (s.rowNumber === r || this.extractRow(s.seatNumber) === r) && s.seatClass === 'BUSINESS')
        );
        this.premiumEconomyRows = allRows.filter(r => 
          this.seats.some(s => (s.rowNumber === r || this.extractRow(s.seatNumber) === r) && s.seatClass === 'PREMIUM_ECONOMY') &&
          !this.businessRows.includes(r)
        );
        this.economyRows = allRows.filter(r => 
          !this.businessRows.includes(r) && !this.premiumEconomyRows.includes(r)
        );

        // Fallback: If no premium economy rows exist in DB, designate rows 3 and 4 as PREMIUM_ECONOMY
        if (this.premiumEconomyRows.length === 0 && allRows.length >= 3) {
          const nonBusiness = allRows.filter(r => !this.businessRows.includes(r));
          const peRows = nonBusiness.slice(0, Math.min(2, nonBusiness.length - 1));
          if (peRows.length > 0) {
            peRows.forEach(r => {
              this.seats.filter(s => (s.rowNumber === r || this.extractRow(s.seatNumber) === r)).forEach(s => {
                s.seatClass = 'PREMIUM_ECONOMY';
              });
            });
            this.premiumEconomyRows = peRows;
            this.economyRows = allRows.filter(r => !this.businessRows.includes(r) && !this.premiumEconomyRows.includes(r));
          }
        }

        if (this.businessRows.length === 0 && this.economyRows.length === 0 && allRows.length > 0) {
          this.economyRows = allRows;
        }

        this.syncPassengerSeatsForCurrentLeg();
        this.isLoading = false;
      },
      error: () => {
        this.seats = [];
        this.isLoading = false;
      }
    });
  }

  public getSeat(row: number, col: string): Seat | undefined {
    return this.seats.find(s =>
      (s.rowNumber === row || this.extractRow(s.seatNumber) === row) &&
      (s.columnLetter === col || this.extractCol(s.seatNumber) === col)
    );
  }

  public isSeatSelectedByAnyPassenger(seat: Seat): boolean {
    const curLeg = this.tripLegs[this.currentLegIndex];
    if (curLeg) {
      return Object.values(curLeg.seats).some(s => s?.id === seat.id || s?.seatNumber === seat.seatNumber);
    }
    return this.passengers.some(p => p.selectedSeat?.id === seat.id || p.selectedSeat?.seatNumber === seat.seatNumber);
  }

  public getPassengerIndexForSeat(seat: Seat): number {
    const curLeg = this.tripLegs[this.currentLegIndex];
    if (curLeg) {
      for (let i = 0; i < this.passengers.length; i++) {
        if (curLeg.seats[i]?.id === seat.id || curLeg.seats[i]?.seatNumber === seat.seatNumber) {
          return i;
        }
      }
    }
    return this.passengers.findIndex(p => p.selectedSeat?.id === seat.id || p.selectedSeat?.seatNumber === seat.seatNumber);
  }

  private extractRow(seatNumber: string): number {
    const match = (seatNumber || '').match(/^(\d+)/);
    return match ? parseInt(match[1], 10) : 1;
  }

  private extractCol(seatNumber: string): string {
    const match = (seatNumber || '').match(/([A-Z])$/);
    return match ? match[1] : 'A';
  }

  public selectSeat(seat: Seat): void {
    if (seat.status === 'OCCUPIED' || seat.status === 'BOOKED') return;

    const passenger = this.currentPassenger;
    // Auto-align passenger travel class if picking a seat in another cabin class (e.g. Premium Economy or Business)
    if (seat.seatClass && seat.seatClass !== passenger.travelClass) {
      passenger.travelClass = seat.seatClass as any;
      this.onClassChange(passenger);
      this.toastService.info('Cabin Class Updated', `Assigned ${passenger.fullName} to ${seat.seatClass} cabin.`);
    }

    // Check if another passenger already took this seat on this leg
    const curLeg = this.tripLegs[this.currentLegIndex];
    if (curLeg) {
      for (let i = 0; i < this.passengers.length; i++) {
        if (i !== this.activePassengerIndex && curLeg.seats[i]?.seatNumber === seat.seatNumber) {
          this.toastService.warning('Seat Occupied', `Seat ${seat.seatNumber} is already selected for Passenger ${i + 1}`);
          return;
        }
      }
    }

    this.isHolding = true;
    this.seatService.holdSeat(seat.id, { holdDurationMinutes: 2 }).subscribe({
      next: () => {
        this.isHolding = false;
        seat.status = 'HELD';
        passenger.selectedSeat = seat;

        // Record in leg config
        if (curLeg) {
          curLeg.seats[this.activePassengerIndex] = seat;
        }

        this.startHoldTimer(120); // 2 minutes (120 seconds)
        this.toastService.info('Seat Reserved', `Seat ${seat.seatNumber} held for ${passenger.fullName}`);

        // Advance to next passenger if needed on this leg
        const nextUnseated = this.passengers.findIndex((_, idx) => !curLeg?.seats[idx]);
        if (nextUnseated !== -1) {
          this.activePassengerIndex = nextUnseated;
        } else if (this.currentLegIndex < this.tripLegs.length - 1) {
          // All passengers have seats for this leg! Suggest next leg
          this.toastService.success(
            `Leg ${this.currentLegIndex + 1} Seats Done!`,
            `Seats selected for all passengers. Switch to Leg ${this.currentLegIndex + 2} to pick return/next seats.`
          );
        }
      },
      error: (err) => {
        this.isHolding = false;
        const msg = err.status === 409 || err.status === 400 ?
          'This seat is no longer available. Please select another seat.' :
          (err?.error?.message || 'Unable to hold seat.');
        this.toastService.error('Seat Unavailable', msg);
        this.loadFlightAndSeats();
      }
    });
  }

  private startHoldTimer(seconds: number): void {
    this.clearHoldTimer();
    this.holdSecondsRemaining = seconds;

    this.holdTimerInterval = setInterval(() => {
      this.holdSecondsRemaining--;
      if (this.holdSecondsRemaining <= 0) {
        this.clearHoldTimer();
        this.toastService.warning('Hold Expired', '2-minute seat reservation time expired. Please re-select seats.');
        this.passengers.forEach(p => p.selectedSeat = null);
        if (this.tripLegs[this.currentLegIndex]) {
          this.tripLegs[this.currentLegIndex].seats = {};
        }
        this.loadFlightAndSeats();
      }
    }, 1000);
  }

  private clearHoldTimer(): void {
    if (this.holdTimerInterval) {
      clearInterval(this.holdTimerInterval);
      this.holdTimerInterval = null;
    }
  }

  public get formattedTimer(): string {
    const mins = Math.floor(this.holdSecondsRemaining / 60);
    const secs = this.holdSecondsRemaining % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  public get totalCombinedFare(): number {
    return this.passengers.reduce((sum, p) => sum + (p.fareAmount || 5000), 0);
  }

  public get totalCombinedTripFare(): number {
    const multiplier = this.tripLegs.length || 1;
    return this.totalCombinedFare * multiplier;
  }

  public get allPassengersHaveSeats(): boolean {
    return this.allTripLegsHaveSeats;
  }

  public proceedToPassengerDetails(): void {
    if (!this.allTripLegsHaveSeats) {
      this.toastService.error('Incomplete Seats', 'Please select a seat for all passengers across each flight leg.');
      return;
    }

    const payload = this.passengers.map((p, pIdx) => ({
      id: p.id,
      fullName: p.fullName,
      dateOfBirth: p.dateOfBirth,
      gender: p.gender,
      travelClass: p.travelClass,
      passengerType: p.passengerType || 'ADULT',
      seatNumber: this.tripLegs[0]?.seats[pIdx]?.seatNumber || '1A',
      fareAmount: p.fareAmount,
      legSeats: this.tripLegs.map(leg => leg.seats[pIdx]?.seatNumber || '1A')
    }));

    const legsInfo = this.tripLegs.map(leg => ({
      flightId: leg.flightId,
      flightNumber: leg.flight?.flightNumber || 'FL-' + leg.flightId,
      airline: leg.flight?.airline || 'SkyWings',
      route: `${leg.flight?.source || leg.flight?.origin || 'DEL'} → ${leg.flight?.destination || 'BOM'}`,
      departureDate: leg.flight?.departureDate || '',
      departureTime: leg.flight?.departureTime || '',
      fare: leg.flight?.basePrice || 4800
    }));

    this.router.navigate(['/booking/passenger-details'], {
      queryParams: {
        flightId: this.tripLegs[0].flightId,
        tripType: this.tripType,
        passengers: this.passengerCount,
        adults: this.adultsCount,
        children: this.childrenCount,
        infants: this.infantsCount,
        passengersData: JSON.stringify(payload),
        tripLegsData: JSON.stringify(legsInfo),
        totalAmount: this.totalCombinedTripFare
      }
    });
  }
}
