import { SESSION_COOKIE_NAME, sessionCookieOptions } from '@/lib/auth';
import { getDb } from '@/lib/db';
import { generateSessionToken, hashPassword } from '@/lib/security';
import { getWorkspaceBundle } from '@/lib/services';
import type { PublicUser, WorkspaceBundle } from '@/lib/types';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const email = String(body?.email ?? '').trim().toLowerCase();
    const fullName = String(body?.fullName ?? '').trim() || email.split('@')[0] || 'Google User';
    const phone = String(body?.phone ?? '').trim();

    if (!email || !email.includes('@')) {
      return NextResponse.json({ error: 'Valid Google email is required.' }, { status: 400 });
    }

    const db = getDb();
    let row = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
    let userId: number;

    if (!row) {
      const dummyPassword = generateSessionToken();
      const { hash, salt } = hashPassword(dummyPassword);
      const res = db
        .prepare(
          'INSERT INTO users (full_name, email, phone, password_hash, salt) VALUES (?, ?, ?, ?, ?)'
        )
        .run(fullName, email, phone, hash, salt);
      userId = Number(res.lastInsertRowid);

      db.prepare(
        `INSERT OR IGNORE INTO user_settings (user_id, confirm_before_sos, include_location_in_sos, simulated_lat, simulated_lng, simulated_address)
         VALUES (?, 1, 1, 19.2638, 72.9629, 'Kandivali (E), Mumbai (Simulated Preset)')`
      ).run(userId);
    } else {
      userId = Number(row.id);
    }

    const token = generateSessionToken();
    const expiresAt = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString();
    db.prepare('INSERT INTO sessions (token, user_id, expires_at) VALUES (?, ?, ?)').run(
      token,
      userId,
      expiresAt
    );

    const userRow = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
    const user: PublicUser = {
      id: Number(userRow!.id),
      fullName: String(userRow!.full_name),
      email: String(userRow!.email),
      phone: String(userRow!.phone ?? ''),
      createdAt: String(userRow!.created_at),
    };

    const bundleRes = getWorkspaceBundle(userId);
    const response = NextResponse.json(
      {
        user,
        token,
        bundle: bundleRes.ok && bundleRes.data ? bundleRes.data : null,
      },
      { status: 200 }
    );
    response.cookies.set(SESSION_COOKIE_NAME, token, sessionCookieOptions(req));
    return response;
  } catch (err) {
    console.error('[api/auth/firebase] Unexpected error:', err);
    return NextResponse.json(
      { error: 'Google sign-in failed due to a server error.' },
      { status: 500 }
    );
  }
}
