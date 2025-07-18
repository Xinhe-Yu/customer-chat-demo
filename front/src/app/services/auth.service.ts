import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface User {
  id: string;
  email?: string;
  username?: string;
  role: 'CLIENT' | 'AGENT';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = '/api';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromToken();
  }

  loginClient(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          this.loadUserInfo();
        })
      );
  }

  loginAgent(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/agent/auth`, credentials)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          this.loadUserFromToken();
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp > Date.now() / 1000;
    } catch {
      return false;
    }
  }

  private setToken(token: string): void {
    localStorage.setItem('token', token);
  }

  private loadUserFromToken(): void {
    const token = this.getToken();
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const user: User = {
        id: payload.sub,
        role: payload.authorities?.[0]?.authority === 'ROLE_AGENT' ? 'AGENT' : 'CLIENT'
      };
      this.currentUserSubject.next(user);
    } catch {
      this.logout();
    }
  }

  private loadUserInfo(): void {
    this.http.get<any>(`${this.apiUrl}/auth/me`).subscribe({
      next: (userInfo) => {
        const currentUser = this.currentUserSubject.value;
        if (currentUser) {
          this.currentUserSubject.next({
            ...currentUser,
            email: userInfo.email,
            username: userInfo.username
          });
        }
      },
      error: () => this.logout()
    });
  }
}
