export interface Flight {
  id: number;
  flightNumber: string;
  airline: string;
  source: string;
  origin?: string;
  destination: string;
  departureDate?: string;
  departureTime: string;
  arrivalDate?: string;
  arrivalTime: string;
  duration?: string;
  totalSeats: number;
  availableSeats: number;
  basePrice?: number;
  status: string;
  aircraftId?: number;
  aircraftModel?: string;
  registrationNumber?: string;
  scheduleId?: number;
  operatingDays?: string | string[];
  bookable?: boolean;
  cutoffHoursRemaining?: number;
  terminal?: string;
  gate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface FlightSearchCriteria {
  source?: string;
  origin?: string;
  destination: string;
  date?: string;
  departureDate?: string;
  returnDate?: string;
  passengers?: number;
  cabinClass?: string;
  maxPrice?: number;
  airlines?: string[];
  tripType?: 'ONE_WAY' | 'ROUND_TRIP' | 'MULTI_CITY';
}

export interface FlightSearchResult {
  flightId: number;
  flightNumber: string;
  airline: string;
  source: string;
  destination: string;
  departureDate: string;
  departureTime: string;
  arrivalTime: string;
  availableSeats: number;
  fare: number;
  status: string;
  aircraftId?: number;
  aircraftModel?: string;
  registrationNumber?: string;
  scheduleId?: number;
  operatingDays?: string | string[];
  bookable?: boolean;
  cutoffHoursRemaining?: number;
  duration?: string;
}

export interface FareDetail {
  id: number;
  flightId: number;
  classType: string;
  baseFare: number;
  tax: number;
  discount: number;
  finalFare: number;
  totalFare?: number;
  currency: string;
}

export interface Aircraft {
  id?: number;
  model: string;
  registrationNumber: string;
  totalCapacity: number;
  seatLayout: string;
  airline: string;
  status: string;
  operationalNotes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MealOption {
  id?: number;
  aircraftId?: number;
  aircraftModel?: string;
  name: string;
  category: 'VEGETARIAN' | 'NON_VEGETARIAN' | 'VEGAN' | 'JAIN' | 'GLUTEN_FREE' | 'CHILD_MEAL' | 'CHILD' | 'COMPLIMENTARY' | 'NO_MEAL' | string;
  description?: string;
  dietaryClassification?: string;
  dietaryType?: string;
  price: number;
  isComplimentary?: boolean;
  complimentary?: boolean;
  available: boolean;
  status?: string;
  inventoryLimit?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface WeeklySchedule {
  id?: number;
  scheduleCode?: string;
  flightNumber?: string;
  serviceNumber?: string;
  airline: string;
  origin?: string;
  source?: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  duration?: string;
  aircraftId?: number;
  aircraftModel?: string;
  registrationNumber?: string;
  baseFare: number;
  totalSeats?: number;
  totalCapacity?: number;
  operatingDays?: string | string[];
  operatingDaysList?: string[];
  startDate: string;
  endDate: string;
  status: string;
  excludedDates?: string;
  generatedFlightsCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface MultiCitySegment {
  id: number;
  source: string;
  destination: string;
  departureDate: string;
  selectedFlight?: FlightSearchResult | Flight;
  availableFlights?: (FlightSearchResult | Flight)[];
  loading?: boolean;
}
