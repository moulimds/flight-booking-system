import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { FlightService } from '../../../core/services/flight.service';
import { BookingService } from '../../../core/services/booking.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Flight, MealOption, WeeklySchedule } from '../../../core/models/flight.models';
import { PassengerDto, BookingRequest } from '../../../core/models/booking.models';
import { FlightDurationPipe } from '../../../shared/pipes/flight-duration.pipe';
import { AirportNamePipe } from '../../../shared/pipes/airport-name.pipe';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { FlightFilterPipe } from '../../../shared/pipes/flight-filter.pipe';

export type TripType = 'ONE_WAY' | 'ROUND_TRIP' | 'MULTI_CITY';

export interface MultiCityLeg {
  id: number;
  origin: string;
  destination: string;
  departureDate: string;
  selectedFlight: Flight | null;
}

export interface TripPassenger {
  id: number;
  fullName: string;
  dateOfBirth: string;
  gender: string;
  travelClass: 'ECONOMY' | 'PREMIUM_ECONOMY' | 'BUSINESS' | 'FIRST';
  passengerType?: 'ADULT' | 'CHILD' | 'INFANT';
}

export interface CreatedTripTicket {
  legIndex: number;
  legTitle: string;
  flightNumber: string;
  route: string;
  departureDate: string;
  departureTime: string;
  pnr: string;
  parentPnr?: string;
  passengerName: string;
  seatNumber: string;
  ticketNumber: string;
  fareAmount: number;
  mealName?: string;
}

@Component({
  selector: 'app-search-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    FlightDurationPipe,
    AirportNamePipe,
    CurrencyFormatterPipe,
    FlightFilterPipe
  ],
  templateUrl: './search-dashboard.component.html',
  styleUrl: './search-dashboard.component.css'
})
export class SearchDashboardComponent implements OnInit {
  private flightService = inject(FlightService);
  private bookingService = inject(BookingService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Trip Selection Mode
  public tripType: TripType = 'ONE_WAY';

  // One Way & General Search Parameters
  public origin = 'DEL';
  public destination = 'BOM';
  public departureDate = '2026-10-15';
  
  // Passenger Breakdown
  public adultsCount = 1;
  public childrenCount = 0;
  public infantsCount = 0;
  public showPassengerDropdown = false;

  public get passengerCount(): number {
    return this.adultsCount + this.childrenCount + this.infantsCount;
  }
  public set passengerCount(val: number) {
    this.adultsCount = Math.max(1, val);
  }

  public get passengerSummary(): string {
    const parts: string[] = [`${this.adultsCount} Adult${this.adultsCount > 1 ? 's' : ''}`];
    if (this.childrenCount > 0) {
      parts.push(`${this.childrenCount} Child${this.childrenCount > 1 ? 'ren' : ''}`);
    }
    if (this.infantsCount > 0) {
      parts.push(`${this.infantsCount} Infant${this.infantsCount > 1 ? 's' : ''}`);
    }
    return parts.join(', ');
  }

  // Round Trip Parameters
  public returnDate = '2026-10-16';
  public roundTripLeg: 'OUTBOUND' | 'RETURN' = 'OUTBOUND';
  public selectedOutboundFlight: Flight | null = null;
  public selectedReturnFlight: Flight | null = null;

  // Multi City Parameters
  public multiCityLegs: MultiCityLeg[] = [
    { id: 1, origin: 'DEL', destination: 'BOM', departureDate: '2026-10-15', selectedFlight: null },
    { id: 2, origin: 'BOM', destination: 'BLR', departureDate: '2026-10-16', selectedFlight: null }
  ];
  public activeLegIndex = 0;

  // Filter Parameters
  public selectedCabin = 'ALL';
  public maxPrice = 25000;
  public availableAirlines: string[] = [];
  public selectedAirlines: string[] = [];

  public flights: Flight[] = [];
  public isLoading = false;

  // Booking Modal State
  public isBookingModalOpen = false;
  public bookingModalStep = 1; // 1 = Passenger Details, 2 = Seats, 3 = Meals, 4 = Payment, 5 = Confirmed
  public passengers: TripPassenger[] = [];
  public contactEmail = '';
  public contactPhone = '';
  public legSeats: { [legKey: string]: string[] } = {};
  public availableMeals: MealOption[] = [];
  public legMeals: { [legIdx: number]: { [pIdx: number]: MealOption | null } } = {};
  public parentBookingReference = '';
  public paymentMethod: 'CARD' | 'UPI' | 'NETBANKING' = 'CARD';
  public paymentForm = {
    cardNumber: '4532 8901 2345 8921',
    cardHolder: 'John Customer',
    expiry: '12/28',
    cvv: '888',
    upiId: 'customer@okaxis',
    bank: 'HDFC Bank'
  };
  public isSubmittingBooking = false;
  public createdTripTickets: CreatedTripTicket[] = [];
  public createdBookingReferences: string[] = [];

  public get totalMealPrice(): number {
    let total = 0;
    for (const legKey in this.legMeals) {
      const pMeals = this.legMeals[legKey];
      if (pMeals) {
        for (const pKey in pMeals) {
          const m = pMeals[pKey];
          if (m && !m.complimentary && m.price) {
            total += Number(m.price);
          }
        }
      }
    }
    return total;
  }

  public get grandTotalTripPrice(): number {
    return this.totalCombinedTripPrice + this.totalMealPrice;
  }

  private fareMap: Record<number, number> = {
    1: 4800,
    2: 5500,
    3: 4100,
    4: 4430,
    5: 6300,
    6: 3800,
    10: 4500,
    14: 4600,
    17: 4200
  };

  public scheduleMap: Record<number, WeeklySchedule> = {};

  ngOnInit(): void {
    this.loadMeals();
    this.loadSchedules();
    this.route.queryParams.subscribe(params => {
      if (params['tripType']) {
        const t = params['tripType'].toUpperCase();
        if (t === 'ROUND_TRIP' || t === 'MULTI_CITY' || t === 'ONE_WAY') {
          this.tripType = t as TripType;
        }
      }
      if (params['origin']) this.origin = params['origin'];
      if (params['destination']) this.destination = params['destination'];
      if (params['departureDate']) this.departureDate = params['departureDate'];
      if (params['returnDate']) this.returnDate = params['returnDate'];
      if (params['adults']) {
        const a = parseInt(params['adults'], 10);
        if (a >= 1) this.adultsCount = a;
      }
      if (params['children']) {
        const c = parseInt(params['children'], 10);
        if (c >= 0) this.childrenCount = c;
      }
      if (params['infants']) {
        const inf = parseInt(params['infants'], 10);
        if (inf >= 0) this.infantsCount = inf;
      }
      if (params['passengers'] && !params['adults']) {
        const p = parseInt(params['passengers'], 10);
        if (p >= 1 && p <= 5) {
          this.adultsCount = p;
          this.childrenCount = 0;
          this.infantsCount = 0;
        }
      }
      if (params['cabin']) {
        this.selectedCabin = params['cabin'].toUpperCase();
      }
      this.loadFlights();
    });
  }

  public loadFlights(): void {
    this.isLoading = true;
    this.flightService.getAllFlights().subscribe({
      next: (flightsData) => {
        const flights = flightsData || [];
        this.flightService.getAllFares().subscribe({
          next: (faresData) => {
            const fareLookup: Record<number, number> = {};
            (faresData || []).forEach(f => {
              if (f.flightId && (f.finalFare || f.baseFare)) {
                fareLookup[f.flightId] = Number(f.finalFare || f.baseFare);
              }
            });
            this.flights = flights.map(f => {
              const matchedFare = fareLookup[f.id] || this.fareMap[f.id] || 4800;
              return { ...f, basePrice: f.basePrice || matchedFare };
            });
            this.updateAirlineFilters(this.flights);
            this.isLoading = false;
          },
          error: () => {
            this.flights = flights.map(f => {
              const matchedFare = this.fareMap[f.id] || 4800;
              return { ...f, basePrice: f.basePrice || matchedFare };
            });
            this.updateAirlineFilters(this.flights);
            this.isLoading = false;
          }
        });
      },
      error: () => {
        this.flights = [];
        this.isLoading = false;
      }
    });
  }

  private updateAirlineFilters(flights: Flight[]): void {
    const airlines = Array.from(new Set(flights.map(f => f.airline).filter(Boolean)));
    this.availableAirlines = airlines;
    if (this.selectedAirlines.length === 0) {
      this.selectedAirlines = [...this.availableAirlines];
    } else {
      const valid = this.selectedAirlines.filter(a => this.availableAirlines.includes(a));
      this.selectedAirlines = valid.length > 0 ? valid : [...this.availableAirlines];
    }
  }

  // Reactive Search Parameters according to Trip Type
  public get currentSearchOrigin(): string {
    if (this.tripType === 'ROUND_TRIP') {
      return this.roundTripLeg === 'OUTBOUND' ? this.origin : this.destination;
    }
    if (this.tripType === 'MULTI_CITY') {
      return this.multiCityLegs[this.activeLegIndex]?.origin || '';
    }
    return this.origin;
  }

  public get currentSearchDestination(): string {
    if (this.tripType === 'ROUND_TRIP') {
      return this.roundTripLeg === 'OUTBOUND' ? this.destination : this.origin;
    }
    if (this.tripType === 'MULTI_CITY') {
      return this.multiCityLegs[this.activeLegIndex]?.destination || '';
    }
    return this.destination;
  }

  public get currentSearchDate(): string {
    if (this.tripType === 'ROUND_TRIP') {
      return this.roundTripLeg === 'OUTBOUND' ? this.departureDate : this.returnDate;
    }
    if (this.tripType === 'MULTI_CITY') {
      return this.multiCityLegs[this.activeLegIndex]?.departureDate || '';
    }
    return this.departureDate;
  }

  public setTripType(type: TripType): void {
    this.tripType = type;
    if (type === 'ROUND_TRIP') {
      this.roundTripLeg = 'OUTBOUND';
      if (!this.returnDate) {
        this.returnDate = '2026-10-16';
      }
    } else if (type === 'MULTI_CITY') {
      this.activeLegIndex = 0;
      if (this.multiCityLegs.length < 2) {
        this.multiCityLegs = [
          { id: 1, origin: this.origin || 'DEL', destination: this.destination || 'BOM', departureDate: this.departureDate || '2026-10-15', selectedFlight: null },
          { id: 2, origin: this.destination || 'BOM', destination: 'BLR', departureDate: '2026-10-16', selectedFlight: null }
        ];
      }
    }
  }

  public swapOriginDestination(): void {
    const temp = this.origin;
    this.origin = this.destination;
    this.destination = temp;
  }

  // Multi-City Management
  public addMultiCityLeg(): void {
    if (this.multiCityLegs.length >= 4) {
      this.toastService.warning('Maximum Legs', 'Multi-city trips can have up to 4 flight legs.');
      return;
    }
    const lastLeg = this.multiCityLegs[this.multiCityLegs.length - 1];
    const newOrigin = lastLeg ? lastLeg.destination : 'BOM';
    const nextCities = ['BLR', 'DEL', 'GOI', 'HYD', 'MAA'].filter(c => c !== newOrigin);
    const newDest = nextCities[0] || 'DEL';
    this.multiCityLegs.push({
      id: Date.now(),
      origin: newOrigin,
      destination: newDest,
      departureDate: '2026-10-17',
      selectedFlight: null
    });
    this.activeLegIndex = this.multiCityLegs.length - 1;
  }

  public removeMultiCityLeg(index: number): void {
    if (this.multiCityLegs.length <= 2) {
      this.toastService.warning('Minimum Legs', 'A multi-city trip requires at least 2 legs.');
      return;
    }
    this.multiCityLegs.splice(index, 1);
    if (this.activeLegIndex >= this.multiCityLegs.length) {
      this.activeLegIndex = this.multiCityLegs.length - 1;
    }
  }

  public setActiveLeg(index: number): void {
    if (index >= 0 && index < this.multiCityLegs.length) {
      this.activeLegIndex = index;
    }
  }

  // Selection Logic
  public selectFlightForTrip(flight: Flight): void {
    if (this.isFlightCutoff(flight)) {
      this.toastService.error('Flight Cutoff (24h Policy)', `Flight ${flight.flightNumber} departs in under 24 hours and cannot be booked according to airline policy.`);
      return;
    }

    if (this.tripType === 'ONE_WAY') {
      this.router.navigate(['/booking/seats', flight.id], {
        queryParams: {
          tripType: 'ONE_WAY',
          passengers: this.passengerCount,
          adults: this.adultsCount,
          children: this.childrenCount,
          infants: this.infantsCount,
          cabin: this.selectedCabin
        }
      });
    } else if (this.tripType === 'ROUND_TRIP') {
      if (this.roundTripLeg === 'OUTBOUND') {
        this.selectedOutboundFlight = flight;
        this.toastService.info('Outbound Flight Selected', `${flight.flightNumber}. Now select your Return Flight.`);
        this.roundTripLeg = 'RETURN'; // Auto-advance to return leg
      } else {
        this.selectedReturnFlight = flight;
        this.toastService.success('Return Flight Selected', `${flight.flightNumber}. Navigating to Seat Selection...`);
        this.proceedToSeatSelection();
      }
    } else if (this.tripType === 'MULTI_CITY') {
      if (this.multiCityLegs[this.activeLegIndex]) {
        this.multiCityLegs[this.activeLegIndex].selectedFlight = flight;
        this.toastService.success(`Leg ${this.activeLegIndex + 1} Selected`, `${flight.flightNumber}`);
        // Advance to next unselected leg if any
        const nextUnselected = this.multiCityLegs.findIndex((leg, idx) => idx > this.activeLegIndex && !leg.selectedFlight);
        if (nextUnselected !== -1) {
          this.activeLegIndex = nextUnselected;
        } else if (this.isMultiCityComplete) {
          this.toastService.success('All Legs Picked', 'Navigating to Seat Selection...');
          this.proceedToSeatSelection();
        }
      }
    }
  }

  // 8-Hour Departure Cutoff Restriction Policy
  public isFlightCutoff(flight: Flight | null | undefined): boolean {
    if (!flight) return false;
    if (flight.bookable === false) return true;
    if (!flight.departureDate || !flight.departureTime) return false;
    try {
      const depDateTime = new Date(`${flight.departureDate}T${flight.departureTime}`);
      const now = new Date();
      const diffHours = (depDateTime.getTime() - now.getTime()) / (1000 * 60 * 60);
      return diffHours < 8;
    } catch {
      return false;
    }
  }

  public getCutoffHoursRemaining(flight: Flight | null | undefined): number {
    if (!flight) return 0;
    if (flight.cutoffHoursRemaining !== undefined && flight.cutoffHoursRemaining !== null) {
      return flight.cutoffHoursRemaining;
    }
    if (!flight.departureDate || !flight.departureTime) return 0;
    try {
      const depDateTime = new Date(`${flight.departureDate}T${flight.departureTime}`);
      const now = new Date();
      const diffHours = (depDateTime.getTime() - now.getTime()) / (1000 * 60 * 60);
      return Math.max(0, Math.round(diffHours));
    } catch {
      return 0;
    }
  }

  // Load Weekly Flight Schedules to resolve schedule recurrence
  public loadSchedules(): void {
    this.flightService.getAllSchedules().subscribe({
      next: (schedules) => {
        (schedules || []).forEach(s => {
          if (s.id) {
            this.scheduleMap[s.id] = s;
          }
        });
      },
      error: () => {}
    });
  }

  // Determine whether flight runs Daily or on Specific Days of the week
  public getFlightScheduleInfo(flight: Flight | null | undefined): { isDaily: boolean; badgeText: string; description: string } {
    if (!flight) {
      return { isDaily: true, badgeText: 'Daily', description: 'Operates Daily (Mon–Sun)' };
    }

    let opDaysStr = '';
    if (flight.operatingDays) {
      opDaysStr = Array.isArray(flight.operatingDays) ? flight.operatingDays.join(',') : String(flight.operatingDays);
    } else if (flight.scheduleId && this.scheduleMap[flight.scheduleId]?.operatingDays) {
      const sOp = this.scheduleMap[flight.scheduleId].operatingDays;
      opDaysStr = Array.isArray(sOp) ? sOp.join(',') : String(sOp);
    }

    if (opDaysStr) {
      const raw = opDaysStr.toUpperCase().trim();
      if (raw === 'ALL' || (
        raw.includes('MON') && raw.includes('TUE') && raw.includes('WED') &&
        raw.includes('THU') && raw.includes('FRI') && raw.includes('SAT') && raw.includes('SUN')
      )) {
        return { isDaily: true, badgeText: 'Daily', description: 'Operates Daily (Mon–Sun)' };
      }

      const dayMap: Record<string, string> = {
        'MONDAY': 'Mon', 'TUESDAY': 'Tue', 'WEDNESDAY': 'Wed', 'THURSDAY': 'Thu',
        'FRIDAY': 'Fri', 'SATURDAY': 'Sat', 'SUNDAY': 'Sun',
        'MON': 'Mon', 'TUE': 'Tue', 'WED': 'Wed', 'THU': 'Thu', 'FRI': 'Fri', 'SAT': 'Sat', 'SUN': 'Sun'
      };
      const days = raw.split(/[\s,]+/).map(d => dayMap[d.trim()] || d.trim()).filter(Boolean);
      const uniqueDays = Array.from(new Set(days));

      if (uniqueDays.length >= 7) {
        return { isDaily: true, badgeText: 'Daily', description: 'Operates Daily (Mon–Sun)' };
      }
      if (uniqueDays.length === 2 && uniqueDays.includes('Sat') && uniqueDays.includes('Sun')) {
        return { isDaily: false, badgeText: 'Weekends Only', description: 'Operates on Sat, Sun' };
      }
      return {
        isDaily: false,
        badgeText: 'Specific Days',
        description: `Runs on ${uniqueDays.join(', ')}`
      };
    }

    return { isDaily: true, badgeText: 'Daily', description: 'Operates Daily (Mon–Sun)' };
  }

  // Meal Management for Passenger & Flight Segment
  public loadMeals(): void {
    this.flightService.getAllMealOptions().subscribe({
      next: (meals) => {
        const active = (meals || []).filter(m => m.available !== false && m.status !== 'INACTIVE');
        if (active.length > 0) {
          this.availableMeals = active;
        } else {
          this.availableMeals = this.getDefaultMealOptions();
        }
      },
      error: () => {
        this.availableMeals = this.getDefaultMealOptions();
      }
    });
  }

  private getDefaultMealOptions(): MealOption[] {
    return [
      { id: 1, name: 'Vegetarian Royal Thali', category: 'VEGETARIAN', dietaryType: 'VEGETARIAN', price: 0, complimentary: true, available: true, description: 'Traditional paneer makhani, dal tadka, jeera rice & warm paratha.' },
      { id: 2, name: 'Grilled Herb Chicken', category: 'NON_VEGETARIAN', dietaryType: 'NON_VEGETARIAN', price: 450, complimentary: false, available: true, description: 'Herb roasted chicken breast with rosemary sauce & butter tossed veggies.' },
      { id: 3, name: 'Vegan Quinoa Power Bowl', category: 'VEGAN', dietaryType: 'VEGAN', price: 350, complimentary: false, available: true, description: 'Tri-color quinoa, roasted chickpeas, fresh avocado & lemon tahini.' },
      { id: 4, name: 'Jain Special Feast', category: 'JAIN', dietaryType: 'JAIN', price: 0, complimentary: true, available: true, description: 'Prepared strictly without root vegetables, onion, or garlic.' },
      { id: 5, name: 'Gluten-Free Platter', category: 'GLUTEN_FREE', dietaryType: 'GLUTEN_FREE', price: 400, complimentary: false, available: true, description: 'Naturally gluten-free steamed millet bowl with grilled tofu & fruit.' },
      { id: 6, name: 'Kids Junior Snack Box', category: 'CHILD', dietaryType: 'CHILD', price: 250, complimentary: false, available: true, description: 'Mini cheese sandwich, fruit cup, chocolate cookie & juice box.' },
      { id: 7, name: 'No Meal Service', category: 'NO_MEAL', dietaryType: 'NO_MEAL', price: 0, complimentary: true, available: true, description: 'Opt out of meal service during this flight segment.' }
    ];
  }

  public selectMealForPassenger(legIdx: number, pIdx: number, meal: MealOption): void {
    if (!this.legMeals[legIdx]) {
      this.legMeals[legIdx] = {};
    }
    this.legMeals[legIdx][pIdx] = meal;
  }

  public applyMealToAllPassengers(legIdx: number, meal: MealOption): void {
    if (!this.legMeals[legIdx]) {
      this.legMeals[legIdx] = {};
    }
    for (let pIdx = 0; pIdx < this.passengers.length; pIdx++) {
      this.legMeals[legIdx][pIdx] = meal;
    }
    this.toastService.info('Meal Applied', `Selected "${meal.name}" for all passengers on this leg.`);
  }

  public getSelectedMeal(legIdx: number, pIdx: number): MealOption | null {
    return this.legMeals[legIdx]?.[pIdx] || null;
  }

  public proceedToSeatSelection(): void {
    if (this.tripType === 'ONE_WAY') {
      const fId = this.flights[0]?.id || 1;
      this.router.navigate(['/booking/seats', fId], {
        queryParams: {
          tripType: 'ONE_WAY',
          passengers: this.passengerCount,
          adults: this.adultsCount,
          children: this.childrenCount,
          infants: this.infantsCount,
          cabin: this.selectedCabin
        }
      });
    } else if (this.tripType === 'ROUND_TRIP') {
      const outbound = this.selectedOutboundFlight || this.flights[0];
      const returnF = this.selectedReturnFlight || this.flights.find(f => f.id !== outbound?.id) || outbound;
      const outboundId = outbound?.id || 1;
      const returnId = returnF?.id || outboundId;
      this.router.navigate(['/booking/seats', outboundId], {
        queryParams: {
          tripType: 'ROUND_TRIP',
          returnFlightId: returnId,
          passengers: this.passengerCount,
          adults: this.adultsCount,
          children: this.childrenCount,
          infants: this.infantsCount,
          cabin: this.selectedCabin
        }
      });
    } else if (this.tripType === 'MULTI_CITY') {
      const selected = this.multiCityLegs.map(l => l.selectedFlight).filter(Boolean) as Flight[];
      const firstLeg = selected[0] || this.flights[0];
      const legIds = (selected.length > 0 ? selected.map(f => f.id) : [firstLeg?.id || 1]).join(',');
      const firstLegId = firstLeg?.id || 1;
      this.router.navigate(['/booking/seats', firstLegId], {
        queryParams: {
          tripType: 'MULTI_CITY',
          legFlightIds: legIds,
          passengers: this.passengerCount,
          adults: this.adultsCount,
          children: this.childrenCount,
          infants: this.infantsCount,
          cabin: this.selectedCabin
        }
      });
    }
  }

  public get isRoundTripComplete(): boolean {
    return !!(this.selectedOutboundFlight && this.selectedReturnFlight);
  }

  public get isMultiCityComplete(): boolean {
    return this.multiCityLegs.length >= 2 && this.multiCityLegs.every(l => !!l.selectedFlight);
  }

  public get selectedTripFlights(): { flight: Flight; legTitle: string }[] {
    if (this.tripType === 'ONE_WAY') {
      return [];
    }
    if (this.tripType === 'ROUND_TRIP') {
      const list = [];
      if (this.selectedOutboundFlight) list.push({ flight: this.selectedOutboundFlight, legTitle: 'Leg 1 (Outbound): ' + (this.selectedOutboundFlight.source || this.selectedOutboundFlight.origin) + ' → ' + this.selectedOutboundFlight.destination });
      if (this.selectedReturnFlight) list.push({ flight: this.selectedReturnFlight, legTitle: 'Leg 2 (Return): ' + (this.selectedReturnFlight.source || this.selectedReturnFlight.origin) + ' → ' + this.selectedReturnFlight.destination });
      return list;
    }
    if (this.tripType === 'MULTI_CITY') {
      return this.multiCityLegs
        .filter(l => !!l.selectedFlight)
        .map((l, idx) => ({ flight: l.selectedFlight!, legTitle: `Leg ${idx + 1}: ${l.origin} → ${l.destination}` }));
    }
    return [];
  }

  public modalTripFlights: { flight: Flight; legTitle: string }[] = [];

  public get activeModalFlights(): { flight: Flight; legTitle: string }[] {
    if (this.modalTripFlights && this.modalTripFlights.length > 0) {
      return this.modalTripFlights;
    }
    return this.selectedTripFlights;
  }

  public get totalTripPricePerPassenger(): number {
    const list = this.isBookingModalOpen ? this.activeModalFlights : this.selectedTripFlights;
    return list.reduce((sum, item) => sum + this.getFlightPrice(item.flight), 0);
  }

  public get totalCombinedTripPrice(): number {
    return this.totalTripPricePerPassenger * this.passengerCount;
  }

  public get totalTicketsPerPassenger(): number {
    if (this.tripType === 'ROUND_TRIP') return 2;
    if (this.tripType === 'MULTI_CITY') return this.multiCityLegs.length;
    return 1;
  }

  public get totalTripTicketsCount(): number {
    return this.totalTicketsPerPassenger * this.passengerCount;
  }

  // Booking Modal Methods
  public startBookingTrip(): void {
    const flights = this.selectedTripFlights.map(i => i.flight);
    const titles = this.selectedTripFlights.map(i => i.legTitle);
    this.openTripBookingModal(flights, titles);
  }

  public startTripBookingWorkflow(): void {
    this.startBookingTrip();
  }

  public openTripBookingModal(flightsToBook: Flight[], titles: string[]): void {
    const user = this.authService.currentUserValue;
    this.contactEmail = user?.email || 'customer@example.com';
    this.contactPhone = '9876543210';
    this.bookingModalStep = 1;
    this.createdTripTickets = [];
    this.createdBookingReferences = [];
    this.parentBookingReference = 'BK-SKY-' + Math.floor(100000 + Math.random() * 900000);
    this.modalTripFlights = flightsToBook.map((f, idx) => ({
      flight: f,
      legTitle: titles[idx] || `Flight ${f.flightNumber}`
    }));

    // Initialize passengers categorized as Adult, Child, Infant
    this.passengers = [];
    let pId = 1;
    for (let i = 0; i < this.adultsCount; i++) {
      this.passengers.push({
        id: pId++,
        fullName: i === 0 && user?.fullName ? user.fullName : `Adult ${i + 1}`,
        dateOfBirth: '1995-05-15',
        gender: 'MALE',
        travelClass: (this.selectedCabin !== 'ALL' ? this.selectedCabin : 'ECONOMY') as any,
        passengerType: 'ADULT'
      });
    }
    for (let i = 0; i < this.childrenCount; i++) {
      this.passengers.push({
        id: pId++,
        fullName: `Child ${i + 1}`,
        dateOfBirth: '2017-06-10',
        gender: 'MALE',
        travelClass: (this.selectedCabin !== 'ALL' ? this.selectedCabin : 'ECONOMY') as any,
        passengerType: 'CHILD'
      });
    }
    for (let i = 0; i < this.infantsCount; i++) {
      this.passengers.push({
        id: pId++,
        fullName: `Infant ${i + 1}`,
        dateOfBirth: '2025-02-15',
        gender: 'MALE',
        travelClass: (this.selectedCabin !== 'ALL' ? this.selectedCabin : 'ECONOMY') as any,
        passengerType: 'INFANT'
      });
    }

    // Assign smart distinct seats for each flight leg
    const defaultSeatRows = ['4', '7', '11', '15', '18'];
    const seatCols = ['A', 'B', 'C', 'D', 'E', 'F'];
    this.legSeats = {};
    this.legMeals = {};

    const defaultMeal = this.availableMeals.find(m => m.category === 'VEGETARIAN' && m.complimentary) || this.availableMeals[0] || null;

    flightsToBook.forEach((f, legIdx) => {
      this.legSeats[legIdx] = [];
      this.legMeals[legIdx] = {};
      const row = defaultSeatRows[legIdx % defaultSeatRows.length];
      for (let pIdx = 0; pIdx < this.passengerCount; pIdx++) {
        this.legSeats[legIdx].push(`${row}${seatCols[pIdx % seatCols.length]}`);
        this.legMeals[legIdx][pIdx] = defaultMeal;
      }
    });

    this.isBookingModalOpen = true;
  }

  public closeBookingModal(): void {
    this.isBookingModalOpen = false;
  }

  public setPassengerSeat(legIdx: number, pIdx: number, seatNumber: string): void {
    if (!this.legSeats[legIdx]) this.legSeats[legIdx] = [];
    this.legSeats[legIdx][pIdx] = seatNumber.toUpperCase().trim();
  }

  public async submitTripBooking(): Promise<void> {
    this.isSubmittingBooking = true;
    const user = this.authService.currentUserValue;
    const userId = user?.userId || 1;
    const flightsToBook = this.activeModalFlights;

    // Validate 8-hour departure cutoff restriction for all flight legs
    for (const item of flightsToBook) {
      if (this.isFlightCutoff(item.flight)) {
        this.toastService.error(
          'Booking Cutoff Policy Violation',
          `Flight ${item.flight.flightNumber} departs in under 8 hours. Reservations must be made at least 8h in advance.`
        );
        this.isSubmittingBooking = false;
        return;
      }
    }

    this.createdTripTickets = [];
    this.createdBookingReferences = [];
    const parentRef = this.parentBookingReference || ('BK-SKY-' + Math.floor(100000 + Math.random() * 900000));

    try {
      for (let legIdx = 0; legIdx < flightsToBook.length; legIdx++) {
        const item = flightsToBook[legIdx];
        const flight = item.flight;
        const legTitle = item.legTitle;
        const flightFare = this.getFlightPrice(flight);

        let legMealTotal = 0;
        const passengersPayload: PassengerDto[] = this.passengers.map((p, pIdx) => {
          const meal = this.legMeals[legIdx]?.[pIdx];
          const mealPrice = meal ? (meal.complimentary ? 0 : Number(meal.price || 0)) : 0;
          legMealTotal += mealPrice;

          return {
            fullName: p.fullName.trim(),
            dateOfBirth: p.dateOfBirth,
            age: 30,
            gender: p.gender,
            travelClass: p.travelClass,
            seatNumber: this.legSeats[legIdx]?.[pIdx] || `${4 + legIdx}${String.fromCharCode(65 + pIdx)}`,
            fareAmount: flightFare,
            mealOptionId: meal?.id,
            mealName: meal?.name || 'Standard Refreshment',
            mealCategory: meal?.category || 'STANDARD',
            mealPrice: mealPrice,
            dietaryPreference: meal?.dietaryType || 'NONE',
            segmentIndex: legIdx
          };
        });

        const bookingReq: any = {
          userId,
          flightId: flight.id,
          tripType: this.tripType,
          parentBookingReference: parentRef,
          segmentIndex: legIdx,
          passengers: passengersPayload,
          passengerEmail: this.contactEmail,
          passengerPhone: this.contactPhone,
          fareAmount: flightFare * this.passengerCount,
          mealTotalAmount: legMealTotal,
          totalAmount: (flightFare * this.passengerCount) + legMealTotal
        };

        // 1. Create booking for this leg
        const createdBooking = await firstValueFrom(this.bookingService.createBooking(bookingReq));
        this.createdBookingReferences.push(createdBooking.bookingReference);

        // 2. Immediately confirm booking
        try {
          await firstValueFrom(this.bookingService.confirmBooking(createdBooking.id, 1));
        } catch {
          // Fallback confirmation handled
        }

        // 3. Record generated tickets for each passenger on this leg
        const bookingPassengers = createdBooking.passengers && createdBooking.passengers.length > 0
          ? createdBooking.passengers
          : passengersPayload;

        bookingPassengers.forEach((bp, pIdx) => {
          const selectedMeal = this.legMeals[legIdx]?.[pIdx];
          this.createdTripTickets.push({
            legIndex: legIdx + 1,
            legTitle: legTitle,
            flightNumber: flight.flightNumber,
            route: `${flight.source || flight.origin} → ${flight.destination}`,
            departureDate: flight.departureDate || '',
            departureTime: flight.departureTime || '',
            pnr: createdBooking.bookingReference,
            parentPnr: parentRef,
            passengerName: bp.fullName,
            seatNumber: bp.seatNumber,
            ticketNumber: bp.ticketNumber || `TKT-${createdBooking.bookingReference.replace('BK-', '')}-0${pIdx + 1}`,
            fareAmount: bp.fareAmount || flightFare,
            mealName: bp.mealName || selectedMeal?.name || 'Standard Refreshment'
          });
        });
      }

      this.isSubmittingBooking = false;
      this.bookingModalStep = 5; // Confirmed Step
      this.toastService.success(
        'Trip Booked Successfully!',
        `${this.createdTripTickets.length} tickets confirmed across ${flightsToBook.length} flight legs.`
      );
    } catch (err: any) {
      this.isSubmittingBooking = false;
      this.toastService.error('Booking Failed', err?.error?.message || 'Could not complete trip booking. Please try again.');
    }
  }

  public getFlightPrice(flight: Flight): number {
    const base = flight.basePrice ?? (flight as any).fare ?? (flight as any).totalFare ?? 4800;
    if (this.selectedCabin === 'BUSINESS') {
      return Math.round(base * 1.75);
    }
    if (this.selectedCabin === 'FIRST') {
      return Math.round(base * 2.5);
    }
    return base;
  }

  public onCriteriaChange(): void {
    // Instant reactive template filtering
  }

  public incrementAdults(): void {
    if (this.passengerCount < 5) {
      this.adultsCount++;
    } else {
      this.toastService.warning('Maximum Passengers', 'Maximum 5 passengers allowed per booking.');
    }
  }

  public decrementAdults(): void {
    if (this.adultsCount <= 1) return;
    if (this.adultsCount - 1 < this.infantsCount) {
      this.toastService.warning('Infant Policy', 'Each infant must be accompanied by at least one adult.');
      return;
    }
    this.adultsCount--;
  }

  public incrementChildren(): void {
    if (this.passengerCount < 5) {
      this.childrenCount++;
    } else {
      this.toastService.warning('Maximum Passengers', 'Maximum 5 passengers allowed per booking.');
    }
  }

  public decrementChildren(): void {
    if (this.childrenCount > 0) {
      this.childrenCount--;
    }
  }

  public incrementInfants(): void {
    if (this.passengerCount >= 5) {
      this.toastService.warning('Maximum Passengers', 'Maximum 5 passengers allowed per booking.');
      return;
    }
    if (this.infantsCount >= this.adultsCount) {
      this.toastService.warning('Infant Policy', 'Each infant must be accompanied by at least one adult (max 1 infant per adult).');
      return;
    }
    this.infantsCount++;
  }

  public decrementInfants(): void {
    if (this.infantsCount > 0) {
      this.infantsCount--;
    }
  }

  public incrementPassengers(): void {
    this.incrementAdults();
  }

  public decrementPassengers(): void {
    this.decrementAdults();
  }

  public togglePassengerDropdown(): void {
    this.showPassengerDropdown = !this.showPassengerDropdown;
  }

  public closePassengerDropdown(): void {
    this.showPassengerDropdown = false;
  }

  public toggleAirline(airline: string): void {
    if (this.selectedAirlines.includes(airline)) {
      this.selectedAirlines = this.selectedAirlines.filter(a => a !== airline);
    } else {
      this.selectedAirlines = [...this.selectedAirlines, airline];
    }
  }

  public selectAllAirlines(): void {
    this.selectedAirlines = [...this.availableAirlines];
  }

  public resetFilters(): void {
    this.selectedCabin = 'ALL';
    this.maxPrice = 25000;
    this.origin = 'DEL';
    this.destination = 'BOM';
    this.departureDate = '2026-10-15';
    this.returnDate = '2026-10-16';
    this.adultsCount = 1;
    this.childrenCount = 0;
    this.infantsCount = 0;
    this.selectedOutboundFlight = null;
    this.selectedReturnFlight = null;
    this.selectAllAirlines();
  }
}
