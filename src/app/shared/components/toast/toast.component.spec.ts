import { TestBed } from '@angular/core/testing';
import { ToastComponent } from './toast.component';

describe('ToastComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ToastComponent]
    }).compileComponents();
  });

  it('should create the toast', () => {
    const fixture = TestBed.createComponent(ToastComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
