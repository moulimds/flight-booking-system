import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthRequest, RegisterRequest, AuthResponse, UserResponse } from '../models/auth.models';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = 'http://localhost:8080/api/auth';

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    this.loadStoredUser();
  }

  private loadStoredUser(): void {
    const token = localStorage.getItem('access_token');
    const userJson = localStorage.getItem('user_profile');
    if (token && userJson) {
      try {
        const user = JSON.parse(userJson) as AuthResponse;
        this.currentUserSubject.next(user);
      } catch (e) {
        this.logout();
      }
    }
  }

  public get token(): string | null {
    return localStorage.getItem('access_token');
  }

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.getValue();
  }

  public getCurrentUser(): AuthResponse | null {
    return this.currentUserValue;
  }

  public get isAuthenticated(): boolean {
    return !!this.token;
  }

  public get role(): string | null {
    const user = this.currentUserValue;
    return user ? user.role : null;
  }

  public get isAdmin(): boolean {
    const r = this.role;
    return r === 'ROLE_ADMIN' || r === 'ADMIN';
  }

  public get isSupport(): boolean {
    const r = this.role;
    return r === 'ROLE_SUPPORT' || r === 'SUPPORT';
  }

  public get isAirportAdmin(): boolean {
    const r = this.role;
    return r === 'ROLE_AIRPORT_ADMIN' || r === 'AIRPORT_ADMIN';
  }

  public get isAircraftCompanyAdmin(): boolean {
    const r = this.role;
    return r === 'ROLE_AIRCRAFT_COMPANY_ADMIN' || r === 'AIRCRAFT_COMPANY_ADMIN';
  }

  public get isStaff(): boolean {
    return this.isAdmin || this.isSupport || this.isAirportAdmin || this.isAircraftCompanyAdmin;
  }

  public login(req: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, {
      email: req.email,
      password: req.password
    }).pipe(
      tap(res => this.setSession(res))
    );
  }

  public register(req: RegisterRequest): Observable<AuthResponse> {
    const payload: any = {
      fullName: req.fullName,
      email: req.email,
      password: req.password
    };
    if (req.role) {
      payload.role = req.role;
    }
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, payload).pipe(
      tap(res => this.setSession(res))
    );
  }

  public loginWithGoogle(data: { email: string; name: string; token?: string; photoUrl?: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/google`, data).pipe(
      tap(res => this.setSession(res))
    );
  }

  public getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/users`);
  }

  public createUser(user: { fullName: string; email: string; password: string; role: string }): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.apiUrl}/users`, user);
  }

  public getMe(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

  public forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
  }

  public resetPassword(data: { email: string; token: string; newPassword: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, data);
  }

  private setSession(auth: AuthResponse): void {
    const token = auth.accessToken || (auth as any).token;
    if (token) {
      auth.accessToken = token;
      localStorage.setItem('access_token', token);
      localStorage.setItem('user_profile', JSON.stringify(auth));
      this.currentUserSubject.next(auth);
    }
  }

  public logout(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user_profile');
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }
}

