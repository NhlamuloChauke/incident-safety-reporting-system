import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IncidentService } from '../../core/services/incident.service';
import { AuthService } from '../../core/auth/auth.service';
import { DashboardStats, Incident } from '../../models/incident.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  template: `
    <div class="page-header">
      <div class="header-row">
        <div>
          <h1>Safety Dashboard</h1>
          <p>Welcome back, {{ currentUser?.fullName }} — {{ today | date:'EEEE, d MMMM yyyy' }}</p>
        </div>
        <a routerLink="/incidents/new" mat-flat-button class="btn-primary">
          <mat-icon>add</mat-icon> Report Incident
        </a>
      </div>
    </div>

    <div *ngIf="loading" class="loading-center">
      <mat-spinner diameter="40"></mat-spinner>
    </div>

    <ng-container *ngIf="!loading && stats">
      <!-- Stat Cards -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon orange"><mat-icon>warning</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalIncidents }}</div>
            <div class="stat-label">Total Incidents</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon red"><mat-icon>priority_high</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.openIncidents }}</div>
            <div class="stat-label">Open Incidents</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon red"><mat-icon>crisis_alert</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.criticalIncidents }}</div>
            <div class="stat-label">Critical</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green"><mat-icon>check_circle</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.closedIncidents }}</div>
            <div class="stat-label">Closed</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon blue"><mat-icon>calendar_month</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.incidentsThisMonth }}</div>
            <div class="stat-label">This Month</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon yellow"><mat-icon>personal_injury</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.injuries }}</div>
            <div class="stat-label">Injuries</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orange"><mat-icon>visibility</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.nearMisses }}</div>
            <div class="stat-label">Near Misses</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon red"><mat-icon>assignment_late</mat-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.overdueActions }}</div>
            <div class="stat-label">Overdue Actions</div>
          </div>
        </div>
      </div>

      <!-- Recent Incidents -->
      <div class="sm-card" style="margin-top:28px">
        <div class="section-header">
          <h3>Recent Incidents</h3>
          <a routerLink="/incidents" mat-button class="btn-ghost">View All</a>
        </div>
        <div *ngIf="recentIncidents.length === 0" class="empty-state">
          <mat-icon>check_circle</mat-icon>
          <p>No incidents reported yet</p>
        </div>
        <div class="incident-list">
          <div class="incident-row" *ngFor="let inc of recentIncidents"
               [routerLink]="['/incidents', inc.id]">
            <div class="inc-left">
              <div class="inc-severity-dot" [ngClass]="inc.severity.toLowerCase()"></div>
              <div>
                <div class="inc-title">{{ inc.title }}</div>
                <div class="inc-meta">
                  {{ inc.referenceNumber }} · {{ inc.location }} · {{ inc.incidentDateTime | date:'d MMM yyyy' }}
                </div>
              </div>
            </div>
            <div class="inc-right">
              <span class="badge {{ inc.severity }}">{{ inc.severity }}</span>
              <span class="badge {{ inc.status }}" style="margin-left:8px">{{ inc.status | titlecase }}</span>
            </div>
          </div>
        </div>
      </div>
    </ng-container>
  `,
  styles: [`
    .header-row {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
    }

    .loading-center {
      display: flex;
      justify-content: center;
      padding: 80px;
    }

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;
      h3 { font-size: 16px; font-weight: 600; }
    }

    .empty-state {
      text-align: center;
      padding: 40px;
      color: var(--text-secondary);
      mat-icon { font-size: 48px; width: 48px; height: 48px; opacity: 0.3; }
      p { margin-top: 8px; }
    }

    .incident-list { display: flex; flex-direction: column; gap: 4px; }

    .incident-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 14px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.15s;
      &:hover { background: var(--bg-card-hover); }
    }

    .inc-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .inc-severity-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      flex-shrink: 0;
      &.low      { background: var(--success); }
      &.medium   { background: var(--warning); }
      &.high     { background: var(--primary); }
      &.critical { background: var(--danger); }
    }

    .inc-title { font-size: 14px; font-weight: 500; }
    .inc-meta  { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
    .inc-right { display: flex; align-items: center; }
  `]
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  recentIncidents: Incident[] = [];
  loading = true;
  today = new Date();

  constructor(
    private incidentService: IncidentService,
    private authService: AuthService
  ) {}

  get currentUser() { return this.authService.currentUser; }

  ngOnInit() {
    this.incidentService.getDashboardStats().subscribe({
      next: s => { this.stats = s; this.loading = false; },
      error: () => { this.loading = false; }
    });
    this.incidentService.getAll().subscribe({
      next: list => { this.recentIncidents = list.slice(0, 8); }
    });
  }
}
