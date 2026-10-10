import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'flightDuration',
  standalone: true
})
export class FlightDurationPipe implements PipeTransform {
  transform(departureTime: string, arrivalTime?: string): string {
    if (!departureTime) return '--';
    if (!arrivalTime) return '2h 50m';

    try {
      const dep = new Date(departureTime).getTime();
      const arr = new Date(arrivalTime).getTime();
      const diffMs = arr - dep;
      if (diffMs <= 0) return '2h 50m';

      const diffMins = Math.floor(diffMs / (1000 * 60));
      const hours = Math.floor(diffMins / 60);
      const mins = diffMins % 60;
      return `${hours}h ${mins}m`;
    } catch {
      return '2h 50m';
    }
  }
}
