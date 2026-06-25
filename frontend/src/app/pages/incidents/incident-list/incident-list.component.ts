import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IncidentService } from '../../../core/services/incident.service';
import { Incident } from '../../../models/incident.model';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, MatTableModule, MatIconModule,
    MatButtonModule, MatInputModule, MatFormFieldModule, MatSelectModule, MatProgressSpinnerModule],
  template: `
    <div class="page-header">
      <div class="header-row">
        <div>
          <h1>Incidents</h1>
          <p>{{ filtered.length }} incident{{ filtered.length !== 1 ? 's' : '' }} found</p>
        </div>
        <a routerLink="/incidents/new" mat-flat-button class="btn-primary">
          <mat-icon>add</mat-icon> Report Incident
        </a>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters sm-card" style="margin-bottom:20px">
      <mat-form-field appearance="outline" style="flex:1">
        <mat-label>Search incidents</mat-label>
        <mat-icon matPrefix>search</mat-icon>
        <input matInput [(ngModel)]="searchTerm" (ngModelChange)="applyFilter()" placeholder="Title, reference, location...">
      </mat-form-field>
      <mat-form-field appearance="outline" style="width:160px">
        <mat-label>Severity</mat-label>
        <mat-select [(ngModel)]="filterSeverity" (ngModelChange)="applyFilter()">
          <mat-option value="">All</mat-option>
          <mat-option value="CRITICAL">Critical</mat-option>
          <mat-option value="HIGH">High</mat-option>
          <mat-option value="MEDIUM">Medium</mat-option>
          <mat-option value="LOW">Low</mat-option>
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline" style="width:180px">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="filterStatus" (ngModelChange)="applyFilter()">
          <mat-option value="">All</mat-option>
          <mat-option value="REPORTED">Reported</mat-option>
          <mat-option value="UNDER_INVESTIGATION">Under Investigation</mat-option>
          <mat-option value="CLOSED">Closed</mat-option>
          <mat-option value="DMR_NOTIFIED">DMR Notified</mat-option>
        </mat-select>
      </mat-form-field>
    </div>

    <div *ngIf="loading" class="loading-center"><mat-spinner diameter="40"></mat-spinner></div>

    <div class="sm-card" *ngIf="!loading" style="padding:0;overflow:hidden">
      <table mat-table [dataSource]="filtered" style="width:100%">
        <ng-container matColumnDef="ref">
          <th mat-header-cell *matHeaderCellDef>Reference</th>
          <td mat-cell *matCellDef="let row">
            <span style="font-family:monospace;font-size:12px;color:var(--info)">{{ row.referenceNumber }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="title">
          <th mat-header-cell *matHeaderCellDef>Title</th>
          <td mat-cell *matCellDef="let row">
            <div style="font-weight:500">{{ row.title }}</div>
            <div style="font-size:12px;color:var(--text-secondary)">{{ row.location }}</div>
          </td>
        </ng-container>
        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef>Type</th>
          <td mat-cell *matCellDef="let row" style="font-size:13px">
            {{ row.incidentType | titlecase }}
          </td>
        </ng-container>
        <ng-container matColumnDef="severity">
          <th mat-header-cell *matHeaderCellDef>Severity</th>
          <td mat-cell *matCellDef="let row">
            <span class="badge {{ row.severity }}">{{ row.severity }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let row">
            <span class="badge {{ row.status }}">{{ row.status | titlecase }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="date">
          <th mat-header-cell *matHeaderCellDef>Date</th>
          <td mat-cell *matCellDef="let row" style="font-size:13px;color:var(--text-secondary)">
            {{ row.incidentDateTime | date:'d MMM yyyy' }}
          </td>
        </ng-container>
        <ng-container matColumnDef="reporter">
          <th mat-header-cell *matHeaderCellDef>Reported By</th>
          <td mat-cell *matCellDef="let row" style="font-size:13px">{{ row.reportedByName }}</td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let row">
            <a mat-icon-button [routerLink]="['/incidents', row.id]" color="primary">
              <mat-icon>chevron_right</mat-icon>
            </a>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let row; columns: columns;"
            [routerLink]="['/incidents', row.id]" style="cursor:pointer"></tr>
      </table>

      <div *ngIf="filtered.length === 0" class="empty-state" style="padding:60px;text-align:center;color:var(--text-secondary)">
        <mat-icon style="font-size:48px;width:48px;height:48px;opacity:0.3">search_off</mat-icon>
        <p style="margin-top:12px">No incidents found</p>
      </div>
    </div>
  `,
  styles: [`
    .header-row { display: flex; align-items: flex-start; justify-content: space-between; }
    .filters { display: flex; gap: 16px; align-items: center; padding: 16px 20px; }
    .loading-center { display: flex; justify-content: center; padding: 80px; }
  `]
})
export class IncidentListComponent implements OnInit {
  incidents: Incident[] = [];
  filtered: Incident[] = [];
  loading = true;
  searchTerm = '';
  filterSeverity = '';
  filterStatus = '';
  columns = ['ref', 'title', 'type', 'severity', 'status', 'date', 'reporter', 'actions'];

  constructor(private incidentService: IncidentService) {}

  ngOnInit() {
    this.incidentService.getAll().subscribe({
      next: list => { this.incidents = list; this.filtered = list; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  applyFilter() {
    this.filtered = this.incidents.filter(i => {
      const matchSearch = !this.searchTerm ||
        i.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        i.referenceNumber.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        i.location?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchSeverity = !this.filterSeverity || i.severity === this.filterSeverity;
      const matchStatus   = !this.filterStatus   || i.status   === this.filterStatus;
      return matchSearch && matchSeverity && matchStatus;
    });
  }
}
