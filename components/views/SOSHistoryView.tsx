'use client';

import { useMemo, useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import {
  CheckCircle2,
  ChevronRight,
  Clock,
  ExternalLink,
  Search,
  Siren,
  Trash2,
  X,
} from 'lucide-react';
import TiltCard from '@/components/TiltCard';
import { triggerHaptic } from '@/lib/haptics';
import {
  apiFetch,
  buildClientGoogleMapsUrl,
  formatClientCoordinates,
  formatDisplayDateTime,
  safeJson,
} from '@/lib/client-utils';
import type { SOSRecord, WorkspaceBundle } from '@/lib/types';

interface SOSHistoryViewProps {
  bundle: WorkspaceBundle;
  onRefresh: () => Promise<void>;
}

export default function SOSHistoryView({
  bundle,
  onRefresh,
}: SOSHistoryViewProps) {
  const [filter, setFilter] = useState<'All' | 'Active' | 'Cancelled'>('All');
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<SOSRecord | null>(null);
  const [actionBusy, setActionBusy] = useState(false);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return bundle.sosHistory.filter((item) => {
      if (filter !== 'All' && item.status !== filter) return false;
      if (!q) return true;
      return (
        item.locationLabel.toLowerCase().includes(q) ||
        item.status.toLowerCase().includes(q) ||
        item.deliveryNote.toLowerCase().includes(q)
      );
    });
  }, [bundle.sosHistory, filter, query]);

  const handleCancelRecord = async (sosId: number) => {
    triggerHaptic('sos_cancel');
    setActionBusy(true);
    try {
      const res = await apiFetch(`/api/sos/${sosId}`, { method: 'PATCH' });
      const data = await safeJson(res);
      if (res.ok && data.sos) {
        setSelected(data.sos);
        await onRefresh();
      }
    } finally {
      setActionBusy(false);
    }
  };

  const handleDeleteRecord = async (sosId: number) => {
    triggerHaptic('confirm');
    setActionBusy(true);
    try {
      const res = await apiFetch(`/api/sos/${sosId}`, { method: 'DELETE' });
      if (res.ok) {
        setSelected(null);
        await onRefresh();
      }
    } finally {
      setActionBusy(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Toolbar: Search + Segmented Status Filter */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div className="relative w-full sm:max-w-sm">
          <Search className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search SOS alerts by location or status..."
            aria-label="Search SOS history"
            className="h-11 w-full rounded-xl border border-[#393333] bg-[#1C1C1C] pr-4 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
          />
        </div>

        <div className="inline-flex items-center gap-1 rounded-xl border border-[#393333] bg-[#111111] p-1 self-start sm:self-auto">
          {(['All', 'Active', 'Cancelled'] as const).map((tab) => {
            const isSelected = filter === tab;
            return (
              <button
                key={tab}
                type="button"
                onClick={() => {
                  triggerHaptic('nav');
                  setFilter(tab);
                }}
                className={`relative rounded-lg px-4 py-1.5 text-xs font-medium transition-colors whitespace-nowrap shrink-0 ${
                  isSelected ? 'text-[#111111]' : 'text-[#A5A0A0] hover:text-[#F5F2F0]'
                }`}
              >
                {isSelected && (
                  <motion.div
                    layoutId="historyFilterPill"
                    className="absolute inset-0 rounded-lg bg-[#F5B6A9]"
                    transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                  />
                )}
                <span className="relative z-10">{tab}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* History List / Empty State */}
      {bundle.sosHistory.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-[#393333] bg-[#1C1C1C] px-6 py-16 text-center shadow-lg">
          <div className="flex h-14 w-14 items-center justify-center rounded-full border border-[#393333] bg-[#111111] text-[#F5B6A9]">
            <Clock className="h-6 w-6" />
          </div>
          <h2 className="mt-4 text-lg font-semibold text-[#F5F2F0]">
            No SOS Alerts Recorded
          </h2>
          <p className="mt-1.5 max-w-md text-sm text-[#A5A0A0]">
            Every SOS activation and cancellation you trigger is persisted to your SQLite history log here.
          </p>
        </div>
      ) : filtered.length === 0 ? (
        <div className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-10 text-center text-sm text-[#A5A0A0]">
          No SOS records match your current filter.
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((item, idx) => {
            const isCancelled = item.status === 'Cancelled';
            return (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.2, delay: idx * 0.03 }}
              >
                <TiltCard
                  maxTilt={2}
                  onClick={() => {
                    triggerHaptic('confirm');
                    setSelected(item);
                  }}
                  className="flex cursor-pointer items-center justify-between gap-4 rounded-2xl border border-[#393333] bg-[#1C1C1C] p-4.5 text-left transition-colors hover:border-[#ECA99C]/60 hover:bg-[#252525]"
                >
                  <div className="flex min-w-0 items-center gap-4">
                    <div
                      className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-full border ${
                        isCancelled
                          ? 'border-[#8DBD87]/40 bg-[#8DBD87]/12 text-[#8DBD87]'
                          : 'border-[#D96B70]/45 bg-[#D96B70]/15 text-[#D96B70]'
                      }`}
                    >
                      {isCancelled ? (
                        <CheckCircle2 className="h-5 w-5" />
                      ) : (
                        <Siren className="h-5 w-5" />
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="text-base font-semibold text-[#F5F2F0]">
                        {isCancelled ? 'SOS Cancelled' : 'SOS Alert'}
                      </p>
                      <p
                        suppressHydrationWarning
                        className="mt-0.5 truncate font-mono text-xs text-[#A5A0A0] tabular-nums"
                      >
                        {formatDisplayDateTime(item.createdAt)} ·{' '}
                        {formatClientCoordinates(item.latitude, item.longitude)} ·{' '}
                        {item.locationLabel}
                      </p>
                    </div>
                  </div>
                  <ChevronRight className="h-4 w-4 shrink-0 text-[#A5A0A0]" />
                </TiltCard>
              </motion.div>
            );
          })}
        </div>
      )}

      {/* SOS Details Modal with Entrance Animation */}
      <AnimatePresence>
        {selected ? (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 px-4 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.94, y: 10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.94, y: 10 }}
              transition={{ duration: 0.2 }}
              role="dialog"
              aria-modal="true"
              aria-labelledby="sos-details-title"
              className="w-full max-w-lg rounded-2xl border border-[#393333] bg-[#1C1C1C] p-6 shadow-2xl"
            >
              <div className="flex items-center justify-between">
                <h3 id="sos-details-title" className="text-lg font-semibold text-[#F5F2F0]">
                  {selected.status === 'Cancelled' ? 'SOS Cancelled' : 'SOS Alert'} #{selected.id}
                </h3>
                <button
                  type="button"
                  aria-label="Close SOS details"
                  onClick={() => setSelected(null)}
                  className="rounded-lg p-1.5 text-[#A5A0A0] hover:bg-[#252525] hover:text-[#F5F2F0]"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>

              <dl className="mt-5 space-y-3 text-sm">
                <div className="flex justify-between gap-4 border-b border-[#393333] pb-2.5">
                  <dt className="text-[#A5A0A0]">Status</dt>
                  <dd
                    className={`font-semibold ${
                      selected.status === 'Cancelled' ? 'text-[#8DBD87]' : 'text-[#D96B70]'
                    }`}
                  >
                    {selected.status}
                  </dd>
                </div>
                <div className="flex justify-between gap-4 border-b border-[#393333] pb-2.5">
                  <dt className="text-[#A5A0A0]">Triggered At</dt>
                  <dd suppressHydrationWarning className="font-mono text-[#F5F2F0] tabular-nums">
                    {formatDisplayDateTime(selected.createdAt)}
                  </dd>
                </div>
                {selected.cancelledAt ? (
                  <div className="flex justify-between gap-4 border-b border-[#393333] pb-2.5">
                    <dt className="text-[#A5A0A0]">Cancelled At</dt>
                    <dd suppressHydrationWarning className="font-mono text-[#F5F2F0] tabular-nums">
                      {formatDisplayDateTime(selected.cancelledAt)}
                    </dd>
                  </div>
                ) : null}
                <div className="flex justify-between gap-4 border-b border-[#393333] pb-2.5">
                  <dt className="text-[#A5A0A0]">Coordinates</dt>
                  <dd className="font-mono text-[#F5F2F0] tabular-nums">
                    {formatClientCoordinates(selected.latitude, selected.longitude)} ({selected.locationType})
                  </dd>
                </div>
                <div className="flex justify-between gap-4 border-b border-[#393333] pb-2.5">
                  <dt className="text-[#A5A0A0]">Location</dt>
                  <dd className="text-right text-[#F5F2F0]">{selected.locationLabel}</dd>
                </div>
                <div>
                  <dt className="text-[#A5A0A0]">Notification Log</dt>
                  <dd className="mt-1 rounded-xl border border-[#393333] bg-[#111111] p-3 text-xs leading-relaxed text-[#A5A0A0]">
                    {selected.deliveryNote}
                  </dd>
                </div>
              </dl>

              <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
                <motion.button
                  whileTap={{ scale: 0.94 }}
                  type="button"
                  disabled={actionBusy}
                  onClick={() => handleDeleteRecord(selected.id)}
                  className="inline-flex h-10 items-center gap-1.5 rounded-full border border-[#393333] bg-[#111111] px-4 text-xs font-medium text-[#D96B70] hover:border-[#D96B70]/60 disabled:opacity-50 whitespace-nowrap shrink-0"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                  Delete Record
                </motion.button>

                <div className="flex flex-wrap items-center gap-2.5">
                  <motion.a
                    whileTap={{ scale: 0.94 }}
                    href={buildClientGoogleMapsUrl(selected.latitude, selected.longitude)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex h-10 items-center gap-1.5 rounded-full border border-[#ECA99C] px-4 text-xs font-medium text-[#F5F2F0] hover:bg-[#252525] whitespace-nowrap shrink-0"
                  >
                    Open Maps
                    <ExternalLink className="h-3.5 w-3.5 text-[#F5B6A9]" />
                  </motion.a>
                  {selected.status === 'Active' ? (
                    <motion.button
                      whileTap={{ scale: 0.94 }}
                      type="button"
                      disabled={actionBusy}
                      onClick={() => handleCancelRecord(selected.id)}
                      className="h-10 rounded-full bg-[#F5B6A9] px-5 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0"
                    >
                      Mark Cancelled
                    </motion.button>
                  ) : null}
                </div>
              </div>
            </motion.div>
          </div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}
