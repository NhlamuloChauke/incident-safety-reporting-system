import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DashboardStats, Incident, IncidentRequest } from '../../models/incident.model';

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly API = '/api';

  constructor(private http: HttpClient) {}

  getAll()          { return this.http.get<Incident[]>(`${this.API}/incidents`); }
  getById(id: number) { return this.http.get<Incident>(`${this.API}/incidents/${id}`); }
  create(req: IncidentRequest) { return this.http.post<Incident>(`${this.API}/incidents`, req); }
  updateStatus(id: number, status: string) {
    return this.http.patch<Incident>(`${this.API}/incidents/${id}/status`, { status });
  }
  updateRootCause(id: number, rootCause: string) {
    return this.http.patch<Incident>(`${this.API}/incidents/${id}/root-cause`, { rootCause });
  }
  getDashboardStats() { return this.http.get<DashboardStats>(`${this.API}/dashboard/stats`); }
}
