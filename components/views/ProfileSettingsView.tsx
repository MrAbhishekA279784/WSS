'use client';

import { useState } from 'react';
import { motion } from 'motion/react';
import {
  Info,
  Lock,
  LogOut,
  Settings,
  User as UserIcon,
} from 'lucide-react';
import TiltCard from '@/components/TiltCard';
import { triggerHaptic } from '@/lib/haptics';
import { apiFetch, formatDisplayDateTime, safeJson } from '@/lib/client-utils';
import type { WorkspaceBundle } from '@/lib/types';

interface ProfileSettingsViewProps {
  bundle: WorkspaceBundle;
  initialSection: 'profile' | 'settings';
  onRefresh: () => Promise<void>;
  onLogout: () => Promise<void>;
}

export default function ProfileSettingsView({
  bundle,
  initialSection,
  onRefresh,
  onLogout,
}: ProfileSettingsViewProps) {
  const [section, setSection] = useState<'profile' | 'settings'>(initialSection);

  // Profile form state
  const [fullName, setFullName] = useState(bundle.user.fullName);
  const [phone, setPhone] = useState(bundle.user.phone);
  const [profileMsg, setProfileMsg] = useState('');
  const [profileErr, setProfileErr] = useState('');
  const [profileBusy, setProfileBusy] = useState(false);

  // Password form state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [pwMsg, setPwMsg] = useState('');
  const [pwErr, setPwErr] = useState('');
  const [pwBusy, setPwBusy] = useState(false);

  // Settings form state
  const [confirmBeforeSos, setConfirmBeforeSos] = useState(bundle.settings.confirmBeforeSos);
  const [includeLocationInSos, setIncludeLocationInSos] = useState(
    bundle.settings.includeLocationInSos
  );
  const [simLat, setSimLat] = useState(String(bundle.settings.simulatedLat));
  const [simLng, setSimLng] = useState(String(bundle.settings.simulatedLng));
  const [simAddress, setSimAddress] = useState(bundle.settings.simulatedAddress);
  const [settingsMsg, setSettingsMsg] = useState('');
  const [settingsErr, setSettingsErr] = useState('');
  const [settingsBusy, setSettingsBusy] = useState(false);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileMsg('');
    setProfileErr('');
    setProfileBusy(true);
    try {
      const res = await apiFetch('/api/profile', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName, phone }),
      });
      const data = await safeJson(res);
      if (!res.ok) {
        triggerHaptic('error');
        setProfileErr(data.error ?? 'Could not update profile.');
        return;
      }
      triggerHaptic('success');
      await onRefresh();
      setProfileMsg('Profile saved.');
    } catch {
      triggerHaptic('error');
      setProfileErr('Network error while saving profile.');
    } finally {
      setProfileBusy(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setPwMsg('');
    setPwErr('');
    setPwBusy(true);
    try {
      const res = await apiFetch('/api/profile/password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
      });
      const data = await safeJson(res);
      if (!res.ok) {
        triggerHaptic('error');
        setPwErr(data.error ?? 'Could not change password.');
        return;
      }
      triggerHaptic('success');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setPwMsg(data.message ?? 'Password changed successfully.');
    } catch {
      triggerHaptic('error');
      setPwErr('Network error while changing password.');
    } finally {
      setPwBusy(false);
    }
  };

  const handleSaveSettings = async (e: React.FormEvent) => {
    e.preventDefault();
    setSettingsMsg('');
    setSettingsErr('');
    const lat = Number(simLat);
    const lng = Number(simLng);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      triggerHaptic('error');
      setSettingsErr('Simulated latitude and longitude must be valid numbers.');
      return;
    }
    setSettingsBusy(true);
    try {
      const res = await apiFetch('/api/settings', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          confirmBeforeSos,
          includeLocationInSos,
          simulatedLat: lat,
          simulatedLng: lng,
          simulatedAddress: simAddress,
        }),
      });
      const data = await safeJson(res);
      if (!res.ok) {
        triggerHaptic('error');
        setSettingsErr(data.error ?? 'Could not update settings.');
        return;
      }
      triggerHaptic('success');
      await onRefresh();
      setSettingsMsg('Preferences saved to SQLite.');
    } catch {
      triggerHaptic('error');
      setSettingsErr('Network error while updating settings.');
    } finally {
      setSettingsBusy(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Identity Hero Strip */}
      <TiltCard
        maxTilt={4}
        className="flex flex-col justify-between gap-5 p-6 sm:flex-row sm:items-center"
      >
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full border border-[#ECA99C]/50 bg-[#252525] text-xl font-bold text-[#F5B6A9] shadow-md">
            {bundle.user.fullName.charAt(0).toUpperCase()}
          </div>
          <div>
            <h2 className="text-xl font-bold text-[#F5F2F0]">
              {bundle.user.fullName}
            </h2>
            <p className="mt-0.5 text-sm text-[#A5A0A0]">{bundle.user.email}</p>
            <p suppressHydrationWarning className="mt-1 font-mono text-xs text-[#777272] tabular-nums">
              Member since {formatDisplayDateTime(bundle.user.createdAt)}
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="inline-flex items-center gap-1 rounded-xl border border-[#393333] bg-[#111111] p-1">
            <button
              type="button"
              onClick={() => {
                triggerHaptic('nav');
                setSection('profile');
              }}
              className={`inline-flex items-center gap-1.5 rounded-lg px-4 py-1.5 text-xs font-medium transition-colors whitespace-nowrap shrink-0 ${
                section === 'profile'
                  ? 'bg-[#F5B6A9] text-[#111111]'
                  : 'text-[#A5A0A0] hover:text-[#F5F2F0]'
              }`}
            >
              <UserIcon className="h-3.5 w-3.5" />
              Profile & Security
            </button>
            <button
              type="button"
              onClick={() => {
                triggerHaptic('nav');
                setSection('settings');
              }}
              className={`inline-flex items-center gap-1.5 rounded-lg px-4 py-1.5 text-xs font-medium transition-colors whitespace-nowrap shrink-0 ${
                section === 'settings'
                  ? 'bg-[#F5B6A9] text-[#111111]'
                  : 'text-[#A5A0A0] hover:text-[#F5F2F0]'
              }`}
            >
              <Settings className="h-3.5 w-3.5" />
              App Settings
            </button>
          </div>

          <motion.button
            type="button"
            whileTap={{ scale: 0.95 }}
            onClick={() => {
              triggerHaptic('confirm');
              void onLogout();
            }}
            className="inline-flex h-10 items-center gap-2 rounded-full border border-[#ECA99C] bg-transparent px-5 text-xs font-medium text-[#F5F2F0] transition-colors hover:bg-[#252525] whitespace-nowrap shrink-0"
          >
            <LogOut className="h-3.5 w-3.5 text-[#F5B6A9]" />
            Logout
          </motion.button>
        </div>
      </TiltCard>

      {section === 'profile' ? (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* Edit Profile Card */}
          <TiltCard maxTilt={4} className="p-6">
            <div className="flex items-center gap-2.5">
              <UserIcon className="h-4 w-4 text-[#F5B6A9]" />
              <h3 className="text-base font-semibold text-[#F5F2F0]">
                My Profile
              </h3>
            </div>
            <form onSubmit={handleSaveProfile} className="mt-5 space-y-4">
              <div>
                <label htmlFor="prof-name" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Full Name
                </label>
                <input
                  id="prof-name"
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              <div>
                <label htmlFor="prof-email" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Email Address
                </label>
                <input
                  id="prof-email"
                  type="email"
                  disabled
                  value={bundle.user.email}
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111]/60 px-3.5 text-sm text-[#777272]"
                />
              </div>

              <div>
                <label htmlFor="prof-phone" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Phone Number
                </label>
                <input
                  id="prof-phone"
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+91 98765 43210"
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 font-mono text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              {profileErr ? (
                <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3 py-2 text-xs text-[#F5B6A9]">
                  {profileErr}
                </p>
              ) : null}
              {profileMsg ? (
                <p className="rounded-xl border border-[#8DBD87]/40 bg-[#8DBD87]/10 px-3 py-2 text-xs text-[#8DBD87]">
                  {profileMsg}
                </p>
              ) : null}

              <motion.button
                type="submit"
                whileTap={{ scale: 0.96 }}
                disabled={profileBusy}
                className="h-11 rounded-full bg-[#F5B6A9] px-6 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0 transition-colors shadow-sm"
              >
                {profileBusy ? 'Saving...' : 'Save Profile'}
              </motion.button>
            </form>
          </TiltCard>

          {/* Change Password Card */}
          <TiltCard maxTilt={4} className="p-6">
            <div className="flex items-center gap-2.5">
              <Lock className="h-4 w-4 text-[#F5B6A9]" />
              <h3 className="text-base font-semibold text-[#F5F2F0]">
                Change Password
              </h3>
            </div>
            <form onSubmit={handleChangePassword} className="mt-5 space-y-4">
              <div>
                <label htmlFor="pw-cur" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Current Password
                </label>
                <input
                  id="pw-cur"
                  type="password"
                  required
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              <div>
                <label htmlFor="pw-new" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  New Password (min 6 characters)
                </label>
                <input
                  id="pw-new"
                  type="password"
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              <div>
                <label htmlFor="pw-conf" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Confirm New Password
                </label>
                <input
                  id="pw-conf"
                  type="password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              {pwErr ? (
                <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3 py-2 text-xs text-[#F5B6A9]">
                  {pwErr}
                </p>
              ) : null}
              {pwMsg ? (
                <p className="rounded-xl border border-[#8DBD87]/40 bg-[#8DBD87]/10 px-3 py-2 text-xs text-[#8DBD87]">
                  {pwMsg}
                </p>
              ) : null}

              <motion.button
                type="submit"
                whileTap={{ scale: 0.96 }}
                disabled={pwBusy}
                className="h-11 rounded-full bg-[#F5B6A9] px-6 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0 transition-colors shadow-sm"
              >
                {pwBusy ? 'Updating...' : 'Update Password'}
              </motion.button>
            </form>
          </TiltCard>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* App Settings Form */}
          <TiltCard maxTilt={4} className="p-6">
            <div className="flex items-center gap-2.5">
              <Settings className="h-4 w-4 text-[#F5B6A9]" />
              <h3 className="text-base font-semibold text-[#F5F2F0]">
                Safety & SOS Preferences
              </h3>
            </div>

            <form onSubmit={handleSaveSettings} className="mt-5 space-y-4">
              <label className="flex cursor-pointer items-center justify-between gap-4 rounded-xl border border-[#393333] bg-[#111111] p-4">
                <div>
                  <p className="text-sm font-medium text-[#F5F2F0]">
                    Confirm before triggering SOS
                  </p>
                  <p className="mt-0.5 text-xs text-[#A5A0A0]">
                    Shows a confirmation prompt before saving an active SOS alert.
                  </p>
                </div>
                <input
                  type="checkbox"
                  checked={confirmBeforeSos}
                  onChange={(e) => setConfirmBeforeSos(e.target.checked)}
                  className="h-5 w-5 accent-[#F5B6A9]"
                />
              </label>

              <label className="flex cursor-pointer items-center justify-between gap-4 rounded-xl border border-[#393333] bg-[#111111] p-4">
                <div>
                  <p className="text-sm font-medium text-[#F5F2F0]">
                    Save location snapshot on SOS
                  </p>
                  <p className="mt-0.5 text-xs text-[#A5A0A0]">
                    Logs the active coordinates to your location history when SOS runs.
                  </p>
                </div>
                <input
                  type="checkbox"
                  checked={includeLocationInSos}
                  onChange={(e) => setIncludeLocationInSos(e.target.checked)}
                  className="h-5 w-5 accent-[#F5B6A9]"
                />
              </label>

              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div>
                  <label htmlFor="set-lat" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                    Default Simulated Latitude
                  </label>
                  <input
                    id="set-lat"
                    type="text"
                    value={simLat}
                    onChange={(e) => setSimLat(e.target.value)}
                    className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 font-mono text-sm text-[#F5F2F0] tabular-nums focus:border-[#ECA99C] focus:outline-none"
                  />
                </div>
                <div>
                  <label htmlFor="set-lng" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                    Default Simulated Longitude
                  </label>
                  <input
                    id="set-lng"
                    type="text"
                    value={simLng}
                    onChange={(e) => setSimLng(e.target.value)}
                    className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 font-mono text-sm text-[#F5F2F0] tabular-nums focus:border-[#ECA99C] focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="set-addr" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Default Simulated Location Label
                </label>
                <input
                  id="set-addr"
                  type="text"
                  value={simAddress}
                  onChange={(e) => setSimAddress(e.target.value)}
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              {settingsErr ? (
                <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3 py-2 text-xs text-[#F5B6A9]">
                  {settingsErr}
                </p>
              ) : null}
              {settingsMsg ? (
                <p className="rounded-xl border border-[#8DBD87]/40 bg-[#8DBD87]/10 px-3 py-2 text-xs text-[#8DBD87]">
                  {settingsMsg}
                </p>
              ) : null}

              <motion.button
                type="submit"
                whileTap={{ scale: 0.96 }}
                disabled={settingsBusy}
                className="h-11 rounded-full bg-[#F5B6A9] px-6 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0 transition-colors shadow-sm"
              >
                {settingsBusy ? 'Saving...' : 'Save Settings'}
              </motion.button>
            </form>
          </TiltCard>

          {/* About Card */}
          <TiltCard maxTilt={4} className="p-6">
            <div className="flex items-center gap-2.5">
              <Info className="h-4 w-4 text-[#F5B6A9]" />
              <h3 className="text-base font-semibold text-[#F5F2F0]">
                About Women Safety System
              </h3>
            </div>
            <div className="mt-4 space-y-3 text-sm leading-relaxed text-[#A5A0A0]">
              <p>
                Women Safety System is a full-screen emergency safety application backed by persistent SQLite storage and PBKDF2-HMAC-SHA256 credential security.
              </p>
              <p>
                Location sharing uses your browser&apos;s Geolocation API when permitted, or falls back to your explicitly labelled simulated preset coordinates and generates shareable Google Maps links.
              </p>
              <p>
                Automated external SMS or email webhook alerts run when <code className="rounded bg-[#111111] px-1.5 py-0.5 font-mono text-xs text-[#F5B6A9]">WSS_SMS_WEBHOOK_URL</code> is configured on the server; otherwise, direct manual SMS and call actions remain available for every trusted contact.
              </p>
            </div>
          </TiltCard>
        </div>
      )}
    </div>
  );
}
