import { TestBed } from '@angular/core/testing';
import { SearchDashboardComponent } from './search-dashboard.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('SearchDashboardComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchDashboardComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create the search dashboard component', () => {
    const fixture = TestBed.createComponent(SearchDashboardComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
