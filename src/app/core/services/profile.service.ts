import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { UserProfile, ProfileUpdateRequest, PassportUpdateRequest } from '../models/profile.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private gatewayUrl = 'http://localhost:8080/api/profiles';

  private getAuthHeaders(): Record<string, string> {
    const user = this.authService.currentUserValue;
    const headers: Record<string, string> = {};
    if (user?.userId) {
      headers['X-User-Id'] = String(user.userId);
    }
    if (user?.role) {
      headers['X-User-Role'] = user.role;
    }
    return headers;
  }

  public getMyProfile(): Observable<UserProfile> {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) {
      return throwError(() => new Error('User is not authenticated.'));
    }
    return this.http.get<UserProfile>(`${this.gatewayUrl}/user/${user.userId}`, {
      headers: this.getAuthHeaders()
    });
  }

  public updateProfile(req: ProfileUpdateRequest): Observable<UserProfile> {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) {
      return throwError(() => new Error('User is not authenticated.'));
    }
    return this.http.put<UserProfile>(`${this.gatewayUrl}/${user.userId}`, req, {
      headers: this.getAuthHeaders()
    });
  }

  public updatePassport(req: PassportUpdateRequest): Observable<UserProfile> {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) {
      return throwError(() => new Error('User is not authenticated.'));
    }
    return this.http.put<UserProfile>(`${this.gatewayUrl}/${user.userId}/passport`, req, {
      headers: this.getAuthHeaders()
    });
  }
}
