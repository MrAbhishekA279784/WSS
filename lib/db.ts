import fs from 'node:fs';
import os from 'node:os';
import { DatabaseSync } from 'node:sqlite';
import path from 'node:path';
import { hashPassword, normalizeEmail } from './security';
import { DEFAULT_SIMULATED_LOCATION } from './types';

const PRECONFIGURED_USER_EMAIL = 'mr.unknown2217op@gmail.com';
const PRECONFIGURED_USER_NAME = 'Abhishek Gupta';
// PBKDF2-HMAC-SHA256 (65,536 iterations, 32-byte key) derived hash + salt; no plaintext password stored in source
const PRECONFIGURED_PASSWORD_SALT = 'DD7q59D1pi1geQPC48X71Q==';
const PRECONFIGURED_PASSWORD_HASH = 'dfL3LfAnu06vZ9znD6Gu+2alXcvf6oWbTP7Koe+gYUA=';

const globalForDb = globalThis as unknown as {
  __wssSqliteDb?: DatabaseSync;
  __wssSqlitePath?: string;
};

function isPathWritable(targetFile: string): boolean {
  if (targetFile === ':memory:') {
    return true;
  }
  try {
    const dir = path.dirname(targetFile);
    fs.accessSync(dir, fs.constants.W_OK);
    if (fs.existsSync(targetFile)) {
      fs.accessSync(targetFile, fs.constants.W_OK);
    }
    return true;
  } catch {
    return false;
  }
}

function getCandidateDbPaths(): string[] {
  const explicit = process.env.WSS_DB_PATH?.trim();
  if (explicit) {
    return [explicit];
  }

  const cwdDb = path.join(process.cwd(), 'women_safety.db');
  const tmpDb = path.join(os.tmpdir(), 'women_safety.db');
  const isServerless = Boolean(
    process.env.VERCEL ||
      process.env.VERCEL_ENV ||
      process.env.AWS_LAMBDA_FUNCTION_NAME ||
      process.env.LAMBDA_TASK_ROOT
  );

  const candidates: string[] = [];
  if (isServerless) {
    candidates.push(tmpDb);
    candidates.push(':memory:');
  } else {
    if (isPathWritable(cwdDb)) {
      candidates.push(cwdDb);
    }
    candidates.push(tmpDb);
    candidates.push(':memory:');
  }
  return candidates;
}

function resolveDbPath(): string {
  const candidates = getCandidateDbPaths();
  return candidates[0];
}

function seedInitialUser(db: DatabaseSync): void {
  const alreadySeeded = db
    .prepare("SELECT value FROM app_meta WHERE key = 'initial_user_seeded'")
    .get();
  if (alreadySeeded) {
    return;
  }

  const envEmail = process.env.WSS_SEED_EMAIL?.trim();
  const envPassword = process.env.WSS_SEED_PASSWORD;
  const envName = process.env.WSS_SEED_NAME?.trim();

  const useEnvSeed = Boolean(envEmail && envPassword && envPassword.length >= 6);
  const targetEmail = normalizeEmail(useEnvSeed ? envEmail! : PRECONFIGURED_USER_EMAIL);
  const targetName = (useEnvSeed && envName ? envName : PRECONFIGURED_USER_NAME).trim();

  const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(targetEmail);
  if (!existing) {
    const creds = useEnvSeed
      ? hashPassword(envPassword!)
      : { hash: PRECONFIGURED_PASSWORD_HASH, salt: PRECONFIGURED_PASSWORD_SALT };

    const res = db
      .prepare(
        'INSERT INTO users (full_name, email, phone, password_hash, salt) VALUES (?, ?, ?, ?, ?)'
      )
      .run(targetName, targetEmail, '', creds.hash, creds.salt);

    const userId = Number(res.lastInsertRowid);
    db.prepare(
      `INSERT OR IGNORE INTO user_settings (user_id, confirm_before_sos, include_location_in_sos, simulated_lat, simulated_lng, simulated_address)
       VALUES (?, 1, 1, ?, ?, ?)`
    ).run(
      userId,
      DEFAULT_SIMULATED_LOCATION.latitude,
      DEFAULT_SIMULATED_LOCATION.longitude,
      DEFAULT_SIMULATED_LOCATION.address
    );
  }

  db.prepare(
    "INSERT OR IGNORE INTO app_meta (key, value) VALUES ('initial_user_seeded', '1')"
  ).run();
}

function initializeSchema(db: DatabaseSync): void {
  try {
    db.exec('PRAGMA journal_mode = WAL;');
  } catch {
    // Fallback gracefully to default journal mode on filesystems without WAL support
  }

  db.exec(`
    PRAGMA foreign_keys = ON;
    PRAGMA busy_timeout = 5000;

    CREATE TABLE IF NOT EXISTS app_meta (
      key TEXT PRIMARY KEY,
      value TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      full_name TEXT NOT NULL,
      email TEXT NOT NULL UNIQUE,
      phone TEXT NOT NULL DEFAULT '',
      password_hash TEXT NOT NULL,
      salt TEXT NOT NULL,
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      updated_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS sessions (
      token TEXT PRIMARY KEY,
      user_id INTEGER NOT NULL,
      expires_at TEXT NOT NULL,
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS emergency_contacts (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      name TEXT NOT NULL,
      phone TEXT NOT NULL,
      relationship TEXT NOT NULL DEFAULT '',
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS sos_history (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      status TEXT NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      location_label TEXT NOT NULL DEFAULT '',
      location_type TEXT NOT NULL DEFAULT 'SIMULATED',
      delivery_status TEXT NOT NULL DEFAULT 'NOT_CONFIGURED',
      delivery_note TEXT NOT NULL DEFAULT '',
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      cancelled_at TEXT,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS location_history (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      address TEXT NOT NULL DEFAULT '',
      location_type TEXT NOT NULL DEFAULT 'SIMULATED',
      created_at TEXT NOT NULL DEFAULT (datetime('now')),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS user_settings (
      user_id INTEGER PRIMARY KEY,
      confirm_before_sos INTEGER NOT NULL DEFAULT 1,
      include_location_in_sos INTEGER NOT NULL DEFAULT 1,
      simulated_lat REAL NOT NULL DEFAULT 19.2638,
      simulated_lng REAL NOT NULL DEFAULT 72.9629,
      simulated_address TEXT NOT NULL DEFAULT 'Kandivali (E), Mumbai (Simulated Preset)',
      updated_at TEXT NOT NULL DEFAULT (datetime('now')),
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
  `);

  seedInitialUser(db);
}

function openAndInitDb(dbPath: string): DatabaseSync {
  if (dbPath !== ':memory:') {
    const dir = path.dirname(dbPath);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
  }
  const db = new DatabaseSync(dbPath);
  try {
    initializeSchema(db);
    return db;
  } catch (err) {
    try {
      db.close();
    } catch {
      // ignore close error
    }
    throw err;
  }
}

export function getDb(): DatabaseSync {
  const preferredPath = resolveDbPath();
  const cachedPath = globalForDb.__wssSqlitePath;
  const cachedExists = cachedPath === ':memory:' || (cachedPath ? fs.existsSync(cachedPath) : false);

  if (
    globalForDb.__wssSqliteDb &&
    cachedExists &&
    (!process.env.WSS_DB_PATH?.trim() || cachedPath === preferredPath)
  ) {
    return globalForDb.__wssSqliteDb;
  }

  if (globalForDb.__wssSqliteDb) {
    try {
      globalForDb.__wssSqliteDb.close();
    } catch {
      // ignore close error on path switch
    }
    globalForDb.__wssSqliteDb = undefined;
    globalForDb.__wssSqlitePath = undefined;
  }

  const candidates = getCandidateDbPaths();
  let lastError: unknown;
  for (const candidate of candidates) {
    try {
      const db = openAndInitDb(candidate);
      globalForDb.__wssSqliteDb = db;
      globalForDb.__wssSqlitePath = candidate;
      return db;
    } catch (err) {
      lastError = err;
    }
  }

  throw lastError instanceof Error
    ? lastError
    : new Error('Unable to initialize SQLite database.');
}

export function resetDbForTests(): void {
  if (globalForDb.__wssSqliteDb) {
    try {
      globalForDb.__wssSqliteDb.close();
    } catch {
      // ignore
    }
  }
  globalForDb.__wssSqliteDb = undefined;
  globalForDb.__wssSqlitePath = undefined;
}

