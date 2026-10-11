import { Pipe, PipeTransform } from '@angular/core';
import { Flight } from '../../core/models/flight.models';

const cityAliases: Record<string, string[]> = {
  'del': ['del', 'delhi', 'new delhi', 'indira gandhi'],
  'bom': ['bom', 'mumbai', 'bombay', 'chhatrapati shivaji'],
  'blr': ['blr', 'bangalore', 'bengaluru', 'kempegowda'],
  'maa': ['maa', 'chennai', 'madras'],
  'hyd': ['hyd', 'hyderabad', 'rajiv gandhi'],
  'ccu': ['ccu', 'kolkata', 'calcutta', 'netaji subhash'],
  'cok': ['cok', 'kochi', 'cochin'],
  'goi': ['goi', 'goa', 'dabolim', 'mopa'],
  'pnq': ['pnq', 'pune'],
  'dxb': ['dxb', 'dubai'],
  'sin': ['sin', 'singapore', 'changi'],
  'eg': ['eg', 'cai', 'cairo', 'egypt'],
  'ksa': ['ksa', 'jed', 'jeddah', 'saudi arabia', 'saudi'],
  'fra': ['fra', 'frankfurt'],
  'lhr': ['lhr', 'london', 'heathrow'],
  'jfk': ['jfk', 'new york']
};

function matchesLocation(flightLocation: string, query: string): boolean {
  if (!query) return true;
  const q = query.trim().toLowerCase();
  const f = flightLocation.trim().toLowerCase();

  if (f.includes(q) || q.includes(f)) return true;

  for (const [code, aliases] of Object.entries(cityAliases)) {
    const flightMatchesCode = f === code || aliases.some(a => f.includes(a) || a.includes(f));
    const queryMatchesCode = q === code || aliases.some(a => q.includes(a) || a.includes(q));
    if (flightMatchesCode && queryMatchesCode) {
      return true;
    }
  }

  return false;
}

@Pipe({
  name: 'flightFilter',
  standalone: true,
  pure: false
})
export class FlightFilterPipe implements PipeTransform {
  transform(
    flights: Flight[] | null,
    selectedAirlines?: string[],
    maxPrice?: number,
    cabinClass?: string,
    origin?: string,
    destination?: string,
    departureDate?: string,
    passengers?: number
  ): Flight[] {
    if (!flights || !Array.isArray(flights)) return [];

    return flights.filter(flight => {
      // 1. Airline filter
      if (selectedAirlines && selectedAirlines.length > 0) {
        if (!selectedAirlines.includes(flight.airline)) {
          return false;
        }
      }

      // 2. Cabin class filter
      if (cabinClass && cabinClass !== 'ALL') {
        const c = cabinClass.toUpperCase();
        const explicitCabin = ((flight as any).cabinClass || (flight as any).classType || '').toUpperCase();
        if (explicitCabin) {
          if (explicitCabin !== c) return false;
        } else {
          const airline = (flight.airline || '').toUpperCase();
          if (c === 'FIRST') {
            const offersFirst = airline.includes('AIR INDIA') || airline.includes('BRITISH') || airline.includes('EMIRATES');
            if (!offersFirst) return false;
          } else if (c === 'BUSINESS') {
            const offersBusiness = airline.includes('AIR INDIA') || airline.includes('VISTARA') || airline.includes('BRITISH') || airline.includes('EMIRATES') || !!(flight.aircraftModel && flight.aircraftModel.includes('380'));
            if (!offersBusiness) return false;
          }
          // ECONOMY is available across all commercial flights
        }
      }

      // 3. Price filter (calculates fare with cabin class multiplier)
      let price = flight.basePrice ?? (flight as any).fare ?? (flight as any).totalFare ?? 0;
      if (cabinClass === 'BUSINESS') {
        price = Math.round(price * 1.75);
      } else if (cabinClass === 'FIRST') {
        price = Math.round(price * 2.5);
      }
      if (maxPrice && maxPrice > 0 && price > 0) {
        if (price > maxPrice) {
          return false;
        }
      }

      // 4. Origin / From filter
      if (origin && origin.trim()) {
        const flightOrigin = flight.source || flight.origin || '';
        if (!matchesLocation(flightOrigin, origin)) {
          return false;
        }
      }

      // 5. Destination / To filter
      if (destination && destination.trim()) {
        const flightDest = flight.destination || '';
        if (!matchesLocation(flightDest, destination)) {
          return false;
        }
      }

      // 6. Departure Date filter
      if (departureDate && departureDate.trim()) {
        const flightDate = (flight.departureDate || '').trim();
        if (flightDate && flightDate !== departureDate.trim()) {
          return false;
        }
      }

      // 7. Passengers filter (available seats)
      const seats = flight.availableSeats ?? flight.totalSeats ?? 0;
      const neededPassengers = passengers && passengers > 0 ? passengers : 1;
      if (seats < neededPassengers) {
        return false;
      }

      // 8. Omit cancelled or completed flights
      if (flight.status) {
        const s = flight.status.toUpperCase();
        if (s === 'CANCELLED' || s === 'COMPLETED') {
          return false;
        }
      }

      // 9. Omit flights where booking cannot be done (bookable = false or < 8h departure cutoff)
      if (flight.bookable === false) {
        return false;
      }

      if (flight.cutoffHoursRemaining !== undefined && flight.cutoffHoursRemaining !== null && flight.cutoffHoursRemaining < 8) {
        return false;
      }

      if (flight.departureDate && flight.departureTime) {
        try {
          const depDateTime = new Date(`${flight.departureDate}T${flight.departureTime}`);
          const now = new Date();
          const diffHours = (depDateTime.getTime() - now.getTime()) / (1000 * 60 * 60);
          if (diffHours < 8) {
            return false;
          }
        } catch {
          
        }
      }

      return true;
    });
  }
}

