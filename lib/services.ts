import { getDb } from './db';
import {
  dispatchWebhookNotification,
  generateSessionToken,
  hashPassword,
  normalizeEmail,
  validateCoordinates,
  validateEmail,
  validateFullName,
  validatePassword,
  validatePhone,
  verifyPassword,
} from './security';
import {
  DEFAULT_SIMULATED_LOCATION,
  type EmergencyContact,
  type LocationMode,
  type LocationRecord,
  type PublicUser,
  type SOSRecord,
  type UserSettings,
  type WorkspaceBundle,
} from './types';

export interface ServiceResponse<T> {
  ok: boolean;
  data?: T;
  error?: string;
  statusCode?: number;
}

function mapUser(row: Record<string, unknown>): PublicUser {
  return {
    id: Number(row.id),
    fullName: String(row.full_name),
    email: String(row.email),
    phone: String(row.phone ?? ''),
    createdAt: String(row.created_at),
  };
}

function mapContact(row: Record<string, unknown>): EmergencyContact {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    name: String(row.name),
    phone: String(row.phone),
    relationship: String(row.relationship ?? ''),
    createdAt: String(row.created_at),
  };
}

function mapSOS(row: Record<string, unknown>): SOSRecord {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    status: String(row.status) === 'Cancelled' ? 'Cancelled' : 'Active',
    latitude: Number(row.latitude),
    longitude: Number(row.longitude),
    locationLabel: String(row.location_label ?? ''),
    locationType: (String(row.location_type) as LocationMode) || 'SIMULATED',
    deliveryStatus: (String(row.delivery_status) as SOSRecord['deliveryStatus']) || 'NOT_CONFIGURED',
    deliveryNote: String(row.delivery_note ?? ''),
    createdAt: String(row.created_at),
    cancelledAt: row.cancelled_at ? String(row.cancelled_at) : null,
  };
}

function mapLocation(row: Record<string, unknown>): LocationRecord {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    latitude: Number(row.latitude),
    longitude: Number(row.longitude),
    address: String(row.address ?? ''),
    locationType: (String(row.location_type) as LocationMode) || 'SIMULATED',
    createdAt: String(row.created_at),
  };
}

function mapSettings(row: Record<string, unknown>): UserSettings {
  return {
    userId: Number(row.user_id),
    confirmBeforeSos: Number(row.confirm_before_sos) === 1,
    includeLocationInSos: Number(row.include_location_in_sos) === 1,
    simulatedLat: Number(row.simulated_lat),
    simulatedLng: Number(row.simulated_lng),
    simulatedAddress: String(row.simulated_address),
    updatedAt: String(row.updated_at),
  };
}

function ensureUserSettings(userId: number): UserSettings {
  const db = getDb();
  const existing = db.prepare('SELECT * FROM user_settings WHERE user_id = ?').get(userId);
  if (existing) {
    return mapSettings(existing);
  }
  db.prepare(
    `INSERT INTO user_settings (user_id, confirm_before_sos, include_location_in_sos, simulated_lat, simulated_lng, simulated_address)
     VALUES (?, 1, 1, ?, ?, ?)`
  ).run(
    userId,
    DEFAULT_SIMULATED_LOCATION.latitude,
    DEFAULT_SIMULATED_LOCATION.longitude,
    DEFAULT_SIMULATED_LOCATION.address
  );
  const created = db.prepare('SELECT * FROM user_settings WHERE user_id = ?').get(userId);
  return mapSettings(created!);
}

function createSession(userId: number): string {
  const db = getDb();
  const token = generateSessionToken();
  const expiresAt = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString();
  db.prepare('INSERT INTO sessions (token, user_id, expires_at) VALUES (?, ?, ?)').run(
    token,
    userId,
    expiresAt
  );
  return token;
}

export function getUserBySessionToken(token: string | undefined): PublicUser | null {
  if (!token) {
    return null;
  }
  const db = getDb();
  const row = db
    .prepare(
      `SELECT u.* FROM sessions s
       JOIN users u ON u.id = s.user_id
       WHERE s.token = ? AND s.expires_at > ?`
    )
    .get(token, new Date().toISOString());
  return row ? mapUser(row) : null;
}

export function deleteSession(token: string | undefined): void {
  if (!token) {
    return;
  }
  const db = getDb();
  db.prepare('DELETE FROM sessions WHERE token = ?').run(token);
}

export function registerUser(params: {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  phone?: string;
}): ServiceResponse<{ user: PublicUser; token: string }> {
  const nameErr = validateFullName(params.fullName);
  if (nameErr) return { ok: false, error: nameErr, statusCode: 400 };

  const emailErr = validateEmail(params.email);
  if (emailErr) return { ok: false, error: emailErr, statusCode: 400 };

  const passErr = validatePassword(params.password);
  if (passErr) return { ok: false, error: passErr, statusCode: 400 };

  if (params.password !== params.confirmPassword) {
    return { ok: false, error: 'Passwords do not match.', statusCode: 400 };
  }

  const phone = params.phone?.trim() ?? '';
  if (phone) {
    const phoneErr = validatePhone(phone, false);
    if (phoneErr) return { ok: false, error: phoneErr, statusCode: 400 };
  }

  const db = getDb();
  const email = normalizeEmail(params.email);
  const duplicate = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
  if (duplicate) {
    return { ok: false, error: 'An account with this email already exists.', statusCode: 409 };
  }

  const { hash, salt } = hashPassword(params.password);
  const res = db
    .prepare(
      'INSERT INTO users (full_name, email, phone, password_hash, salt) VALUES (?, ?, ?, ?, ?)'
    )
    .run(params.fullName.trim(), email, phone, hash, salt);

  const userId = Number(res.lastInsertRowid);
  ensureUserSettings(userId);
  const row = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  const user = mapUser(row!);
  const token = createSession(userId);
  return { ok: true, data: { user, token }, statusCode: 201 };
}

export function loginUser(params: {
  email: string;
  password: string;
}): ServiceResponse<{ user: PublicUser; token: string }> {
  const emailErr = validateEmail(params.email);
  if (emailErr) return { ok: false, error: emailErr, statusCode: 400 };

  if (!params.password) {
    return { ok: false, error: 'Password is required.', statusCode: 400 };
  }

  const db = getDb();
  const email = normalizeEmail(params.email);
  const row = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
  if (!row) {
    return { ok: false, error: 'Invalid email or password.', statusCode: 401 };
  }

  const valid = verifyPassword(params.password, String(row.password_hash), String(row.salt));
  if (!valid) {
    return { ok: false, error: 'Invalid email or password.', statusCode: 401 };
  }

  const user = mapUser(row);
  ensureUserSettings(user.id);
  const token = createSession(user.id);
  return { ok: true, data: { user, token }, statusCode: 200 };
}

export function resetPassword(params: {
  email: string;
  fullName: string;
  newPassword: string;
  confirmPassword: string;
}): ServiceResponse<{ message: string }> {
  const emailErr = validateEmail(params.email);
  if (emailErr) return { ok: false, error: emailErr, statusCode: 400 };

  if (!params.fullName.trim()) {
    return { ok: false, error: 'Registered full name is required to verify ownership.', statusCode: 400 };
  }

  const passErr = validatePassword(params.newPassword);
  if (passErr) return { ok: false, error: passErr, statusCode: 400 };

  if (params.newPassword !== params.confirmPassword) {
    return { ok: false, error: 'Passwords do not match.', statusCode: 400 };
  }

  const db = getDb();
  const email = normalizeEmail(params.email);
  const row = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
  if (!row) {
    return { ok: false, error: 'No account found with that email.', statusCode: 404 };
  }

  const storedName = String(row.full_name).trim().toLowerCase();
  if (storedName !== params.fullName.trim().toLowerCase()) {
    return { ok: false, error: 'Full name does not match the registered account.', statusCode: 403 };
  }

  const { hash, salt } = hashPassword(params.newPassword);
  db.prepare("UPDATE users SET password_hash = ?, salt = ?, updated_at = datetime('now') WHERE id = ?").run(
    hash,
    salt,
    Number(row.id)
  );
  return { ok: true, data: { message: 'Password updated successfully. You may now sign in.' } };
}

export function getWorkspaceBundle(userId: number): ServiceResponse<WorkspaceBundle> {
  const db = getDb();
  const userRow = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  if (!userRow) {
    return { ok: false, error: 'User not found.', statusCode: 404 };
  }

  const user = mapUser(userRow);
  const settings = ensureUserSettings(userId);
  const contacts = db
    .prepare('SELECT * FROM emergency_contacts WHERE user_id = ? ORDER BY id DESC')
    .all(userId)
    .map(mapContact);
  const sosHistory = db
    .prepare('SELECT * FROM sos_history WHERE user_id = ? ORDER BY id DESC')
    .all(userId)
    .map(mapSOS);
  const locationHistory = db
    .prepare('SELECT * FROM location_history WHERE user_id = ? ORDER BY id DESC LIMIT 25')
    .all(userId)
    .map(mapLocation);

  const currentLocation: LocationRecord =
    locationHistory[0] ?? {
      id: 0,
      userId,
      latitude: settings.simulatedLat,
      longitude: settings.simulatedLng,
      address: settings.simulatedAddress,
      locationType: 'SIMULATED',
      createdAt: settings.updatedAt,
    };

  return {
    ok: true,
    data: {
      user,
      settings,
      contacts,
      sosHistory,
      locationHistory,
      currentLocation,
    },
  };
}

export function listContacts(userId: number): EmergencyContact[] {
  const db = getDb();
  return db
    .prepare('SELECT * FROM emergency_contacts WHERE user_id = ? ORDER BY id DESC')
    .all(userId)
    .map(mapContact);
}

export function createContact(
  userId: number,
  params: { name: string; phone: string; relationship?: string }
): ServiceResponse<EmergencyContact> {
  const name = params.name.trim();
  if (name.length < 2) {
    return { ok: false, error: 'Contact name must be at least 2 characters.', statusCode: 400 };
  }
  const phoneErr = validatePhone(params.phone, true);
  if (phoneErr) return { ok: false, error: phoneErr, statusCode: 400 };

  const db = getDb();
  const res = db
    .prepare('INSERT INTO emergency_contacts (user_id, name, phone, relationship) VALUES (?, ?, ?, ?)')
    .run(userId, name, params.phone.trim(), params.relationship?.trim() ?? '');

  const row = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(Number(res.lastInsertRowid));
  return { ok: true, data: mapContact(row!), statusCode: 201 };
}

export function updateContact(
  userId: number,
  contactId: number,
  params: { name: string; phone: string; relationship?: string }
): ServiceResponse<EmergencyContact> {
  const name = params.name.trim();
  if (name.length < 2) {
    return { ok: false, error: 'Contact name must be at least 2 characters.', statusCode: 400 };
  }
  const phoneErr = validatePhone(params.phone, true);
  if (phoneErr) return { ok: false, error: phoneErr, statusCode: 400 };

  const db = getDb();
  const existing = db
    .prepare('SELECT * FROM emergency_contacts WHERE id = ? AND user_id = ?')
    .get(contactId, userId);
  if (!existing) {
    return { ok: false, error: 'Contact not found.', statusCode: 404 };
  }

  db.prepare(
    'UPDATE emergency_contacts SET name = ?, phone = ?, relationship = ? WHERE id = ? AND user_id = ?'
  ).run(name, params.phone.trim(), params.relationship?.trim() ?? '', contactId, userId);

  const updated = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(contactId);
  return { ok: true, data: mapContact(updated!) };
}

export function deleteContact(userId: number, contactId: number): ServiceResponse<{ deleted: boolean }> {
  const db = getDb();
  const res = db
    .prepare('DELETE FROM emergency_contacts WHERE id = ? AND user_id = ?')
    .run(contactId, userId);
  if (Number(res.changes) === 0) {
    return { ok: false, error: 'Contact not found.', statusCode: 404 };
  }
  return { ok: true, data: { deleted: true } };
}

export function saveLocationSnapshot(
  userId: number,
  params: { latitude: number; longitude: number; address?: string; locationType?: LocationMode }
): ServiceResponse<LocationRecord> {
  const coordErr = validateCoordinates(params.latitude, params.longitude);
  if (coordErr) return { ok: false, error: coordErr, statusCode: 400 };

  const mode: LocationMode =
    params.locationType === 'GPS' || params.locationType === 'MANUAL' ? params.locationType : 'SIMULATED';
  const address =
    params.address?.trim() ||
    (mode === 'GPS' ? 'Browser Geolocation Fix' : DEFAULT_SIMULATED_LOCATION.address);

  const db = getDb();
  const res = db
    .prepare(
      'INSERT INTO location_history (user_id, latitude, longitude, address, location_type) VALUES (?, ?, ?, ?, ?)'
    )
    .run(userId, params.latitude, params.longitude, address, mode);

  const row = db.prepare('SELECT * FROM location_history WHERE id = ?').get(Number(res.lastInsertRowid));
  return { ok: true, data: mapLocation(row!), statusCode: 201 };
}

export async function triggerSOS(
  userId: number,
  params?: {
    latitude?: number;
    longitude?: number;
    address?: string;
    locationType?: LocationMode;
  }
): Promise<ServiceResponse<SOSRecord>> {
  const db = getDb();
  const userRow = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  if (!userRow) {
    return { ok: false, error: 'User not found.', statusCode: 404 };
  }
  const user = mapUser(userRow);
  const settings = ensureUserSettings(userId);

  const lat =
    typeof params?.latitude === 'number' && Number.isFinite(params.latitude)
      ? params.latitude
      : settings.simulatedLat;
  const lng =
    typeof params?.longitude === 'number' && Number.isFinite(params.longitude)
      ? params.longitude
      : settings.simulatedLng;

  const coordErr = validateCoordinates(lat, lng);
  if (coordErr) return { ok: false, error: coordErr, statusCode: 400 };

  const locationType: LocationMode =
    params?.locationType === 'GPS' || params?.locationType === 'MANUAL'
      ? params.locationType
      : 'SIMULATED';
  const locationLabel = params?.address?.trim() || settings.simulatedAddress;

  // Record location snapshot if enabled in settings
  if (settings.includeLocationInSos) {
    saveLocationSnapshot(userId, {
      latitude: lat,
      longitude: lng,
      address: locationLabel,
      locationType,
    });
  }

  const contacts = listContacts(userId);
  const dispatch = await dispatchWebhookNotification({
    senderName: user.fullName,
    senderEmail: user.email,
    contacts,
    latitude: lat,
    longitude: lng,
    locationLabel,
    locationType,
  });

  const res = db
    .prepare(
      `INSERT INTO sos_history (user_id, status, latitude, longitude, location_label, location_type, delivery_status, delivery_note)
       VALUES (?, 'Active', ?, ?, ?, ?, ?, ?)`
    )
    .run(userId, lat, lng, locationLabel, locationType, dispatch.status, dispatch.note);

  const row = db.prepare('SELECT * FROM sos_history WHERE id = ?').get(Number(res.lastInsertRowid));
  return { ok: true, data: mapSOS(row!), statusCode: 201 };
}

export function cancelSOS(userId: number, sosId: number): ServiceResponse<SOSRecord> {
  const db = getDb();
  const existing = db
    .prepare('SELECT * FROM sos_history WHERE id = ? AND user_id = ?')
    .get(sosId, userId);
  if (!existing) {
    return { ok: false, error: 'SOS record not found.', statusCode: 404 };
  }

  db.prepare(
    "UPDATE sos_history SET status = 'Cancelled', cancelled_at = datetime('now') WHERE id = ? AND user_id = ?"
  ).run(sosId, userId);

  const updated = db.prepare('SELECT * FROM sos_history WHERE id = ?').get(sosId);
  return { ok: true, data: mapSOS(updated!) };
}

export function deleteSOSRecord(userId: number, sosId: number): ServiceResponse<{ deleted: boolean }> {
  const db = getDb();
  const res = db.prepare('DELETE FROM sos_history WHERE id = ? AND user_id = ?').run(sosId, userId);
  if (Number(res.changes) === 0) {
    return { ok: false, error: 'SOS record not found.', statusCode: 404 };
  }
  return { ok: true, data: { deleted: true } };
}

export function updateProfile(
  userId: number,
  params: { fullName: string; phone?: string }
): ServiceResponse<PublicUser> {
  const nameErr = validateFullName(params.fullName);
  if (nameErr) return { ok: false, error: nameErr, statusCode: 400 };

  const phone = params.phone?.trim() ?? '';
  if (phone) {
    const phoneErr = validatePhone(phone, false);
    if (phoneErr) return { ok: false, error: phoneErr, statusCode: 400 };
  }

  const db = getDb();
  db.prepare("UPDATE users SET full_name = ?, phone = ?, updated_at = datetime('now') WHERE id = ?").run(
    params.fullName.trim(),
    phone,
    userId
  );

  const row = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  return { ok: true, data: mapUser(row!) };
}

export function changePassword(
  userId: number,
  params: { currentPassword: string; newPassword: string; confirmPassword: string }
): ServiceResponse<{ message: string }> {
  const db = getDb();
  const row = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  if (!row) {
    return { ok: false, error: 'User not found.', statusCode: 404 };
  }

  const valid = verifyPassword(
    params.currentPassword,
    String(row.password_hash),
    String(row.salt)
  );
  if (!valid) {
    return { ok: false, error: 'Current password is incorrect.', statusCode: 401 };
  }

  const passErr = validatePassword(params.newPassword);
  if (passErr) return { ok: false, error: passErr, statusCode: 400 };

  if (params.newPassword !== params.confirmPassword) {
    return { ok: false, error: 'New passwords do not match.', statusCode: 400 };
  }

  const { hash, salt } = hashPassword(params.newPassword);
  db.prepare("UPDATE users SET password_hash = ?, salt = ?, updated_at = datetime('now') WHERE id = ?").run(
    hash,
    salt,
    userId
  );
  return { ok: true, data: { message: 'Password changed successfully.' } };
}

export function updateSettings(
  userId: number,
  params: Partial<{
    confirmBeforeSos: boolean;
    includeLocationInSos: boolean;
    simulatedLat: number;
    simulatedLng: number;
    simulatedAddress: string;
  }>
): ServiceResponse<UserSettings> {
  const current = ensureUserSettings(userId);
  const nextLat = params.simulatedLat ?? current.simulatedLat;
  const nextLng = params.simulatedLng ?? current.simulatedLng;
  const coordErr = validateCoordinates(nextLat, nextLng);
  if (coordErr) return { ok: false, error: coordErr, statusCode: 400 };

  const nextConfirm = params.confirmBeforeSos ?? current.confirmBeforeSos;
  const nextIncludeLoc = params.includeLocationInSos ?? current.includeLocationInSos;
  const nextAddress = params.simulatedAddress?.trim() || current.simulatedAddress;

  const db = getDb();
  db.prepare(
    `UPDATE user_settings
     SET confirm_before_sos = ?, include_location_in_sos = ?, simulated_lat = ?, simulated_lng = ?, simulated_address = ?, updated_at = datetime('now')
     WHERE user_id = ?`
  ).run(nextConfirm ? 1 : 0, nextIncludeLoc ? 1 : 0, nextLat, nextLng, nextAddress, userId);

  const row = db.prepare('SELECT * FROM user_settings WHERE user_id = ?').get(userId);
  return { ok: true, data: mapSettings(row!) };
}
