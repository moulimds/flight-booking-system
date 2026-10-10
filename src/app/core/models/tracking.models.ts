export interface FlightTracking {
  id: number;
  flightId?: number;
  flightNumber: string;
  airline?: string;
  origin?: string;
  destination?: string;
  status: string;
  latitude?: number;
  longitude?: number;
  altitude?: number; // in feet
  groundSpeed?: number; // in knots
  speed?: number;
  heading?: number; // in degrees
  estimatedArrival?: string;
  scheduledDepartureTime?: string;
  actualDepartureTime?: string;
  scheduledArrivalTime?: string;
  actualArrivalTime?: string;
  departureGate?: string;
  arrivalGate?: string;
  gate?: string;
  terminal?: string;
  delayMinutes?: number;
  delayReason?: string;
  lastUpdated?: string;
  trackingAvailable?: boolean;
  trackingMessage?: string;
}

export interface TrackingWaypoint {
  latitude: number;
  longitude: number;
  altitude: number;
  speed: number;
  timestamp: string;
}

