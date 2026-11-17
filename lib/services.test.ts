import test, { after, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { getDb, resetDbForTests } from './db';
import {
  buildGoogleMapsUrl,
  formatCoordinates,
  hashPassword,
  verifyPassword,
} from './security';
import {
  cancelSOS,
  changePassword,
  createContact,
  deleteContact,
  getUserBySessionToken,
  getWorkspaceBundle,
  loginUser,
  registerUser,
  resetPassword,
  saveLocationSnapshot,
  triggerSOS,
  updateContact,
  updateProfile,
  updateSettings,
} from './services';

const testDbPath = path.join(os.tmpdir(), `wss-web-test-${process.pid}.db`);

beforeEach(() => {
  resetDbForTests();
  for (const suffix of ['', '-wal', '-shm']) {
    if (fs.existsSync(`${testDbPath}${suffix}`)) {
      fs.unlinkSync(`${testDbPath}${suffix}`);
    }
  }
  process.env.WSS_DB_PATH = testDbPath;
  delete process.env.WSS_SMS_WEBHOOK_URL;
  delete process.env.WSS_SEED_EMAIL;
  delete process.env.WSS_SEED_PASSWORD;
  delete process.env.WSS_SEED_NAME;
});

after(() => {
  resetDbForTests();
  for (const suffix of ['', '-wal', '-shm']) {
    if (fs.existsSync(`${testDbPath}${suffix}`)) {
      fs.unlinkSync(`${testDbPath}${suffix}`);
    }
  }
});

test('PBKDF2 password hashing and verification works and rejects wrong passwords', () => {
  const { hash, salt } = hashPassword('Safety@2026');
  assert.equal(verifyPassword('Safety@2026', hash, salt), true);
  assert.equal(verifyPassword('WrongPass', hash, salt), false);
});

test('Google Maps URL and coordinate formatting helpers match expected format', () => {
  assert.equal(
    buildGoogleMapsUrl(19.2638, 72.9629),
    'https://www.google.com/maps?q=19.263800,72.962900'
  );
  assert.equal(formatCoordinates(19.2638, 72.9629), '19.2638° N, 72.9629° E');
  assert.equal(formatCoordinates(-33.8688, -151.2093), '33.8688° S, 151.2093° W');
});

test('User registration, duplicate rejection, login, session lookup, and password reset', () => {
  const reg = registerUser({
    fullName: 'Abhishek Gupta',
    email: 'Abhishek@example.com',
    password: 'Safety@2026',
    confirmPassword: 'Safety@2026',
  });
  assert.equal(reg.ok, true);
  assert.ok(reg.data?.token);
  assert.equal(reg.data?.user.email, 'abhishek@example.com');

  const dup = registerUser({
    fullName: 'Another User',
    email: 'abhishek@example.com',
    password: 'Safety@2026',
    confirmPassword: 'Safety@2026',
  });
  assert.equal(dup.ok, false);
  assert.equal(dup.statusCode, 409);

  const sessionUser = getUserBySessionToken(reg.data?.token);
  assert.equal(sessionUser?.fullName, 'Abhishek Gupta');

  const badLogin = loginUser({ email: 'abhishek@example.com', password: 'WrongPassword' });
  assert.equal(badLogin.ok, false);

  const reset = resetPassword({
    email: 'abhishek@example.com',
    fullName: 'Abhishek Gupta',
    newPassword: 'NewSafety@2026',
    confirmPassword: 'NewSafety@2026',
  });
  assert.equal(reset.ok, true);

  const goodLogin = loginUser({ email: 'abhishek@example.com', password: 'NewSafety@2026' });
  assert.equal(goodLogin.ok, true);
});

test('Contacts CRUD, location history, SOS activation, honest gateway status, and cancellation', async () => {
  const reg = registerUser({
    fullName: 'Priya Sharma',
    email: 'priya@example.com',
    password: 'Safety@2026',
    confirmPassword: 'Safety@2026',
  });
  const userId = reg.data!.user.id;

  const c1 = createContact(userId, { name: 'Mom', phone: '+91 98765 43210', relationship: 'Mother' });
  assert.equal(c1.ok, true);
  const c2 = createContact(userId, { name: 'Brother', phone: '+91 99887 76655', relationship: 'Brother' });
  assert.equal(c2.ok, true);

  const updated = updateContact(userId, c2.data!.id, {
    name: 'Elder Brother',
    phone: '+91 99887 76655',
    relationship: 'Brother',
  });
  assert.equal(updated.ok, true);
  assert.equal(updated.data?.name, 'Elder Brother');

  const del = deleteContact(userId, c2.data!.id);
  assert.equal(del.ok, true);

  const loc = saveLocationSnapshot(userId, {
    latitude: 19.076,
    longitude: 72.8777,
    address: 'Mumbai Central',
    locationType: 'MANUAL',
  });
  assert.equal(loc.ok, true);

  const sos = await triggerSOS(userId, {
    latitude: 19.076,
    longitude: 72.8777,
    address: 'Mumbai Central',
    locationType: 'MANUAL',
  });
  assert.equal(sos.ok, true);
  assert.equal(sos.data?.status, 'Active');
  assert.equal(sos.data?.deliveryStatus, 'NOT_CONFIGURED');

  const cancelled = cancelSOS(userId, sos.data!.id);
  assert.equal(cancelled.ok, true);
  assert.equal(cancelled.data?.status, 'Cancelled');
  assert.ok(cancelled.data?.cancelledAt);

  const bundle = getWorkspaceBundle(userId);
  assert.equal(bundle.data?.contacts.length, 1);
  assert.equal(bundle.data?.sosHistory.length, 1);
  assert.equal(bundle.data?.sosHistory[0].status, 'Cancelled');
});

test('Profile update, password change, and user settings persistence', () => {
  const reg = registerUser({
    fullName: 'Riya Verma',
    email: 'riya@example.com',
    password: 'Safety@2026',
    confirmPassword: 'Safety@2026',
  });
  const userId = reg.data!.user.id;

  const prof = updateProfile(userId, { fullName: 'Riya K. Verma', phone: '+91 91234 56789' });
  assert.equal(prof.ok, true);
  assert.equal(prof.data?.fullName, 'Riya K. Verma');

  const settings = updateSettings(userId, {
    confirmBeforeSos: false,
    simulatedLat: 28.6139,
    simulatedLng: 77.209,
    simulatedAddress: 'Connaught Place, New Delhi',
  });
  assert.equal(settings.ok, true);
  assert.equal(settings.data?.confirmBeforeSos, false);
  assert.equal(settings.data?.simulatedAddress, 'Connaught Place, New Delhi');

  const pwChange = changePassword(userId, {
    currentPassword: 'Safety@2026',
    newPassword: 'Updated@2026',
    confirmPassword: 'Updated@2026',
  });
  assert.equal(pwChange.ok, true);
  assert.equal(loginUser({ email: 'riya@example.com', password: 'Updated@2026' }).ok, true);
});

test('Preconfigured user is seeded once with PBKDF2 hash, rejects duplicates, and preserves changes across restarts', () => {
  const db = getDb();
  const row = db
    .prepare('SELECT id, email, password_hash, salt FROM users WHERE email = ?')
    .get('mr.unknown2217op@gmail.com');
  assert.ok(row);
  assert.equal(String(row.email), 'mr.unknown2217op@gmail.com');
  assert.ok(String(row.password_hash).length >= 32);
  assert.ok(String(row.salt).length >= 16);

  // Attempting to register the same email must return 409 rather than duplicating
  const dup = registerUser({
    fullName: 'Duplicate Check',
    email: 'mr.unknown2217op@gmail.com',
    password: 'TestDuplicatePassword',
    confirmPassword: 'TestDuplicatePassword',
  });
  assert.equal(dup.ok, false);
  assert.equal(dup.statusCode, 409);

  // Restart simulation does not create duplicates
  resetDbForTests();
  const dbAfterRestart = getDb();
  const countRow = dbAfterRestart
    .prepare('SELECT COUNT(*) as total FROM users WHERE email = ?')
    .get('mr.unknown2217op@gmail.com');
  assert.equal(Number(countRow?.total), 1);
});

test('Seeded user password change persists across database restarts without being overwritten', () => {
  process.env.WSS_SEED_EMAIL = 'seeded.check@example.com';
  process.env.WSS_SEED_PASSWORD = 'TempSeedPass!99';
  process.env.WSS_SEED_NAME = 'Seeded Account';

  const firstLogin = loginUser({
    email: 'seeded.check@example.com',
    password: 'TempSeedPass!99',
  });
  assert.equal(firstLogin.ok, true);
  const userId = firstLogin.data!.user.id;

  const changed = changePassword(userId, {
    currentPassword: 'TempSeedPass!99',
    newPassword: 'RotatedUserPass!88',
    confirmPassword: 'RotatedUserPass!88',
  });
  assert.equal(changed.ok, true);

  // Simulate app restart
  resetDbForTests();

  const oldLogin = loginUser({
    email: 'seeded.check@example.com',
    password: 'TempSeedPass!99',
  });
  assert.equal(oldLogin.ok, false);

  const newLogin = loginUser({
    email: 'seeded.check@example.com',
    password: 'RotatedUserPass!88',
  });
  assert.equal(newLogin.ok, true);
  assert.equal(newLogin.data?.user.fullName, 'Seeded Account');
});
