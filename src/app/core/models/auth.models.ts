export type UserRole = 'CUSTOMER' | 'ADMIN' | 'SUPPORT' | 'AIRPORT_ADMIN' | 'AIRCRAFT_COMPANY_ADMIN' | 'OPERATOR';

export interface AuthRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role?: UserRole | string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType?: string;
  userId: number;
  email: string;
  fullName: string;
  role: string;
  message?: string;
}

export interface UserResponse {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  role: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface DecodedToken {
  sub: string;
  userId?: number;
  roles?: string[];
  role?: string;
  exp: number;
  iat: number;
}

