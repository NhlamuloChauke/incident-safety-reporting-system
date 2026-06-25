import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-user-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatIconModule,
    MatSlideToggleModule, MatProgressSpinnerModule, MatSnackBarModule
  ],
  template: `
    <div class="dialog-container">
      <div class="dialog-header">
        <div class="dialog-title-row">
          <div class="dialog-icon">
            <mat-icon>{{ isEdit ? 'edit' : 'person_add' }}</mat-icon>
          </div>
          <div>
            <h2>{{ isEdit ? 'Edit User' : 'Add New User' }}</h2>
            <p>{{ isEdit ? 'Update user details and role' : 'Create a new system user and assign a role' }}</p>
          </div>
        </div>
        <button mat-icon-button mat-dialog-close>
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="dialog-body">

        <!-- Full Name + Employee Number -->
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Full Name *</mat-label>
            <input matInput formControlName="fullName" placeholder="e.g. John Dlamini">
            <mat-icon matPrefix>person</mat-icon>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Employee Number *</mat-label>
            <input matInput formControlName="employeeNumber" placeholder="e.g. EMP004">
            <mat-icon matPrefix>badge</mat-icon>
          </mat-form-field>
        </div>

        <!-- Email -->
        <mat-form-field appearance="outline">
          <mat-label>Email Address *</mat-label>
          <input matInput formControlName="email" type="email" placeholder="john&#64;mine.co.za">
          <mat-icon matPrefix>email</mat-icon>
        </mat-form-field>

        <!-- Role -->
        <mat-form-field appearance="outline">
          <mat-label>Role *</mat-label>
          <mat-select formControlName="role">
            <mat-option value="ADMIN">
              <div class="role-option admin">Admin — Full system access</div>
            </mat-option>
            <mat-option value="MANAGER">
              <div class="role-option manager">Manager — View all, update status</div>
            </mat-option>
            <mat-option value="SAFETY_OFFICER">
              <div class="role-option safety">Safety Officer — Manage incidents, actions</div>
            </mat-option>
            <mat-option value="SUPERVISOR">
              <div class="role-option supervisor">Supervisor — Report & view incidents</div>
            </mat-option>
            <mat-option value="WORKER">
              <div class="role-option worker">Worker — Report incidents only</div>
            </mat-option>
          </mat-select>
          <mat-icon matPrefix>security</mat-icon>
        </mat-form-field>

        <!-- Department + Section -->
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Department</mat-label>
            <input matInput formControlName="department" placeholder="e.g. Mining, Safety">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Section / Area</mat-label>
            <input matInput formControlName="section" placeholder="e.g. Underground East">
          </mat-form-field>
        </div>

        <!-- Phone -->
        <mat-form-field appearance="outline">
          <mat-label>Phone Number</mat-label>
          <input matInput formControlName="phoneNumber" placeholder="+27 82 000 0000">
          <mat-icon matPrefix>phone</mat-icon>
        </mat-form-field>

        <!-- Password -->
        <mat-form-field appearance="outline">
          <mat-label>{{ isEdit ? 'New Password (leave blank to keep current)' : 'Password' }}</mat-label>
          <input matInput formControlName="password" [type]="showPwd ? 'text' : 'password'"
                 placeholder="{{ isEdit ? 'Leave blank to keep unchanged' : 'Min 6 characters' }}">
          <mat-icon matPrefix>lock</mat-icon>
          <button mat-icon-button matSuffix type="button" (click)="showPwd = !showPwd">
            <mat-icon>{{ showPwd ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          <mat-hint *ngIf="!isEdit">Default: SafeMine&#64;2024 if left blank</mat-hint>
        </mat-form-field>

        <!-- Active Toggle -->
        <div class="active-row">
          <mat-slide-toggle formControlName="active" color="primary">
            Account Active
          </mat-slide-toggle>
          <span class="active-hint">
            {{ form.get('active')?.value ? 'User can log in' : 'User cannot log in' }}
          </span>
        </div>

        <!-- Role Description -->
        <div class="role-info" *ngIf="form.get('role')?.value">
          <mat-icon>info</mat-icon>
          <span>{{ roleDescription }}</span>
        </div>

        <div class="error-msg" *ngIf="errorMsg">
          <mat-icon>error</mat-icon> {{ errorMsg }}
        </div>
      </form>

      <div class="dialog-footer">
        <button mat-button mat-dialog-close class="btn-ghost">Cancel</button>
        <button mat-flat-button class="btn-primary" (click)="onSubmit()"
                [disabled]="form.invalid || loading">
          <mat-spinner *ngIf="loading" diameter="18"></mat-spinner>
          <mat-icon *ngIf="!loading">{{ isEdit ? 'save' : 'person_add' }}</mat-icon>
          {{ loading ? 'Saving...' : (isEdit ? 'Update User' : 'Create User') }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container {
      background: var(--bg-card);
      color: var(--text-primary);
      border-radius: 12px;
      overflow: hidden;
      min-width: 520px;
    }

    .dialog-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      padding: 24px 24px 0;

      .dialog-title-row {
        display: flex;
        align-items: center;
        gap: 14px;
      }

      .dialog-icon {
        width: 44px; height: 44px;
        background: rgba(255,107,0,0.15);
        border-radius: 10px;
        display: flex; align-items: center; justify-content: center;
        mat-icon { color: var(--primary); font-size: 22px; }
      }

      h2 { font-size: 18px; font-weight: 700; }
      p  { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }
    }

    .dialog-body {
      padding: 20px 24px;
      display: flex;
      flex-direction: column;
      gap: 4px;
      max-height: 65vh;
      overflow-y: auto;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }

    .active-row {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;
      .active-hint { font-size: 12px; color: var(--text-secondary); }
    }

    .role-info {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: var(--text-secondary);
      padding: 10px 12px;
      background: rgba(88,166,255,0.08);
      border-radius: 8px;
      border: 1px solid rgba(88,166,255,0.15);
      mat-icon { font-size: 16px; color: var(--info); }
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
      mat-icon { font-size: 18px; }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding: 16px 24px;
      border-top: 1px solid var(--border);

      button { display: flex; align-items: center; gap: 8px; }
    }

    .role-option { font-size: 13px; }
  `]
})
export class UserDialogComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  errorMsg = '';
  showPwd = false;
  isEdit = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<UserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: User | null
  ) {}

  ngOnInit() {
    this.isEdit = !!this.data;
    this.form = this.fb.group({
      fullName:       [this.data?.fullName ?? '', Validators.required],
      employeeNumber: [this.data?.employeeNumber ?? '', Validators.required],
      email:          [this.data?.email ?? '', [Validators.required, Validators.email]],
      role:           [this.data?.role ?? '', Validators.required],
      department:     [this.data?.department ?? ''],
      section:        [this.data?.section ?? ''],
      phoneNumber:    [this.data?.phoneNumber ?? ''],
      password:       [''],
      active:         [this.data?.active ?? true]
    });
  }

  get roleDescription(): string {
    const map: Record<string, string> = {
      ADMIN:          'Full access — manage users, all incidents, reports, and system settings.',
      MANAGER:        'View all incidents and reports, update statuses, assign to safety officers.',
      SAFETY_OFFICER: 'Manage incidents, add corrective actions, perform investigations.',
      SUPERVISOR:     'Report incidents, view their section\'s incidents and corrective actions.',
      WORKER:         'Report new incidents only. Cannot view other users\' reports.'
    };
    return map[this.form.get('role')?.value] ?? '';
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMsg = '';

    const payload = this.form.value;
    const action = this.isEdit
      ? this.userService.update(this.data!.id, payload)
      : this.userService.create(payload);

    action.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEdit ? 'User updated successfully' : 'User created successfully',
          'Close', { duration: 3000, panelClass: 'success-snack' }
        );
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.errorMsg = err?.error?.message ?? 'Failed to save user. Please try again.';
        this.loading = false;
      }
    });
  }
}
