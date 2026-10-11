import { Directive, ElementRef, Input, OnChanges, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appStatusBadge]',
  standalone: true
})
export class StatusBadgeDirective implements OnChanges {
  @Input('appStatusBadge') status = '';

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnChanges(): void {
    const element = this.el.nativeElement;
    this.renderer.addClass(element, 'badge');

    
    ['badge-confirmed', 'badge-pending', 'badge-cancelled', 'badge-info', 'badge-success', 'badge-warning', 'badge-danger'].forEach(cls => {
      this.renderer.removeClass(element, cls);
    });

    const s = (this.status || '').toUpperCase();
    if (s === 'CONFIRMED' || s === 'SUCCESS' || s === 'PAID' || s === 'ON_TIME' || s === 'LANDED') {
      this.renderer.addClass(element, 'badge-confirmed');
    } else if (s === 'PENDING' || s === 'SCHEDULED' || s === 'IN_FLIGHT' || s === 'BOARDING') {
      this.renderer.addClass(element, 'badge-pending');
    } else if (s === 'CANCELLED' || s === 'FAILED' || s === 'DELAYED') {
      this.renderer.addClass(element, 'badge-cancelled');
    } else {
      this.renderer.addClass(element, 'badge-info');
    }
  }
}
