import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User, UserRequest } from '../../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly API = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  getAll()                              { return this.http.get<User[]>(this.API); }
  getById(id: number)                   { return this.http.get<User>(`${this.API}/${id}`); }
  create(req: UserRequest)              { return this.http.post<User>(this.API, req); }
  update(id: number, req: UserRequest)  { return this.http.put<User>(`${this.API}/${id}`, req); }
  toggleStatus(id: number)              { return this.http.patch<void>(`${this.API}/${id}/toggle-status`, {}); }
  delete(id: number)                    { return this.http.delete<void>(`${this.API}/${id}`); }
}
