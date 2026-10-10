import { TestBed } from '@angular/core/testing';
import { NavbarComponent } from './navbar.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('NavbarComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create the navbar', () => {
    const fixture = TestBed.createComponent(NavbarComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
