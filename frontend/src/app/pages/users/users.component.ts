import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../../core/services/user.service';
import { User, UserRequest, Role } from '../../models/user.model';
import { UserDialogComponent } from './user-dialog/user-dialog.component';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatTableModule, MatIconModule, MatButtonModule,
    MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatSlideToggleModule, MatTooltipModule, MatProgressSpinnerModule, MatSnackBarModule
  ],
  template: `
    <div class="page-header">
      <div class="header-row">
        <div>
          <h1>User Management</h1>
          <p>{{ users.length }} registered user{{ users.length !== 1 ? 's' : '' }}</p>
        </div>
        <button mat-flat-button class="btn-primary" (click)="openDialog()">
          <mat-icon>person_add</mat-icon> Add User
        </button>
      </div>
    </div>

    <!-- Role Summary Cards -->
    <div class="role-cards">
      <div class="role-card" *ngFor="let r of roleStats">
        <div class="role-icon" [style.background]="r.bg">
          <mat-icon [style.color]="r.color">{{ r.icon }}</mat-icon>
        </div>
        <div>
          <div class="role-count">{{ r.count }}</div>
          <div class="role-label">{{ r.label }}</div>
        </div>
      </div>
    </div>

    <div *ngIf="loading" class="loading-center"><mat-spinner diameter="40"></mat-spinner></div>

    <div class="sm-card" *ngIf="!loading" style="padding:0;overflow:hidden;margin-top:20px">
      <table mat-table [dataSource]="users" style="width:100%">

        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Employee</th>
          <td mat-cell *matCellDef="let u">
            <div class="user-cell">
              <div class="user-avatar-sm">{{ initials(u.fullName) }}</div>
              <div>
                <div class="user-name">{{ u.fullName }}</div>
                <div class="user-emp">{{ u.employeeNumber }}</div>
              </div>
            </div>
          </td>
        </ng-container>

        <ng-container matColumnDef="email">
          <th mat-header-cell *matHeaderCellDef>Email</th>
          <td mat-cell *matCellDef="let u" style="font-size:13px;color:var(--text-secondary)">{{ u.email }}</td>
        </ng-container>

        <ng-container matColumnDef="role">
          <th mat-header-cell *matHeaderCellDef>Role</th>
          <td mat-cell *matCellDef="let u">
            <span class="role-badge" [ngClass]="u.role.toLowerCase()">{{ roleLabel(u.role) }}</span>
          </td>
        </ng-container>

        <ng-container matColumnDef="dept">
          <th mat-header-cell *matHeaderCellDef>Department</th>
          <td mat-cell *matCellDef="let u" style="font-size:13px">{{ u.department || '—' }}</td>
        </ng-container>

        <ng-container matColumnDef="section">
          <th mat-header-cell *matHeaderCellDef>Section</th>
          <td mat-cell *matCellDef="let u" style="font-size:13px">{{ u.section || '—' }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let u">
            <span class="status-dot" [class.active]="u.active" [class.inactive]="!u.active">
              {{ u.active ? 'Active' : 'Inactive' }}
            </span>
          </td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let u">
            <button mat-icon-button matTooltip="Edit User" (click)="openDialog(u)">
              <mat-icon style="color:var(--info)">edit</mat-icon>
            </button>
            <button mat-icon-button [matTooltip]="u.active ? 'Deactivate' : 'Activate'"
                    (click)="toggleStatus(u)">
              <mat-icon [style.color]="u.active ? 'var(--warning)' : 'var(--success)'">
                {{ u.active ? 'block' : 'check_circle' }}
              </mat-icon>
            </button>
            <button mat-icon-button matTooltip="Delete User" (click)="deleteUser(u)">
              <mat-icon style="color:var(--danger)">delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let row; columns: columns;"></tr>
      </table>

      <div *ngIf="users.length === 0" style="padding:60px;text-align:center;color:var(--text-secondary)">
        <mat-icon style="font-size:48px;width:48px;height:48px;opacity:0.3">group</mat-icon>
        <p style="margin-top:12px">No users found</p>
      </div>
    </div>
  `,
  styles: [`
    .header-row { display: flex; align-items: flex-start; justify-content: space-between; }

    .role-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 12px;
      margin-bottom: 4px;
    }
    .role-card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      .role-icon {
        width: 40px; height: 40px; border-radius: 8px;
        display: flex; align-items: center; justify-content: center;
        mat-icon { font-size: 20px; }
      }
      .role-count { font-size: 22px; font-weight: 700; }
      .role-label { font-size: 11px; color: var(--text-secondary); }
    }

    .loading-center { display: flex; justify-content: center; padding: 80px; }

    .user-cell { display: flex; align-items: center; gap: 10px; }
    .user-avatar-sm {
      width: 34px; height: 34px; border-radius: 50%;
      background: var(--primary); color: white;
      display: flex; align-items: center; justify-content: center;
      font-size: 12px; font-weight: 700; flex-shrink: 0;
    }
    .user-name { font-size: 14px; font-weight: 500; }
    .user-emp  { font-size: 11px; color: var(--text-secondary); }

    .role-badge {
      padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600;
      text-transform: uppercase; letter-spacing: 0.5px;
      &.admin          { background: rgba(255,107,0,0.15); color: #FF6B00; }
      &.manager        { background: rgba(88,166,255,0.15); color: #58A6FF; }
      &.safety_officer { background: rgba(210,153,34,0.15); color: #D29922; }
      &.supervisor     { background: rgba(63,185,80,0.15); color: #3FB950; }
      &.worker         { background: rgba(139,148,158,0.15); color: #8B949E; }
    }

    .status-dot {
      display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 500;
      &::before { content: ''; width: 7px; height: 7px; border-radius: 50%; }
      &.active   { color: var(--success); &::before { background: var(--success); } }
      &.inactive { color: var(--text-secondary); &::before { background: var(--text-secondary); } }
    }
  `]
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  loading = true;
  columns = ['name', 'email', 'role', 'dept', 'section', 'status', 'actions'];

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  get roleStats() {
    return [
      { label: 'Admins',          icon: 'admin_panel_settings', count: this.count('ADMIN'),          bg: 'rgba(255,107,0,0.15)', color: '#FF6B00' },
      { label: 'Managers',        icon: 'manage_accounts',       count: this.count('MANAGER'),        bg: 'rgba(88,166,255,0.15)', color: '#58A6FF' },
      { label: 'Safety Officers', icon: 'health_and_safety',     count: this.count('SAFETY_OFFICER'), bg: 'rgba(210,153,34,0.15)', color: '#D29922' },
      { label: 'Supervisors',     icon: 'supervisor_account',    count: this.count('SUPERVISOR'),     bg: 'rgba(63,185,80,0.15)', color: '#3FB950' },
      { label: 'Workers',         icon: 'engineering',           count: this.count('WORKER'),         bg: 'rgba(139,148,158,0.15)', color: '#8B949E' },
    ];
  }

  ngOnInit() { this.loadUsers(); }

  loadUsers() {
    this.loading = true;
    this.userService.getAll().subscribe({
      next: list => { this.users = list; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  openDialog(user?: User) {
    const ref = this.dialog.open(UserDialogComponent, {
      width: '560px',
      data: user ?? null,
      panelClass: 'dark-dialog'
    });
    ref.afterClosed().subscribe(result => { if (result) this.loadUsers(); });
  }

  toggleStatus(user: User) {
    this.userService.toggleStatus(user.id).subscribe({
      next: () => {
        this.snackBar.open(`User ${user.active ? 'deactivated' : 'activated'}`, 'Close',
          { duration: 3000, panelClass: 'success-snack' });
        this.loadUsers();
      }
    });
  }

  deleteUser(user: User) {
    if (!confirm(`Delete ${user.fullName}? This cannot be undone.`)) return;
    this.userService.delete(user.id).subscribe({
      next: () => {
        this.snackBar.open('User deleted', 'Close', { duration: 3000, panelClass: 'success-snack' });
        this.loadUsers();
      }
    });
  }

  initials(name: string) {
    return name?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) ?? 'U';
  }

  roleLabel(role: Role) {
    const map: Record<Role, string> = {
      ADMIN: 'Admin', MANAGER: 'Manager', SAFETY_OFFICER: 'Safety Officer',
      SUPERVISOR: 'Supervisor', WORKER: 'Worker'
    };
    return map[role] ?? role;
  }

  count(role: string) { return this.users.filter(u => u.role === role).length; }
}
