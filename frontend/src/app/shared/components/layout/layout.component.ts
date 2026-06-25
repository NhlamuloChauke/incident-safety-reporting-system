import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="layout">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-logo">
          <div class="logo-icon">
            <mat-icon>shield</mat-icon>
          </div>
          <div class="logo-text">
            <span class="logo-title">SafeMine</span>
            <span class="logo-sub">Incident Reporting</span>
          </div>
        </div>

        <nav class="sidebar-nav">
          <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
            <mat-icon>dashboard</mat-icon>
            <span>Dashboard</span>
          </a>
          <a routerLink="/incidents" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}" class="nav-item">
            <mat-icon>warning</mat-icon>
            <span>Incidents</span>
          </a>
          <a routerLink="/incidents/new" routerLinkActive="active" class="nav-item">
            <mat-icon>add_circle</mat-icon>
            <span>Report Incident</span>
          </a>
          <a routerLink="/reports" routerLinkActive="active" class="nav-item">
            <mat-icon>assessment</mat-icon>
            <span>Reports</span>
          </a>
          <a *ngIf="currentUser?.role === 'ADMIN'" routerLink="/users" routerLinkActive="active" class="nav-item">
            <mat-icon>manage_accounts</mat-icon>
            <span>User Management</span>
          </a>
        </nav>

        <div class="sidebar-footer">
          <div class="user-info">
            <div class="user-avatar">{{ initials }}</div>
            <div class="user-details">
              <span class="user-name">{{ currentUser?.fullName }}</span>
              <span class="user-role">{{ currentUser?.role }}</span>
            </div>
          </div>
          <button class="logout-btn" (click)="logout()" matTooltip="Logout">
            <mat-icon>logout</mat-icon>
          </button>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .layout {
      display: flex;
      height: 100vh;
      overflow: hidden;
    }

    .sidebar {
      width: 240px;
      min-width: 240px;
      background: var(--bg-card);
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      padding: 0;
    }

    .sidebar-logo {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 24px 20px;
      border-bottom: 1px solid var(--border);

      .logo-icon {
        width: 40px;
        height: 40px;
        background: var(--primary);
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        mat-icon { color: white; font-size: 22px; }
      }

      .logo-title {
        display: block;
        font-size: 16px;
        font-weight: 700;
        color: var(--text-primary);
      }
      .logo-sub {
        display: block;
        font-size: 10px;
        color: var(--text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }

    .sidebar-nav {
      flex: 1;
      padding: 16px 12px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      border-radius: 8px;
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 14px;
      font-weight: 500;
      transition: all 0.15s ease;

      mat-icon { font-size: 20px; width: 20px; height: 20px; }

      &:hover {
        background: rgba(255,107,0,0.08);
        color: var(--text-primary);
      }

      &.active {
        background: rgba(255,107,0,0.15);
        color: var(--primary);
        mat-icon { color: var(--primary); }
      }
    }

    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid var(--border);
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      flex: 1;
      min-width: 0;
    }

    .user-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: var(--primary);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      font-weight: 700;
      flex-shrink: 0;
    }

    .user-details {
      min-width: 0;
      .user-name {
        display: block;
        font-size: 13px;
        font-weight: 600;
        color: var(--text-primary);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .user-role {
        display: block;
        font-size: 11px;
        color: var(--text-secondary);
      }
    }

    .logout-btn {
      background: none;
      border: none;
      cursor: pointer;
      color: var(--text-secondary);
      padding: 6px;
      border-radius: 6px;
      transition: all 0.15s;
      display: flex;
      align-items: center;
      &:hover { color: var(--danger); background: rgba(248,81,73,0.1); }
    }

    .main-content {
      flex: 1;
      overflow-y: auto;
      padding: 32px;
      background: var(--bg-dark);
    }
  `]
})
export class LayoutComponent {
  constructor(private authService: AuthService) {}

  get currentUser() { return this.authService.currentUser; }

  get initials(): string {
    return this.currentUser?.fullName
      ?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) ?? 'U';
  }

  logout() { this.authService.logout(); }
}
