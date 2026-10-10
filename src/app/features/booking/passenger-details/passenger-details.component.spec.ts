import { TestBed } from '@angular/core/testing';
import { PassengerDetailsComponent } from './passenger-details.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('PassengerDetailsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerDetailsComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create passenger details component', () => {
    const fixture = TestBed.createComponent(PassengerDetailsComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
