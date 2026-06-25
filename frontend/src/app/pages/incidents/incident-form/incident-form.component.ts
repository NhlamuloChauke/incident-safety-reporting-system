import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IncidentService } from '../../../core/services/incident.service';

@Component({
  selector: 'app-incident-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatCheckboxModule,
    MatSnackBarModule, MatProgressSpinnerModule],
  template: `
    <div class="page-header">
      <h1>Report New Incident</h1>
      <p>Fill in all relevant details to report a safety incident</p>
    </div>

    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <div class="form-grid">

        <!-- Section 1: Incident Details -->
        <div class="sm-card form-section">
          <h3 class="section-title"><mat-icon>info</mat-icon> Incident Details</h3>
          <div class="field-row">
            <mat-form-field appearance="outline">
              <mat-label>Incident Title *</mat-label>
              <input matInput formControlName="title" placeholder="Brief title of the incident">
            </mat-form-field>
          </div>
          <div class="field-row two-col">
            <mat-form-field appearance="outline">
              <mat-label>Incident Type *</mat-label>
              <mat-select formControlName="incidentType">
                <mat-option value="INJURY">Injury</mat-option>
                <mat-option value="NEAR_MISS">Near Miss</mat-option>
                <mat-option value="FATALITY">Fatality</mat-option>
                <mat-option value="PROPERTY_DAMAGE">Property Damage</mat-option>
                <mat-option value="ENVIRONMENTAL">Environmental</mat-option>
                <mat-option value="FIRE">Fire</mat-option>
                <mat-option value="EXPLOSION">Explosion</mat-option>
                <mat-option value="ELECTRICAL">Electrical</mat-option>
                <mat-option value="FALL_OF_GROUND">Fall of Ground</mat-option>
                <mat-option value="EQUIPMENT_FAILURE">Equipment Failure</mat-option>
                <mat-option value="HAZARDOUS_SUBSTANCE">Hazardous Substance</mat-option>
                <mat-option value="OTHER">Other</mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Severity *</mat-label>
              <mat-select formControlName="severity">
                <mat-option value="LOW">Low</mat-option>
                <mat-option value="MEDIUM">Medium</mat-option>
                <mat-option value="HIGH">High</mat-option>
                <mat-option value="CRITICAL">Critical</mat-option>
              </mat-select>
            </mat-form-field>
          </div>
          <div class="field-row">
            <mat-form-field appearance="outline">
              <mat-label>Description *</mat-label>
              <textarea matInput formControlName="description" rows="4"
                        placeholder="Detailed description of what happened..."></textarea>
            </mat-form-field>
          </div>
        </div>

        <!-- Section 2: Location & Time -->
        <div class="sm-card form-section">
          <h3 class="section-title"><mat-icon>location_on</mat-icon> Location & Time</h3>
          <div class="field-row two-col">
            <mat-form-field appearance="outline">
              <mat-label>Location *</mat-label>
              <input matInput formControlName="location" placeholder="e.g. Level 5, Shaft 2">
              <mat-icon matPrefix>place</mat-icon>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Section / Area</mat-label>
              <input matInput formControlName="section" placeholder="e.g. Underground East">
            </mat-form-field>
          </div>
          <div class="field-row two-col">
            <mat-form-field appearance="outline">
              <mat-label>Date & Time of Incident *</mat-label>
              <input matInput formControlName="incidentDateTime" type="datetime-local">
              <mat-icon matPrefix>schedule</mat-icon>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Shift</mat-label>
              <mat-select formControlName="shiftTime">
                <mat-option value="DAY">Day Shift</mat-option>
                <mat-option value="AFTERNOON">Afternoon Shift</mat-option>
                <mat-option value="NIGHT">Night Shift</mat-option>
              </mat-select>
            </mat-form-field>
          </div>
        </div>

        <!-- Section 3: Injury Info -->
        <div class="sm-card form-section">
          <h3 class="section-title"><mat-icon>personal_injury</mat-icon> Injury Information</h3>
          <mat-checkbox formControlName="injuryOccurred" color="warn">
            Injury occurred in this incident
          </mat-checkbox>
          <div *ngIf="form.get('injuryOccurred')?.value" class="injury-fields">
            <div class="field-row two-col">
              <mat-form-field appearance="outline">
                <mat-label>Number of People Injured</mat-label>
                <input matInput formControlName="numberOfInjured" type="number" min="1">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Nature of Injury</mat-label>
                <input matInput formControlName="injuryDescription" placeholder="e.g. Laceration to left hand">
              </mat-form-field>
            </div>
          </div>
        </div>

        <!-- Section 4: Immediate Actions -->
        <div class="sm-card form-section">
          <h3 class="section-title"><mat-icon>bolt</mat-icon> Immediate Actions Taken</h3>
          <div class="field-row">
            <mat-form-field appearance="outline">
              <mat-label>Immediate Actions</mat-label>
              <textarea matInput formControlName="immediateActions" rows="3"
                        placeholder="What immediate actions were taken after the incident?"></textarea>
            </mat-form-field>
          </div>
        </div>

      </div>

      <!-- Submit -->
      <div class="form-actions">
        <button mat-button type="button" class="btn-ghost" (click)="cancel()">Cancel</button>
        <button mat-flat-button type="submit" class="btn-primary" [disabled]="form.invalid || loading">
          <mat-spinner *ngIf="loading" diameter="18"></mat-spinner>
          <mat-icon *ngIf="!loading">send</mat-icon>
          {{ loading ? 'Submitting...' : 'Submit Incident Report' }}
        </button>
      </div>
    </form>
  `,
  styles: [`
    .form-grid { display: flex; flex-direction: column; gap: 20px; }
    .form-section { padding: 24px; }
    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 20px;
      color: var(--text-primary);
      mat-icon { color: var(--primary); font-size: 20px; }
    }
    .field-row { margin-bottom: 4px; }
    .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .injury-fields { margin-top: 16px; }
    .form-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      margin-top: 8px;
      padding: 16px 0;
    }
    button[mat-flat-button] {
      display: flex;
      align-items: center;
      gap: 8px;
      height: 44px;
      padding: 0 24px;
    }
  `]
})
export class IncidentFormComponent {
  form: FormGroup;
  loading = false;

  constructor(private fb: FormBuilder, private incidentService: IncidentService,
              private router: Router, private snackBar: MatSnackBar) {
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      incidentType: ['', Validators.required],
      severity: ['', Validators.required],
      location: ['', Validators.required],
      section: [''],
      shiftTime: [''],
      incidentDateTime: ['', Validators.required],
      injuryOccurred: [false],
      numberOfInjured: [0],
      injuryDescription: [''],
      immediateActions: ['']
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.loading = true;
    this.incidentService.create(this.form.value).subscribe({
      next: inc => {
        this.snackBar.open('Incident reported successfully!', 'Close',
          { duration: 4000, panelClass: 'success-snack' });
        this.router.navigate(['/incidents', inc.id]);
      },
      error: () => {
        this.snackBar.open('Failed to submit incident. Please try again.', 'Close',
          { duration: 4000, panelClass: 'error-snack' });
        this.loading = false;
      }
    });
  }

  cancel() { this.router.navigate(['/incidents']); }
}
