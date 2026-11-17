'use client';

import { motion } from 'motion/react';
import {
  ChevronRight,
  Clock,
  MapPin,
  Phone,
  PhoneCall,
  Users,
} from 'lucide-react';
import TiltCard from '@/components/TiltCard';
import { triggerHaptic } from '@/lib/haptics';
import { formatClientCoordinates, formatDisplayDateTime } from '@/lib/client-utils';
import type { AppView, WorkspaceBundle } from '@/lib/types';

interface DashboardViewProps {
  bundle: WorkspaceBundle;
  onTriggerSOS: () => void;
  onNavigate: (view: AppView) => void;
}

export default function DashboardView({
  bundle,
  onTriggerSOS,
  onNavigate,
}: DashboardViewProps) {
  const firstName = bundle.user.fullName.trim().split(/\s+/)[0] || 'User';
  const topContacts = bundle.contacts.slice(0, 3);
  const recentSOS = bundle.sosHistory.slice(0, 3);

  const featureCards: Array<{
    view: AppView;
    title: string;
    subtitle: string;
    icon: typeof MapPin;
  }> = [
    {
      view: 'location',
      title: 'Live Location',
      subtitle: 'Coordinates & Google Maps link',
      icon: MapPin,
    },
    {
      view: 'contacts',
      title: 'Contacts',
      subtitle: `${bundle.contacts.length} trusted contact${bundle.contacts.length === 1 ? '' : 's'} saved`,
      icon: Users,
    },
    {
      view: 'history',
      title: 'SOS History',
      subtitle: `${bundle.sosHistory.length} recorded SOS alert${bundle.sosHistory.length === 1 ? '' : 's'}`,
      icon: Clock,
    },
    {
      view: 'support',
      title: 'Emergency Support',
      subtitle: 'Helplines 100 · 181 · 108 · 112',
      icon: PhoneCall,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Greeting Header */}
      <motion.div
        initial={{ opacity: 0, y: -6 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.25 }}
        className="flex items-center justify-between"
      >
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-[#F5F2F0] sm:text-3xl">
            Hello, {firstName}
          </h1>
          <p className="mt-1 text-sm text-[#A5A0A0]">Stay Safe, Stay Connected</p>
        </div>
      </motion.div>

      {/* Central Focal SOS Control with Breathing Glow and 3D Depth */}
      <TiltCard
        maxTilt={2}
        className="rounded-3xl border border-[#393333] bg-[#111111] px-6 py-10 shadow-[0_12px_40px_-15px_rgba(0,0,0,0.8)] sm:py-14"
      >
        <div className="flex flex-col items-center justify-center">
          <div className="relative flex items-center justify-center">
            {/* Ambient Breathing Peach Glow */}
            <div
              aria-hidden="true"
              className="pointer-events-none absolute h-64 w-64 rounded-full bg-radial from-[#F5B6A9]/30 via-[#ECA99C]/12 to-transparent blur-3xl sm:h-72 sm:w-72 animate-sos-glow"
            />

            {/* Subtle Rotating Pulse Accent Ring */}
            <div
              aria-hidden="true"
              className="pointer-events-none absolute h-56 w-56 rounded-full border border-dashed border-[#ECA99C]/20 sm:h-64 sm:w-64 animate-[spin_30s_linear_infinite]"
            />

            {/* Circular SOS Button with Beveled Depth */}
            <motion.button
              type="button"
              suppressHydrationWarning
              onClick={() => {
                triggerHaptic('sos_activate');
                onTriggerSOS();
              }}
              aria-label="Tap to Send SOS"
              whileHover={{ scale: 1.03, y: -2 }}
              whileTap={{ scale: 0.94 }}
              className="group relative flex h-48 w-48 items-center justify-center rounded-full border-[3.5px] border-[#F5B6A9] bg-gradient-to-b from-[#2B1F1E] via-[#1B1514] to-[#0E0C0C] text-[#F5F2F0] focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-[#F5B6A9] sm:h-56 sm:w-56 animate-sos-pulse"
            >
              <div className="flex flex-col items-center">
                <span className="text-4xl font-extrabold tracking-wider text-[#F5F2F0] drop-shadow-[0_2px_12px_rgba(245,182,169,0.55)] sm:text-5xl">
                  SOS
                </span>
              </div>
            </motion.button>
          </div>

          <p className="mt-6 text-sm font-semibold tracking-wide text-[#F5F2F0]">
            Tap to Send SOS
          </p>
          <p className="mt-1 text-xs text-[#A5A0A0]">
            {bundle.settings.confirmBeforeSos
              ? 'Asks for confirmation before activating alert'
              : 'Immediate SOS activation enabled in Settings'}
          </p>
        </div>
      </TiltCard>

      {/* 4 Feature Action Cards with 3D Tilt */}
      <div>
        <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-[#A5A0A0]">
          Quick Features
        </h2>
        <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-2">
          {featureCards.map((card, idx) => {
            const Icon = card.icon;
            return (
              <motion.div
                key={card.view}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.25, delay: idx * 0.04 }}
              >
                <TiltCard
                  maxTilt={3}
                  onClick={() => {
                    triggerHaptic('nav');
                    onNavigate(card.view);
                  }}
                  className="group flex cursor-pointer items-center justify-between rounded-2xl border border-[#393333] bg-[#1C1C1C] p-4.5 transition-colors hover:border-[#ECA99C]/60 hover:bg-[#252525]"
                >
                  <div className="flex min-w-0 items-center gap-3.5">
                    <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-[#393333] bg-[#111111] text-[#F5B6A9] transition-transform duration-200 group-hover:scale-105 group-hover:border-[#ECA99C]/40">
                      <Icon className="h-5 w-5" />
                    </div>
                    <div className="min-w-0">
                      <p className="truncate text-sm font-semibold text-[#F5F2F0]">
                        {card.title}
                      </p>
                      <p className="mt-0.5 truncate text-xs text-[#A5A0A0]">
                        {card.subtitle}
                      </p>
                    </div>
                  </div>
                  <ChevronRight className="h-4 w-4 shrink-0 text-[#A5A0A0] transition-transform group-hover:translate-x-0.5 group-hover:text-[#F5B6A9]" />
                </TiltCard>
              </motion.div>
            );
          })}
        </div>
      </div>

      {/* Two-Column Responsive Cards: Contacts & Location/History */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {/* Emergency Contacts Summary */}
        <TiltCard
          maxTilt={2}
          className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-5 shadow-[0_6px_25px_-10px_rgba(0,0,0,0.6)]"
        >
          <div className="flex items-center justify-between gap-2">
            <h2 className="text-sm font-semibold text-[#F5F2F0]">
              Emergency Contacts
            </h2>
            <button
              type="button"
              onClick={() => {
                triggerHaptic('nav');
                onNavigate('contacts');
              }}
              className="text-xs font-medium text-[#F5B6A9] transition-colors hover:text-[#ECA99C] whitespace-nowrap shrink-0"
            >
              Manage
            </button>
          </div>

          {topContacts.length === 0 ? (
            <div className="mt-3 border-t border-[#393333] pt-3.5">
              <p className="text-xs leading-relaxed text-[#A5A0A0]">
                No trusted contacts saved yet. Add family or friends to notify during SOS.
              </p>
              <button
                type="button"
                onClick={() => {
                  triggerHaptic('nav');
                  onNavigate('contacts');
                }}
                className="mt-3 rounded-full bg-[#F5B6A9] px-4 py-1.5 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] whitespace-nowrap shrink-0"
              >
                Add Contact
              </button>
            </div>
          ) : (
            <div className="mt-3 divide-y divide-[#393333]">
              {topContacts.map((c) => (
                <div key={c.id} className="flex items-center justify-between gap-3 py-2.5 first:pt-1 last:pb-0">
                  <div className="flex min-w-0 items-center gap-3">
                    <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[#252525] text-xs font-semibold text-[#F5F2F0]">
                      {c.name.charAt(0).toUpperCase()}
                    </div>
                    <div className="min-w-0">
                      <p className="truncate text-xs font-semibold text-[#F5F2F0]">
                        {c.name}
                      </p>
                      <p className="truncate font-mono text-[11px] text-[#A5A0A0] tabular-nums">
                        {c.phone}
                      </p>
                    </div>
                  </div>
                  <motion.a
                    whileTap={{ scale: 0.92 }}
                    href={`tel:${c.phone}`}
                    onClick={() => triggerHaptic('confirm')}
                    aria-label={`Call ${c.name}`}
                    className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-[#393333] bg-[#111111] text-[#F5B6A9] transition-colors hover:border-[#ECA99C]"
                  >
                    <Phone className="h-3.5 w-3.5" />
                  </motion.a>
                </div>
              ))}
            </div>
          )}
        </TiltCard>

        {/* Location Status & Recent Alert */}
        <div className="space-y-4">
          <TiltCard
            maxTilt={2}
            className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-5 shadow-[0_6px_25px_-10px_rgba(0,0,0,0.6)]"
          >
            <div className="flex items-center justify-between gap-2">
              <h2 className="text-sm font-semibold text-[#F5F2F0]">Location Status</h2>
              <button
                type="button"
                onClick={() => {
                  triggerHaptic('nav');
                  onNavigate('location');
                }}
                className="text-xs font-medium text-[#F5B6A9] transition-colors hover:text-[#ECA99C] whitespace-nowrap shrink-0"
              >
                Open Map
              </button>
            </div>
            <div className="mt-3 border-t border-[#393333] pt-3.5">
              <div className="flex items-start gap-3">
                <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-[#F5B6A9]" />
                <div className="min-w-0">
                  <p className="font-mono text-xs font-semibold text-[#F5F2F0] tabular-nums">
                    {formatClientCoordinates(
                      bundle.currentLocation.latitude,
                      bundle.currentLocation.longitude
                    )}
                  </p>
                  <p className="mt-0.5 truncate text-xs text-[#A5A0A0]">
                    {bundle.currentLocation.address}
                  </p>
                  <p className="mt-1 text-[11px] text-[#777272]">
                    Mode · {bundle.currentLocation.locationType}
                  </p>
                </div>
              </div>
            </div>
          </TiltCard>

          <TiltCard
            maxTilt={2}
            className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-5 shadow-[0_6px_25px_-10px_rgba(0,0,0,0.6)]"
          >
            <div className="flex items-center justify-between gap-2">
              <h2 className="text-sm font-semibold text-[#F5F2F0]">Recent Activity</h2>
              <button
                type="button"
                onClick={() => {
                  triggerHaptic('nav');
                  onNavigate('history');
                }}
                className="text-xs font-medium text-[#F5B6A9] transition-colors hover:text-[#ECA99C] whitespace-nowrap shrink-0"
              >
                View History
              </button>
            </div>
            {recentSOS.length === 0 ? (
              <p className="mt-3 text-xs text-[#A5A0A0]">
                No SOS alerts recorded yet.
              </p>
            ) : (
              <div className="mt-3 divide-y divide-[#393333]">
                {recentSOS.slice(0, 2).map((item) => {
                  const isCancelled = item.status === 'Cancelled';
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => {
                        triggerHaptic('nav');
                        onNavigate('history');
                      }}
                      className="flex w-full items-center justify-between gap-3 py-2 text-left first:pt-1 last:pb-0 hover:opacity-85"
                    >
                      <div className="min-w-0">
                        <span
                          className={`text-xs font-semibold ${
                            isCancelled ? 'text-[#8DBD87]' : 'text-[#D96B70]'
                          }`}
                        >
                          {isCancelled ? 'SOS Cancelled' : 'SOS Alert'}
                        </span>
                        <span
                          suppressHydrationWarning
                          className="ml-2 font-mono text-[11px] text-[#777272] tabular-nums"
                        >
                          {formatDisplayDateTime(item.createdAt)}
                        </span>
                      </div>
                      <ChevronRight className="h-3.5 w-3.5 shrink-0 text-[#777272]" />
                    </button>
                  );
                })}
              </div>
            )}
          </TiltCard>
        </div>
      </div>
    </div>
  );
}
