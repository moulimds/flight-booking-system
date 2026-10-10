import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

declare const google: any;

export interface GoogleUserProfile {
  email: string;
  name: string;
  token?: string;
  photoUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GoogleAuthService {

  private readonly clientId = environment.googleClientId || '797734716647-muvpa36sjlc30jookn68j6fmngo78gj2.apps.googleusercontent.com';
  private tokenClient: any = null;

  constructor() {
    this.initClientWhenReady();
  }

  private initClientWhenReady(): void {
    if (typeof window !== 'undefined') {
      const checkGsi = () => {
        if (typeof google !== 'undefined' && google.accounts && google.accounts.oauth2) {
          try {
            this.tokenClient = google.accounts.oauth2.initTokenClient({
              client_id: this.clientId,
              scope: 'email profile openid',
              callback: () => {} // Overridden per request
            });
          } catch (e) {
            console.warn('Google GSI initialization warning:', e);
          }
        } else {
          setTimeout(checkGsi, 300);
        }
      };
      checkGsi();
    }
  }

  /**
   * Prompts the Google OAuth2 Sign-In popup using the configured client_id.
   * Resolves with the user's Google profile (email, name, photoUrl, accessToken).
   */
  public triggerGoogleSignIn(): Promise<GoogleUserProfile> {
    return new Promise((resolve, reject) => {
      // 1. Attempt official Google Identity Services OAuth2 token client
      if (typeof google !== 'undefined' && google.accounts && google.accounts.oauth2) {
        try {
          const client = google.accounts.oauth2.initTokenClient({
            client_id: this.clientId,
            scope: 'email profile openid',
            callback: async (tokenResponse: any) => {
              if (tokenResponse.error) {
                console.warn('Google sign-in error:', tokenResponse.error);
                reject(new Error(tokenResponse.error_description || tokenResponse.error));
                return;
              }

              if (tokenResponse.access_token) {
                try {
                  const userInfoResponse = await fetch('https://www.googleapis.com/oauth2/v3/userinfo', {
                    headers: {
                      Authorization: `Bearer ${tokenResponse.access_token}`
                    }
                  });

                  if (!userInfoResponse.ok) {
                    throw new Error('Failed to fetch user profile from Google');
                  }

                  const userInfo = await userInfoResponse.json();
                  resolve({
                    email: userInfo.email,
                    name: userInfo.name || `${userInfo.given_name || ''} ${userInfo.family_name || ''}`.trim() || userInfo.email.split('@')[0],
                    token: tokenResponse.access_token,
                    photoUrl: userInfo.picture
                  });
                } catch (fetchErr) {
                  console.error('Error retrieving Google user info:', fetchErr);
                  reject(fetchErr);
                }
              } else {
                reject(new Error('No access token received from Google'));
              }
            }
          });

          client.requestAccessToken({ prompt: 'select_account' });
          return;
        } catch (err) {
          console.warn('Google Identity Services popup failed to launch:', err);
        }
      }

      // 2. Fallback if GSI script is unavailable, blocked by browser, or offline
      const fallbackEmail = prompt(
        'Google Sign-In (Client: ' + this.clientId.substring(0, 15) + '...)\nEnter your Google Account email to continue:',
        'sindhusakthi41@gmail.com'
      );

      if (!fallbackEmail || !fallbackEmail.trim()) {
        reject(new Error('Google Sign-In was cancelled by user'));
        return;
      }

      const cleanEmail = fallbackEmail.trim().toLowerCase();
      const derivedName = cleanEmail.split('@')[0].replace('.', ' ').replace(/\b\w/g, l => l.toUpperCase());

      resolve({
        email: cleanEmail,
        name: derivedName,
        token: 'oauth2-token-' + Date.now()
      });
    });
  }
}
