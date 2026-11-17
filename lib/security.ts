import crypto from 'node:crypto';
import type { EmergencyContact, LocationMode } from './types';

const PBKDF2_ITERATIONS = 65536;
const PBKDF2_KEYLEN = 32;
const PBKDF2_DIGEST = 'sha256';

export function hashPassword(password: string): { hash: string; salt: string } {
  const salt = crypto.randomBytes(16).toString('base64');
  const derived = crypto.pbkdf2Sync(
    password,
    Buffer.from(salt, 'base64'),
    PBKDF2_ITERATIONS,
    PBKDF2_KEYLEN,
    PBKDF2_DIGEST
  );
  return { hash: derived.toString('base64'), salt };
}

export function verifyPassword(password: string, storedHash: string, storedSalt: string): boolean {
  if (!password || !storedHash || !storedSalt) {
    return false;
  }
  try {
    const derived = crypto.pbkdf2Sync(
      password,
      Buffer.from(storedSalt, 'base64'),
      PBKDF2_ITERATIONS,
      PBKDF2_KEYLEN,
      PBKDF2_DIGEST
    );
    const expected = Buffer.from(storedHash, 'base64');
    if (derived.length !== expected.length) {
      return false;
    }
    return crypto.timingSafeEqual(derived, expected);
  } catch {
    return false;
  }
}

export function generateSessionToken(): string {
  return crypto.randomBytes(32).toString('hex');
}

export function normalizeEmail(email: string): string {
  return email.trim().toLowerCase();
}

export function validateEmail(email: string): string | null {
  const clean = email.trim();
  if (!clean) {
    return 'Email address is required.';
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
  if (!emailRegex.test(clean)) {
    return 'Please enter a valid email address.';
  }
  return null;
}

export function validateFullName(name: string): string | null {
  const clean = name.trim();
  if (clean.length < 2) {
    return 'Full name must be at least 2 characters.';
  }
  if (clean.length > 80) {
    return 'Full name cannot exceed 80 characters.';
  }
  return null;
}

export function validatePassword(password: string): string | null {
  if (!password || password.length < 6) {
    return 'Password must be at least 6 characters.';
  }
  if (password.length > 128) {
    return 'Password is too long.';
  }
  return null;
}

export function validatePhone(phone: string, required = true): string | null {
  const clean = phone.trim();
  if (!clean) {
    return required ? 'Phone number is required.' : null;
  }
  const digits = clean.replace(/\D/g, '');
  if (digits.length < 7 || digits.length > 15) {
    return 'Please enter a valid phone number (7–15 digits).';
  }
  return null;
}

export function validateCoordinates(lat: number, lng: number): string | null {
  if (!Number.isFinite(lat) || lat < -90 || lat > 90) {
    return 'Latitude must be between -90 and 90.';
  }
  if (!Number.isFinite(lng) || lng < -180 || lng > 180) {
    return 'Longitude must be between -180 and 180.';
  }
  return null;
}

export function buildGoogleMapsUrl(lat: number, lng: number): string {
  return `https://www.google.com/maps?q=${lat.toFixed(6)},${lng.toFixed(6)}`;
}

export function formatCoordinates(lat: number, lng: number): string {
  const latDir = lat >= 0 ? 'N' : 'S';
  const lngDir = lng >= 0 ? 'E' : 'W';
  return `${Math.abs(lat).toFixed(4)}° ${latDir}, ${Math.abs(lng).toFixed(4)}° ${lngDir}`;
}

export function buildEmergencyAlertText(params: {
  senderName: string;
  latitude: number;
  longitude: number;
  locationLabel: string;
  locationType: LocationMode;
}): string {
  const mapUrl = buildGoogleMapsUrl(params.latitude, params.longitude);
  return `EMERGENCY SOS from ${params.senderName}. Location (${params.locationType}): ${params.locationLabel} (${formatCoordinates(params.latitude, params.longitude)}). Map: ${mapUrl}`;
}

export async function dispatchWebhookNotification(params: {
  senderName: string;
  senderEmail: string;
  contacts: EmergencyContact[];
  latitude: number;
  longitude: number;
  locationLabel: string;
  locationType: LocationMode;
}): Promise<{ status: 'SENT' | 'NOT_CONFIGURED' | 'NO_CONTACTS' | 'FAILED'; note: string }> {
  if (params.contacts.length === 0) {
    return {
      status: 'NO_CONTACTS',
      note: 'No trusted contacts saved. Add trusted contacts to dispatch alerts.',
    };
  }

  const webhookUrl = process.env.WSS_SMS_WEBHOOK_URL?.trim();
  if (!webhookUrl) {
    return {
      status: 'NOT_CONFIGURED',
      note: `External SMS/Email gateway not configured (WSS_SMS_WEBHOOK_URL unset). ${params.contacts.length} trusted contact${params.contacts.length === 1 ? '' : 's'} ready for manual SMS/call.`,
    };
  }

  try {
    const res = await fetch(webhookUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        event: 'WOMEN_SAFETY_SOS',
        senderName: params.senderName,
        senderEmail: params.senderEmail,
        location: {
          latitude: params.latitude,
          longitude: params.longitude,
          label: params.locationLabel,
          mode: params.locationType,
          googleMapsUrl: buildGoogleMapsUrl(params.latitude, params.longitude),
        },
        recipients: params.contacts.map((c) => ({
          name: c.name,
          phone: c.phone,
          relationship: c.relationship,
        })),
        message: buildEmergencyAlertText({
          senderName: params.senderName,
          latitude: params.latitude,
          longitude: params.longitude,
          locationLabel: params.locationLabel,
          locationType: params.locationType,
        }),
      }),
      signal: AbortSignal.timeout(6000),
    });

    if (!res.ok) {
      return {
        status: 'FAILED',
        note: `Configured alert webhook returned HTTP ${res.status}. SOS saved to database.`,
      };
    }

    return {
      status: 'SENT',
      note: `Alert dispatched via webhook to ${params.contacts.length} trusted contact${params.contacts.length === 1 ? '' : 's'}.`,
    };
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Network error';
    return {
      status: 'FAILED',
      note: `Webhook delivery failed (${message}). SOS saved to database.`,
    };
  }
}
