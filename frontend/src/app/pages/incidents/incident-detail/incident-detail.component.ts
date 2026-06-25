import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { IncidentService } from '../../../core/services/incident.service';
import { Incident, IncidentStatus } from '../../../models/incident.model';

@Component({
  selector: 'app-incident-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, MatIconModule, MatButtonModule,
    MatSelectModule, MatFormFieldModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div *ngIf="loading" class="loading-center"><mat-spinner diameter="40"></mat-spinner></div>

    <ng-container *ngIf="!loading && incident">
      <!-- Header -->
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:4px">
          <a routerLink="/incidents" style="color:var(--text-secondary);display:flex;align-items:center">
            <mat-icon>arrow_back</mat-icon>
          </a>
          <span style="color:var(--text-secondary);font-size:13px">Incidents</span>
          <mat-icon style="font-size:16px;color:var(--text-secondary)">chevron_right</mat-icon>
          <span style="font-family:monospace;font-size:13px;color:var(--info)">{{ incident.referenceNumber }}</span>
        </div>
        <div style="display:flex;align-items:flex-start;justify-content:space-between">
          <div>
            <h1>{{ incident.title }}</h1>
            <div style="display:flex;gap:10px;margin-top:8px">
              <span class="badge {{ incident.severity }}">{{ incident.severity }}</span>
              <span class="badge {{ incident.status }}">{{ incident.status }}</span>
              <span *ngIf="incident.injuryOccurred" class="badge CRITICAL">INJURY</span>
            </div>
          </div>
          <div style="display:flex;gap:10px;align-items:center">
            <mat-form-field appearance="outline" style="width:220px;margin-bottom:-20px">
              <mat-label>Update Status</mat-label>
              <mat-select [(ngModel)]="newStatus" (ngModelChange)="updateStatus()">
                <mat-option value="REPORTED">Reported</mat-option>
                <mat-option value="UNDER_INVESTIGATION">Under Investigation</mat-option>
                <mat-option value="CORRECTIVE_ACTION_PENDING">Corrective Action Pending</mat-option>
                <mat-option value="CORRECTIVE_ACTION_IN_PROGRESS">In Progress</mat-option>
                <mat-option value="CLOSED">Closed</mat-option>
                <mat-option value="DMR_NOTIFIED">DMR Notified</mat-option>
              </mat-select>
            </mat-form-field>
          </div>
        </div>
      </div>

      <!-- Detail Grid -->
      <div class="detail-grid">
        <!-- Left Column -->
        <div class="detail-col">
          <div class="sm-card">
            <h3 class="detail-section-title">Incident Description</h3>
            <p style="color:var(--text-secondary);line-height:1.7;font-size:14px">{{ incident.description }}</p>
          </div>

          <div class="sm-card" *ngIf="incident.immediateActions">
            <h3 class="detail-section-title"><mat-icon>bolt</mat-icon> Immediate Actions</h3>
            <p style="color:var(--text-secondary);line-height:1.7;font-size:14px">{{ incident.immediateActions }}</p>
          </div>

          <div class="sm-card">
            <h3 class="detail-section-title"><mat-icon>psychology</mat-icon> Root Cause Analysis</h3>
            <div *ngIf="!editingRootCause">
              <p *ngIf="incident.rootCause" style="color:var(--text-secondary);line-height:1.7;font-size:14px">
                {{ incident.rootCause }}
              </p>
              <p *ngIf="!incident.rootCause" style="color:var(--text-secondary);font-size:14px;font-style:italic">
                Root cause not yet determined
              </p>
              <button mat-button style="margin-top:12px;color:var(--primary)" (click)="editingRootCause=true">
                <mat-icon>edit</mat-icon> {{ incident.rootCause ? 'Edit' : 'Add Root Cause' }}
              </button>
            </div>
            <div *ngIf="editingRootCause">
              <textarea [(ngModel)]="rootCauseText" rows="4" style="width:100%;background:var(--bg-dark);
                border:1px solid var(--border);border-radius:8px;padding:12px;color:var(--text-primary);
                font-family:inherit;font-size:14px;resize:vertical"></textarea>
              <div style="display:flex;gap:10px;margin-top:12px">
                <button mat-flat-button class="btn-primary" (click)="saveRootCause()">Save</button>
                <button mat-button class="btn-ghost" (click)="editingRootCause=false">Cancel</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="detail-col">
          <div class="sm-card">
            <h3 class="detail-section-title">Incident Details</h3>
            <div class="detail-row"><span>Type</span><span>{{ incident.incidentType | titlecase }}</span></div>
            <div class="detail-row"><span>Location</span><span>{{ incident.location }}</span></div>
            <div class="detail-row" *ngIf="incident.section"><span>Section</span><span>{{ incident.section }}</span></div>
            <div class="detail-row" *ngIf="incident.shiftTime"><span>Shift</span><span>{{ incident.shiftTime }}</span></div>
            <div class="detail-row"><span>Incident Date</span><span>{{ incident.incidentDateTime | date:'d MMM yyyy, HH:mm' }}</span></div>
            <div class="detail-row"><span>Reported At</span><span>{{ incident.reportedAt | date:'d MMM yyyy, HH:mm' }}</span></div>
            <div class="detail-row"><span>Reported By</span><span>{{ incident.reportedByName }}</span></div>
            <div class="detail-row" *ngIf="incident.assignedToName"><span>Assigned To</span><span>{{ incident.assignedToName }}</span></div>
            <div class="detail-row"><span>DMR Notified</span>
              <span [style.color]="incident.dmrNotified ? 'var(--success)' : 'var(--text-secondary)'">
                {{ incident.dmrNotified ? 'Yes' : 'No' }}
              </span>
            </div>
          </div>

          <div class="sm-card" *ngIf="incident.injuryOccurred">
            <h3 class="detail-section-title" style="color:var(--danger)">
              <mat-icon style="color:var(--danger)">personal_injury</mat-icon> Injury Details
            </h3>
            <div class="detail-row"><span>Injuries</span><span style="color:var(--danger)">{{ incident.numberOfInjured }} person(s)</span></div>
            <div class="detail-row" *ngIf="incident.injuryDescription"><span>Description</span><span>{{ incident.injuryDescription }}</span></div>
          </div>
        </div>
      </div>
    </ng-container>
  `,
  styles: [`
    .loading-center { display: flex; justify-content: center; padding: 80px; }

    .detail-grid {
      display: grid;
      grid-template-columns: 1fr 380px;
      gap: 20px;
      align-items: start;
    }

    .detail-col { display: flex; flex-direction: column; gap: 20px; }

    .detail-section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 16px;
      color: var(--text-primary);
      mat-icon { font-size: 18px; color: var(--primary); }
    }

    .detail-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid var(--border);
      font-size: 14px;

      &:last-child { border-bottom: none; }

      span:first-child { color: var(--text-secondary); }
      span:last-child  { font-weight: 500; text-align: right; }
    }

    @media (max-width: 900px) {
      .detail-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class IncidentDetailComponent implements OnInit {
  incident: Incident | null = null;
  loading = true;
  newStatus = '';
  editingRootCause = false;
  rootCauseText = '';

  constructor(private route: ActivatedRoute, private incidentService: IncidentService,
              private snackBar: MatSnackBar) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.incidentService.getById(id).subscribe({
      next: inc => {
        this.incident = inc;
        this.newStatus = inc.status;
        this.rootCauseText = inc.rootCause ?? '';
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  updateStatus() {
    if (!this.incident) return;
    this.incidentService.updateStatus(this.incident.id, this.newStatus).subscribe({
      next: updated => {
        this.incident = updated;
        this.snackBar.open('Status updated', 'Close', { duration: 3000, panelClass: 'success-snack' });
      }
    });
  }

  saveRootCause() {
    if (!this.incident) return;
    this.incidentService.updateRootCause(this.incident.id, this.rootCauseText).subscribe({
      next: updated => {
        this.incident = updated;
        this.editingRootCause = false;
        this.snackBar.open('Root cause saved', 'Close', { duration: 3000, panelClass: 'success-snack' });
      }
    });
  }
}
