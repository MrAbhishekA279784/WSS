'use client';

import { useCallback, useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import {
  Bell,
  ChevronLeft,
  Clock,
  Home,
  LogOut,
  MapPin,
  PhoneCall,
  Siren,
  User as UserIcon,
  Users,
  X,
} from 'lucide-react';
import BrandMark from './BrandMark';
import SplashAndAuth from './SplashAndAuth';
import DashboardView from './views/DashboardView';
import SOSActivationView from './views/SOSActivationView';
import LiveLocationView from './views/LiveLocationView';
import TrustedContactsView from './views/TrustedContactsView';
import SOSHistoryView from './views/SOSHistoryView';
import EmergencySupportView from './views/EmergencySupportView';
import ProfileSettingsView from './views/ProfileSettingsView';
import { triggerHaptic } from '@/lib/haptics';
import {
  apiFetch,
  clearSessionToken,
  formatDisplayDateTime,
  getStoredSessionToken,
  safeJson,
} from '@/lib/client-utils';
import type { AppView, WorkspaceBundle } from '@/lib/types';

interface AppShellProps {
  initialBundle: WorkspaceBundle | null;
  skipSplash: boolean;
}

// 5 Fixed Bottom Navigation Items in Exact Required Order
const BOTTOM_NAV_ITEMS: Array<{
  id: 'home' | 'location' | 'contacts' | 'history' | 'profile';
  label: string;
  icon: typeof Home;
}> = [
  { id: 'home', label: 'Home', icon: Home },
  { id: 'location', label: 'Location', icon: MapPin },
  { id: 'contacts', label: 'Contacts', icon: Users },
  { id: 'history', label: 'History', icon: Clock },
  { id: 'profile', label: 'Profile', icon: UserIcon },
];

const VIEW_TITLES: Record<AppView, string> = {
  home: 'Home Dashboard',
  location: 'Live Location',
  contacts: 'Trusted Contacts',
  history: 'SOS History',
  support: 'Emergency Support',
  profile: 'Profile',
  settings: 'Settings',
  sos: 'Sending SOS',
};

export default function AppShell({ initialBundle, skipSplash }: AppShellProps) {
  const [bundle, setBundle] = useState<WorkspaceBundle | null>(initialBundle);
  const [activeView, setActiveView] = useState<AppView>('home');
  const [confirmSosModalOpen, setConfirmSosModalOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  const refreshBundle = useCallback(async () => {
    const res = await apiFetch('/api/me', { cache: 'no-store' });
    if (res.status === 401) {
      clearSessionToken();
      setBundle(null);
      return;
    }
    if (res.ok) {
      const data: WorkspaceBundle = (await safeJson(res)) as WorkspaceBundle;
      setBundle(data);
    }
  }, []);

  useEffect(() => {
    if (initialBundle || !getStoredSessionToken()) {
      return;
    }
    let active = true;
    apiFetch('/api/me', { cache: 'no-store' })
      .then(async (res) => {
        if (!active) return;
        if (res.status === 401) {
          clearSessionToken();
          setBundle(null);
          return;
        }
        if (res.ok) {
          const data: WorkspaceBundle = (await safeJson(res)) as WorkspaceBundle;
          if (active) {
            setBundle(data);
          }
        }
      })
      .catch(() => {
        // ignore transient network errors on mount
      });
    return () => {
      active = false;
    };
  }, [initialBundle]);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setConfirmSosModalOpen(false);
        setNotificationsOpen(false);
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, []);

  const handleLogout = async () => {
    await apiFetch('/api/auth/logout', { method: 'POST' });
    clearSessionToken();
    setBundle(null);
    setActiveView('home');
  };

  const handleStartSOS = () => {
    if (!bundle) return;
    triggerHaptic('sos_activate');
    if (bundle.settings.confirmBeforeSos) {
      setConfirmSosModalOpen(true);
    } else {
      setActiveView('sos');
    }
  };

  const navigateTo = (view: AppView) => {
    triggerHaptic('nav');
    setActiveView(view);
  };

  if (!bundle) {
    return (
      <SplashAndAuth
        skipSplash={skipSplash}
        onAuthenticated={async (authenticatedBundle) => {
          if (authenticatedBundle) {
            setBundle(authenticatedBundle);
          } else {
            await refreshBundle();
          }
          setActiveView('home');
        }}
      />
    );
  }

  const activeAlertsCount = bundle.sosHistory.filter((s) => s.status === 'Active').length;
  const isDrilldownView = activeView === 'support' || activeView === 'settings' || activeView === 'sos';

  return (
    <div className="flex min-h-screen w-full flex-col bg-[#080808] text-[#F5F2F0]">
      {/* Top Application Header (No sidebar, no top navigation tabs) */}
      <header className="sticky top-0 z-30 flex h-16 w-full shrink-0 items-center justify-between border-b border-[#393333] bg-[#080808]/95 px-4 backdrop-blur-md sm:px-6 lg:px-8">
        <div className="flex min-w-0 items-center gap-3">
          {isDrilldownView ? (
            <motion.button
              whileTap={{ scale: 0.94 }}
              type="button"
              onClick={() => navigateTo(activeView === 'settings' ? 'profile' : 'home')}
              className="flex h-9 w-9 items-center justify-center rounded-full border border-[#393333] bg-[#1C1C1C] text-[#F5F2F0] hover:border-[#ECA99C]"
              aria-label="Back"
            >
              <ChevronLeft className="h-5 w-5" />
            </motion.button>
          ) : (
            <button
              type="button"
              onClick={() => navigateTo('home')}
              className="flex items-center gap-2.5"
            >
              <BrandMark size="sm" />
              <span className="text-base font-bold tracking-tight text-[#F5F2F0]">
                Women Safety
              </span>
            </button>
          )}

          <span className="hidden text-xs text-[#777272] sm:inline">·</span>
          <span className="truncate text-xs font-medium text-[#A5A0A0] sm:text-sm">
            {VIEW_TITLES[activeView]}
          </span>
        </div>

        <div className="flex shrink-0 items-center gap-2.5 sm:gap-3">
          {/* Notification Bell with Animated Badge */}
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.94 }}
            type="button"
            aria-label="Open safety notifications"
            onClick={() => {
              triggerHaptic('confirm');
              setNotificationsOpen(true);
            }}
            className="relative flex h-10 w-10 items-center justify-center rounded-full border border-[#393333] bg-[#1C1C1C] text-[#F5F2F0] transition-colors hover:border-[#ECA99C]"
          >
            <Bell className="h-4 w-4" />
            {activeAlertsCount > 0 ? (
              <span
                suppressHydrationWarning
                className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-[#D96B70] text-[10px] font-bold text-white shadow-[0_0_8px_rgba(217,107,112,0.7)] animate-badge-pulse"
              >
                {activeAlertsCount}
              </span>
            ) : null}
          </motion.button>

          {/* Quick SOS Trigger if not already in SOS or Home view */}
          {activeView !== 'home' && activeView !== 'sos' ? (
            <motion.button
              whileHover={{ scale: 1.04 }}
              whileTap={{ scale: 0.94 }}
              type="button"
              onClick={handleStartSOS}
              className="inline-flex h-9 items-center gap-1.5 rounded-full bg-[#F5B6A9] px-3.5 text-xs font-semibold text-[#111111] transition-colors hover:bg-[#ECA99C] whitespace-nowrap shrink-0"
            >
              <Siren className="h-3.5 w-3.5" />
              Send SOS
            </motion.button>
          ) : (
            <motion.button
              whileHover={{ scale: 1.04 }}
              whileTap={{ scale: 0.94 }}
              type="button"
              onClick={() => navigateTo('support')}
              className="inline-flex h-9 items-center gap-1.5 rounded-full border border-[#393333] bg-[#1C1C1C] px-3 text-xs font-medium text-[#F5B6A9] transition-colors hover:border-[#ECA99C] whitespace-nowrap shrink-0"
            >
              <PhoneCall className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">112 Helplines</span>
            </motion.button>
          )}
        </div>
      </header>

      {/* Main Responsive Content Viewport with pb-28 bottom clearance for Fixed Bottom Bar */}
      <main className="mx-auto w-full max-w-4xl flex-1 px-4 pt-5 pb-28 sm:px-6 sm:pt-7 sm:pb-32">
        <AnimatePresence mode="wait">
          <motion.div
            key={activeView}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.2, ease: 'easeOut' }}
          >
            {activeView === 'home' ? (
              <DashboardView
                bundle={bundle}
                onTriggerSOS={handleStartSOS}
                onNavigate={navigateTo}
              />
            ) : null}
            {activeView === 'sos' ? (
              <SOSActivationView
                bundle={bundle}
                onSOSUpdated={refreshBundle}
                onDone={() => navigateTo('home')}
              />
            ) : null}
            {activeView === 'location' ? (
              <LiveLocationView bundle={bundle} onRefresh={refreshBundle} />
            ) : null}
            {activeView === 'contacts' ? (
              <TrustedContactsView bundle={bundle} onRefresh={refreshBundle} />
            ) : null}
            {activeView === 'history' ? (
              <SOSHistoryView bundle={bundle} onRefresh={refreshBundle} />
            ) : null}
            {activeView === 'support' ? <EmergencySupportView /> : null}
            {activeView === 'profile' ? (
              <ProfileSettingsView
                key="profile"
                bundle={bundle}
                initialSection="profile"
                onRefresh={refreshBundle}
                onLogout={handleLogout}
              />
            ) : null}
            {activeView === 'settings' ? (
              <ProfileSettingsView
                key="settings"
                bundle={bundle}
                initialSection="settings"
                onRefresh={refreshBundle}
                onLogout={handleLogout}
              />
            ) : null}
          </motion.div>
        </AnimatePresence>
      </main>

      {/* CRITICAL FIX: FIXED BOTTOM NAVIGATION BAR EXACTLY AS IN REFERENCE IMAGE */}
      <nav
        aria-label="Bottom Navigation"
        className="fixed bottom-0 inset-x-0 z-40 border-t border-[#393333] bg-[#111111]/95 backdrop-blur-lg shadow-[0_-10px_35px_rgba(0,0,0,0.6)]"
      >
        <div className="mx-auto flex h-18 max-w-lg items-center justify-around px-2 sm:px-4">
          {BOTTOM_NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive =
              activeView === item.id ||
              (item.id === 'profile' && activeView === 'settings');

            return (
              <button
                key={item.id}
                type="button"
                onClick={() => navigateTo(item.id)}
                className="group relative flex flex-1 flex-col items-center justify-center py-1 transition-transform active:scale-95"
              >
                {/* Icon Container with Soft Peach Circular Highlight when Active */}
                <div className="relative flex h-10 w-10 items-center justify-center">
                  {isActive && (
                    <motion.div
                      layoutId="bottomNavIndicator"
                      className="absolute inset-0 rounded-full bg-[#F5B6A9]/20 border border-[#ECA99C]/40 shadow-[0_0_12px_rgba(245,182,169,0.3)]"
                      transition={{ type: 'spring', stiffness: 450, damping: 32 }}
                    />
                  )}
                  <Icon
                    className={`relative z-10 h-5 w-5 transition-colors ${
                      isActive
                        ? 'text-[#F5B6A9]'
                        : 'text-[#777272] group-hover:text-[#A5A0A0]'
                    }`}
                  />
                </div>

                {/* Text Label Below Icon in Peach or Muted Grey */}
                <span
                  className={`mt-0.5 text-[11px] transition-colors ${
                    isActive
                      ? 'font-semibold text-[#F5B6A9]'
                      : 'font-medium text-[#777272] group-hover:text-[#A5A0A0]'
                  }`}
                >
                  {item.label}
                </span>
              </button>
            );
          })}
        </div>
      </nav>

      {/* Confirm SOS Modal Entrance Animation */}
      <AnimatePresence>
        {confirmSosModalOpen ? (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 px-4 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.94, y: 10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.94, y: 10 }}
              transition={{ duration: 0.2 }}
              role="dialog"
              aria-modal="true"
              aria-labelledby="confirm-sos-title"
              className="w-full max-w-md rounded-2xl border border-[#393333] bg-[#1C1C1C] p-6 shadow-2xl"
            >
              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-[#ECA99C]/45 bg-[#251D1B] text-[#F5B6A9]">
                  <Siren className="h-5 w-5" />
                </div>
                <div>
                  <h3 id="confirm-sos-title" className="text-lg font-semibold text-[#F5F2F0]">
                    Activate Emergency SOS?
                  </h3>
                  <p className="text-xs text-[#A5A0A0]">
                    {bundle.contacts.length} trusted contact{bundle.contacts.length === 1 ? '' : 's'} configured
                  </p>
                </div>
              </div>
              <p className="mt-4 text-sm leading-relaxed text-[#A5A0A0]">
                This will capture your active location, log an emergency SOS record to your database, and prepare alert actions for your trusted contacts.
              </p>
              <div className="mt-6 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setConfirmSosModalOpen(false)}
                  className="h-10 rounded-full border border-[#393333] bg-[#252525] px-5 text-xs font-medium text-[#F5F2F0] hover:bg-[#2F2F2F] whitespace-nowrap shrink-0"
                >
                  Cancel
                </button>
                <motion.button
                  whileTap={{ scale: 0.95 }}
                  type="button"
                  onClick={() => {
                    triggerHaptic('sos_activate');
                    setConfirmSosModalOpen(false);
                    setActiveView('sos');
                  }}
                  className="h-10 rounded-full bg-[#F5B6A9] px-6 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] whitespace-nowrap shrink-0"
                >
                  Activate SOS
                </motion.button>
              </div>
            </motion.div>
          </div>
        ) : null}
      </AnimatePresence>

      {/* Notifications Drawer/Modal */}
      <AnimatePresence>
        {notificationsOpen ? (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 px-4 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.94, y: 10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.94, y: 10 }}
              transition={{ duration: 0.2 }}
              role="dialog"
              aria-modal="true"
              aria-labelledby="notifications-title"
              className="w-full max-w-md rounded-2xl border border-[#393333] bg-[#1C1C1C] p-6 shadow-2xl"
            >
              <div className="flex items-center justify-between">
                <h3 id="notifications-title" className="text-lg font-semibold text-[#F5F2F0]">
                  Safety Activity & Status
                </h3>
                <button
                  type="button"
                  aria-label="Close notifications"
                  onClick={() => setNotificationsOpen(false)}
                  className="rounded-lg p-1.5 text-[#A5A0A0] hover:bg-[#252525] hover:text-[#F5F2F0]"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>

              <div className="mt-4 space-y-3">
                <div className="rounded-xl border border-[#393333] bg-[#111111] p-3.5 text-xs leading-relaxed text-[#A5A0A0]">
                  Trusted Contacts: {bundle.contacts.length} saved · Location Mode:{' '}
                  {bundle.currentLocation.locationType}
                </div>

                {bundle.sosHistory.length === 0 ? (
                  <p className="py-4 text-center text-xs text-[#A5A0A0]">
                    No SOS alerts recorded yet.
                  </p>
                ) : (
                  <div className="max-h-64 space-y-2 overflow-y-auto">
                    {bundle.sosHistory.slice(0, 5).map((item) => (
                      <div
                        key={item.id}
                        className="rounded-xl border border-[#393333] bg-[#111111] p-3"
                      >
                        <div className="flex items-center justify-between gap-2">
                          <span
                            className={`text-xs font-semibold ${
                              item.status === 'Cancelled' ? 'text-[#8DBD87]' : 'text-[#D96B70]'
                            }`}
                          >
                            {item.status === 'Cancelled' ? 'SOS Cancelled' : 'SOS Alert'}
                          </span>
                          <span
                            suppressHydrationWarning
                            className="font-mono text-[11px] text-[#777272] tabular-nums"
                          >
                            {formatDisplayDateTime(item.createdAt)}
                          </span>
                        </div>
                        <p className="mt-1 text-xs text-[#A5A0A0]">{item.deliveryNote}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="mt-5 flex justify-end gap-2.5">
                <button
                  type="button"
                  onClick={() => {
                    setNotificationsOpen(false);
                    navigateTo('history');
                  }}
                  className="h-10 rounded-full bg-[#F5B6A9] px-5 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] whitespace-nowrap shrink-0"
                >
                  Open SOS History
                </button>
              </div>
            </motion.div>
          </div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}
