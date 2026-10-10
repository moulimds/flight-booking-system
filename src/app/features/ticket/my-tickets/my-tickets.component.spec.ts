import { TestBed } from '@angular/core/testing';
import { MyTicketsComponent } from './my-tickets.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('MyTicketsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyTicketsComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create my tickets component', () => {
    const fixture = TestBed.createComponent(MyTicketsComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
