'use client';

import { useMemo, useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import {
  Edit3,
  Phone,
  Plus,
  Search,
  Trash2,
  Users,
  X,
} from 'lucide-react';
import TiltCard from '@/components/TiltCard';
import { triggerHaptic } from '@/lib/haptics';
import { apiFetch, safeJson } from '@/lib/client-utils';
import type { EmergencyContact, WorkspaceBundle } from '@/lib/types';

interface TrustedContactsViewProps {
  bundle: WorkspaceBundle;
  onRefresh: () => Promise<void>;
}

export default function TrustedContactsView({
  bundle,
  onRefresh,
}: TrustedContactsViewProps) {
  const [query, setQuery] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<EmergencyContact | null>(null);

  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [relationship, setRelationship] = useState('');
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return bundle.contacts;
    return bundle.contacts.filter(
      (c) =>
        c.name.toLowerCase().includes(q) ||
        c.phone.toLowerCase().includes(q) ||
        c.relationship.toLowerCase().includes(q)
    );
  }, [bundle.contacts, query]);

  const openAddModal = () => {
    triggerHaptic('confirm');
    setEditingContact(null);
    setName('');
    setPhone('');
    setRelationship('');
    setFormError('');
    setModalOpen(true);
  };

  const openEditModal = (contact: EmergencyContact) => {
    triggerHaptic('confirm');
    setEditingContact(contact);
    setName(contact.name);
    setPhone(contact.phone);
    setRelationship(contact.relationship);
    setFormError('');
    setModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    setBusy(true);
    try {
      const url = editingContact
        ? `/api/contacts/${editingContact.id}`
        : '/api/contacts';
      const method = editingContact ? 'PATCH' : 'POST';
      const res = await apiFetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, phone, relationship }),
      });
      const data = await safeJson(res);
      if (!res.ok) {
        triggerHaptic('error');
        setFormError(data.error ?? 'Failed to save contact.');
        return;
      }
      triggerHaptic('success');
      await onRefresh();
      setModalOpen(false);
    } catch {
      triggerHaptic('error');
      setFormError('Network error while saving contact.');
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async (contactId: number) => {
    triggerHaptic('confirm');
    setDeletingId(contactId);
    try {
      const res = await apiFetch(`/api/contacts/${contactId}`, {
        method: 'DELETE',
      });
      if (res.ok) {
        triggerHaptic('success');
        await onRefresh();
      }
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Toolbar */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div className="relative w-full sm:max-w-sm">
          <Search className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search contacts by name, phone, or relation..."
            aria-label="Search trusted contacts"
            className="h-11 w-full rounded-xl border border-[#393333] bg-[#1C1C1C] pr-4 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
          />
        </div>

        <motion.button
          whileTap={{ scale: 0.95 }}
          type="button"
          onClick={openAddModal}
          className="inline-flex h-11 items-center justify-center gap-2 rounded-full bg-[#F5B6A9] px-6 text-sm font-semibold text-[#111111] transition-colors hover:bg-[#ECA99C] whitespace-nowrap shrink-0"
        >
          <Plus className="h-4 w-4" />
          Add Contact
        </motion.button>
      </div>

      {/* Contact List / Empty State */}
      {bundle.contacts.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-[#393333] bg-[#1C1C1C] px-6 py-16 text-center">
          <div className="flex h-14 w-14 items-center justify-center rounded-full border border-[#393333] bg-[#111111] text-[#F5B6A9]">
            <Users className="h-6 w-6" />
          </div>
          <h2 className="mt-4 text-lg font-semibold text-[#F5F2F0]">
            No Trusted Contacts Yet
          </h2>
          <p className="mt-1.5 max-w-md text-sm text-[#A5A0A0]">
            Add family members, close friends, or emergency guardians to include in your SOS dispatch list.
          </p>
          <motion.button
            whileTap={{ scale: 0.95 }}
            type="button"
            onClick={openAddModal}
            className="mt-6 inline-flex h-11 items-center gap-2 rounded-full bg-[#F5B6A9] px-6 text-sm font-semibold text-[#111111] hover:bg-[#ECA99C] whitespace-nowrap shrink-0"
          >
            <Plus className="h-4 w-4" />
            Add First Contact
          </motion.button>
        </div>
      ) : filtered.length === 0 ? (
        <div className="rounded-2xl border border-[#393333] bg-[#1C1C1C] p-10 text-center text-sm text-[#A5A0A0]">
          No trusted contacts match &ldquo;{query}&rdquo;.
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {filtered.map((contact, idx) => (
            <motion.div
              key={contact.id}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.2, delay: idx * 0.03 }}
            >
              <TiltCard
                maxTilt={2.5}
                className="flex flex-col justify-between gap-4 rounded-2xl border border-[#393333] bg-[#1C1C1C] p-5 shadow-[0_4px_20px_-8px_rgba(0,0,0,0.6)] transition-colors hover:border-[#ECA99C]/50"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex min-w-0 items-center gap-3.5">
                    <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-[#252525] text-sm font-semibold text-[#F5F2F0]">
                      {contact.name.charAt(0).toUpperCase()}
                    </div>
                    <div className="min-w-0">
                      <p className="truncate text-base font-semibold text-[#F5F2F0]">
                        {contact.name}
                      </p>
                      <p className="mt-0.5 font-mono text-xs text-[#A5A0A0] tabular-nums">
                        {contact.phone}
                      </p>
                      {contact.relationship ? (
                        <p className="mt-1 text-xs text-[#777272]">
                          {contact.relationship}
                        </p>
                      ) : null}
                    </div>
                  </div>
                  <motion.a
                    whileTap={{ scale: 0.92 }}
                    href={`tel:${contact.phone}`}
                    onClick={() => triggerHaptic('confirm')}
                    aria-label={`Call ${contact.name}`}
                    className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-[#393333] bg-[#111111] text-[#F5B6A9] hover:border-[#ECA99C]"
                  >
                    <Phone className="h-4 w-4" />
                  </motion.a>
                </div>

                <div className="flex items-center justify-end gap-2 border-t border-[#393333] pt-3">
                  <button
                    type="button"
                    onClick={() => openEditModal(contact)}
                    className="inline-flex h-8 items-center gap-1.5 rounded-full border border-[#393333] bg-[#111111] px-3.5 text-xs font-medium text-[#F5F2F0] hover:border-[#ECA99C] whitespace-nowrap shrink-0"
                  >
                    <Edit3 className="h-3.5 w-3.5 text-[#F5B6A9]" />
                    Edit
                  </button>
                  <button
                    type="button"
                    disabled={deletingId === contact.id}
                    onClick={() => handleDelete(contact.id)}
                    className="inline-flex h-8 items-center gap-1.5 rounded-full border border-[#393333] bg-[#111111] px-3.5 text-xs font-medium text-[#D96B70] hover:border-[#D96B70]/60 disabled:opacity-50 whitespace-nowrap shrink-0"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                    {deletingId === contact.id ? 'Removing...' : 'Remove'}
                  </button>
                </div>
              </TiltCard>
            </motion.div>
          ))}
        </div>
      )}

      {/* Add / Edit Contact Dialog */}
      <AnimatePresence>
        {modalOpen ? (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 px-4 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.94, y: 10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.94, y: 10 }}
              transition={{ duration: 0.2 }}
              role="dialog"
              aria-modal="true"
              aria-labelledby="contact-dialog-title"
              className="w-full max-w-md rounded-2xl border border-[#393333] bg-[#1C1C1C] p-6 shadow-2xl"
            >
              <div className="flex items-center justify-between">
                <h3 id="contact-dialog-title" className="text-lg font-semibold text-[#F5F2F0]">
                  {editingContact ? 'Edit Trusted Contact' : 'Add Trusted Contact'}
                </h3>
              <button
                type="button"
                aria-label="Close contact dialog"
                onClick={() => setModalOpen(false)}
                className="rounded-lg p-1.5 text-[#A5A0A0] hover:bg-[#252525] hover:text-[#F5F2F0]"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <form onSubmit={handleSave} className="mt-5 space-y-4">
              <div>
                <label htmlFor="contact-name" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Full Name
                </label>
                <input
                  id="contact-name"
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Mom"
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              <div>
                <label htmlFor="contact-phone" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Phone Number
                </label>
                <input
                  id="contact-phone"
                  type="tel"
                  required
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+91 98765 43210"
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 font-mono text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              <div>
                <label htmlFor="contact-relation" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                  Relationship (optional)
                </label>
                <input
                  id="contact-relation"
                  type="text"
                  value={relationship}
                  onChange={(e) => setRelationship(e.target.value)}
                  placeholder="e.g. Mother, Friend, Guardian"
                  className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                />
              </div>

              {formError ? (
                <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3.5 py-2.5 text-xs text-[#F5B6A9]">
                  {formError}
                </p>
              ) : null}

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="h-10 rounded-full border border-[#393333] bg-[#252525] px-5 text-xs font-medium text-[#F5F2F0] hover:bg-[#2F2F2F] whitespace-nowrap shrink-0"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={busy}
                  className="h-10 rounded-full bg-[#F5B6A9] px-6 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0"
                >
                  {busy ? 'Saving...' : editingContact ? 'Save Changes' : 'Save Contact'}
                </button>
              </div>
            </form>
          </motion.div>
        </div>
      ) : null}
    </AnimatePresence>
  </div>
);
}
