import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FlightService } from '../../../core/services/flight.service';
import { TrackingService } from '../../../core/services/tracking.service';
import { Flight } from '../../../core/models/flight.models';
import { FlightTracking } from '../../../core/models/tracking.models';
import { StatusBadgeDirective } from '../../../shared/directives/status-badge.directive';
import { ToastService } from '../../../core/services/toast.service';
import { NotificationService } from '../../../core/services/notification.service';
import { BaggageService } from '../../../core/services/baggage.service';
import { BaggageRecord, BaggageStatus, BaggageType, NewBaggageCheckinRequest, SpecialHandlingType } from '../../../core/models/baggage.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';

@Component({
  selector: 'app-airport-console',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, StatusBadgeDirective, CurrencyFormatterPipe],
  templateUrl: './airport-console.component.html'
})
export class AirportConsoleComponent implements OnInit {
  private flightService = inject(FlightService);
  private trackingService = inject(TrackingService);
  private notificationService = inject(NotificationService);
  private toastService = inject(ToastService);
  private baggageService = inject(BaggageService);

  // Flight Operations State
  public flights: Flight[] = [];
  public activeTracking: FlightTracking[] = [];
  public selectedFlight: Flight | null = null;
  public newGate = '';
  public delayMinutes = 0;
  public delayReason = '';
  public isLoading = false;

  // View / Tab State: 'baggage' | 'flights' | 'all'
  public activeTab: 'baggage' | 'flights' | 'all' = 'all';

  // Flight Table Pagination & Filter State
  public flightSearchTerm = '';
  public flightStatusFilter = 'ALL';
  public flightCurrentPage = 1;
  public flightPageSize = 5;
  public flightPageSizeOptions: number[] = [5, 10, 20, 50];

  // Baggage Operations State
  public baggageRecords: BaggageRecord[] = [];
  public isBaggageLoading = false;

  // Baggage Filter & Search State
  public baggageSearchTerm = '';
  public baggageStatusFilter: string = 'ALL';
  public baggageTypeFilter: string = 'ALL';
  public baggageFlightFilter: string = 'ALL';
  public baggageHandlingFilter: string = 'ALL';

  // Baggage Table Pagination State
  public baggageCurrentPage = 1;
  public baggagePageSize = 5;
  public baggagePageSizeOptions: number[] = [5, 10, 20, 50];

  // Edit / Details Baggage Modal State
  public isEditModalOpen = false;
  public selectedBaggage: BaggageRecord | null = null;
  public editForm: Partial<BaggageRecord> = {};

  // Counter Check-in Modal State
  public isCounterCheckInOpen = false;
  public newCheckIn: NewBaggageCheckinRequest = this.getEmptyCounterCheckIn();

  // Print Luggage Tag Modal State
  public isTagModalOpen = false;
  public selectedTagBaggage: BaggageRecord | null = null;

  ngOnInit(): void {
    this.loadData();
  }

  public loadData(): void {
    this.isLoading = true;
    this.flightService.getAllFlights().subscribe({
      next: (f) => {
        this.flights = f || [];
        if (this.flights.length > 0 && !this.selectedFlight) {
          this.selectedFlight = this.flights[0];
          this.newGate = this.selectedFlight.gate || '';
        }
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });

    this.trackingService.getActiveTracking().subscribe({
      next: (t) => this.activeTracking = t || [],
      error: () => {}
    });

    this.loadBaggageRecords();
  }

  public loadBaggageRecords(): void {
    this.isBaggageLoading = true;
    this.baggageService.getBaggageRecords().subscribe({
      next: (records) => {
        this.baggageRecords = records || [];
        this.isBaggageLoading = false;
      },
      error: () => {
        this.isBaggageLoading = false;
      }
    });
  }

  // =========================================================================
  // FLIGHT OPERATIONS & PAGINATION
  // =========================================================================

  public selectFlight(flight: Flight): void {
    this.selectedFlight = flight;
    this.newGate = flight.gate || '';
  }

  public updateGate(): void {
    if (!this.selectedFlight || !this.newGate) return;
    this.selectedFlight.gate = this.newGate;
    this.toastService.success('Gate Assigned', `Flight ${this.selectedFlight.flightNumber} gate updated to ${this.newGate}`);
  }

  public postDelay(): void {
    if (!this.selectedFlight || this.delayMinutes <= 0) return;

    this.notificationService.sendFlightDelayed({
      recipient: 'sindhusakthi41@gmail.com',
      flightNumber: this.selectedFlight.flightNumber,
      source: this.selectedFlight.source,
      destination: this.selectedFlight.destination,
      delayMinutes: this.delayMinutes,
      delayReason: this.delayReason || 'Air Traffic Control Sequencing & Weather Advisory'
    }).subscribe();

    this.toastService.warning('Delay Broadcasted', `Flight ${this.selectedFlight.flightNumber} delayed by ${this.delayMinutes} mins. Advisory notice PDF sent to email.`);
    this.delayMinutes = 0;
    this.delayReason = '';
  }

  public get filteredFlights(): Flight[] {
    let result = this.flights;

    if (this.flightStatusFilter !== 'ALL') {
      result = result.filter(f => (f.status || '').toUpperCase() === this.flightStatusFilter);
    }

    if (this.flightSearchTerm.trim()) {
      const term = this.flightSearchTerm.trim().toLowerCase();
      result = result.filter(f =>
        (f.flightNumber || '').toLowerCase().includes(term) ||
        (f.airline || '').toLowerCase().includes(term) ||
        (f.source || f.origin || '').toLowerCase().includes(term) ||
        (f.destination || '').toLowerCase().includes(term) ||
        (f.gate || '').toLowerCase().includes(term)
      );
    }

    return result;
  }

  public onFlightFilterChange(): void {
    this.flightCurrentPage = 1;
  }

  public get flightTotalPages(): number {
    return Math.ceil(this.filteredFlights.length / this.flightPageSize) || 1;
  }

  public get flightStartItemIndex(): number {
    return this.filteredFlights.length === 0 ? 0 : (this.flightCurrentPage - 1) * this.flightPageSize + 1;
  }

  public get flightEndItemIndex(): number {
    return Math.min(this.flightCurrentPage * this.flightPageSize, this.filteredFlights.length);
  }

  public get paginatedFlights(): Flight[] {
    const start = (this.flightCurrentPage - 1) * this.flightPageSize;
    return this.filteredFlights.slice(start, start + this.flightPageSize);
  }

  public get flightPageNumbers(): number[] {
    const total = this.flightTotalPages;
    const current = this.flightCurrentPage;
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

  public setFlightPage(page: number): void {
    if (page >= 1 && page <= this.flightTotalPages) {
      this.flightCurrentPage = page;
    }
  }

  public nextFlightPage(): void {
    if (this.flightCurrentPage < this.flightTotalPages) {
      this.flightCurrentPage++;
    }
  }

  public prevFlightPage(): void {
    if (this.flightCurrentPage > 1) {
      this.flightCurrentPage--;
    }
  }

  public onFlightPageSizeChange(newSize: any): void {
    this.flightPageSize = Number(newSize);
    this.flightCurrentPage = 1;
  }

  // =========================================================================
  // BAGGAGE OPERATIONS & PAGINATION
  // =========================================================================

  public get uniqueFlightNumbers(): string[] {
    const set = new Set<string>();
    this.baggageRecords.forEach(r => {
      if (r.flightNumber) set.add(r.flightNumber.trim().toUpperCase());
    });
    return Array.from(set).sort();
  }

  public get filteredBaggage(): BaggageRecord[] {
    let result = this.baggageRecords;

    if (this.baggageStatusFilter !== 'ALL') {
      result = result.filter(b => b.baggageStatus === this.baggageStatusFilter);
    }

    if (this.baggageTypeFilter !== 'ALL') {
      result = result.filter(b => b.baggageType === this.baggageTypeFilter);
    }

    if (this.baggageFlightFilter !== 'ALL') {
      result = result.filter(b => (b.flightNumber || '').trim().toUpperCase() === this.baggageFlightFilter);
    }

    if (this.baggageHandlingFilter !== 'ALL') {
      result = result.filter(b => b.specialHandling === this.baggageHandlingFilter);
    }

    if (this.baggageSearchTerm.trim()) {
      const term = this.baggageSearchTerm.trim().toLowerCase();
      result = result.filter(b =>
        (b.passengerName || '').toLowerCase().includes(term) ||
        (b.bookingReference || '').toLowerCase().includes(term) ||
        (b.flightNumber || '').toLowerCase().includes(term) ||
        (b.bagTagNumber || '').toLowerCase().includes(term) ||
        (b.counterLocation || '').toLowerCase().includes(term) ||
        (b.origin || '').toLowerCase().includes(term) ||
        (b.destination || '').toLowerCase().includes(term)
      );
    }

    return result;
  }

  public onBaggageFilterChange(): void {
    this.baggageCurrentPage = 1;
  }

  public get baggageTotalPages(): number {
    return Math.ceil(this.filteredBaggage.length / this.baggagePageSize) || 1;
  }

  public get baggageStartItemIndex(): number {
    return this.filteredBaggage.length === 0 ? 0 : (this.baggageCurrentPage - 1) * this.baggagePageSize + 1;
  }

  public get baggageEndItemIndex(): number {
    return Math.min(this.baggageCurrentPage * this.baggagePageSize, this.filteredBaggage.length);
  }

  public get paginatedBaggage(): BaggageRecord[] {
    const start = (this.baggageCurrentPage - 1) * this.baggagePageSize;
    return this.filteredBaggage.slice(start, start + this.baggagePageSize);
  }

  public get baggagePageNumbers(): number[] {
    const total = this.baggageTotalPages;
    const current = this.baggageCurrentPage;
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

  public setBaggagePage(page: number): void {
    if (page >= 1 && page <= this.baggageTotalPages) {
      this.baggageCurrentPage = page;
    }
  }

  public nextBaggagePage(): void {
    if (this.baggageCurrentPage < this.baggageTotalPages) {
      this.baggageCurrentPage++;
    }
  }

  public prevBaggagePage(): void {
    if (this.baggageCurrentPage > 1) {
      this.baggageCurrentPage--;
    }
  }

  public onBaggagePageSizeChange(newSize: any): void {
    this.baggagePageSize = Number(newSize);
    this.baggageCurrentPage = 1;
  }

  // =========================================================================
  // BAGGAGE KPIS / METRICS
  // =========================================================================

  public get totalBagsCount(): number {
    return this.baggageRecords.length;
  }

  public get checkedInCount(): number {
    return this.baggageRecords.filter(b => b.baggageStatus === 'CHECKED_IN').length;
  }

  public get loadedCount(): number {
    return this.baggageRecords.filter(b => b.baggageStatus === 'LOADED').length;
  }

  public get specialHandlingCount(): number {
    return this.baggageRecords.filter(b => b.baggageType === 'SPECIAL' || b.specialHandling !== 'NONE').length;
  }

  public get totalExcessFeeCollected(): number {
    return this.baggageRecords.reduce((acc, curr) => acc + (curr.extraBaggageFee || 0), 0);
  }

  public get lostOrDelayedCount(): number {
    return this.baggageRecords.filter(b => b.baggageStatus === 'LOST_OR_DELAYED').length;
  }

  // =========================================================================
  // BAGGAGE MANAGEMENT & MODALS
  // =========================================================================

  public openEditBaggage(record: BaggageRecord): void {
    this.selectedBaggage = record;
    this.editForm = {
      ...record,
      actualBaggageWeight: record.actualBaggageWeight,
      baggageAllowance: record.baggageAllowance,
      extraBaggageWeight: record.extraBaggageWeight,
      extraBaggageFee: record.extraBaggageFee
    };
    this.isEditModalOpen = true;
  }

  public closeEditModal(): void {
    this.isEditModalOpen = false;
    this.selectedBaggage = null;
    this.editForm = {};
  }

  public onWeightInputChange(): void {
    const allowance = Number(this.editForm.baggageAllowance) || 15;
    const actual = Number(this.editForm.actualBaggageWeight) || 0;
    const extraWeight = Math.max(0, +(actual - allowance).toFixed(1));
    const extraFee = Math.round(extraWeight * 500);

    this.editForm.extraBaggageWeight = extraWeight;
    this.editForm.extraBaggageFee = extraFee;
  }

  public saveEditBaggage(): void {
    if (!this.selectedBaggage) return;

    this.onWeightInputChange();

    const updated: BaggageRecord = {
      ...this.selectedBaggage,
      ...(this.editForm as BaggageRecord),
      baggageAllowanceDisplay: `${this.editForm.baggageAllowance || 15} kg`,
      bagsAllowedDisplay: `${this.editForm.bagsAllowed || 1} ${this.editForm.bagsAllowed === 1 ? 'bag' : 'bags'}`
    };

    this.baggageService.saveBaggageRecord(updated).subscribe({
      next: (res) => {
        this.toastService.success('Baggage Record Updated', `Bag tag ${res.bagTagNumber} updated successfully.`);
        this.loadBaggageRecords();
        this.closeEditModal();
      }
    });
  }

  public quickUpdateStatus(record: BaggageRecord, newStatus: any): void {
    const status = newStatus as BaggageStatus;
    this.baggageService.updateStatus(record.id, status).subscribe({
      next: () => {
        this.toastService.success('Status Transitioned', `Tag ${record.bagTagNumber} status changed to ${this.getStatusLabel(status)}`);
        this.loadBaggageRecords();
      }
    });
  }

  // =========================================================================
  // COUNTER CHECK-IN MODAL
  // =========================================================================

  public openCounterCheckIn(): void {
    this.newCheckIn = this.getEmptyCounterCheckIn();
    this.isCounterCheckInOpen = true;
  }

  public closeCounterCheckIn(): void {
    this.isCounterCheckInOpen = false;
  }

  public onNewCheckInWeightChange(): void {
    // Dynamically calculate excess weight preview
  }

  public get calculatedNewExtraWeight(): number {
    const allowance = Number(this.newCheckIn.baggageAllowance) || 15;
    const actual = Number(this.newCheckIn.actualBaggageWeight) || 0;
    return Math.max(0, +(actual - allowance).toFixed(1));
  }

  public get calculatedNewExtraFee(): number {
    return Math.round(this.calculatedNewExtraWeight * 500);
  }

  public submitCounterCheckIn(): void {
    if (!this.newCheckIn.passengerName || !this.newCheckIn.flightNumber || !this.newCheckIn.bookingReference) {
      this.toastService.warning('Required Fields Missing', 'Please fill in Passenger Name, Flight Number, and Booking ID / PNR.');
      return;
    }

    this.baggageService.checkInAtCounter(this.newCheckIn).subscribe({
      next: (created) => {
        this.toastService.success('Check-in Completed!', `Bag tag ${created.bagTagNumber} issued for ${created.passengerName}.`);
        this.loadBaggageRecords();
        this.closeCounterCheckIn();
        this.openPrintTag(created);
      }
    });
  }

  private getEmptyCounterCheckIn(): NewBaggageCheckinRequest {
    const defaultFlight = this.flights.length > 0 ? this.flights[0] : null;
    return {
      passengerName: '',
      bookingReference: `PNR-${Math.floor(100000 + Math.random() * 900000)}`,
      flightNumber: defaultFlight ? defaultFlight.flightNumber : 'AI-202',
      travelDate: defaultFlight ? (defaultFlight.departureDate || '2026-10-15') : '2026-10-15',
      origin: defaultFlight ? (defaultFlight.source || defaultFlight.origin || 'DEL') : 'DEL',
      destination: defaultFlight ? defaultFlight.destination : 'BOM',
      seatNumber: '12A',
      baggageAllowance: 15,
      bagsAllowed: 1,
      actualBaggageWeight: 14.5,
      counterLocation: 'Terminal 3 - Counter T3-B14',
      baggageType: 'REGULAR',
      specialHandling: 'NONE',
      remarks: 'Verified at check-in counter.'
    };
  }

  // =========================================================================
  // LUGGAGE TAG PRINT & EXPORT
  // =========================================================================

  public openPrintTag(record: BaggageRecord): void {
    this.selectedTagBaggage = record;
    this.isTagModalOpen = true;
  }

  public closePrintTag(): void {
    this.isTagModalOpen = false;
    this.selectedTagBaggage = null;
  }

  public printCurrentTag(): void {
    this.toastService.success('Bag Tag Dispatched', `Bag Tag ${this.selectedTagBaggage?.bagTagNumber} sent to thermal tag printer.`);
    window.print();
  }

  public exportBaggageManifestCsv(): void {
    const headers = [
      'Bag Tag Number',
      'Passenger Name',
      'Booking ID / PNR',
      'Flight Number',
      'Travel Date',
      'Origin',
      'Destination',
      'Seat Number',
      'Baggage Allowance (kg)',
      'Bags Allowed',
      'Actual Baggage Weight (kg)',
      'Extra Baggage Weight (kg)',
      'Extra Baggage Fee (INR)',
      'Baggage Status',
      'Check-in Time',
      'Counter Location',
      'Baggage Type',
      'Special Handling',
      'Remarks'
    ];

    const rows = this.filteredBaggage.map(b => [
      `"${b.bagTagNumber}"`,
      `"${b.passengerName}"`,
      `"${b.bookingReference}"`,
      `"${b.flightNumber}"`,
      `"${b.travelDate}"`,
      `"${b.origin}"`,
      `"${b.destination}"`,
      `"${b.seatNumber || ''}"`,
      `"${b.baggageAllowance}"`,
      `"${b.bagsAllowed}"`,
      `"${b.actualBaggageWeight}"`,
      `"${b.extraBaggageWeight}"`,
      `"${b.extraBaggageFee}"`,
      `"${this.getStatusLabel(b.baggageStatus)}"`,
      `"${b.checkInTime || ''}"`,
      `"${b.counterLocation}"`,
      `"${b.baggageType}"`,
      `"${this.getHandlingLabel(b.specialHandling)}"`,
      `"${(b.remarks || '').replace(/"/g, '""')}"`
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `SkyWings_Baggage_Manifest_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    this.toastService.success('CSV Manifest Exported', 'Baggage operations manifest downloaded successfully.');
  }

  // =========================================================================
  // FORMATTING & LABELS
  // =========================================================================

  public getStatusBadgeClass(status: BaggageStatus): string {
    switch (status) {
      case 'NOT_CHECKED_IN':
        return 'bg-slate-100 text-slate-700 border-slate-300';
      case 'CHECKED_IN':
        return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'LOADED':
        return 'bg-indigo-100 text-indigo-800 border-indigo-300';
      case 'UNLOADED':
        return 'bg-purple-100 text-purple-800 border-purple-300';
      case 'DELIVERED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      case 'LOST_OR_DELAYED':
        return 'bg-rose-100 text-rose-800 border-rose-300 animate-pulse';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-300';
    }
  }

  public getStatusLabel(status: BaggageStatus): string {
    switch (status) {
      case 'NOT_CHECKED_IN':
        return 'Not Checked In';
      case 'CHECKED_IN':
        return 'Checked In';
      case 'LOADED':
        return 'Loaded';
      case 'UNLOADED':
        return 'Unloaded';
      case 'DELIVERED':
        return 'Delivered';
      case 'LOST_OR_DELAYED':
        return 'Lost / Delayed';
      default:
        return status;
    }
  }

  public getHandlingBadgeClass(handling: SpecialHandlingType): string {
    switch (handling) {
      case 'FRAGILE':
        return 'bg-amber-100 text-amber-900 border-amber-300';
      case 'WHEELCHAIR':
        return 'bg-sky-100 text-sky-900 border-sky-300';
      case 'SPORTS_EQUIPMENT':
        return 'bg-teal-100 text-teal-900 border-teal-300';
      case 'MUSICAL_INSTRUMENT':
        return 'bg-violet-100 text-violet-900 border-violet-300';
      case 'HAZARDOUS_INSPECTED':
        return 'bg-red-100 text-red-900 border-red-300';
      default:
        return 'bg-slate-100 text-slate-600 border-slate-200';
    }
  }

  public getHandlingLabel(handling: SpecialHandlingType): string {
    switch (handling) {
      case 'NONE':
        return 'Standard Handling';
      case 'FRAGILE':
        return 'Fragile / Priority';
      case 'WHEELCHAIR':
        return 'Wheelchair equipment';
      case 'SPORTS_EQUIPMENT':
        return 'Sports equipment';
      case 'MUSICAL_INSTRUMENT':
        return 'Musical instrument';
      case 'MEDICAL_SUPPLIES':
        return 'Medical supplies';
      case 'HAZARDOUS_INSPECTED':
        return 'Hazardous Inspection';
      default:
        return handling;
    }
  }
}
