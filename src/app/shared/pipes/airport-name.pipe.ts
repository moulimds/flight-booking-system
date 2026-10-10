import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'airportName',
  standalone: true
})
export class AirportNamePipe implements PipeTransform {
  private airports: Record<string, string> = {
    'EG': 'Egypt Airline Airport (CAI)',
    'KSA': 'King Abdulaziz Airport (JED)',
    'DEL': 'Indira Gandhi Int\'l (DEL)',
    'BOM': 'Chhatrapati Shivaji Int\'l (BOM)',
    'FRA': 'Frankfurt Airport (FRA)',
    'LHR': 'Heathrow Airport (LHR)',
    'JFK': 'John F. Kennedy Int\'l (JFK)',
    'DXB': 'Dubai Int\'l (DXB)',
    'SIN': 'Changi Airport (SIN)'
  };

  transform(code: string): string {
    if (!code) return '';
    const upper = code.toUpperCase().trim();
    return this.airports[upper] || `${code} Airport`;
  }
}
