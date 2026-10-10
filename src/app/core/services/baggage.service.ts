import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { BaggageRecord, BaggageStatus, BaggageType, NewBaggageCheckinRequest, SpecialHandlingType } from '../models/baggage.models';
import { BookingService } from './booking.service';
import { FlightService } from './flight.service';
import { BookingResponse } from '../models/booking.models';
import { Flight } from '../models/flight.models';

@Injectable({
  providedIn: 'root'
})
export class BaggageService {
  private http = inject(HttpClient);
  private bookingService = inject(BookingService);
  private flightService = inject(FlightService);

  private readonly STORAGE_KEY = 'skywings_airport_baggage_records';
  private readonly CHECKIN_API_URL = 'http://localhost:8080/api/checkins';

  constructor() {}

  /**
   * Retrieves all Baggage Records, combining persistent local overrides with live bookings & flights.
   */
  public getBaggageRecords(): Observable<BaggageRecord[]> {
    const cached = this.getCachedRecords();
    if (cached && cached.length > 0) {
      return of(cached);
    }

    // If cache is empty, seed from live bookings and initial baseline
    return this.bookingService.getAllBookings().pipe(
      catchError(() => of([])),
      map((bookings) => {
        const generated = this.generateInitialRecords(bookings);
        this.saveCachedRecords(generated);
        return generated;
      })
    );
  }

  /**
   * Saves or updates an existing baggage record.
   */
  public saveBaggageRecord(record: BaggageRecord): Observable<BaggageRecord> {
    const list = this.getCachedRecords();
    const index = list.findIndex(r => String(r.id) === String(record.id) || r.bagTagNumber === record.bagTagNumber);

    // Recalculate weights & fees
    const allowance = Number(record.baggageAllowance) || 15;
    const actual = Number(record.actualBaggageWeight) || 0;
    const extraWeight = Math.max(0, +(actual - allowance).toFixed(1));
    const extraFee = Math.round(extraWeight * 500); // Standard ₹500/kg excess fee

    const updated: BaggageRecord = {
      ...record,
      baggageAllowance: allowance,
      baggageAllowanceDisplay: `${allowance} kg`,
      actualBaggageWeight: actual,
      extraBaggageWeight: extraWeight,
      extraBaggageFee: extraFee,
      bagsAllowedDisplay: `${record.bagsAllowed || 1} ${record.bagsAllowed === 1 ? 'bag' : 'bags'}`,
      lastUpdated: new Date().toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })
    };

    if (index >= 0) {
      list[index] = updated;
    } else {
      list.unshift(updated);
    }

    this.saveCachedRecords(list);

    // Also attempt background sync with backend checkin service if available
    if (record.id && typeof record.id === 'number') {
      this.http.put(`${this.CHECKIN_API_URL}/${record.id}/baggage`, {
        baggageCount: record.bagsAllowed,
        baggageWeight: record.actualBaggageWeight
      }).pipe(catchError(() => of(null))).subscribe();
    }

    return of(updated);
  }

  /**
   * Quick status transition for ground crew (e.g. Checked In -> Loaded -> Delivered)
   */
  public updateStatus(id: string | number, newStatus: BaggageStatus): Observable<BaggageRecord | null> {
    const list = this.getCachedRecords();
    const record = list.find(r => String(r.id) === String(id));
    if (!record) return of(null);

    record.baggageStatus = newStatus;
    if (newStatus === 'CHECKED_IN' && (!record.checkInTime || record.checkInTime === 'Pending')) {
      record.checkInTime = new Date().toLocaleString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      });
    }
    record.lastUpdated = new Date().toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });

    this.saveCachedRecords(list);
    return of(record);
  }

  /**
   * Ground staff check-in from counter
   */
  public checkInAtCounter(req: NewBaggageCheckinRequest): Observable<BaggageRecord> {
    const list = this.getCachedRecords();
    const cleanFlight = req.flightNumber.replace(/[^A-Za-z0-9]/g, '').toUpperCase();
    const randomTagSuffix = Math.floor(1000 + Math.random() * 9000);
    const tagNumber = `TAG-${cleanFlight}-${randomTagSuffix}`;

    const allowance = Number(req.baggageAllowance) || 15;
    const actual = Number(req.actualBaggageWeight) || 0;
    const extraWeight = Math.max(0, +(actual - allowance).toFixed(1));
    const extraFee = Math.round(extraWeight * 500);

    const nowFormatted = new Date().toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });

    const newRecord: BaggageRecord = {
      id: 'BG-' + Date.now(),
      passengerName: req.passengerName.trim(),
      bookingReference: req.bookingReference.trim().toUpperCase(),
      flightNumber: req.flightNumber.trim().toUpperCase(),
      travelDate: req.travelDate || '2026-10-15',
      origin: req.origin.trim().toUpperCase(),
      destination: req.destination.trim().toUpperCase(),
      seatNumber: req.seatNumber || 'TBD',
      baggageAllowance: allowance,
      baggageAllowanceDisplay: `${allowance} kg`,
      bagsAllowed: req.bagsAllowed || 1,
      bagsAllowedDisplay: `${req.bagsAllowed || 1} ${req.bagsAllowed === 1 ? 'bag' : 'bags'}`,
      bagTagNumber: tagNumber,
      actualBaggageWeight: actual,
      extraBaggageWeight: extraWeight,
      extraBaggageFee: extraFee,
      baggageStatus: 'CHECKED_IN',
      checkInTime: nowFormatted,
      counterLocation: req.counterLocation || 'Terminal 3 - Counter T3-B14',
      baggageType: req.baggageType || 'REGULAR',
      specialHandling: req.specialHandling || 'NONE',
      remarks: req.remarks || 'Counter check-in verified by ground staff.',
      lastUpdated: nowFormatted
    };

    list.unshift(newRecord);
    this.saveCachedRecords(list);
    return of(newRecord);
  }

  /**
   * Resets and re-seeds baseline records from live database
   */
  public resetToDefault(bookings: BookingResponse[]): BaggageRecord[] {
    const records = this.generateInitialRecords(bookings);
    this.saveCachedRecords(records);
    return records;
  }

  private getCachedRecords(): BaggageRecord[] {
    try {
      const saved = localStorage.getItem(this.STORAGE_KEY);
      if (saved) {
        return JSON.parse(saved);
      }
    } catch {
      // ignore
    }
    return [];
  }

  private saveCachedRecords(records: BaggageRecord[]): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(records));
    } catch {
      // ignore
    }
  }

  private generateInitialRecords(bookings: BookingResponse[]): BaggageRecord[] {
    const list: BaggageRecord[] = [];

    // If confirmed bookings exist in backend, convert them into rich Baggage Records
    if (bookings && bookings.length > 0) {
      bookings.forEach((b, idx) => {
        const pName = b.passengerName || (b.passengers && b.passengers[0]?.fullName) || `Passenger ${idx + 1}`;
        const flightNum = b.flightNumber || 'AI-202';
        const cleanFlight = flightNum.replace(/[^A-Za-z0-9]/g, '').toUpperCase();
        const tag = `TAG-${cleanFlight}-${1000 + (b.id ? Number(b.id) * 37 : idx * 53) % 9000}`;
        const date = b.departureDate || '2026-10-15';
        const orig = b.source || 'DEL';
        const dest = b.destination || 'BOM';
        const isChecked = b.checkInStatus === 'CHECKED_IN';
        
        const weights = [14.2, 18.5, 12.0, 21.0, 15.0, 16.8, 24.5];
        const actualWeight = weights[idx % weights.length];
        const allowance = 15;
        const extraWeight = Math.max(0, +(actualWeight - allowance).toFixed(1));
        const extraFee = Math.round(extraWeight * 500);

        const statuses: BaggageStatus[] = ['CHECKED_IN', 'LOADED', 'DELIVERED', 'UNLOADED', 'NOT_CHECKED_IN', 'LOADED'];
        const bagStatus = isChecked ? (statuses[idx % statuses.length]) : 'NOT_CHECKED_IN';

        const handlings: SpecialHandlingType[] = ['NONE', 'FRAGILE', 'NONE', 'WHEELCHAIR', 'SPORTS_EQUIPMENT', 'NONE'];
        const handling = handlings[idx % handlings.length];
        const bagType: BaggageType = handling !== 'NONE' ? 'SPECIAL' : 'REGULAR';

        list.push({
          id: b.id || `BG-INIT-${idx + 1}`,
          passengerName: pName,
          bookingReference: b.bookingReference || `BK-${1000 + idx}`,
          flightNumber: flightNum,
          flightId: b.flightId,
          travelDate: date,
          origin: orig,
          destination: dest,
          seatNumber: b.seatNumber || (b.passengers && b.passengers[0]?.seatNumber) || '12B',
          baggageAllowance: allowance,
          baggageAllowanceDisplay: `${allowance} kg`,
          bagsAllowed: 1,
          bagsAllowedDisplay: '1 bag',
          bagTagNumber: tag,
          actualBaggageWeight: actualWeight,
          extraBaggageWeight: extraWeight,
          extraBaggageFee: extraFee,
          baggageStatus: bagStatus,
          checkInTime: bagStatus !== 'NOT_CHECKED_IN' ? '2026-10-07 08:15 AM' : 'Pending',
          counterLocation: `Terminal 3 - Counter T3-${['A02', 'B14', 'C08', 'D11'][idx % 4]}`,
          baggageType: bagType,
          specialHandling: handling,
          customerEmail: b.passengerEmail,
          customerPhone: b.passengerPhone,
          lastUpdated: '2026-10-07 08:30 AM'
        });
      });
    }

    // Baseline realistic records representing diverse operational states
    const presets: BaggageRecord[] = [
      {
        id: 'BG-AI202-01',
        passengerName: 'Rahul Sharma',
        bookingReference: 'BK-78921',
        flightNumber: 'AI-202',
        flightId: 14,
        travelDate: '2026-10-16',
        origin: 'BOM',
        destination: 'DEL',
        seatNumber: '14A',
        baggageAllowance: 15,
        baggageAllowanceDisplay: '15 kg',
        bagsAllowed: 1,
        bagsAllowedDisplay: '1 bag',
        bagTagNumber: 'TAG-AI202-7821',
        actualBaggageWeight: 18.5,
        extraBaggageWeight: 3.5,
        extraBaggageFee: 1750,
        baggageStatus: 'LOADED',
        checkInTime: '2026-10-07 06:45 AM',
        counterLocation: 'Terminal 2 - Counter T2-A04',
        baggageType: 'REGULAR',
        specialHandling: 'FRAGILE',
        remarks: 'Glassware packaging enclosed, priority loading mark applied.',
        lastUpdated: '2026-10-07 07:15 AM'
      },
      {
        id: 'BG-6E501-02',
        passengerName: 'Ananya Verma',
        bookingReference: 'BK-54210',
        flightNumber: '6E-501',
        flightId: 12,
        travelDate: '2026-10-15',
        origin: 'DEL',
        destination: 'BLR',
        seatNumber: '08C',
        baggageAllowance: 15,
        baggageAllowanceDisplay: '15 kg',
        bagsAllowed: 1,
        bagsAllowedDisplay: '1 bag',
        bagTagNumber: 'TAG-6E501-9042',
        actualBaggageWeight: 14.2,
        extraBaggageWeight: 0.0,
        extraBaggageFee: 0,
        baggageStatus: 'CHECKED_IN',
        checkInTime: '2026-10-07 07:10 AM',
        counterLocation: 'Terminal 1D - Counter 6E-12',
        baggageType: 'REGULAR',
        specialHandling: 'NONE',
        remarks: 'Standard hardshell trolley bag.',
        lastUpdated: '2026-10-07 07:12 AM'
      },
      {
        id: 'BG-UK811-03',
        passengerName: 'Vikram Malhotra',
        bookingReference: 'BK-99341',
        flightNumber: 'UK-811',
        flightId: 16,
        travelDate: '2026-10-15',
        origin: 'DEL',
        destination: 'BOM',
        seatNumber: '02D',
        baggageAllowance: 25,
        baggageAllowanceDisplay: '25 kg',
        bagsAllowed: 2,
        bagsAllowedDisplay: '2 bags',
        bagTagNumber: 'TAG-UK811-3319',
        actualBaggageWeight: 27.5,
        extraBaggageWeight: 2.5,
        extraBaggageFee: 1250,
        baggageStatus: 'LOADED',
        checkInTime: '2026-10-07 08:05 AM',
        counterLocation: 'Terminal 3 - Club Vistara Counter 01',
        baggageType: 'SPECIAL',
        specialHandling: 'SPORTS_EQUIPMENT',
        remarks: 'Golf bag & clubs tagged with oversized baggage container seal.',
        lastUpdated: '2026-10-07 08:20 AM'
      },
      {
        id: 'BG-INDIGO-04',
        passengerName: 'Sunita Mehra',
        bookingReference: 'BK-11029',
        flightNumber: 'INDIGO101',
        flightId: 10,
        travelDate: '2026-10-15',
        origin: 'DEL',
        destination: 'BOM',
        seatNumber: '21F',
        baggageAllowance: 15,
        baggageAllowanceDisplay: '15 kg',
        bagsAllowed: 1,
        bagsAllowedDisplay: '1 bag',
        bagTagNumber: 'TAG-6E101-1184',
        actualBaggageWeight: 13.8,
        extraBaggageWeight: 0.0,
        extraBaggageFee: 0,
        baggageStatus: 'DELIVERED',
        checkInTime: '2026-10-07 06:15 AM',
        counterLocation: 'Terminal 1D - Counter 6E-06',
        baggageType: 'SPECIAL',
        specialHandling: 'WHEELCHAIR',
        remarks: 'Manual folding wheelchair checked at gate and transferred to belt 4.',
        lastUpdated: '2026-10-07 10:30 AM'
      },
      {
        id: 'BG-AI101-05',
        passengerName: 'David K. Miller',
        bookingReference: 'BK-65123',
        flightNumber: 'AI101_9',
        flightId: 9,
        travelDate: '2026-10-15',
        origin: 'DEL',
        destination: 'BOM',
        seatNumber: '04A',
        baggageAllowance: 20,
        baggageAllowanceDisplay: '20 kg',
        bagsAllowed: 1,
        bagsAllowedDisplay: '1 bag',
        bagTagNumber: 'TAG-AI101-5502',
        actualBaggageWeight: 23.4,
        extraBaggageWeight: 3.4,
        extraBaggageFee: 1700,
        baggageStatus: 'LOST_OR_DELAYED',
        checkInTime: '2026-10-07 07:00 AM',
        counterLocation: 'Terminal 3 - Counter T3-C10',
        baggageType: 'REGULAR',
        specialHandling: 'NONE',
        remarks: 'Tag scan missed at transfer conveyor; trace report filed with ground supervisor.',
        lastUpdated: '2026-10-07 09:45 AM'
      },
      {
        id: 'BG-SG123-06',
        passengerName: 'Pooja Iyer',
        bookingReference: 'BK-88402',
        flightNumber: 'SG-123',
        flightId: 20,
        travelDate: '2026-10-19',
        origin: 'DEL',
        destination: 'GOI',
        seatNumber: '19B',
        baggageAllowance: 15,
        baggageAllowanceDisplay: '15 kg',
        bagsAllowed: 1,
        bagsAllowedDisplay: '1 bag',
        bagTagNumber: 'TAG-SG123-4411',
        actualBaggageWeight: 15.0,
        extraBaggageWeight: 0.0,
        extraBaggageFee: 0,
        baggageStatus: 'NOT_CHECKED_IN',
        checkInTime: 'Pending',
        counterLocation: 'Terminal 1 - Counter SG-03',
        baggageType: 'REGULAR',
        specialHandling: 'NONE',
        remarks: 'Web booking confirmed; passenger not yet arrived at terminal counter.',
        lastUpdated: '2026-10-07 08:00 AM'
      },
      {
        id: 'BG-6E502-07',
        passengerName: 'Karthik Raman',
        bookingReference: 'BK-33912',
        flightNumber: '6E-502',
        flightId: 13,
        travelDate: '2026-10-15',
        origin: 'BLR',
        destination: 'DEL',
        seatNumber: '11D',
        baggageAllowance: 15,
        baggageAllowanceDisplay: '15 kg',
        bagsAllowed: 1,
        bagsAllowedDisplay: '1 bag',
        bagTagNumber: 'TAG-6E502-6019',
        actualBaggageWeight: 19.8,
        extraBaggageWeight: 4.8,
        extraBaggageFee: 2400,
        baggageStatus: 'UNLOADED',
        checkInTime: '2026-10-07 09:20 AM',
        counterLocation: 'Kempegowda T1 - Counter 6E-22',
        baggageType: 'SPECIAL',
        specialHandling: 'MUSICAL_INSTRUMENT',
        remarks: 'Acoustic violin in hardcase, cabin-hold handoff confirmed.',
        lastUpdated: '2026-10-07 12:40 PM'
      }
    ];

    // Combine any converted bookings with presets without duplicating IDs
    const combined = [...list];
    presets.forEach(p => {
      if (!combined.some(c => c.bagTagNumber === p.bagTagNumber || c.bookingReference === p.bookingReference)) {
        combined.push(p);
      }
    });

    return combined;
  }
}
