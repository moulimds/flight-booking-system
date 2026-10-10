import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FlightService } from '../../../core/services/flight.service';
import { SeatService } from '../../../core/services/seat.service';
import { BookingService } from '../../../core/services/booking.service';
import { Flight, Aircraft, MealOption, WeeklySchedule } from '../../../core/models/flight.models';
import { BookingResponse } from '../../../core/models/booking.models';
import { Seat } from '../../../core/models/seat.models';
import { StatusBadgeDirective } from '../../../shared/directives/status-badge.directive';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { ToastService } from '../../../core/services/toast.service';

export interface FlightPassengerSeatRecord {
  seatNumber: string;
  travelClass: string;
  passengerName: string;
  customerEmail: string;
  customerPhone: string;
  bookingReference: string;
  bookingStatus: string;
  fareAmount: number;
  bookedAt?: string;
}

@Component({
  selector: 'app-fleet-console',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, StatusBadgeDirective, CurrencyFormatterPipe],
  templateUrl: './fleet-console.component.html'
})
export class FleetConsoleComponent implements OnInit {
  private flightService = inject(FlightService);
  private seatService = inject(SeatService);
  private bookingService = inject(BookingService);
  private toastService = inject(ToastService);

  public flights: Flight[] = [];
  public allBookings: BookingResponse[] = [];
  public totalSeats = 0;
  public isLoading = false;
  public isSubmitting = false;

  // Active Admin Workspace Tab
  public activeTab: 'FLIGHTS' | 'AIRCRAFT' | 'MEALS' | 'SCHEDULES' | 'CABIN' = 'FLIGHTS';

  // Aircraft Management State
  public aircraftList: Aircraft[] = [];
  public isAircraftLoading = false;
  public showAircraftModal = false;
  public aircraftModalMode: 'CREATE' | 'EDIT' = 'CREATE';
  public isAircraftSubmitting = false;
  public aircraftForm = {
    id: 0,
    model: '',
    registrationNumber: '',
    totalCapacity: 180,
    seatLayout: '3x3',
    status: 'ACTIVE',
    airline: 'SkyWings'
  };

  // Meal Options Management State
  public mealList: MealOption[] = [];
  public isMealsLoading = false;
  public showMealModal = false;
  public mealModalMode: 'CREATE' | 'EDIT' = 'CREATE';
  public isMealSubmitting = false;
  public mealForm = {
    id: 0,
    name: '',
    category: 'VEGETARIAN',
    description: '',
    dietaryType: 'VEGETARIAN',
    price: 0,
    complimentary: true,
    available: true,
    status: 'ACTIVE',
    inventoryLimit: 100,
    aircraftId: null as number | null
  };

  // Weekly Flight Schedules Management State
  public scheduleList: WeeklySchedule[] = [];
  public isSchedulesLoading = false;
  public showScheduleModal = false;
  public isScheduleSubmitting = false;
  public isGeneratingScheduleFlights = false;
  public daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
  public scheduleForm = {
    scheduleCode: '',
    flightNumber: '',
    airline: 'SkyWings',
    origin: 'DEL',
    destination: 'BOM',
    departureTime: '08:00',
    arrivalTime: '10:15',
    aircraftId: 1,
    baseFare: 4500,
    totalCapacity: 180,
    startDate: '2026-11-01',
    endDate: '2026-12-31',
    operatingDays: ['MONDAY', 'WEDNESDAY', 'FRIDAY'] as string[],
    status: 'ACTIVE'
  };

  // Pagination State for Flight Inventory
  public currentPage = 1;
  public pageSize = 5;
  public pageSizeOptions: number[] = [5, 10, 20, 50];

  // Flight Passenger & Seat Report Modal State
  public isFlightReportModalOpen = false;
  public selectedFlightForReport: Flight | null = null;
  public flightPassengersList: FlightPassengerSeatRecord[] = [];
  public flightSeatsList: Seat[] = [];
  public isGeneratingFlightReport = false;
  public reportGeneratedAt = '';

  // Filter & Search State
  public searchTerm = '';
  public statusFilter = 'ALL';

  // Flight Modal State (Create / Edit)
  public showFlightModal = false;
  public modalMode: 'CREATE' | 'EDIT' = 'CREATE';
  public isFlightSubmitting = false;

  // Delete Confirmation Modal State
  public flightToDelete: Flight | null = null;
  public showDeleteModal = false;

  public flightForm = this.getEmptyFlightForm();

  // Airframe Cache & Presets for Flight / Registration / Aircraft Model Sync
  private readonly AIRFRAME_STORAGE_KEY = 'skywings_flight_airframes';
  public airframeCache: { [flightId: number]: { model: string; registration: string } } = {};

  private readonly AIRLINE_PRESETS: { [prefix: string]: { model: string; regPrefix: string } } = {
    'AI': { model: 'Airbus A350-900', regPrefix: 'VT-EX' },
    '6E': { model: 'Airbus A320neo', regPrefix: 'VT-IN' },
    'UK': { model: 'Airbus A321neo', regPrefix: 'VT-TN' },
    'SG': { model: 'Boeing 737 MAX 8', regPrefix: 'VT-SZ' },
    'IX': { model: 'Boeing 737-800', regPrefix: 'VT-AX' },
    'EK': { model: 'Airbus A380-800', regPrefix: 'A6-EE' },
    'QR': { model: 'Boeing 777-300ER', regPrefix: 'A7-BE' }
  };

  // Aircraft Cabin Configuration Form
  public config = {
    aircraftModel: 'Airbus A380-800',
    registrationNumber: 'VT-AIR380',
    flightId: 1,
    businessSeats: 20,
    businessPrice: 15000,
    premiumSeats: 30,
    premiumPrice: 9000,
    economySeats: 100,
    economyPrice: 5000,
    layoutType: '3x3' // 3x3 (A-F) or 2x2 (A-D)
  };

  ngOnInit(): void {
    this.initAirframeCache();
    this.loadData();
  }

  private initAirframeCache(): void {
    try {
      const saved = localStorage.getItem(this.AIRFRAME_STORAGE_KEY);
      if (saved) {
        this.airframeCache = JSON.parse(saved);
      }
    } catch {
      this.airframeCache = {};
    }
  }

  private saveAirframeCache(): void {
    try {
      localStorage.setItem(this.AIRFRAME_STORAGE_KEY, JSON.stringify(this.airframeCache));
    } catch {
      // ignore
    }
  }

  public loadData(): void {
    this.isLoading = true;
    this.flightService.getAllFlights().subscribe({
      next: (f) => {
        this.flights = f || [];
        this.totalSeats = this.flights.reduce((acc, curr) => acc + (curr.totalSeats || 20), 0);
        if (this.flights.length > 0) {
          const currentFlightId = this.config.flightId ? Number(this.config.flightId) : this.flights[0].id;
          const exists = this.flights.some(fl => fl.id === currentFlightId);
          this.onFlightChange(exists ? currentFlightId : this.flights[0].id);
        }
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });

    // Fetch all bookings to support flight passenger & seat reporting
    this.bookingService.getAllBookings().subscribe({
      next: (b) => {
        this.allBookings = b || [];
      },
      error: () => {}
    });

    // Load specialized fleet management modules
    this.loadAircraft();
    this.loadMeals();
    this.loadSchedules();
  }

  // ==========================================
  // Flight Search & Filter
  // ==========================================
  public get filteredFlights(): Flight[] {
    let result = this.flights;

    if (this.statusFilter !== 'ALL') {
      result = result.filter(f => (f.status || '').toUpperCase() === this.statusFilter);
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.trim().toLowerCase();
      result = result.filter(f =>
        (f.flightNumber || '').toLowerCase().includes(term) ||
        (f.airline || '').toLowerCase().includes(term) ||
        (f.source || f.origin || '').toLowerCase().includes(term) ||
        (f.destination || '').toLowerCase().includes(term) ||
        (f.aircraftModel || '').toLowerCase().includes(term)
      );
    }

    return result;
  }

  public onFilterChange(): void {
    this.currentPage = 1;
  }

  // ==========================================
  // Flight Inventory Pagination
  // ==========================================
  public get totalFilteredFlights(): number {
    return this.filteredFlights.length;
  }

  public get totalPages(): number {
    return Math.ceil(this.filteredFlights.length / this.pageSize) || 1;
  }

  public get startItemIndex(): number {
    return this.filteredFlights.length === 0 ? 0 : (this.currentPage - 1) * this.pageSize + 1;
  }

  public get endItemIndex(): number {
    return Math.min(this.currentPage * this.pageSize, this.filteredFlights.length);
  }

  public get paginatedFlights(): Flight[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredFlights.slice(start, start + this.pageSize);
  }

  public get pageNumbers(): number[] {
    const total = this.totalPages;
    const current = this.currentPage;
    const pages: number[] = [];
    const maxVisible = 5;

    let start = Math.max(1, current - Math.floor(maxVisible / 2));
    let end = Math.min(total, start + maxVisible - 1);

    if (end - start + 1 < maxVisible) {
      start = Math.max(1, end - maxVisible + 1);
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  public setPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  public nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  public prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  public onPageSizeChange(newSize: any): void {
    this.pageSize = Number(newSize);
    this.currentPage = 1;
  }

  // ==========================================
  // Flight Manifest & Seat Report Generation
  // ==========================================
  public openFlightReport(flight: Flight): void {
    this.selectedFlightForReport = flight;
    this.isFlightReportModalOpen = true;
    this.isGeneratingFlightReport = true;
    this.reportGeneratedAt = new Date().toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });

    // Refresh bookings to ensure latest manifest
    this.bookingService.getAllBookings().subscribe({
      next: (b) => {
        this.allBookings = b || [];
        this.buildFlightPassengerRoster(flight);
        this.isGeneratingFlightReport = false;
      },
      error: () => {
        this.buildFlightPassengerRoster(flight);
        this.isGeneratingFlightReport = false;
      }
    });

    // Also fetch physical seats if configured
    this.seatService.getSeatsForFlight(flight.id).subscribe({
      next: (seats) => {
        this.flightSeatsList = seats || [];
      },
      error: () => {
        this.flightSeatsList = [];
      }
    });
  }

  public closeFlightReportModal(): void {
    this.isFlightReportModalOpen = false;
    this.selectedFlightForReport = null;
    this.flightPassengersList = [];
    this.flightSeatsList = [];
    this.isGeneratingFlightReport = false;
  }

  private buildFlightPassengerRoster(flight: Flight): void {
    const records: FlightPassengerSeatRecord[] = [];
    const flightNum = (flight.flightNumber || '').trim().toUpperCase();

    const matchingBookings = this.allBookings.filter(b => {
      const bFlightId = Number(b.flightId);
      const bFlightNum = (b.flightNumber || '').trim().toUpperCase();
      return bFlightId === flight.id || (flightNum && bFlightNum === flightNum);
    });

    matchingBookings.forEach((b) => {
      if (b.passengers && b.passengers.length > 0) {
        b.passengers.forEach((p, idx) => {
          records.push({
            seatNumber: p.seatNumber || (b.seatNumber ? `${b.seatNumber}-${idx + 1}` : `S-${idx + 1}`),
            travelClass: (p.travelClass || 'ECONOMY').toUpperCase(),
            passengerName: p.fullName || b.passengerName || 'Passenger',
            customerEmail: b.passengerEmail || '-',
            customerPhone: b.passengerPhone || '-',
            bookingReference: b.bookingReference || `BK-${b.id}`,
            bookingStatus: p.passengerStatus || b.bookingStatus || 'CONFIRMED',
            fareAmount: p.fareAmount || (b.fareAmount ? Math.round(b.fareAmount / b.passengers.length) : (flight.basePrice || 5000)),
            bookedAt: b.createdAt
          });
        });
      } else {
        records.push({
          seatNumber: b.seatNumber || 'S-1',
          travelClass: 'ECONOMY',
          passengerName: b.passengerName || 'Passenger',
          customerEmail: b.passengerEmail || '-',
          customerPhone: b.passengerPhone || '-',
          bookingReference: b.bookingReference || `BK-${b.id}`,
          bookingStatus: b.bookingStatus || 'CONFIRMED',
          fareAmount: b.fareAmount || flight.basePrice || 5000,
          bookedAt: b.createdAt
        });
      }
    });

    this.flightPassengersList = records;
  }

  public get reportTotalBooked(): number {
    return this.flightPassengersList.length;
  }

  public get reportTotalCapacity(): number {
    return this.selectedFlightForReport?.totalSeats || 180;
  }

  public get reportAvailableSeats(): number {
    return Math.max(0, this.reportTotalCapacity - this.reportTotalBooked);
  }

  public get reportOccupancyPercent(): number {
    if (this.reportTotalCapacity === 0) return 0;
    return Math.min(100, Math.round((this.reportTotalBooked / this.reportTotalCapacity) * 100));
  }

  public get reportTotalRevenue(): number {
    return this.flightPassengersList.reduce((acc, curr) => acc + (Number(curr.fareAmount) || 0), 0);
  }

  public get reportBusinessBooked(): number {
    return this.flightPassengersList.filter(p => p.travelClass.includes('BUSIN')).length;
  }

  public get reportPremiumBooked(): number {
    return this.flightPassengersList.filter(p => p.travelClass.includes('PREM')).length;
  }

  public get reportEconomyBooked(): number {
    return this.flightPassengersList.filter(p => !p.travelClass.includes('BUSIN') && !p.travelClass.includes('PREM')).length;
  }

  public exportFlightReportCsv(): void {
    if (!this.selectedFlightForReport) return;
    const f = this.selectedFlightForReport;
    const reg = this.getRegistrationForFlight(f);
    const model = this.getAircraftModelForFlight(f);

    const lines: string[] = [
      `SkyWings Airlines - Flight Operational Manifest & Seat Assignment Report`,
      `Flight Number:,"${f.flightNumber}"`,
      `Operating Airline:,"${f.airline}"`,
      `Aircraft Model:,"${model}"`,
      `Tail / Registration:,"${reg}"`,
      `Route:,"${f.source || f.origin} -> ${f.destination}"`,
      `Departure Date:,"${f.departureDate}"`,
      `Departure Time:,"${f.departureTime}"`,
      `Arrival Date:,"${f.arrivalDate || f.departureDate}"`,
      `Arrival Time:,"${f.arrivalTime}"`,
      `Duration:,"${f.duration || '2h 15m'}"`,
      `Operational Status:,"${f.status}"`,
      `Total Aircraft Capacity:,"${this.reportTotalCapacity} Seats"`,
      `Total Booked Passengers:,"${this.reportTotalBooked} Passengers"`,
      `Available Seats:,"${this.reportAvailableSeats} Seats"`,
      `Flight Load Factor:,"${this.reportOccupancyPercent}%"`,
      `Total Booked Revenue:,"INR ${this.reportTotalRevenue.toLocaleString('en-IN')}"`,
      `Generated At:,"${this.reportGeneratedAt}"`,
      '',
      `"Seat Number","Cabin Class","Passenger Name","Customer Email","Customer Phone","Booking Reference","Booking Status","Fare (INR)"`
    ];

    if (this.flightPassengersList.length === 0) {
      lines.push(`"N/A","N/A","No passengers booked yet","N/A","N/A","N/A","N/A","0"`);
    } else {
      this.flightPassengersList.forEach(p => {
        lines.push([
          `"${p.seatNumber}"`,
          `"${p.travelClass}"`,
          `"${(p.passengerName || '').replace(/"/g, '""')}"`,
          `"${(p.customerEmail || '').replace(/"/g, '""')}"`,
          `"${(p.customerPhone || '').replace(/"/g, '""')}"`,
          `"${(p.bookingReference || '').replace(/"/g, '""')}"`,
          `"${(p.bookingStatus || '').replace(/"/g, '""')}"`,
          `"${p.fareAmount}"`
        ].join(','));
      });
    }

    const csvContent = lines.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `SkyWings_${f.flightNumber}_Passenger_Seat_Report_${Date.now()}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    this.toastService.success('CSV Downloaded', `Flight ${f.flightNumber} passenger & seat report exported to CSV.`);
  }

  public printFlightReport(): void {
    if (!this.selectedFlightForReport) return;
    const f = this.selectedFlightForReport;
    const reg = this.getRegistrationForFlight(f);
    const model = this.getAircraftModelForFlight(f);

    const printWin = window.open('', '_blank');
    if (!printWin) {
      window.print();
      return;
    }

    let tableRows = '';
    if (this.flightPassengersList.length === 0) {
      tableRows = `
        <tr>
          <td colspan="8" style="text-align: center; padding: 24px; color: #64748b; font-style: italic;">
            No passenger bookings currently recorded on this flight. All ${this.reportTotalCapacity} seats remain vacant & available for reservation.
          </td>
        </tr>
      `;
    } else {
      tableRows = this.flightPassengersList.map((p) => `
        <tr>
          <td style="font-weight: 900; font-family: monospace; color: #1e40af;">${p.seatNumber}</td>
          <td><span style="display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 9px; font-weight: bold; background: #f1f5f9; color: #334155;">${p.travelClass}</span></td>
          <td style="font-weight: bold; color: #0f172a;">${p.passengerName}</td>
          <td style="color: #475569;">${p.customerEmail}</td>
          <td style="color: #475569;">${p.customerPhone}</td>
          <td style="font-family: monospace; font-size: 10px; color: #64748b;">${p.bookingReference}</td>
          <td><span style="display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 9px; font-weight: bold; background: #ecfdf5; color: #047857;">${p.bookingStatus}</span></td>
          <td style="text-align: right; font-weight: bold; color: #0f172a;">₹${p.fareAmount.toLocaleString('en-IN')}</td>
        </tr>
      `).join('');
    }

    printWin.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Flight ${f.flightNumber} Manifest & Seat Report - SkyWings Airlines</title>
        <style>
          @page { size: A4 landscape; margin: 12mm; }
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #0f172a; margin: 0; padding: 16px; font-size: 11px; }
          .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 2px solid #1e3c72; padding-bottom: 12px; margin-bottom: 14px; }
          .brand { font-size: 20px; font-weight: 900; color: #1e3c72; }
          .subbrand { font-size: 11px; color: #64748b; margin-top: 2px; }
          .badge { background: #1e3c72; color: #ffffff; padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 10px; text-transform: uppercase; }
          
          .flight-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px 16px; margin-bottom: 14px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
          .card-item { display: flex; flex-direction: column; }
          .lbl { font-size: 9px; color: #64748b; text-transform: uppercase; font-weight: bold; margin-bottom: 2px; }
          .val { font-size: 12px; color: #0f172a; font-weight: bold; }
          
          .stats-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; margin-bottom: 16px; }
          .stat-box { background: #ffffff; border: 1px solid #cbd5e1; border-radius: 6px; padding: 8px 12px; text-align: center; }
          .stat-val { font-size: 16px; font-weight: 900; color: #1e3c72; }
          .stat-lbl { font-size: 9px; color: #64748b; text-transform: uppercase; font-weight: bold; margin-top: 2px; }
          
          table { width: 100%; border-collapse: collapse; margin-top: 8px; }
          th { background: #1e3c72; color: #ffffff; padding: 8px 10px; text-align: left; font-size: 10px; text-transform: uppercase; border: 1px solid #1e3c72; }
          td { padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 10px; border-right: 1px solid #f1f5f9; }
          tr:nth-child(even) { background-color: #f8fafc; }
          
          .footer { margin-top: 20px; padding-top: 10px; border-top: 1px solid #cbd5e1; display: flex; justify-content: space-between; font-size: 10px; color: #64748b; }
        </style>
      </head>
      <body>
        <div class="header">
          <div>
            <div class="brand">✈ SKYWINGS AIRLINES</div>
            <div class="subbrand">Flight Operations Command • Passenger Manifest & Seat Assignment Roster</div>
          </div>
          <div style="text-align: right;">
            <span class="badge">OFFICIAL FLIGHT REPORT</span>
            <div style="font-size: 11px; color: #475569; margin-top: 5px; font-weight: bold;">Flight ${f.flightNumber} (${f.airline})</div>
          </div>
        </div>

        <div class="flight-card">
          <div class="card-item"><span class="lbl">Route & Service</span><span class="val">${f.source || f.origin} → ${f.destination} (${f.duration || '2h 15m'})</span></div>
          <div class="card-item"><span class="lbl">Aircraft & Tail</span><span class="val">${model} • ${reg}</span></div>
          <div class="card-item"><span class="lbl">Departure Schedule</span><span class="val">${f.departureDate} at ${f.departureTime}</span></div>
          <div class="card-item"><span class="lbl">Operational Status</span><span class="val">${f.status}</span></div>
        </div>

        <div class="stats-grid">
          <div class="stat-box"><div class="stat-val">${this.reportTotalCapacity}</div><div class="stat-lbl">Fleet Capacity</div></div>
          <div class="stat-box"><div class="stat-val" style="color: #047857;">${this.reportTotalBooked}</div><div class="stat-lbl">Booked Passengers</div></div>
          <div class="stat-box"><div class="stat-val" style="color: #2563eb;">${this.reportAvailableSeats}</div><div class="stat-lbl">Available Seats</div></div>
          <div class="stat-box"><div class="stat-val" style="color: #7c3aed;">${this.reportOccupancyPercent}%</div><div class="stat-lbl">Load Factor</div></div>
          <div class="stat-box"><div class="stat-val" style="color: #0f766e;">₹${this.reportTotalRevenue.toLocaleString('en-IN')}</div><div class="stat-lbl">Gross Revenue</div></div>
        </div>

        <div style="font-size: 11px; font-weight: bold; margin-bottom: 4px; color: #334155; text-transform: uppercase;">
          Passenger Manifest & Seat Allocations (${this.reportTotalBooked} Recorded)
        </div>

        <table>
          <thead>
            <tr>
              <th>Seat</th>
              <th>Class</th>
              <th>Passenger Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Booking Reference</th>
              <th>Status</th>
              <th style="text-align: right;">Fare</th>
            </tr>
          </thead>
          <tbody>
            ${tableRows}
          </tbody>
        </table>

        <div class="footer">
          <div>Certified By: Aircraft Operations Lead • SkyWings Aviation Authority</div>
          <div>Generated: ${this.reportGeneratedAt} • Confidential Manifest Document</div>
        </div>
      </body>
      </html>
    `);

    printWin.document.close();
    printWin.focus();
    setTimeout(() => {
      printWin.print();
    }, 400);
  }

  // ==========================================
  // Flight CRUD Operations
  // ==========================================
  public getEmptyFlightForm() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateStr = tomorrow.toISOString().split('T')[0];

    return {
      id: 0,
      flightNumber: '',
      airline: 'Air India',
      aircraftModel: 'Airbus A380-800',
      source: 'DEL',
      destination: 'BOM',
      departureDate: dateStr,
      departureTime: '08:30',
      arrivalDate: dateStr,
      arrivalTime: '10:45',
      duration: '2h 15m',
      totalSeats: 180,
      availableSeats: 180,
      basePrice: 5500,
      status: 'SCHEDULED'
    };
  }

  public openCreateFlightModal(): void {
    this.modalMode = 'CREATE';
    this.flightForm = this.getEmptyFlightForm();
    this.showFlightModal = true;
  }

  public openEditFlightModal(f: Flight): void {
    this.modalMode = 'EDIT';
    this.flightForm = {
      id: f.id,
      flightNumber: f.flightNumber || '',
      airline: f.airline || 'Air India',
      aircraftModel: f.aircraftModel || 'Airbus A380-800',
      source: f.source || f.origin || 'DEL',
      destination: f.destination || 'BOM',
      departureDate: f.departureDate || new Date().toISOString().split('T')[0],
      departureTime: (f.departureTime || '08:30').substring(0, 5),
      arrivalDate: f.arrivalDate || f.departureDate || new Date().toISOString().split('T')[0],
      arrivalTime: (f.arrivalTime || '10:45').substring(0, 5),
      duration: f.duration || '2h 15m',
      totalSeats: f.totalSeats || 180,
      availableSeats: f.availableSeats ?? (f.totalSeats || 180),
      basePrice: f.basePrice || 5500,
      status: f.status || 'SCHEDULED'
    };
    this.showFlightModal = true;
  }

  public closeFlightModal(): void {
    this.showFlightModal = false;
    this.isFlightSubmitting = false;
  }

  public saveFlight(): void {
    if (!this.flightForm.flightNumber.trim()) {
      this.toastService.warning('Validation Error', 'Flight number is required (e.g. AI-202).');
      return;
    }
    if (!this.flightForm.source.trim() || !this.flightForm.destination.trim()) {
      this.toastService.warning('Validation Error', 'Source and destination airports are required.');
      return;
    }
    if (this.flightForm.source.trim().toUpperCase() === this.flightForm.destination.trim().toUpperCase()) {
      this.toastService.warning('Validation Error', 'Origin and destination cannot be identical.');
      return;
    }
    if (!this.flightForm.totalSeats || this.flightForm.totalSeats <= 0) {
      this.toastService.warning('Validation Error', 'Total seat capacity must be greater than zero.');
      return;
    }

    this.isFlightSubmitting = true;

    // Format times with seconds for backend LocalTime parsing
    const depTime = this.flightForm.departureTime.length === 5 
      ? this.flightForm.departureTime + ':00' 
      : this.flightForm.departureTime;
    const arrTime = this.flightForm.arrivalTime.length === 5 
      ? this.flightForm.arrivalTime + ':00' 
      : this.flightForm.arrivalTime;

    const payload: Partial<Flight> = {
      flightNumber: this.flightForm.flightNumber.trim().toUpperCase(),
      airline: this.flightForm.airline.trim(),
      aircraftModel: this.flightForm.aircraftModel.trim(),
      source: this.flightForm.source.trim().toUpperCase(),
      destination: this.flightForm.destination.trim().toUpperCase(),
      departureDate: this.flightForm.departureDate,
      departureTime: depTime,
      arrivalDate: this.flightForm.arrivalDate || this.flightForm.departureDate,
      arrivalTime: arrTime,
      duration: this.flightForm.duration || '2h 15m',
      totalSeats: Number(this.flightForm.totalSeats),
      availableSeats: Number(this.flightForm.availableSeats),
      basePrice: Number(this.flightForm.basePrice),
      status: this.flightForm.status.toUpperCase()
    };

    if (this.modalMode === 'CREATE') {
      this.flightService.createFlight(payload).subscribe({
        next: (created) => {
          this.isFlightSubmitting = false;
          this.toastService.success(
            'Flight Created',
            `Flight ${created.flightNumber} (${created.source} → ${created.destination}) successfully scheduled.`
          );
          this.closeFlightModal();
          this.loadData();
        },
        error: (err) => {
          this.isFlightSubmitting = false;
          this.toastService.error('Creation Failed', err?.error?.message || 'Could not register new flight.');
        }
      });
    } else {
      this.flightService.updateFlight(this.flightForm.id, payload).subscribe({
        next: (updated) => {
          this.isFlightSubmitting = false;
          this.toastService.success(
            'Flight Updated',
            `Flight ${updated.flightNumber} details have been updated successfully.`
          );
          this.closeFlightModal();
          this.loadData();
        },
        error: (err) => {
          this.isFlightSubmitting = false;
          this.toastService.error('Update Failed', err?.error?.message || 'Could not update flight details.');
        }
      });
    }
  }

  public openDeleteModal(f: Flight): void {
    this.flightToDelete = f;
    this.showDeleteModal = true;
  }

  public closeDeleteModal(): void {
    this.flightToDelete = null;
    this.showDeleteModal = false;
  }

  public confirmDeleteFlight(): void {
    if (!this.flightToDelete) return;

    const id = this.flightToDelete.id;
    const fNum = this.flightToDelete.flightNumber;

    this.flightService.deleteFlight(id).subscribe({
      next: () => {
        this.toastService.success('Flight Deleted', `Flight ${fNum} has been permanently deleted from fleet.`);
        this.closeDeleteModal();
        this.loadData();
      },
      error: (err) => {
        this.toastService.error('Delete Failed', err?.error?.message || 'Could not delete flight.');
      }
    });
  }

  public quickUpdateStatus(f: Flight, newStatus: string): void {
    this.flightService.updateFlight(f.id, { ...f, status: newStatus }).subscribe({
      next: () => {
        f.status = newStatus;
        this.toastService.success('Status Updated', `Flight ${f.flightNumber} status set to ${newStatus}.`);
      },
      error: (err) => {
        this.toastService.error('Status Update Failed', err?.error?.message || 'Could not update flight status.');
      }
    });
  }

  public selectFlightForCabinConfig(f: Flight): void {
    this.onFlightChange(f.id);
    // Scroll smoothly to the configurator
    const el = document.getElementById('cabin-configurator');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
    this.toastService.info('Flight Selected', `Configurator loaded for flight ${f.flightNumber} (${this.config.registrationNumber} • ${this.config.aircraftModel}).`);
  }

  // ==========================================
  // Aircraft Cabin Configurator & Airframe Sync
  // ==========================================
  public getAircraftModelForFlight(f: Flight): string {
    if (!f) return 'Airbus A380-800';
    if (this.airframeCache[f.id]?.model) {
      return this.airframeCache[f.id].model;
    }
    if (f.aircraftModel && f.aircraftModel.trim()) {
      return f.aircraftModel.trim();
    }
    if ((f.totalSeats || 0) >= 220 || f.destination === 'DXB' || f.source === 'DXB' || f.destination === 'SIN') {
      return 'Boeing 787-9 Dreamliner';
    }
    const prefix = (f.flightNumber || '').split('-')[0].trim().toUpperCase();
    if (this.AIRLINE_PRESETS[prefix]) {
      return this.AIRLINE_PRESETS[prefix].model;
    }
    const airlineUpper = (f.airline || '').toUpperCase();
    if (airlineUpper.includes('AIR INDIA EXPRESS')) return 'Boeing 737-800';
    if (airlineUpper.includes('AIR INDIA')) return 'Airbus A350-900';
    if (airlineUpper.includes('INDIGO')) return 'Airbus A320neo';
    if (airlineUpper.includes('VISTARA')) return 'Airbus A321neo';
    if (airlineUpper.includes('SPICEJET')) return 'Boeing 737 MAX 8';
    if (airlineUpper.includes('EMIRATES')) return 'Airbus A380-800';

    return 'Airbus A320neo';
  }

  public getRegistrationForFlight(f: Flight): string {
    if (!f) return 'VT-AIR380';
    if (this.airframeCache[f.id]?.registration) {
      return this.airframeCache[f.id].registration;
    }
    const cleanNum = (f.flightNumber || '').replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
    const prefix = (f.flightNumber || '').split('-')[0].trim().toUpperCase();
    const preset = this.AIRLINE_PRESETS[prefix];
    if (preset) {
      const digits = (f.flightNumber || '').replace(/[^0-9]/g, '');
      return `${preset.regPrefix}${digits || f.id}`;
    }
    return `VT-${cleanNum || ('FL' + f.id)}`;
  }

  // 1. When Assign to Operational Flight changes: update model & registration
  public onFlightChange(flightId: any): void {
    const numericId = Number(flightId);
    this.config.flightId = numericId;
    const flight = this.flights.find(f => f.id === numericId);
    if (!flight) return;

    this.config.aircraftModel = this.getAircraftModelForFlight(flight);
    this.config.registrationNumber = this.getRegistrationForFlight(flight);

    // Auto-detect layout based on airframe model
    const m = this.config.aircraftModel.toLowerCase();
    if (m.includes('380') || m.includes('787') || m.includes('777') || m.includes('350')) {
      this.config.layoutType = '3x3';
    } else if (m.includes('atr') || m.includes('q400') || m.includes('crj')) {
      this.config.layoutType = '2x2';
    } else {
      this.config.layoutType = '3x3';
    }

    this.syncSeatsBreakdown(flight.totalSeats || 180);
  }

  // 2. Vice versa: When Tail / Registration Number changes, update flight & model
  public onRegistrationChange(reg: string): void {
    const cleanReg = (reg || '').toUpperCase();
    this.config.registrationNumber = cleanReg;
    if (!cleanReg) return;

    // Vice versa: match flight from registration
    const matched = this.flights.find(f => {
      const r = this.getRegistrationForFlight(f).toUpperCase();
      const numOnly = f.flightNumber.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
      return r === cleanReg || cleanReg.endsWith(numOnly);
    });

    if (matched && matched.id !== this.config.flightId) {
      this.config.flightId = matched.id;
      this.config.aircraftModel = this.getAircraftModelForFlight(matched);
      this.syncSeatsBreakdown(matched.totalSeats || 180);
      this.toastService.info('Flight Switched', `Selected flight ${matched.flightNumber} via tail ${cleanReg}.`);
    } else if (this.config.flightId) {
      // User is customizing registration for currently selected flight
      if (!this.airframeCache[this.config.flightId]) {
        this.airframeCache[this.config.flightId] = {
          model: this.config.aircraftModel,
          registration: cleanReg
        };
      } else {
        this.airframeCache[this.config.flightId].registration = cleanReg;
      }
      this.saveAirframeCache();
    }
  }

  // 3. Vice versa: When Aircraft Model changes, update layout or switch to matched flight
  public onAircraftModelChange(modelName: string): void {
    this.config.aircraftModel = modelName;
    if (!modelName) return;

    const query = modelName.trim().toLowerCase();

    // Auto-adjust layout
    if (query.includes('380') || query.includes('787') || query.includes('777') || query.includes('350')) {
      this.config.layoutType = '3x3';
    } else if (query.includes('atr') || query.includes('q400') || query.includes('crj')) {
      this.config.layoutType = '2x2';
    }

    // Vice versa: find if another flight operates this aircraft model
    const matched = this.flights.find(f => {
      const m = this.getAircraftModelForFlight(f).toLowerCase();
      return m === query && f.id !== this.config.flightId;
    });

    if (matched) {
      this.config.flightId = matched.id;
      this.config.registrationNumber = this.getRegistrationForFlight(matched);
      this.syncSeatsBreakdown(matched.totalSeats || 180);
      this.toastService.info('Flight Linked', `Linked to flight ${matched.flightNumber} operating ${modelName}.`);
    } else if (this.config.flightId) {
      // User is updating model for current flight
      if (!this.airframeCache[this.config.flightId]) {
        this.airframeCache[this.config.flightId] = {
          model: modelName,
          registration: this.config.registrationNumber
        };
      } else {
        this.airframeCache[this.config.flightId].model = modelName;
      }
      this.saveAirframeCache();
    }
  }

  private syncSeatsBreakdown(totalSeats: number): void {
    if (totalSeats <= 0) return;
    if (totalSeats >= 200) {
      this.config.businessSeats = 30;
      this.config.premiumSeats = 40;
      this.config.economySeats = totalSeats - 70;
    } else if (totalSeats >= 150) {
      this.config.businessSeats = 20;
      this.config.premiumSeats = 30;
      this.config.economySeats = totalSeats - 50;
    } else {
      this.config.businessSeats = 12;
      this.config.premiumSeats = 18;
      this.config.economySeats = totalSeats - 30;
    }
  }

  public get computedTotalCapacity(): number {
    return (Number(this.config.businessSeats) || 0) +
           (Number(this.config.premiumSeats) || 0) +
           (Number(this.config.economySeats) || 0);
  }

  public get computedTotalRows(): number {
    const seatsPerRow = this.config.layoutType === '3x3' ? 6 : 4;
    return Math.ceil(this.computedTotalCapacity / seatsPerRow);
  }

  public applyAircraftConfiguration(): void {
    if (!this.config.flightId) {
      this.toastService.warning('Select Flight', 'Please select a flight to apply the cabin configuration.');
      return;
    }

    const total = this.computedTotalCapacity;
    if (total <= 0) {
      this.toastService.warning('Invalid Capacity', 'Total seat capacity must be greater than 0.');
      return;
    }

    this.isSubmitting = true;
    const targetFlight = this.flights.find(f => f.id === Number(this.config.flightId));

    if (targetFlight) {
      // Persist in airframe cache
      this.airframeCache[targetFlight.id] = {
        model: this.config.aircraftModel,
        registration: this.config.registrationNumber
      };
      this.saveAirframeCache();
      targetFlight.aircraftModel = this.config.aircraftModel;

      const updatedFlight: Partial<Flight> = {
        ...targetFlight,
        totalSeats: total,
        availableSeats: total,
        aircraftModel: this.config.aircraftModel
      };

      this.flightService.updateFlight(targetFlight.id, updatedFlight).subscribe({
        next: () => {
          // Generate precise cabin seat records in seat-service
          this.seatService.configureCabinSeats({
            flightId: targetFlight.id,
            businessSeats: Number(this.config.businessSeats) || 20,
            premiumEconomySeats: Number(this.config.premiumSeats) || 30,
            economySeats: Number(this.config.economySeats) || 100,
            columnsPerRow: this.config.layoutType === '3x3' ? 6 : 4,
            clearExisting: true
          }).subscribe({
            next: () => {
              this.isSubmitting = false;
              this.toastService.success(
                'Configuration Applied',
                `Successfully configured ${this.config.aircraftModel} (${this.config.registrationNumber}, ${total} seats) for flight ${targetFlight.flightNumber}.`
              );
              this.loadData();
            },
            error: () => {
              this.isSubmitting = false;
              this.toastService.success(
                'Flight Updated',
                `Flight ${targetFlight.flightNumber} capacity updated to ${total} seats.`
              );
              this.loadData();
            }
          });
        },
        error: (err) => {
          this.isSubmitting = false;
          this.toastService.error('Update Failed', err?.error?.message || 'Could not update flight configuration.');
        }
      });
    } else {
      this.isSubmitting = false;
    }
  }

  // ==========================================
  // Workspace Tab Switcher
  // ==========================================
  public setActiveTab(tab: 'FLIGHTS' | 'AIRCRAFT' | 'MEALS' | 'SCHEDULES' | 'CABIN'): void {
    this.activeTab = tab;
    if (tab === 'AIRCRAFT' && this.aircraftList.length === 0) {
      this.loadAircraft();
    } else if (tab === 'MEALS' && this.mealList.length === 0) {
      this.loadMeals();
    } else if (tab === 'SCHEDULES' && this.scheduleList.length === 0) {
      this.loadSchedules();
    }
  }

  // ==========================================
  // Aircraft Fleet Management
  // ==========================================
  public loadAircraft(): void {
    this.isAircraftLoading = true;
    this.flightService.getAllAircraft().subscribe({
      next: (list) => {
        this.aircraftList = list || [];
        this.isAircraftLoading = false;
      },
      error: () => {
        this.aircraftList = [];
        this.isAircraftLoading = false;
      }
    });
  }

  public openCreateAircraftModal(): void {
    this.aircraftModalMode = 'CREATE';
    this.aircraftForm = {
      id: 0,
      model: '',
      registrationNumber: '',
      totalCapacity: 180,
      seatLayout: '3x3',
      status: 'ACTIVE',
      airline: 'SkyWings'
    };
    this.showAircraftModal = true;
  }

  public openEditAircraftModal(a: Aircraft): void {
    this.aircraftModalMode = 'EDIT';
    this.aircraftForm = {
      id: a.id,
      model: a.model,
      registrationNumber: a.registrationNumber,
      totalCapacity: a.totalCapacity,
      seatLayout: a.seatLayout || '3x3',
      status: a.status || 'ACTIVE',
      airline: a.airline || 'SkyWings'
    };
    this.showAircraftModal = true;
  }

  public closeAircraftModal(): void {
    this.showAircraftModal = false;
  }

  public saveAircraft(): void {
    if (!this.aircraftForm.model.trim()) {
      this.toastService.warning('Validation', 'Aircraft model name is required.');
      return;
    }
    if (!this.aircraftForm.registrationNumber.trim()) {
      this.toastService.warning('Validation', 'Tail/Registration number is required.');
      return;
    }
    if (!this.aircraftForm.totalCapacity || this.aircraftForm.totalCapacity <= 0) {
      this.toastService.warning('Validation', 'Total capacity must be greater than 0.');
      return;
    }

    this.isAircraftSubmitting = true;
    if (this.aircraftModalMode === 'CREATE') {
      this.flightService.createAircraft(this.aircraftForm).subscribe({
        next: (created) => {
          this.isAircraftSubmitting = false;
          this.toastService.success('Aircraft Registered', `${created.model} (${created.registrationNumber}) added to fleet.`);
          this.closeAircraftModal();
          this.loadAircraft();
        },
        error: (err) => {
          this.isAircraftSubmitting = false;
          this.toastService.error('Error', err?.error?.message || 'Could not register aircraft.');
        }
      });
    } else {
      this.flightService.updateAircraft(this.aircraftForm.id, this.aircraftForm).subscribe({
        next: (updated) => {
          this.isAircraftSubmitting = false;
          this.toastService.success('Aircraft Updated', `${updated.model} details saved.`);
          this.closeAircraftModal();
          this.loadAircraft();
        },
        error: (err) => {
          this.isAircraftSubmitting = false;
          this.toastService.error('Error', err?.error?.message || 'Could not update aircraft.');
        }
      });
    }
  }

  public toggleAircraftStatus(a: Aircraft): void {
    const nextStatus = a.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.flightService.updateAircraftStatus(a.id, nextStatus).subscribe({
      next: (res) => {
        a.status = res.status;
        this.toastService.success('Aircraft Status', `${a.model} (${a.registrationNumber}) is now ${a.status}.`);
      },
      error: (err) => {
        this.toastService.error('Error', err?.error?.message || 'Failed to update status.');
      }
    });
  }

  // ==========================================
  // Meal Options Management
  // ==========================================
  public loadMeals(): void {
    this.isMealsLoading = true;
    this.flightService.getAllMealOptions().subscribe({
      next: (list) => {
        this.mealList = list && list.length > 0 ? list : this.getDefaultMeals();
        this.isMealsLoading = false;
      },
      error: () => {
        this.mealList = this.getDefaultMeals();
        this.isMealsLoading = false;
      }
    });
  }

  private getDefaultMeals(): MealOption[] {
    return [
      { id: 1, name: 'Vegetarian Royal Thali', category: 'VEGETARIAN', dietaryType: 'VEGETARIAN', price: 0, complimentary: true, available: true, description: 'Traditional paneer makhani, dal tadka, jeera rice & warm paratha.' },
      { id: 2, name: 'Grilled Herb Chicken', category: 'NON_VEGETARIAN', dietaryType: 'NON_VEGETARIAN', price: 450, complimentary: false, available: true, description: 'Herb roasted chicken breast with rosemary sauce & butter tossed veggies.' },
      { id: 3, name: 'Vegan Quinoa Power Bowl', category: 'VEGAN', dietaryType: 'VEGAN', price: 350, complimentary: false, available: true, description: 'Tri-color quinoa, roasted chickpeas, fresh avocado & lemon tahini.' },
      { id: 4, name: 'Jain Special Feast', category: 'JAIN', dietaryType: 'JAIN', price: 0, complimentary: true, available: true, description: 'Prepared strictly without root vegetables, onion, or garlic.' },
      { id: 5, name: 'Gluten-Free Platter', category: 'GLUTEN_FREE', dietaryType: 'GLUTEN_FREE', price: 400, complimentary: false, available: true, description: 'Naturally gluten-free steamed millet bowl with grilled tofu & fruit.' },
      { id: 6, name: 'Kids Junior Snack Box', category: 'CHILD', dietaryType: 'CHILD', price: 250, complimentary: false, available: true, description: 'Mini cheese sandwich, fruit cup, chocolate cookie & juice box.' },
      { id: 7, name: 'No Meal Service', category: 'NO_MEAL', dietaryType: 'NO_MEAL', price: 0, complimentary: true, available: true, description: 'Opt out of meal service during this flight segment.' }
    ];
  }

  public openCreateMealModal(): void {
    this.mealModalMode = 'CREATE';
    this.mealForm = {
      id: 0,
      name: '',
      category: 'VEGETARIAN',
      description: '',
      dietaryType: 'VEGETARIAN',
      price: 0,
      complimentary: true,
      available: true,
      status: 'ACTIVE',
      inventoryLimit: 100,
      aircraftId: null
    };
    this.showMealModal = true;
  }

  public openEditMealModal(m: MealOption): void {
    this.mealModalMode = 'EDIT';
    this.mealForm = {
      id: m.id,
      name: m.name,
      category: m.category,
      description: m.description || '',
      dietaryType: m.dietaryType || m.category,
      price: m.price || 0,
      complimentary: !!m.complimentary,
      available: m.available !== false,
      status: m.status || 'ACTIVE',
      inventoryLimit: m.inventoryLimit || 100,
      aircraftId: m.aircraftId || null
    };
    this.showMealModal = true;
  }

  public closeMealModal(): void {
    this.showMealModal = false;
  }

  public saveMeal(): void {
    if (!this.mealForm.name.trim()) {
      this.toastService.warning('Validation', 'Meal name is required.');
      return;
    }

    this.isMealSubmitting = true;
    const payload = {
      ...this.mealForm,
      dietaryType: this.mealForm.dietaryType || this.mealForm.category,
      price: this.mealForm.complimentary ? 0 : Number(this.mealForm.price)
    };

    if (this.mealModalMode === 'CREATE') {
      this.flightService.createMealOption(payload).subscribe({
        next: (created) => {
          this.isMealSubmitting = false;
          this.toastService.success('Meal Created', `"${created.name}" added to in-flight options.`);
          this.closeMealModal();
          this.loadMeals();
        },
        error: (err) => {
          this.isMealSubmitting = false;
          this.toastService.error('Error', err?.error?.message || 'Could not create meal option.');
        }
      });
    } else {
      this.flightService.updateMealOption(this.mealForm.id, payload).subscribe({
        next: (updated) => {
          this.isMealSubmitting = false;
          this.toastService.success('Meal Updated', `"${updated.name}" updated successfully.`);
          this.closeMealModal();
          this.loadMeals();
        },
        error: (err) => {
          this.isMealSubmitting = false;
          this.toastService.error('Error', err?.error?.message || 'Could not update meal option.');
        }
      });
    }
  }

  public toggleMealAvailability(m: MealOption): void {
    const nextVal = !m.available;
    this.flightService.toggleMealAvailability(m.id, nextVal).subscribe({
      next: (res) => {
        m.available = res.available;
        this.toastService.success('Meal Status', `"${m.name}" is now ${m.available ? 'AVAILABLE' : 'UNAVAILABLE'}.`);
      },
      error: (err) => {
        this.toastService.error('Error', err?.error?.message || 'Failed to toggle availability.');
      }
    });
  }

  // ==========================================
  // Weekly Flight Schedules Management
  // ==========================================
  public loadSchedules(): void {
    this.isSchedulesLoading = true;
    this.flightService.getAllSchedules().subscribe({
      next: (list) => {
        this.scheduleList = list || [];
        this.isSchedulesLoading = false;
      },
      error: () => {
        this.scheduleList = [];
        this.isSchedulesLoading = false;
      }
    });
  }

  public openCreateScheduleModal(): void {
    this.scheduleForm = {
      scheduleCode: 'SCH-' + Math.floor(100 + Math.random() * 900),
      flightNumber: 'SW-80' + Math.floor(1 + Math.random() * 9),
      airline: 'SkyWings',
      origin: 'DEL',
      destination: 'BOM',
      departureTime: '08:00',
      arrivalTime: '10:15',
      aircraftId: this.aircraftList[0]?.id || 1,
      baseFare: 4500,
      totalCapacity: 180,
      startDate: '2026-11-01',
      endDate: '2026-12-31',
      operatingDays: ['MONDAY', 'WEDNESDAY', 'FRIDAY'],
      status: 'ACTIVE'
    };
    this.showScheduleModal = true;
  }

  public closeScheduleModal(): void {
    this.showScheduleModal = false;
  }

  public toggleOperatingDay(day: string): void {
    if (this.scheduleForm.operatingDays.includes(day)) {
      if (this.scheduleForm.operatingDays.length <= 1) {
        this.toastService.warning('Schedule Validation', 'At least one operating day must be selected.');
        return;
      }
      this.scheduleForm.operatingDays = this.scheduleForm.operatingDays.filter(d => d !== day);
    } else {
      this.scheduleForm.operatingDays = [...this.scheduleForm.operatingDays, day];
    }
  }

  public isOperatingDaySelected(day: string): boolean {
    return this.scheduleForm.operatingDays.includes(day);
  }

  public saveSchedule(): void {
    if (!this.scheduleForm.scheduleCode.trim()) {
      this.toastService.warning('Validation', 'Schedule code is required.');
      return;
    }
    if (!this.scheduleForm.flightNumber.trim()) {
      this.toastService.warning('Validation', 'Flight number is required.');
      return;
    }
    if (this.scheduleForm.origin.toUpperCase() === this.scheduleForm.destination.toUpperCase()) {
      this.toastService.warning('Validation', 'Origin and destination airports must differ.');
      return;
    }
    if (!this.scheduleForm.startDate || !this.scheduleForm.endDate) {
      this.toastService.warning('Validation', 'Start and end validity dates are required.');
      return;
    }
    if (this.scheduleForm.startDate > this.scheduleForm.endDate) {
      this.toastService.warning('Validation', 'Start date cannot be after end date.');
      return;
    }
    if (this.scheduleForm.operatingDays.length === 0) {
      this.toastService.warning('Validation', 'Select at least one operating day (Mon–Sun).');
      return;
    }

    this.isScheduleSubmitting = true;
    const payload: any = {
      serviceNumber: this.scheduleForm.flightNumber || this.scheduleForm.scheduleCode,
      scheduleCode: this.scheduleForm.scheduleCode,
      flightNumber: this.scheduleForm.flightNumber,
      airline: this.scheduleForm.airline || 'SkyWings',
      source: this.scheduleForm.origin,
      origin: this.scheduleForm.origin,
      destination: this.scheduleForm.destination,
      departureTime: this.scheduleForm.departureTime,
      arrivalTime: this.scheduleForm.arrivalTime,
      aircraftId: Number(this.scheduleForm.aircraftId) || 1,
      baseFare: Number(this.scheduleForm.baseFare),
      totalSeats: Number(this.scheduleForm.totalCapacity),
      totalCapacity: Number(this.scheduleForm.totalCapacity),
      operatingDays: this.scheduleForm.operatingDays.join(','),
      operatingDaysList: this.scheduleForm.operatingDays,
      startDate: this.scheduleForm.startDate,
      endDate: this.scheduleForm.endDate,
      status: this.scheduleForm.status || 'ACTIVE',
      generateFlightsNow: true
    };

    this.flightService.createSchedule(payload).subscribe({
      next: (created) => {
        this.isScheduleSubmitting = false;
        this.toastService.success(
          'Schedule Created',
          `Schedule ${created.scheduleCode || created.serviceNumber} created with operating days.`
        );
        this.closeScheduleModal();
        this.loadSchedules();
        this.loadData();
      },
      error: (err) => {
        this.isScheduleSubmitting = false;
        this.toastService.error('Error', err?.error?.message || 'Could not create recurring schedule.');
      }
    });
  }

  public generateFlightsForSchedule(s: WeeklySchedule): void {
    this.isGeneratingScheduleFlights = true;
    this.flightService.generateScheduleFlights(s.id).subscribe({
      next: (res) => {
        this.isGeneratingScheduleFlights = false;
        this.toastService.success(
          'Flight Instances Generated',
          `${res.generatedFlightsCount} individual flights generated for schedule ${s.scheduleCode}.`
        );
        this.loadSchedules();
        this.loadData();
      },
      error: (err) => {
        this.isGeneratingScheduleFlights = false;
        this.toastService.error('Generation Failed', err?.error?.message || 'Could not generate schedule flight instances.');
      }
    });
  }

  public toggleScheduleStatus(s: WeeklySchedule): void {
    const nextStatus = s.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    this.flightService.updateScheduleStatus(s.id, nextStatus).subscribe({
      next: (res) => {
        s.status = res.status;
        this.toastService.success('Schedule Status', `Schedule ${s.scheduleCode} is now ${s.status}.`);
      },
      error: (err) => {
        this.toastService.error('Error', err?.error?.message || 'Failed to update schedule status.');
      }
    });
  }
}
