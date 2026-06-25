export type Role = 'WORKER' | 'SUPERVISOR' | 'SAFETY_OFFICER' | 'MANAGER' | 'ADMIN';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  employeeNumber: string;
  phoneNumber?: string;
  department?: string;
  section?: string;
  active: boolean;
  createdAt?: string;
}

export interface UserRequest {
  fullName: string;
  employeeNumber: string;
  email: string;
  password?: string;
  role: Role;
  phoneNumber?: string;
  department?: string;
  section?: string;
  active: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  fullName: string;
  email: string;
  role: Role;
  userId: number;
}
