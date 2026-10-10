import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService, AdminDashboardMetrics } from '../../../core/services/admin.service';
import { BookingService } from '../../../core/services/booking.service';
import { FlightService } from '../../../core/services/flight.service';
import { BookingResponse } from '../../../core/models/booking.models';
import { Flight } from '../../../core/models/flight.models';
import { UserResponse } from '../../../core/models/auth.models';
import { CurrencyFormatterPipe } from '../../../shared/pipes/currency-formatter.pipe';
import { StatusBadgeDirective } from '../../../shared/directives/status-badge.directive';
import { ToastService } from '../../../core/services/toast.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, CurrencyFormatterPipe, StatusBadgeDirective],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  private bookingService = inject(BookingService);
  private flightService = inject(FlightService);
  private notificationService = inject(NotificationService);
  private toastService = inject(ToastService);

  public metrics: AdminDashboardMetrics | null = null;
  public bookings: BookingResponse[] = [];
  public pendingRefunds: BookingResponse[] = [];
  public flights: Flight[] = [];
  public users: UserResponse[] = [];
  public activeTab: 'bookings' | 'flights' | 'users' = 'bookings';

  // ==========================================
  // DASHBOARD NAVBAR PAGINATION STATE
  // ==========================================
  public currentPage = 1;
  public pageSize = 5;
  public pageSizeOptions = [5, 10, 20, 50];

  public get currentTotalItems(): number {
    if (this.activeTab === 'bookings') return this.bookings.length;
    if (this.activeTab === 'flights') return this.flights.length;
    if (this.activeTab === 'users') return this.users.length;
    return 0;
  }

  public get totalPages(): number {
    return Math.ceil(this.currentTotalItems / this.pageSize) || 1;
  }

  public get startItemIndex(): number {
    if (this.currentTotalItems === 0) return 0;
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  public get endItemIndex(): number {
    return Math.min(this.currentPage * this.pageSize, this.currentTotalItems);
  }

  public get paginatedBookings(): BookingResponse[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.bookings.slice(start, start + this.pageSize);
  }

  public get paginatedFlights(): Flight[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.flights.slice(start, start + this.pageSize);
  }

  public get paginatedUsers(): UserResponse[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.users.slice(start, start + this.pageSize);
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

  public switchTab(tab: 'bookings' | 'flights' | 'users'): void {
    this.activeTab = tab;
    this.currentPage = 1;
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

  public onPageSizeChange(newSize: number): void {
    this.pageSize = Number(newSize);
    this.currentPage = 1;
  }

  // Refund Review Modal State
  public selectedRefund: BookingResponse | null = null;
  public adminRemarks = '';
  public isProcessingRefund = false;

  public get totalUsersCount(): number {
    if (this.users && this.users.length > 0) {
      return this.users.length;
    }
    return this.metrics?.totalCustomers || 0;
  }

  private syncMetricsFromUsers(): void {
    const userCount = this.users.length;
    if (this.metrics && userCount > 0) {
      this.metrics.totalCustomers = userCount;
      this.metrics.activeUsers = userCount;
    }
  }

  ngOnInit(): void {
    this.loadData();
  }

  public loadData(): void {
    this.adminService.getDashboardMetrics().subscribe({
      next: (m) => {
        this.metrics = m;
        if (this.users.length > 0) {
          this.syncMetricsFromUsers();
        }
        if (this.pendingRefunds.length > 0 && this.metrics) {
          this.metrics.cancellationRequests = Math.max(this.metrics.cancellationRequests, this.pendingRefunds.length);
        }
        if (this.bookings.length > 0) {
          this.syncMetricsFromBookings();
        }
      },
      error: () => this.toastService.error('Metrics Error', 'Unable to fetch admin summary metrics.')
    });

    this.adminService.getAllBookings().subscribe({
      next: (b) => {
        this.bookings = b || [];
        if (this.metrics && this.bookings.length > 0) {
          this.syncMetricsFromBookings();
        }
      },
      error: () => this.toastService.error('Bookings Error', 'Unable to fetch passenger bookings.')
    });

    // Fetch Pending Refund Approvals for Admin
    this.bookingService.getPendingAdminRefunds().subscribe({
      next: (r) => {
        this.pendingRefunds = r || [];
        if (this.metrics && this.pendingRefunds.length > this.metrics.cancellationRequests) {
          this.metrics.cancellationRequests = this.pendingRefunds.length;
        }
      },
      error: () => {}
    });

    this.adminService.getAllFlights().subscribe({
      next: (f) => this.flights = f || [],
      error: () => {}
    });

    this.adminService.getAllUsers().subscribe({
      next: (u) => {
        this.users = u || [];
        this.syncMetricsFromUsers();
      },
      error: () => {}
    });
  }

  private syncMetricsFromBookings(): void {
    if (!this.metrics) return;
    const pendingCancels = this.bookings.filter(b => 
      b.cancellationStatus === 'CANCELLATION_REQUESTED' || 
      b.cancellationStatus === 'SUPPORT_APPROVED' || 
      b.cancellationStatus === 'REFUND_PENDING_ADMIN_APPROVAL' || 
      b.bookingStatus === 'CANCELLATION_REQUESTED' ||
      b.paymentStatus === 'REFUND_PENDING_ADMIN_APPROVAL'
    ).length;
    this.metrics.cancellationRequests = Math.max(this.metrics.cancellationRequests, pendingCancels, this.pendingRefunds.length);
  }

  public refreshMetrics(): void {
    this.loadData();
    this.toastService.success('Dashboard Refreshed', 'Latest metrics, bookings and refund requests loaded.');
  }

  public openRefundModal(b: BookingResponse): void {
    this.selectedRefund = b;
    this.adminRemarks = '';
  }

  public closeRefundModal(): void {
    this.selectedRefund = null;
    this.adminRemarks = '';
  }

  public submitAdminRefundReview(decision: 'APPROVE' | 'REJECT'): void {
    if (!this.selectedRefund) return;

    if (decision === 'REJECT' && !this.adminRemarks.trim()) {
      this.toastService.warning('Remarks Required', 'Please provide a reason for rejecting the refund request.');
      return;
    }

    this.isProcessingRefund = true;
    const ref = this.selectedRefund.bookingReference || String(this.selectedRefund.id);

    this.bookingService.reviewRefundByAdmin(ref, decision, this.adminRemarks).subscribe({
      next: (res) => {
        this.isProcessingRefund = false;
        if (decision === 'APPROVE') {
          const refundAmount = res.calculatedRefundAmount ?? res.eligibleRefundAmount ?? 0;

          // Dispatch Refund Approved Email + Credit Note PDF to sindhusakthi41@gmail.com
          this.notificationService.sendRefundApproved({
            recipient: 'sindhusakthi41@gmail.com',
            bookingReference: ref,
            passengerName: res.passengerName || 'Valued Passenger',
            refundAmount: refundAmount,
            paymentReference: res.refundTransactionRef || ('RF-' + Date.now())
          }).subscribe();

          this.toastService.success('Refund Authorized & Seats Released', `Refund of ₹${refundAmount} processed for PNR ${ref}. Credit Note PDF sent to email.`);
        } else {
          // Dispatch Refund Denied Email + Decision Notice PDF to sindhusakthi41@gmail.com
          this.notificationService.sendRefundDenied({
            recipient: 'sindhusakthi41@gmail.com',
            bookingReference: ref,
            passengerName: res.passengerName || 'Valued Passenger',
            reason: this.adminRemarks || 'Cancellation outside eligible window per fare rules'
          }).subscribe();

          this.toastService.info('Refund Rejected', `Refund for PNR ${ref} rejected. Decision notice PDF sent to email.`);
        }
        this.closeRefundModal();
        this.loadData();
      },
      error: (err) => {
        this.isProcessingRefund = false;
        this.toastService.error('Refund Processing Error', err?.error?.message || 'Failed to process refund authorization.');
      }
    });
  }

  public onDeleteFlight(flightId: number): void {
    if (!confirm('Are you sure you want to delete this flight?')) return;

    this.flightService.deleteFlight(flightId).subscribe({
      next: () => {
        this.toastService.success('Flight Deleted', 'Flight has been removed successfully.');
        this.loadData();
      },
      error: (err) => {
        this.toastService.error('Delete Failed', err?.error?.message || 'Failed to delete flight.');
      }
    });
  }

  // ==========================================
  // INDIVIDUAL KPI DETAILED REPORT STATE & ACTIONS
  // ==========================================
  public isKpiReportModalOpen = false;
  public selectedKpiReport: {
    kpiKey: string;
    title: string;
    emoji: string;
    description: string;
    badgeClass: string;
    totalCount: number;
    totalAmount?: number;
    columns: string[];
    rows: Array<{
      col1: string;
      col2: string;
      col3: string;
      col4: string;
      col5: string;
      col6?: string;
      status?: string;
      statusType?: 'confirmed' | 'pending' | 'cancelled' | 'info';
    }>;
    generatedAt: string;
  } | null = null;

  public openKpiReport(kpiKey: string): void {
    const timestamp = new Date().toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

    switch (kpiKey) {
      case 'totalFlights': {
        const rows = this.flights.map(f => ({
          col1: f.flightNumber,
          col2: f.airline,
          col3: `${f.origin || f.source} → ${f.destination}`,
          col4: f.departureTime || '-',
          col5: `${f.availableSeats} / ${f.totalSeats}`,
          col6: `₹${(f.basePrice || 0).toLocaleString('en-IN')}`,
          status: f.status,
          statusType: this.getStatusType(f.status)
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Total Flights Catalog Report',
          emoji: '✈️',
          description: 'Comprehensive route inventory and fleet capacity catalog',
          badgeClass: 'badge-info',
          totalCount: this.flights.length,
          columns: ['Flight No', 'Airline', 'Route', 'Departure', 'Seats', 'Base Fare', 'Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }

      case 'activeFlights': {
        const activeStatuses = ['ACTIVE', 'ON_TIME', 'BOARDING', 'IN_AIR', 'DEPARTED', 'DELAYED', 'IN_FLIGHT', 'LANDED'];
        const activeList = this.flights.filter(f => {
          const s = (f.status || '').toUpperCase();
          return activeStatuses.includes(s) || (s !== 'CANCELLED' && s !== 'SCHEDULED' && s !== 'COMPLETED');
        });
        const rows = activeList.map(f => ({
          col1: f.flightNumber,
          col2: f.airline,
          col3: `${f.origin || f.source} → ${f.destination}`,
          col4: f.departureTime || '-',
          col5: `${f.availableSeats} / ${f.totalSeats}`,
          col6: `₹${(f.basePrice || 0).toLocaleString('en-IN')}`,
          status: f.status,
          statusType: 'confirmed' as const
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Active & Operational Flights Report',
          emoji: '🟢',
          description: 'Flights currently operational, in-air, boarding, departed, or operating on schedule',
          badgeClass: 'badge-success',
          totalCount: activeList.length,
          columns: ['Flight No', 'Airline', 'Route', 'Departure', 'Seats', 'Base Fare', 'Current Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }

      case 'scheduledFlights': {
        const scheduledList = this.flights.filter(f => (f.status || '').toUpperCase() === 'SCHEDULED');
        const rows = scheduledList.map(f => ({
          col1: f.flightNumber,
          col2: f.airline,
          col3: `${f.origin || f.source} → ${f.destination}`,
          col4: f.departureTime || '-',
          col5: `${f.availableSeats} / ${f.totalSeats}`,
          col6: `₹${(f.basePrice || 0).toLocaleString('en-IN')}`,
          status: f.status,
          statusType: 'pending' as const
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Scheduled Flights Report',
          emoji: '🟡',
          description: 'Upcoming scheduled flight departures awaiting gate assignment and boarding',
          badgeClass: 'badge-warning',
          totalCount: scheduledList.length,
          columns: ['Flight No', 'Airline', 'Route', 'Departure', 'Seats', 'Base Fare', 'Flight Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }

      case 'cancelledFlights': {
        const cancelledList = this.flights.filter(f => (f.status || '').toUpperCase() === 'CANCELLED');
        const rows = cancelledList.map(f => ({
          col1: f.flightNumber,
          col2: f.airline,
          col3: `${f.origin || f.source} → ${f.destination}`,
          col4: f.departureTime || '-',
          col5: `${f.availableSeats} / ${f.totalSeats}`,
          col6: `₹${(f.basePrice || 0).toLocaleString('en-IN')}`,
          status: f.status,
          statusType: 'cancelled' as const
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Cancelled Flights Operations Report',
          emoji: '🔴',
          description: 'Flight services cancelled, grounded, or withdrawn from active flight schedule',
          badgeClass: 'badge-danger',
          totalCount: cancelledList.length,
          columns: ['Flight No', 'Airline', 'Route', 'Departure', 'Seats', 'Base Fare', 'Flight Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }

      case 'totalCustomers': {
        const usersToShow = this.users;
        const rows = usersToShow.map(u => ({
          col1: `#${u.id}`,
          col2: u.fullName,
          col3: u.email,
          col4: u.phone || '-',
          col5: u.role,
          col6: u.active !== false ? 'Active' : 'Inactive',
          status: u.active !== false ? 'Active' : 'Inactive',
          statusType: (u.active !== false ? 'confirmed' : 'cancelled') as 'confirmed' | 'cancelled'
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Total Customers & Users Report',
          emoji: '👥',
          description: 'Verified passenger profiles and active user accounts registered in SkyWings',
          badgeClass: 'badge-info',
          totalCount: usersToShow.length,
          columns: ['User ID', 'Full Name', 'Email Address', 'Phone Number', 'Account Role', 'Profile Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }

      case 'totalBookings': {
        const rows = this.bookings.map(b => ({
          col1: b.bookingReference,
          col2: b.passengerName || 'Passenger',
          col3: b.flightNumber || `Flight #${b.flightId}`,
          col4: b.seatNumber || (b.passengers?.length ? `${b.passengers.length} Seat(s)` : '-'),
          col5: `₹${(b.fareAmount || 0).toLocaleString('en-IN')}`,
          col6: b.paymentStatus || 'PENDING',
          status: b.bookingStatus,
          statusType: this.getStatusType(b.bookingStatus)
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Total Passenger Bookings Report',
          emoji: '🎫',
          description: 'Complete reservations master register with passenger, seat, and payment details',
          badgeClass: 'badge-info',
          totalCount: this.bookings.length,
          columns: ['Booking Ref (PNR)', 'Passenger Name', 'Flight', 'Seat / Seats', 'Fare Amount', 'Payment', 'Booking Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }

      case 'totalRevenue': {
        const revList = this.bookings.filter(b => 
          b.bookingStatus === 'CONFIRMED' || 
          b.paymentStatus === 'SUCCESS' || 
          b.paymentStatus === 'PAID'
        );
        const totalRev = revList.reduce((acc, curr) => acc + (curr.fareAmount || 0), 0);
        const rows = revList.map(b => ({
          col1: b.bookingReference,
          col2: b.passengerName || 'Passenger',
          col3: b.flightNumber || `Flight #${b.flightId}`,
          col4: b.seatNumber || (b.passengers?.length ? `${b.passengers.length} Seat(s)` : '-'),
          col5: `₹${(b.fareAmount || 0).toLocaleString('en-IN')}`,
          col6: b.bookingStatus,
          status: b.paymentStatus || 'PAID',
          statusType: 'confirmed' as const
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Total Commercial Revenue Report',
          emoji: '💰',
          description: 'Audited gross revenue collections from confirmed and paid flight ticket reservations',
          badgeClass: 'badge-success',
          totalCount: revList.length,
          totalAmount: totalRev,
          columns: ['Booking Ref (PNR)', 'Passenger Name', 'Flight', 'Seat / Seats', 'Audited Amount', 'Booking Status', 'Payment Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }

      case 'pendingPayments': {
        const pendList = this.bookings.filter(b => 
          b.paymentStatus === 'PENDING' || 
          (b.bookingStatus === 'PENDING' && b.paymentStatus !== 'SUCCESS' && b.paymentStatus !== 'PAID')
        );
        const totalPending = pendList.reduce((acc, curr) => acc + (curr.fareAmount || 0), 0);
        const rows = pendList.map(b => ({
          col1: b.bookingReference,
          col2: b.passengerName || 'Passenger',
          col3: b.flightNumber || `Flight #${b.flightId}`,
          col4: b.seatNumber || (b.passengers?.length ? `${b.passengers.length} Seat(s)` : '-'),
          col5: `₹${(b.fareAmount || 0).toLocaleString('en-IN')}`,
          col6: b.bookingStatus || 'PENDING',
          status: 'PENDING',
          statusType: 'pending' as const
        }));
        this.selectedKpiReport = {
          kpiKey,
          title: 'Pending Payments & Checkout Queue Report',
          emoji: '💳',
          description: 'Flight reservations currently in checkout stage awaiting transaction settlement',
          badgeClass: 'badge-warning',
          totalCount: pendList.length,
          totalAmount: totalPending,
          columns: ['Booking Ref (PNR)', 'Passenger Name', 'Flight', 'Seat / Seats', 'Pending Amount', 'Booking Status', 'Payment Status'],
          rows,
          generatedAt: timestamp
        };
        break;
      }
    }

    this.isKpiReportModalOpen = true;
  }

  public closeKpiReportModal(): void {
    this.isKpiReportModalOpen = false;
    this.selectedKpiReport = null;
  }

  public getStatusType(status: string): 'confirmed' | 'pending' | 'cancelled' | 'info' {
    const s = (status || '').toUpperCase();
    if (['CONFIRMED', 'SUCCESS', 'PAID', 'ON_TIME', 'ACTIVE', 'LANDED'].includes(s)) return 'confirmed';
    if (['PENDING', 'SCHEDULED', 'BOARDING', 'IN_FLIGHT'].includes(s)) return 'pending';
    if (['CANCELLED', 'FAILED', 'DELAYED'].includes(s)) return 'cancelled';
    return 'info';
  }

  public downloadSelectedKpiCsv(): void {
    if (!this.selectedKpiReport) return;
    const r = this.selectedKpiReport;

    const lines: string[] = [
      `SkyWings Aviation - ${r.title}`,
      `Scope:,"${r.description}"`,
      `Generated At:,"${r.generatedAt}"`,
      `Total Records:,"${r.totalCount}"`,
      r.totalAmount != null ? `Total Value:,"INR ${r.totalAmount.toLocaleString('en-IN')}"` : '',
      '',
      r.columns.map(c => `"${c}"`).join(',')
    ].filter(line => line !== '');

    r.rows.forEach(row => {
      const rowCells = [
        row.col1,
        row.col2,
        row.col3,
        row.col4,
        row.col5,
        row.col6 || '',
        row.status || ''
      ].slice(0, r.columns.length);

      lines.push(rowCells.map(val => `"${(val || '').replace(/"/g, '""')}"`).join(','));
    });

    const csvContent = lines.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `SkyWings_${r.kpiKey}_Report_${Date.now()}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    this.toastService.success('CSV Downloaded', `${r.title} CSV file downloaded successfully.`);
  }

  public downloadSelectedKpiPdf(): void {
    if (!this.selectedKpiReport) return;
    const r = this.selectedKpiReport;

    const printWin = window.open('', '_blank');
    if (!printWin) {
      window.print();
      return;
    }

    const tableHeaders = r.columns.map(c => `<th>${c}</th>`).join('');
    const tableRows = r.rows.map(row => {
      const cells = [
        row.col1,
        row.col2,
        row.col3,
        row.col4,
        row.col5,
        row.col6,
        row.status
      ].filter(x => x !== undefined).slice(0, r.columns.length);

      return `<tr>${cells.map(c => `<td>${c || '-'}</td>`).join('')}</tr>`;
    }).join('');

    const totalAmtBlock = r.totalAmount != null ? `
      <div style="background: #ecfdf5; border: 1px solid #a7f3d0; padding: 12px 18px; border-radius: 8px; margin-bottom: 18px; display: inline-block;">
        <span style="font-size: 12px; color: #047857; font-weight: bold; text-transform: uppercase;">Total Audited Amount:</span>
        <span style="font-size: 18px; color: #065f46; font-weight: 900; margin-left: 8px;">₹${r.totalAmount.toLocaleString('en-IN')}</span>
      </div>
    ` : '';

    printWin.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>${r.title} - SkyWings Official Report</title>
        <style>
          @page { size: A4 landscape; margin: 15mm; }
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #0f172a; margin: 0; padding: 20px; font-size: 11px; }
          .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 2px solid #1e3c72; padding-bottom: 12px; margin-bottom: 16px; }
          .brand { font-size: 20px; font-weight: 900; color: #1e3c72; }
          .subbrand { font-size: 11px; color: #64748b; margin-top: 2px; }
          .report-tag { text-align: right; }
          .badge { background: #1e3c72; color: #ffffff; padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 10px; text-transform: uppercase; }
          .meta-grid { display: flex; gap: 20px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 16px; margin-bottom: 16px; }
          .meta-item { display: flex; flex-direction: column; }
          .meta-lbl { font-size: 9px; color: #64748b; text-transform: uppercase; font-weight: bold; }
          .meta-val { font-size: 12px; color: #0f172a; font-weight: bold; }
          table { width: 100%; border-collapse: collapse; margin-top: 10px; }
          th { background: #1e3c72; color: #ffffff; padding: 8px 10px; text-align: left; font-size: 10px; text-transform: uppercase; border: 1px solid #1e3c72; }
          td { padding: 7px 10px; border-bottom: 1px solid #e2e8f0; font-size: 10px; border-right: 1px solid #f1f5f9; }
          tr:nth-child(even) { background-color: #f8fafc; }
          .footer { margin-top: 24px; padding-top: 12px; border-top: 1px solid #cbd5e1; display: flex; justify-content: space-between; font-size: 10px; color: #64748b; }
        </style>
      </head>
      <body>
        <div class="header">
          <div>
            <div class="brand">✈ SKYWINGS AIRLINES</div>
            <div class="subbrand">Central Operations Command & Commercial Analytics Bureau</div>
          </div>
          <div class="report-tag">
            <span class="badge">OFFICIAL OPERATIONS REPORT</span>
            <div style="font-size: 11px; color: #475569; margin-top: 6px; font-weight: bold;">${r.title}</div>
          </div>
        </div>

        <div class="meta-grid">
          <div class="meta-item"><span class="meta-lbl">Report Scope</span><span class="meta-val">${r.description}</span></div>
          <div class="meta-item"><span class="meta-lbl">Generated At</span><span class="meta-val">${r.generatedAt}</span></div>
          <div class="meta-item"><span class="meta-lbl">Total Records</span><span class="meta-val">${r.totalCount} Entries</span></div>
          <div class="meta-item"><span class="meta-lbl">Classification</span><span class="meta-val">CONFIDENTIAL</span></div>
        </div>

        ${totalAmtBlock}

        <table>
          <thead><tr>${tableHeaders}</tr></thead>
          <tbody>${tableRows}</tbody>
        </table>

        <div class="footer">
          <div>Certified By: Operations Administrator • SkyWings Aviation Authority</div>
          <div>Generated directly from verified central database cluster</div>
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

  public printSelectedKpiReport(): void {
    this.downloadSelectedKpiPdf();
  }

  // ==========================================
  // ALL-OPERATIONS SUMMARY REPORT ACTIONS
  // ==========================================
  public isReportModalOpen = false;
  public reportGeneratedAt = '';
  public isExportingPdf = false;

  public openReportModal(): void {
    this.reportGeneratedAt = new Date().toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
    this.isReportModalOpen = true;
  }

  public closeReportModal(): void {
    this.isReportModalOpen = false;
  }

  public printReport(): void {
    window.print();
  }

  public exportReportCsv(): void {
    const p = this.getReportPayload();
    const csvRows = [
      'SkyWings Aviation - Executive Operations & Analytics Report',
      `Generated At:,"${this.reportGeneratedAt}"`,
      `Authority:,"Operations Administrator"`,
      '',
      'Metric Code,Key Performance Indicator (KPI),Value,Unit / Status',
      `FL-01,Total Flights,${p.totalFlights},Routes`,
      `FL-02,Active Flights,${p.activeFlights},Operational`,
      `FL-03,Scheduled Flights,${p.scheduledFlights},Upcoming`,
      `FL-04,Cancelled Flights,${p.cancelledFlights},Grounded`,
      `US-01,Total Customers,${p.totalCustomers},Registered Accounts`,
      `BK-01,Total Bookings,${p.totalBookings},Reservations`,
      `RV-01,Total Revenue,${p.totalRevenue},INR`,
      `PY-01,Pending Payments,${p.pendingPayments},Pending Checkout`
    ];

    const blob = new Blob([csvRows.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `SkyWings_Operations_Report_${Date.now()}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    this.toastService.success('CSV Exported', 'Operations report CSV downloaded successfully.');
  }

  public exportReportPdf(): void {
    this.isExportingPdf = true;
    const payload = this.getReportPayload();

    this.notificationService.downloadOperationsReportPdf(payload).subscribe({
      next: (blob) => {
        this.isExportingPdf = false;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `SkyWings_Operations_Report_${Date.now()}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.success('PDF Downloaded', 'Official Executive Operations Report PDF downloaded.');
      },
      error: () => {
        this.isExportingPdf = false;
        window.print();
      }
    });
  }

  public getReportPayload(): any {
    return {
      totalFlights: this.metrics?.totalFlights ?? this.flights.length,
      activeFlights: this.metrics?.activeFlights ?? 0,
      scheduledFlights: this.metrics?.scheduledFlights ?? 0,
      cancelledFlights: this.metrics?.cancelledFlights ?? 0,
      totalCustomers: this.totalUsersCount,
      totalBookings: this.metrics?.totalBookings ?? this.bookings.length,
      totalRevenue: (this.metrics?.totalRevenue ?? 0).toFixed(2),
      pendingPayments: this.metrics?.pendingPayments ?? 0,
      adminUser: 'Operations Administrator'
    };
  }
}
