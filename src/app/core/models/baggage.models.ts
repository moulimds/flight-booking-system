export type BaggageStatus = 
  | 'NOT_CHECKED_IN'
  | 'CHECKED_IN'
  | 'LOADED'
  | 'UNLOADED'
  | 'DELIVERED'
  | 'LOST_OR_DELAYED';

export type BaggageType = 'REGULAR' | 'SPECIAL';

export type SpecialHandlingType = 
  | 'NONE'
  | 'FRAGILE'
  | 'WHEELCHAIR'
  | 'SPORTS_EQUIPMENT'
  | 'MUSICAL_INSTRUMENT'
  | 'MEDICAL_SUPPLIES'
  | 'HAZARDOUS_INSPECTED';

export interface BaggageRecord {
  id: string | number;
  passengerName: string;
  bookingReference: string; // Booking ID / PNR
  flightNumber: string;
  flightId?: number;
  travelDate: string; // e.g. '2026-10-15'
  origin: string; // e.g. 'DEL'
  destination: string; // e.g. 'BOM'
  seatNumber?: string;
  
  // Baggage Quota & Tag
  baggageAllowance: number; // e.g. 15 (kg)
  baggageAllowanceDisplay?: string; // '15 kg'
  bagsAllowed: number; // e.g. 1 (bag)
  bagsAllowedDisplay?: string; // '1 bag'
  bagTagNumber: string; // e.g. 'TAG-AI101-7821'
  
  // Weights & Fees
  actualBaggageWeight: number; // e.g. 18.5 (kg)
  extraBaggageWeight: number; // e.g. 3.5 (kg)
  extraBaggageFee: number; // e.g. 1750 (₹)
  
  // Status & Operations
  baggageStatus: BaggageStatus;
  checkInTime?: string; // e.g. '2026-10-07 08:30 AM'
  counterLocation: string; // e.g. 'Terminal 3 - Counter T3-B14'
  baggageType: BaggageType; // 'Regular' / 'Special'
  specialHandling: SpecialHandlingType; // 'Fragile', 'Wheelchair equipment', 'Sports equipment', etc.
  
  // Optional metadata
  remarks?: string;
  customerPhone?: string;
  customerEmail?: string;
  lastUpdated?: string;
}

export interface NewBaggageCheckinRequest {
  passengerName: string;
  bookingReference: string;
  flightNumber: string;
  travelDate: string;
  origin: string;
  destination: string;
  seatNumber?: string;
  baggageAllowance: number;
  bagsAllowed: number;
  actualBaggageWeight: number;
  counterLocation: string;
  baggageType: BaggageType;
  specialHandling: SpecialHandlingType;
  remarks?: string;
}
