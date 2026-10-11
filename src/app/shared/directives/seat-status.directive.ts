import { Directive, ElementRef, Input, OnChanges, Renderer2 } from '@angular/core';
import { SeatStatus, SeatClass } from '../../core/models/seat.models';

@Directive({
  selector: '[appSeatStatus]',
  standalone: true
})
export class SeatStatusDirective implements OnChanges {
  @Input('appSeatStatus') status: SeatStatus | string = 'AVAILABLE';
  @Input() seatClass: SeatClass | string = 'ECONOMY';
  @Input() isSelected = false;

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnChanges(): void {
    const element = this.el.nativeElement;


    this.renderer.removeClass(element, 'seat-available');
    this.renderer.removeClass(element, 'seat-occupied');
    this.renderer.removeClass(element, 'seat-selected');
    this.renderer.removeClass(element, 'seat-business');

    if (this.seatClass === 'BUSINESS') {
      this.renderer.addClass(element, 'seat-business');
    }

    if (this.isSelected) {
      this.renderer.addClass(element, 'seat-selected');
      this.renderer.setAttribute(element, 'title', `Selected - ${this.seatClass}`);
      return;
    }

    if (this.status === 'OCCUPIED' || this.status === 'BOOKED') {
      this.renderer.addClass(element, 'seat-occupied');
      this.renderer.setAttribute(element, 'title', 'Occupied');
    } else {
      this.renderer.addClass(element, 'seat-available');
      this.renderer.setAttribute(element, 'title', `Available - ${this.seatClass}`);
    }
  }
}
