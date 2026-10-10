import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyFormatter',
  standalone: true
})
export class CurrencyFormatterPipe implements PipeTransform {
  transform(value: number | string | undefined | null, currencyCode = 'INR'): string {
    if (value === undefined || value === null) return '₹0';
    const num = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(num)) return '₹0';

    if (currencyCode === 'INR') {
      return `₹${num.toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;
    }
    return `${currencyCode} ${num.toLocaleString('en-IN')}`;
  }
}

