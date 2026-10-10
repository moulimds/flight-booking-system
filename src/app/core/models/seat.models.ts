export type SeatStatus = 'AVAILABLE' | 'HELD' | 'BOOKED' | 'OCCUPIED' | 'MAINTENANCE';
export type SeatClass = 'ECONOMY' | 'BUSINESS' | 'FIRST';

export interface Seat {
  id: number;
  flightId: number;
  seatNumber: string; // e.g. "1A", "1B", "12C"
  seatClass: string;
  status: SeatStatus;
  bookingId?: number;
  price?: number;
  rowNumber?: number;
  columnLetter?: string;
  isWindow?: boolean;
  isAisle?: boolean;
  isExtraLegroom?: boolean;
}

export interface SeatHoldRequest {
  userId?: number;
  holdDurationMinutes?: number;
}

export interface SeatBookRequest {
  bookingId: number;
}

