import { TestBed } from '@angular/core/testing';
import { FlightRadarComponent } from './flight-radar.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('FlightRadarComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlightRadarComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create flight radar component', () => {
    const fixture = TestBed.createComponent(FlightRadarComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
