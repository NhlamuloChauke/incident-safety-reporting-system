import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IncidentService } from '../../core/services/incident.service';
import { Incident } from '../../models/incident.model';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  template: `
    <div class="page-header">
      <h1>Safety Reports</h1>
      <p>Analytics and compliance reporting for mine safety</p>
    </div>

    <div *ngIf="loading" class="loading-center"><mat-spinner diameter="40"></mat-spinner></div>

    <ng-container *ngIf="!loading">
      <!-- Summary Cards -->
      <div class="report-cards">
        <div class="report-card" *ngFor="let card of summaryCards">
          <div class="rc-icon" [style.background]="card.bg">
            <mat-icon [style.color]="card.color">{{ card.icon }}</mat-icon>
          </div>
          <div class="rc-content">
            <div class="rc-value">{{ card.value }}</div>
            <div class="rc-label">{{ card.label }}</div>
          </div>
        </div>
      </div>

      <!-- Breakdown Table -->
      <div class="sm-card" style="margin-top:24px">
        <h3 style="font-size:16px;font-weight:600;margin-bottom:20px">Incident Breakdown by Type</h3>
        <div class="breakdown-grid">
          <div class="breakdown-row" *ngFor="let item of typeBreakdown">
            <span class="breakdown-label">{{ item.label }}</span>
            <div class="breakdown-bar-wrap">
              <div class="breakdown-bar" [style.width.%]="item.percent" [style.background]="item.color"></div>
            </div>
            <span class="breakdown-count">{{ item.count }}</span>
          </div>
        </div>
      </div>

      <!-- Severity Table -->
      <div class="sm-card" style="margin-top:20px">
        <h3 style="font-size:16px;font-weight:600;margin-bottom:20px">Severity Distribution</h3>
        <div class="breakdown-grid">
          <div class="breakdown-row" *ngFor="let item of severityBreakdown">
            <span class="breakdown-label">
              <span class="badge {{ item.label.toUpperCase() }}" style="width:70px;justify-content:center">{{ item.label }}</span>
            </span>
            <div class="breakdown-bar-wrap">
              <div class="breakdown-bar" [style.width.%]="item.percent" [style.background]="item.color"></div>
            </div>
            <span class="breakdown-count">{{ item.count }}</span>
          </div>
        </div>
      </div>

      <!-- Export note -->
      <div class="sm-card export-note" style="margin-top:20px">
        <mat-icon>info</mat-icon>
        <div>
          <strong>DMR Report Export</strong>
          <p>Full PDF export for Department of Mineral Resources (DMR) compliance reporting will be available in the next release. All incident data is stored and ready for export.</p>
        </div>
      </div>
    </ng-container>
  `,
  styles: [`
    .loading-center { display: flex; justify-content: center; padding: 80px; }

    .report-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 16px;
    }

    .report-card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;

      .rc-icon {
        width: 48px;
        height: 48px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        mat-icon { font-size: 22px; }
      }

      .rc-value { font-size: 26px; font-weight: 700; }
      .rc-label { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
    }

    .breakdown-grid { display: flex; flex-direction: column; gap: 12px; }

    .breakdown-row {
      display: flex;
      align-items: center;
      gap: 16px;

      .breakdown-label { width: 160px; font-size: 13px; color: var(--text-secondary); flex-shrink: 0; }

      .breakdown-bar-wrap {
        flex: 1;
        height: 8px;
        background: var(--border);
        border-radius: 4px;
        overflow: hidden;
      }

      .breakdown-bar { height: 100%; border-radius: 4px; transition: width 0.5s ease; }

      .breakdown-count { width: 30px; text-align: right; font-size: 14px; font-weight: 600; flex-shrink: 0; }
    }

    .export-note {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      mat-icon { color: var(--info); margin-top: 2px; flex-shrink: 0; }
      strong { font-size: 14px; }
      p { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }
    }
  `]
})
export class ReportsComponent implements OnInit {
  incidents: Incident[] = [];
  loading = true;
  summaryCards: any[] = [];
  typeBreakdown: any[] = [];
  severityBreakdown: any[] = [];

  constructor(private incidentService: IncidentService) {}

  ngOnInit() {
    this.incidentService.getAll().subscribe({
      next: list => {
        this.incidents = list;
        this.buildStats();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  buildStats() {
    const total = this.incidents.length;

    this.summaryCards = [
      { label: 'Total Incidents', value: total, icon: 'summarize', bg: 'rgba(255,107,0,0.15)', color: '#FF6B00' },
      { label: 'With Injuries', value: this.incidents.filter(i => i.injuryOccurred).length, icon: 'personal_injury', bg: 'rgba(248,81,73,0.15)', color: '#F85149' },
      { label: 'Near Misses', value: this.incidents.filter(i => i.incidentType === 'NEAR_MISS').length, icon: 'visibility', bg: 'rgba(88,166,255,0.15)', color: '#58A6FF' },
      { label: 'Closed', value: this.incidents.filter(i => i.status === 'CLOSED').length, icon: 'check_circle', bg: 'rgba(63,185,80,0.15)', color: '#3FB950' },
    ];

    const types = ['INJURY','NEAR_MISS','FALL_OF_GROUND','EQUIPMENT_FAILURE','FIRE','PROPERTY_DAMAGE','OTHER'];
    const typeLabels: Record<string,string> = {
      INJURY:'Injury', NEAR_MISS:'Near Miss', FALL_OF_GROUND:'Fall of Ground',
      EQUIPMENT_FAILURE:'Equipment Failure', FIRE:'Fire', PROPERTY_DAMAGE:'Property Damage', OTHER:'Other'
    };
    const max = Math.max(...types.map(t => this.incidents.filter(i => i.incidentType === t).length), 1);
    this.typeBreakdown = types.map(t => ({
      label: typeLabels[t], count: this.incidents.filter(i => i.incidentType === t).length,
      percent: Math.round(this.incidents.filter(i => i.incidentType === t).length / max * 100),
      color: '#FF6B00'
    })).filter(t => t.count > 0);

    const sevs = [
      { key:'CRITICAL', color:'#F85149' }, { key:'HIGH', color:'#FF6B00' },
      { key:'MEDIUM', color:'#D29922' }, { key:'LOW', color:'#3FB950' }
    ];
    const maxS = Math.max(...sevs.map(s => this.incidents.filter(i => i.severity === s.key).length), 1);
    this.severityBreakdown = sevs.map(s => ({
      label: s.key, count: this.incidents.filter(i => i.severity === s.key).length,
      percent: Math.round(this.incidents.filter(i => i.severity === s.key).length / maxS * 100),
      color: s.color
    }));
  }
}
