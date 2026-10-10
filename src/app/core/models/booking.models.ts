export interface PassengerDto {
  id?: number;
  fullName: string;
  dateOfBirth?: string;
  age?: number;
  gender?: string;
  travelClass: 'ECONOMY' | 'PREMIUM_ECONOMY' | 'BUSINESS' | 'FIRST' | string;
  seatNumber: string;
  fareAmount?: number;
  ticketNumber?: string;
  passengerStatus?: string;
  mealOptionId?: number;
  mealName?: string;
  mealCategory?: string;
  mealPrice?: number;
  dietaryPreference?: string;
  segmentIndex?: number;
}

export interface CancellationRequest {
  reason?: string;
  accountHolderName?: string;
  bankName?: string;
  accountNumber?: string;
  ifscCode?: string;
  upiId?: string;
}

export interface BookingRequest {
  userId: number;
  flightId: number;
  passengers: PassengerDto[];
  passengerEmail?: string;
  passengerPhone?: string;
  passengerName?: string;
  seatNumber?: string;
  fareAmount?: number;
  tripType?: 'ONE_WAY' | 'ROUND_TRIP' | 'MULTI_CITY' | string;
  parentBookingReference?: string;
  segmentIndex?: number;
  mealTotalAmount?: number;
}

export interface BookingResponse {
  id: number;
  bookingReference: string;
  userId: number;
  flightId: number;
  flightNumber?: string;
  source?: string;
  destination?: string;
  departureDate?: string;
  departureDateTime?: string;
  passengerName: string;
  passengerEmail: string;
  passengerPhone: string;
  passengerDateOfBirth?: string;
  passengerGender?: string;
  seatNumber: string;
  fareAmount: number;
  bookingStatus: 'PENDING' | 'CONFIRMED' | 'CANCELLATION_REQUESTED' | 'CANCELLED' | 'FAILED' | string;
  paymentStatus: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUND_PENDING_ADMIN_APPROVAL' | 'REFUND_APPROVED' | 'REFUNDED' | string;
  checkInStatus: 'NOT_CHECKED_IN' | 'CHECKED_IN' | string;
  passengers: PassengerDto[];

  tripType?: 'ONE_WAY' | 'ROUND_TRIP' | 'MULTI_CITY' | string;
  parentBookingReference?: string;
  segmentIndex?: number;
  mealTotalAmount?: number;
  totalAmount?: number;

  // Multi-step Cancellation & Locked Refund Audit Details
  cancellationStatus?: 'NONE' | 'CANCELLATION_REQUESTED' | 'SUPPORT_APPROVED' | 'SUPPORT_REJECTED' | 'REFUND_PENDING_ADMIN_APPROVAL' | 'REFUND_APPROVED' | 'REFUND_REJECTED' | 'CANCELLED' | string;
  cancellationRequestedAt?: string;
  hoursBeforeDepartureAtRequest?: number;
  appliedRefundPolicy?: string;
  refundPercentage?: number;
  eligibleRefundAmount?: number;
  calculatedRefundAmount?: number;
  supportReviewedBy?: string;
  supportReviewedAt?: string;
  supportRemarks?: string;
  adminReviewedBy?: string;
  adminReviewedAt?: string;
  adminRemarks?: string;
  refundTransactionRef?: string;
  refundCompletedAt?: string;
  isRescheduled?: boolean;
  rescheduledAt?: string;
  originalFlightId?: number;
  createdAt: string;
}

export interface CancellationPreviewResponse {
  bookingReference: string;
  flightId: number;
  flightNumber: string;
  flightDepartureTime: string;
  currentTime: string;
  hoursRemaining: number;
  policyTier: string;
  refundPercentage: number;
  eligibleAmount: number;
  estimatedRefundAmount: number;
  eligibleForCancellation: boolean;
  message: string;
}

export interface PaymentRequest {
  bookingId: number;
  userId: number;
  amount: number;
  currency?: string;
  paymentMethod: string;
  cardNumber?: string;
  cvv?: string;
  expiryDate?: string;
}

export interface PaymentResponse {
  id?: number;
  paymentReference: string;
  bookingId: number;
  userId?: number;
  amount: number;
  currency?: string;
  paymentMethod?: string;
  paymentStatus: string;
  transactionDate?: string;
  createdAt?: string;
}
