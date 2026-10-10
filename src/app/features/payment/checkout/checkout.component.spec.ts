import { TestBed } from '@angular/core/testing';
import { CheckoutComponent } from './checkout.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('CheckoutComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CheckoutComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create checkout component', () => {
    const fixture = TestBed.createComponent(CheckoutComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
