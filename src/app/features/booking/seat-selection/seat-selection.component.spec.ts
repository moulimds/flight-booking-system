import { TestBed } from '@angular/core/testing';
import { SeatSelectionComponent } from './seat-selection.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('SeatSelectionComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeatSelectionComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create seat selection component', () => {
    const fixture = TestBed.createComponent(SeatSelectionComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
