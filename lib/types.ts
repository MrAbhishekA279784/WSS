export type AppView =
  | 'home'
  | 'location'
  | 'contacts'
  | 'history'
  | 'support'
  | 'profile'
  | 'settings'
  | 'sos';

export interface PublicUser {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  createdAt: string;
}

export interface EmergencyContact {
  id: number;
  userId: number;
  name: string;
  phone: string;
  relationship: string;
  createdAt: string;
}

export type SOSStatus = 'Active' | 'Cancelled';
export type LocationMode = 'GPS' | 'SIMULATED' | 'MANUAL';

export interface SOSRecord {
  id: number;
  userId: number;
  status: SOSStatus;
  latitude: number;
  longitude: number;
  locationLabel: string;
  locationType: LocationMode;
  deliveryStatus: 'SENT' | 'NOT_CONFIGURED' | 'NO_CONTACTS' | 'FAILED';
  deliveryNote: string;
  createdAt: string;
  cancelledAt: string | null;
}

export interface LocationRecord {
  id: number;
  userId: number;
  latitude: number;
  longitude: number;
  address: string;
  locationType: LocationMode;
  createdAt: string;
}

export interface UserSettings {
  userId: number;
  confirmBeforeSos: boolean;
  includeLocationInSos: boolean;
  simulatedLat: number;
  simulatedLng: number;
  simulatedAddress: string;
  updatedAt: string;
}

export interface EmergencyHelpline {
  id: string;
  title: string;
  number: string;
  description: string;
  icon: 'shield' | 'women' | 'ambulance' | 'phone';
}

export interface WorkspaceBundle {
  user: PublicUser;
  settings: UserSettings;
  contacts: EmergencyContact[];
  sosHistory: SOSRecord[];
  locationHistory: LocationRecord[];
  currentLocation: LocationRecord;
}

export const DEFAULT_SIMULATED_LOCATION = {
  latitude: 19.2638,
  longitude: 72.9629,
  address: 'Kandivali (E), Mumbai (Simulated Preset)',
};

export const EMERGENCY_HELPLINES: EmergencyHelpline[] = [
  {
    id: 'police',
    title: 'Police',
    number: '100',
    description: 'Immediate police dispatch and law enforcement response.',
    icon: 'shield',
  },
  {
    id: 'women',
    title: 'Women Helpline',
    number: '181',
    description: 'Dedicated 24/7 women safety support and crisis counseling.',
    icon: 'women',
  },
  {
    id: 'ambulance',
    title: 'Ambulance',
    number: '108',
    description: 'Emergency medical response and paramedic transport.',
    icon: 'ambulance',
  },
  {
    id: 'all',
    title: 'Emergency (All Services)',
    number: '112',
    description: 'Unified national emergency response across all services.',
    icon: 'phone',
  },
];
