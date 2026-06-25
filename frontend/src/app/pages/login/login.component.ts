import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="login-page">
      <div class="login-left">
        <div class="login-branding">
          <div class="brand-logo">
            <mat-icon>shield</mat-icon>
          </div>
          <h1>SafeMine</h1>
          <p>Mine Incident & Safety Reporting System</p>
          <div class="features">
            <div class="feature"><mat-icon>check_circle</mat-icon><span>Real-time incident reporting</span></div>
            <div class="feature"><mat-icon>check_circle</mat-icon><span>DMR compliance tracking</span></div>
            <div class="feature"><mat-icon>check_circle</mat-icon><span>Corrective action management</span></div>
            <div class="feature"><mat-icon>check_circle</mat-icon><span>Safety analytics dashboard</span></div>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="login-card">
          <div class="login-header">
            <h2>Welcome back</h2>
            <p>Sign in to your SafeMine account</p>
          </div>

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline">
              <mat-label>Email Address</mat-label>
              <input matInput formControlName="email" type="email" placeholder="your@mine.co.za">
              <mat-icon matPrefix>email</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Password</mat-label>
              <input matInput formControlName="password" [type]="showPassword ? 'text' : 'password'">
              <mat-icon matPrefix>lock</mat-icon>
              <button mat-icon-button matSuffix type="button" (click)="showPassword = !showPassword">
                <mat-icon>{{ showPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
            </mat-form-field>

            <div class="error-msg" *ngIf="errorMsg">
              <mat-icon>error</mat-icon> {{ errorMsg }}
            </div>

            <button mat-flat-button type="submit" class="btn-primary login-btn"
                    [disabled]="loginForm.invalid || loading">
              <mat-spinner *ngIf="loading" diameter="20"></mat-spinner>
              <mat-icon *ngIf="!loading">login</mat-icon>
              {{ loading ? 'Signing in...' : 'Sign In' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-page {
      display: flex;
      height: 100vh;
      background: var(--bg-dark);
    }

    .login-left {
      flex: 1;
      background: linear-gradient(135deg, #1a0a00 0%, #2d1400 50%, #1a0a00 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 60px;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: -50%;
        left: -50%;
        width: 200%;
        height: 200%;
        background: radial-gradient(circle at 30% 40%, rgba(255,107,0,0.15) 0%, transparent 50%);
      }
    }

    .login-branding {
      position: relative;
      z-index: 1;

      .brand-logo {
        width: 64px;
        height: 64px;
        background: var(--primary);
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 24px;
        mat-icon { color: white; font-size: 36px; width: 36px; height: 36px; }
      }

      h1 { font-size: 40px; font-weight: 800; color: white; margin-bottom: 8px; }
      p  { font-size: 16px; color: rgba(255,255,255,0.6); margin-bottom: 40px; }
    }

    .features {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .feature {
        display: flex;
        align-items: center;
        gap: 12px;
        color: rgba(255,255,255,0.8);
        font-size: 15px;
        mat-icon { color: var(--primary); font-size: 20px; }
      }
    }

    .login-right {
      width: 480px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px;
    }

    .login-card {
      width: 100%;
      max-width: 400px;
    }

    .login-header {
      margin-bottom: 32px;
      h2 { font-size: 28px; font-weight: 700; color: var(--text-primary); }
      p  { font-size: 14px; color: var(--text-secondary); margin-top: 6px; }
    }

    form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .login-btn {
      height: 48px;
      font-size: 15px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-top: 8px;
    }

    .error-msg {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--danger);
      font-size: 13px;
      padding: 10px 14px;
      background: rgba(248,81,73,0.1);
      border-radius: 8px;
      border: 1px solid rgba(248,81,73,0.2);
      mat-icon { font-size: 18px; width: 18px; height: 18px; }
    }

    .demo-creds {
      margin-top: 28px;
      padding: 16px;
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 10px;

      .demo-title {
        font-size: 11px;
        color: var(--text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-bottom: 12px;
        font-weight: 600;
      }

      .cred-row {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px;
        border-radius: 6px;
        cursor: pointer;
        font-size: 12px;
        color: var(--text-secondary);
        transition: background 0.15s;
        &:hover { background: var(--bg-card-hover); color: var(--text-primary); }
      }

      .badge-role {
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 10px;
        font-weight: 600;
        white-space: nowrap;
        &.admin  { background: rgba(255,107,0,0.2); color: var(--primary); }
        &.safety { background: rgba(88,166,255,0.2); color: var(--info); }
        &.worker { background: rgba(63,185,80,0.2); color: var(--success); }
      }
    }

    @media (max-width: 768px) {
      .login-left { display: none; }
      .login-right { width: 100%; }
    }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMsg = '';
  showPassword = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
    if (this.authService.isLoggedIn) this.router.navigate(['/dashboard']);
  }

  fillCreds(email: string, password: string) {
    this.loginForm.setValue({ email, password });
  }

  onSubmit() {
    if (this.loginForm.invalid) return;
    this.loading = true;
    this.errorMsg = '';
    this.authService.login(this.loginForm.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.errorMsg = 'Invalid email or password. Please try again.';
        this.loading = false;
      }
    });
  }
}
