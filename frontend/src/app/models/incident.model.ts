export type IncidentType = 'INJURY' | 'NEAR_MISS' | 'FATALITY' | 'PROPERTY_DAMAGE' |
  'ENVIRONMENTAL' | 'FIRE' | 'EXPLOSION' | 'ELECTRICAL' | 'FALL_OF_GROUND' |
  'EQUIPMENT_FAILURE' | 'HAZARDOUS_SUBSTANCE' | 'OTHER';

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type IncidentStatus = 'REPORTED' | 'UNDER_INVESTIGATION' |
  'CORRECTIVE_ACTION_PENDING' | 'CORRECTIVE_ACTION_IN_PROGRESS' | 'CLOSED' | 'DMR_NOTIFIED';

export interface Incident {
  id: number;
  referenceNumber: string;
  title: string;
  description: string;
  incidentType: IncidentType;
  severity: Severity;
  status: IncidentStatus;
  location: string;
  section: string;
  shiftTime: string;
  incidentDateTime: string;
  reportedAt: string;
  injuryOccurred: boolean;
  numberOfInjured: number;
  injuryDescription: string;
  dmrNotified: boolean;
  immediateActions: string;
  rootCause: string;
  reportedByName: string;
  assignedToName: string;
}

export interface IncidentRequest {
  title: string;
  description: string;
  incidentType: IncidentType;
  severity: Severity;
  location: string;
  section?: string;
  shiftTime?: string;
  incidentDateTime: string;
  injuryOccurred: boolean;
  numberOfInjured?: number;
  injuryDescription?: string;
  immediateActions?: string;
  assignedToId?: number;
}

export interface DashboardStats {
  totalIncidents: number;
  openIncidents: number;
  criticalIncidents: number;
  closedIncidents: number;
  incidentsThisMonth: number;
  nearMisses: number;
  injuries: number;
  dmrNotified: number;
  overdueActions: number;
  pendingActions: number;
}
