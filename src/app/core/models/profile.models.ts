export interface UserProfile {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  address?: string;
  city?: string;
  country?: string;
  dateOfBirth?: string;
  gender?: string;
  passportNumber?: string;
  passportExpiryDate?: string;
  passportIssuingCountry?: string;
  nationality?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  frequentFlyerNumber?: string;
  preferredMeal?: string;
  preferredSeatType?: string;
}

export interface ProfileUpdateRequest {
  fullName?: string;
  phoneNumber?: string;
  address?: string;
  city?: string;
  country?: string;
  dateOfBirth?: string;
  gender?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  preferredMeal?: string;
  preferredSeatType?: string;
}

export interface PassportUpdateRequest {
  passportNumber: string;
  passportExpiryDate: string;
  passportIssuingCountry: string;
  nationality: string;
}
