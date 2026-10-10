import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { BookingService } from '../../../core/services/booking.service';
import { FlightService } from '../../../core/services/flight.service';
import { AuthService } from '../../../core/services/auth.service';
import { Flight, MealOption } from '../../../core/models/flight.models';
import { PassengerDto } from '../../../core/models/booking.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-passenger-details',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, CurrencyFormatterPipe],
  templateUrl: './passenger-details.component.html',
  styleUrl: './passenger-details.component.css'
})
export class PassengerDetailsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private bookingService = inject(BookingService);
  private flightService = inject(FlightService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  public flightId = 1;
  public flight: Flight | null = null;
  public isSubmitting = false;

  // In-Flight Dining & Meals
  public availableMeals: MealOption[] = [];
  public isMealsLoading = false;
  public selectedMeals: { [legIdx: number]: { [pIdx: number]: MealOption } } = {};

  // Trip Mode & Multi-Leg Support
  public tripType: 'ONE_WAY' | 'ROUND_TRIP' | 'MULTI_CITY' = 'ONE_WAY';
  public tripLegs: any[] = [];
  public passengersRawData: any[] = [];
  public totalTripAmount = 0;

  public bookingForm: FormGroup = this.fb.group({
    contactEmail: ['', [Validators.required, Validators.email]],
    contactPhone: ['', [Validators.required, Validators.pattern(/^[0-9+ -]{8,15}$/)]],
    passengers: this.fb.array([])
  });

  public get passengersArray(): FormArray {
    return this.bookingForm.get('passengers') as FormArray;
  }

  public get totalCombinedTripFare(): number {
    if (this.totalTripAmount > 0) return this.totalTripAmount;
    const multiplier = this.tripLegs.length > 1 ? this.tripLegs.length : 1;
    return this.totalCombinedFare * multiplier;
  }

  public get totalTripTicketsCount(): number {
    const legs = this.tripLegs.length > 1 ? this.tripLegs.length : 1;
    return legs * this.passengersArray.length;
  }

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    if (user) {
      this.bookingForm.patchValue({
        contactEmail: user.email || '',
        contactPhone: '9876543210'
      });
    }

    this.route.queryParams.subscribe(params => {
      if (params['tripType']) {
        this.tripType = params['tripType'];
      }
      if (params['totalAmount']) {
        this.totalTripAmount = +params['totalAmount'];
      }
      if (params['tripLegsData']) {
        try {
          this.tripLegs = JSON.parse(params['tripLegsData']);
        } catch {
          this.tripLegs = [];
        }
      }

      if (params['flightId']) {
        this.flightId = +params['flightId'];
        this.flightService.getFlightById(this.flightId).subscribe(f => this.flight = f);
      }

      this.loadMeals();

      // Check if pre-configured passengers/seats passed from seat-selection
      if (params['passengersData']) {
        try {
          const parsed: any[] = JSON.parse(params['passengersData']);
          this.passengersRawData = parsed;
          this.passengersArray.clear();
          parsed.forEach(p => {
            this.addPassengerGroup(
              p.fullName,
              p.dateOfBirth,
              p.gender,
              p.travelClass,
              p.seatNumber,
              p.fareAmount,
              p.passengerType || 'ADULT'
            );
          });
        } catch (e) {
          this.initializeDefaultPassenger(params);
        }
      } else {
        this.initializeDefaultPassenger(params);
      }
    });
  }

  private initializeDefaultPassenger(params: any): void {
    this.passengersArray.clear();
    const user = this.authService.currentUserValue;
    const seatNumber = params['seatNumber'] || '1A';
    const totalAmount = +params['totalAmount'] || 5000;
    const travelClass = params['travelClass'] || 'ECONOMY';

    this.addPassengerGroup(
      user?.fullName || '',
      '1995-05-15',
      'MALE',
      travelClass,
      seatNumber,
      totalAmount,
      'ADULT'
    );
  }

  public addPassengerGroup(
    fullName = '',
    dob = '',
    gender = 'MALE',
    travelClass = 'ECONOMY',
    seatNumber = '',
    fareAmount = 5000,
    passengerType: 'ADULT' | 'CHILD' | 'INFANT' = 'ADULT'
  ): void {
    if (this.passengersArray.length >= 5) {
      this.toastService.warning('Maximum Limit', 'A maximum of 5 passengers can be booked together in one PNR.');
      return;
    }

    if (!dob) {
      if (passengerType === 'CHILD') dob = '2017-06-10';
      else if (passengerType === 'INFANT') dob = '2025-02-15';
      else dob = '1995-05-15';
    }

    const age = this.calculateAge(dob);
    const pGroup = this.fb.group({
      fullName: [fullName, [Validators.required, Validators.minLength(3)]],
      dateOfBirth: [dob, Validators.required],
      age: [age],
      gender: [gender, Validators.required],
      travelClass: [travelClass, Validators.required],
      seatNumber: [seatNumber, Validators.required],
      fareAmount: [fareAmount],
      passengerType: [passengerType, Validators.required]
    });

    // Update age whenever DOB changes
    pGroup.get('dateOfBirth')?.valueChanges.subscribe(val => {
      const calculatedAge = this.calculateAge(val);
      pGroup.get('age')?.setValue(calculatedAge, { emitEvent: false });
    });

    // Auto-align DOB when passengerType is explicitly changed
    pGroup.get('passengerType')?.valueChanges.subscribe(type => {
      const curAge = pGroup.get('age')?.value || 0;
      if (type === 'INFANT' && curAge >= 2) {
        pGroup.get('dateOfBirth')?.setValue('2025-02-15');
      } else if (type === 'CHILD' && (curAge < 2 || curAge >= 12)) {
        pGroup.get('dateOfBirth')?.setValue('2017-06-10');
      } else if (type === 'ADULT' && curAge < 12) {
        pGroup.get('dateOfBirth')?.setValue('1995-05-15');
      }
    });

    // Update fare if class changes
    pGroup.get('travelClass')?.valueChanges.subscribe(newClass => {
      const updatedFare = this.calculateClassFare(newClass);
      pGroup.get('fareAmount')?.setValue(updatedFare, { emitEvent: false });
    });

    this.passengersArray.push(pGroup);
  }

  public removePassenger(index: number): void {
    if (this.passengersArray.length <= 1) {
      this.toastService.warning('Minimum Limit', 'At least 1 passenger is required for booking.');
      return;
    }
    this.passengersArray.removeAt(index);
  }

  public addNewPassenger(): void {
    if (this.passengersArray.length >= 5) {
      this.toastService.warning('Limit Reached', 'Up to 5 passengers can be added.');
      return;
    }
    const idx = this.passengersArray.length + 1;
    this.addPassengerGroup('', '1998-01-01', 'MALE', 'ECONOMY', 'Auto-' + idx, 5000, 'ADULT');
  }

  public calculateAge(dobStr: string): number {
    if (!dobStr) return 25;
    try {
      const dob = new Date(dobStr);
      const today = new Date();
      let age = today.getFullYear() - dob.getFullYear();
      const m = today.getMonth() - dob.getMonth();
      if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) {
        age--;
      }
      return age > 0 ? age : 1;
    } catch {
      return 25;
    }
  }

  public calculateClassFare(travelClass: string): number {
    const base = this.flight?.basePrice || 5000;
    if (travelClass === 'BUSINESS') return Math.round(base * 1.75);
    if (travelClass === 'PREMIUM_ECONOMY') return Math.round(base * 1.35);
    return base;
  }

  public get totalCombinedFare(): number {
    let sum = 0;
    for (let i = 0; i < this.passengersArray.length; i++) {
      const val = this.passengersArray.at(i).value;
      sum += (val.fareAmount || this.calculateClassFare(val.travelClass));
    }
    return sum;
  }

  // --- Meal Loading & Configuration ---
  public loadMeals(): void {
    this.isMealsLoading = true;
    this.flightService.getMealsByFlight(this.flightId).subscribe({
      next: (meals) => {
        if (meals && meals.length > 0) {
          this.availableMeals = meals;
        } else {
          this.loadAllMealsFallback();
        }
        this.isMealsLoading = false;
        this.initPassengerMeals();
      },
      error: () => {
        this.loadAllMealsFallback();
      }
    });
  }

  private loadAllMealsFallback(): void {
    this.flightService.getAllMealOptions().subscribe({
      next: (meals) => {
        this.availableMeals = meals && meals.length > 0 ? meals : this.getDefaultMealOptions();
        this.isMealsLoading = false;
        this.initPassengerMeals();
      },
      error: () => {
        this.availableMeals = this.getDefaultMealOptions();
        this.isMealsLoading = false;
        this.initPassengerMeals();
      }
    });
  }

  private initPassengerMeals(): void {
    if (!this.availableMeals || this.availableMeals.length === 0) return;
    const defaultMeal = this.availableMeals.find(m => m.complimentary || m.price === 0) || this.availableMeals[0];
    const numLegs = this.tripLegs && this.tripLegs.length > 0 ? this.tripLegs.length : 1;
    for (let leg = 0; leg < numLegs; leg++) {
      if (!this.selectedMeals[leg]) this.selectedMeals[leg] = {};
      for (let p = 0; p < this.passengersArray.length; p++) {
        if (!this.selectedMeals[leg][p]) {
          this.selectedMeals[leg][p] = defaultMeal;
        }
      }
    }
  }

  public selectMeal(legIdx: number, pIdx: number, meal: MealOption): void {
    if (!this.selectedMeals[legIdx]) this.selectedMeals[legIdx] = {};
    this.selectedMeals[legIdx][pIdx] = meal;
  }

  public applyMealToAllPassengers(legIdx: number, meal: MealOption): void {
    if (!this.selectedMeals[legIdx]) this.selectedMeals[legIdx] = {};
    for (let p = 0; p < this.passengersArray.length; p++) {
      this.selectedMeals[legIdx][p] = meal;
    }
    this.toastService.info('Meal Applied', `Selected "${meal.name}" for all passengers on this flight.`);
  }

  public getPassengerMeal(legIdx: number, pIdx: number): MealOption | null {
    if (this.selectedMeals[legIdx]?.[pIdx]) {
      return this.selectedMeals[legIdx][pIdx];
    }
    return this.availableMeals.length > 0 ? this.availableMeals[0] : null;
  }

  public get totalMealsAmount(): number {
    let total = 0;
    const numLegs = this.tripLegs && this.tripLegs.length > 0 ? this.tripLegs.length : 1;
    for (let leg = 0; leg < numLegs; leg++) {
      for (let p = 0; p < this.passengersArray.length; p++) {
        const m = this.getPassengerMeal(leg, p);
        if (m && !m.complimentary && m.price) {
          total += m.price;
        }
      }
    }
    return total;
  }

  public get grandTotalAmount(): number {
    return this.totalCombinedTripFare + this.totalMealsAmount;
  }

  public getDietaryIcon(category?: string): string {
    const c = (category || '').toUpperCase();
    if (c.includes('VEGETARIAN') && !c.includes('NON')) return '🌱';
    if (c.includes('NON_VEGETARIAN') || c.includes('CHICKEN') || c.includes('MEAT')) return '🍗';
    if (c.includes('VEGAN')) return '🌿';
    if (c.includes('JAIN')) return '🥗';
    if (c.includes('GLUTEN')) return '🌾';
    if (c.includes('CHILD') || c.includes('KID')) return '👶';
    if (c.includes('NO_MEAL') || c.includes('NONE')) return '🚫';
    return '🍱';
  }

  public getDietaryBadgeClass(category?: string): string {
    const c = (category || '').toUpperCase();
    if (c.includes('VEGETARIAN') && !c.includes('NON')) return 'bg-emerald-50 text-emerald-800 border-emerald-200';
    if (c.includes('NON_VEGETARIAN')) return 'bg-rose-50 text-rose-800 border-rose-200';
    if (c.includes('VEGAN')) return 'bg-teal-50 text-teal-800 border-teal-200';
    if (c.includes('JAIN')) return 'bg-amber-50 text-amber-800 border-amber-200';
    if (c.includes('GLUTEN')) return 'bg-orange-50 text-orange-800 border-orange-200';
    if (c.includes('CHILD')) return 'bg-purple-50 text-purple-800 border-purple-200';
    return 'bg-slate-100 text-slate-700 border-slate-200';
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

  public isInvalid(formGroupName: string, controlName: string): boolean {
    const group = this.bookingForm.get(formGroupName);
    if (!group) return false;
    const ctrl = group.get(controlName);
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  }

  public isPassengerInvalid(pIndex: number, controlName: string): boolean {
    const pGroup = this.passengersArray.at(pIndex);
    if (!pGroup) return false;
    const ctrl = pGroup.get(controlName);
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  }

  public onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      this.toastService.error('Form Incomplete', 'Please fill all required passenger details (Name, DOB, Seats).');
      return;
    }

    this.isSubmitting = true;
    const currentUser = this.authService.currentUserValue;
    const formVal = this.bookingForm.value;

    // Check if Round Trip or Multi-City with multiple legs
    if (this.tripLegs && this.tripLegs.length > 1) {
      const parentRef = `BK-SKY-${Math.floor(100000 + Math.random() * 900000)}`;

      const legObservables = this.tripLegs.map((leg, legIdx) => {
        const legPassengers: PassengerDto[] = formVal.passengers.map((p: any, pIdx: number) => {
          const assignedSeat = this.passengersRawData[pIdx]?.legSeats?.[legIdx] || p.seatNumber;
          const chosenMeal = this.getPassengerMeal(legIdx, pIdx);
          return {
            fullName: p.fullName.trim(),
            dateOfBirth: p.dateOfBirth,
            age: p.age || this.calculateAge(p.dateOfBirth),
            gender: p.gender,
            travelClass: p.travelClass,
            seatNumber: assignedSeat,
            fareAmount: p.fareAmount || this.calculateClassFare(p.travelClass),
            mealOptionId: chosenMeal?.id,
            mealName: chosenMeal?.name,
            mealCategory: chosenMeal?.category,
            mealPrice: chosenMeal?.complimentary ? 0 : (chosenMeal?.price || 0),
            dietaryPreference: chosenMeal?.dietaryType || chosenMeal?.category,
            segmentIndex: legIdx
          };
        });

        const legFare = legPassengers.reduce((sum, p) => sum + (p.fareAmount || 5000), 0);
        const legMealTotal = legPassengers.reduce((sum, p) => sum + (p.mealPrice || 0), 0);

        return this.bookingService.createBooking({
          userId: currentUser?.userId || 1,
          flightId: leg.flightId,
          passengers: legPassengers,
          passengerEmail: formVal.contactEmail,
          passengerPhone: formVal.contactPhone,
          fareAmount: legFare + legMealTotal,
          mealTotalAmount: legMealTotal,
          tripType: this.tripType,
          parentBookingReference: parentRef,
          segmentIndex: legIdx
        });
      });

      forkJoin(legObservables).subscribe({
        next: (results) => {
          this.isSubmitting = false;
          const totalTickets = results.reduce((sum, r) => sum + (r.passengers?.length || 0), 0);
          const pnrs = results.map(r => r.bookingReference).join(', ');

          if (this.tripType === 'ROUND_TRIP') {
            this.toastService.success(
              'Round Trip Confirmed!',
              `2 tickets issued per passenger (${totalTickets} tickets total). PNRs: ${pnrs}`
            );
          } else {
            this.toastService.success(
              'Multi-City Trip Confirmed!',
              `${this.tripLegs.length} flight legs booked (${totalTickets} tickets total). PNRs: ${pnrs}`
            );
          }

          const totalAmount = results.reduce((sum, r) => sum + (r.fareAmount || 0), 0);
          const primaryRef = results[0]?.bookingReference || '';
          const primaryId = results[0]?.id || 0;
          const allRefs = results.map(r => r.bookingReference).join(',');

          this.router.navigate(['/booking/checkout'], {
            queryParams: {
              bookingId: primaryId,
              bookingReference: primaryRef,
              allBookingReferences: allRefs,
              amount: totalAmount,
              tripType: this.tripType
            }
          });
        },
        error: (err) => {
          this.isSubmitting = false;
          const msg = err.error?.message || 'Failed to complete reservations for all flight legs. Please try again.';
          this.toastService.error('Booking Failed', msg);
        }
      });
      return;
    }

    // Default: One Way Single Flight Booking
    const passengersPayload: PassengerDto[] = formVal.passengers.map((p: any, pIdx: number) => {
      const chosenMeal = this.getPassengerMeal(0, pIdx);
      return {
        fullName: p.fullName.trim(),
        dateOfBirth: p.dateOfBirth,
        age: p.age || this.calculateAge(p.dateOfBirth),
        gender: p.gender,
        travelClass: p.travelClass,
        seatNumber: p.seatNumber,
        fareAmount: p.fareAmount || this.calculateClassFare(p.travelClass),
        mealOptionId: chosenMeal?.id,
        mealName: chosenMeal?.name,
        mealCategory: chosenMeal?.category,
        mealPrice: chosenMeal?.complimentary ? 0 : (chosenMeal?.price || 0),
        dietaryPreference: chosenMeal?.dietaryType || chosenMeal?.category,
        segmentIndex: 0
      };
    });

    const totalMealPrice = passengersPayload.reduce((sum, p) => sum + (p.mealPrice || 0), 0);
    const totalAmountPayable = this.totalCombinedFare + totalMealPrice;

    this.bookingService.createBooking({
      userId: currentUser?.userId || 1,
      flightId: this.flightId,
      passengers: passengersPayload,
      passengerEmail: formVal.contactEmail,
      passengerPhone: formVal.contactPhone,
      fareAmount: totalAmountPayable,
      mealTotalAmount: totalMealPrice,
      tripType: 'ONE_WAY'
    }).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.toastService.success('Seats Held!', `PNR: ${res.bookingReference}. Please complete payment to confirm booking.`);
        this.router.navigate(['/booking/checkout'], {
          queryParams: {
            bookingId: res.id,
            bookingReference: res.bookingReference,
            amount: res.fareAmount
          }
        });
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Failed to create multi-passenger booking. Please try again.';
        this.toastService.error('Booking Failed', msg);
      }
    });
  }
}
