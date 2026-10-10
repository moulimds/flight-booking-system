import { TestBed } from '@angular/core/testing';
import { BoardingPassComponent } from './boarding-pass.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('BoardingPassComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoardingPassComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create boarding pass component', () => {
    const fixture = TestBed.createComponent(BoardingPassComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
