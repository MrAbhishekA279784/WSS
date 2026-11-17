'use client';

import { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import {
  ArrowRight,
  Clock,
  Eye,
  EyeOff,
  Lock,
  Mail,
  MapPin,
  Plus,
  ShieldAlert,
  User,
  Users,
  X,
} from 'lucide-react';
import BrandMark from './BrandMark';
import { apiFetch, storeSessionToken } from '@/lib/client-utils';
import type { WorkspaceBundle } from '@/lib/types';

interface SplashAndAuthProps {
  skipSplash: boolean;
  onAuthenticated: (bundle: WorkspaceBundle | null) => Promise<void>;
}

export default function SplashAndAuth({
  skipSplash,
  onAuthenticated,
}: SplashAndAuthProps) {
  const [showSplash, setShowSplash] = useState(!skipSplash);
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [showForgot, setShowForgot] = useState(false);

  // Login state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [showLoginPw, setShowLoginPw] = useState(false);
  const [loginError, setLoginError] = useState('');
  const [loginBusy, setLoginBusy] = useState(false);

  // Register state
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirm, setRegConfirm] = useState('');
  const [showRegPw, setShowRegPw] = useState(false);
  const [regError, setRegError] = useState('');
  const [regBusy, setRegBusy] = useState(false);

  // Forgot password modal state
  const [fpEmail, setFpEmail] = useState('');
  const [fpName, setFpName] = useState('');
  const [fpNewPw, setFpNewPw] = useState('');
  const [fpConfirmPw, setFpConfirmPw] = useState('');
  const [fpError, setFpError] = useState('');
  const [fpSuccess, setFpSuccess] = useState('');
  const [fpBusy, setFpBusy] = useState(false);

  useEffect(() => {
    if (!showSplash) return;
    const timer = setTimeout(() => {
      setShowSplash(false);
    }, 1100);
    return () => clearTimeout(timer);
  }, [showSplash]);

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError('');
    setLoginBusy(true);
    try {
      const res = await apiFetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginEmail, password: loginPassword }),
      });
      const data = await res.json();
      if (!res.ok) {
        setLoginError(data.error ?? 'Invalid email or password.');
        return;
      }
      storeSessionToken(data.token);
      await onAuthenticated(data.bundle ?? null);
    } catch {
      setLoginError('Unable to sign in right now. Please try again.');
    } finally {
      setLoginBusy(false);
    }
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegError('');
    setRegBusy(true);
    try {
      const res = await apiFetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: regName,
          email: regEmail,
          phone: regPhone,
          password: regPassword,
          confirmPassword: regConfirm,
        }),
      });
      const data = await res.json();
      if (!res.ok) {
        setRegError(data.error ?? 'Registration failed.');
        return;
      }
      storeSessionToken(data.token);
      await onAuthenticated(data.bundle ?? null);
    } catch {
      setRegError('Unable to create account right now. Please try again.');
    } finally {
      setRegBusy(false);
    }
  };

  const handleForgotSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFpError('');
    setFpSuccess('');
    setFpBusy(true);
    try {
      const res = await fetch('/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: fpEmail,
          fullName: fpName,
          newPassword: fpNewPw,
          confirmPassword: fpConfirmPw,
        }),
      });
      const data = await res.json();
      if (!res.ok) {
        setFpError(data.error ?? 'Could not reset password.');
        return;
      }
      setFpSuccess(data.message ?? 'Password updated.');
      setFpNewPw('');
      setFpConfirmPw('');
    } catch {
      setFpError('Unable to reset password right now.');
    } finally {
      setFpBusy(false);
    }
  };

  if (showSplash) {
    return (
      <div className="relative flex min-h-screen w-full flex-col items-center justify-center overflow-hidden bg-[#080808] px-6">
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -top-40 -left-40 h-[420px] w-[420px] rounded-full border border-[#ECA99C]/15"
        />
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -right-44 -bottom-44 h-[460px] w-[460px] rounded-full border border-[#ECA99C]/15"
        />

        <motion.div
          initial={{ opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.35 }}
          className="flex flex-col items-center text-center"
        >
          <BrandMark size="lg" />
          <h1 className="mt-6 text-3xl font-bold tracking-tight text-[#F5F2F0] sm:text-4xl">
            Women Safety
          </h1>
          <p className="mt-2 text-sm text-[#A5A0A0] sm:text-base">
            Your Personal Safety Companion
          </p>

          <div className="mt-8 h-1 w-44 overflow-hidden rounded-full bg-[#252525]">
            <motion.div
              initial={{ x: '-100%' }}
              animate={{ x: '0%' }}
              transition={{ duration: 0.95, ease: 'easeOut' }}
              className="h-full w-full rounded-full bg-[#ECA99C]"
            />
          </div>

          <button
            type="button"
            onClick={() => setShowSplash(false)}
            className="mt-6 inline-flex items-center gap-2 rounded-full border border-[#393333] bg-[#1C1C1C] px-5 py-2 text-xs font-medium text-[#F5F2F0] transition-colors hover:border-[#ECA99C]/60 hover:bg-[#252525] whitespace-nowrap shrink-0"
          >
            Continue
            <ArrowRight className="h-3.5 w-3.5 text-[#F5B6A9]" />
          </button>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="relative grid min-h-screen w-full bg-[#080808] lg:grid-cols-[1.05fr_1fr]">
      {/* Left Brand & Capabilities Canvas (Full desktop height) */}
      <div className="relative hidden flex-col justify-between overflow-hidden border-r border-[#393333] bg-[#111111] p-12 lg:flex xl:p-16">
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -top-36 -left-36 h-[400px] w-[400px] rounded-full border border-[#ECA99C]/12"
        />
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -right-40 -bottom-40 h-[440px] w-[440px] rounded-full border border-[#ECA99C]/12"
        />

        <div className="relative flex items-center gap-3.5">
          <BrandMark size="md" />
          <span className="text-lg font-semibold tracking-tight text-[#F5F2F0]">
            Women Safety
          </span>
        </div>

        <div className="relative my-auto max-w-lg py-10">
          <h1 className="text-4xl leading-tight font-bold tracking-tight text-[#F5F2F0] text-balance">
            Your Personal Safety Companion
          </h1>
          <p className="mt-4 text-base leading-relaxed text-[#A5A0A0]">
            Activate immediate SOS alerts, share live or preset coordinates via Google Maps, manage trusted emergency contacts, and track SOS history in one focused workspace.
          </p>

          <div className="mt-10 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="rounded-xl border border-[#393333] bg-[#1C1C1C] p-4">
              <ShieldAlert className="h-5 w-5 text-[#F5B6A9]" />
              <h2 className="mt-3 text-sm font-semibold text-[#F5F2F0]">
                One-Tap SOS Pipeline
              </h2>
              <p className="mt-1 text-xs leading-relaxed text-[#A5A0A0]">
                Captures coordinates, logs alerts to SQLite, and prepares contact dispatch.
              </p>
            </div>
            <div className="rounded-xl border border-[#393333] bg-[#1C1C1C] p-4">
              <MapPin className="h-5 w-5 text-[#F5B6A9]" />
              <h2 className="mt-3 text-sm font-semibold text-[#F5F2F0]">
                Live & Preset Location
              </h2>
              <p className="mt-1 text-xs leading-relaxed text-[#A5A0A0]">
                Uses browser geolocation with a labelled simulation mode and Google Maps links.
              </p>
            </div>
            <div className="rounded-xl border border-[#393333] bg-[#1C1C1C] p-4">
              <Users className="h-5 w-5 text-[#F5B6A9]" />
              <h2 className="mt-3 text-sm font-semibold text-[#F5F2F0]">
                Trusted Contacts
              </h2>
              <p className="mt-1 text-xs leading-relaxed text-[#A5A0A0]">
                Store family and emergency numbers with direct dial and SMS actions.
              </p>
            </div>
            <div className="rounded-xl border border-[#393333] bg-[#1C1C1C] p-4">
              <Clock className="h-5 w-5 text-[#F5B6A9]" />
              <h2 className="mt-3 text-sm font-semibold text-[#F5F2F0]">
                Persistent History
              </h2>
              <p className="mt-1 text-xs leading-relaxed text-[#A5A0A0]">
                Review, cancel, or inspect every SOS alert and location snapshot.
              </p>
            </div>
          </div>
        </div>

        <p className="relative text-xs text-[#777272]">
          Women Safety System · Personal Emergency Workspace
        </p>
      </div>

      {/* Right Interactive Auth Workspace */}
      <div className="flex min-h-screen w-full flex-col items-center justify-center px-6 py-10 sm:px-10">
        {/* Compact brand lockup on mobile/tablet */}
        <div className="mb-8 flex items-center gap-3 lg:hidden">
          <BrandMark size="md" />
          <div>
            <p className="text-base font-semibold text-[#F5F2F0]">Women Safety</p>
            <p className="text-xs text-[#A5A0A0]">Your Personal Safety Companion</p>
          </div>
        </div>

        <div className="w-full max-w-md rounded-2xl border border-[#393333] bg-[#1C1C1C] p-7 sm:p-8">
          {mode === 'login' ? (
            <>
              <div>
                <h2 className="text-2xl font-bold tracking-tight text-[#F5F2F0]">
                  Welcome Back
                </h2>
                <p className="mt-1 text-sm text-[#A5A0A0]">Login to continue</p>
              </div>

              <form suppressHydrationWarning onSubmit={handleLoginSubmit} className="mt-7 space-y-4">
                <div>
                  <label htmlFor="login-email" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                    Email
                  </label>
                  <div className="relative">
                    <Mail className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
                    <input
                      id="login-email"
                      suppressHydrationWarning
                      type="email"
                      autoComplete="email"
                      autoCapitalize="none"
                      spellCheck={false}
                      required
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      placeholder="Email"
                      className="h-12 w-full rounded-xl border border-[#393333] bg-[#111111] pr-4 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="login-password" className="mb-1.5 block text-xs font-medium text-[#A5A0A0]">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
                    <input
                      id="login-password"
                      suppressHydrationWarning
                      type={showLoginPw ? 'text' : 'password'}
                      autoComplete="current-password"
                      required
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      placeholder="Password"
                      className="h-12 w-full rounded-xl border border-[#393333] bg-[#111111] pr-11 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                    />
                    <button
                      type="button"
                      aria-label={showLoginPw ? 'Hide password' : 'Show password'}
                      onClick={() => setShowLoginPw((v) => !v)}
                      className="absolute top-1/2 right-3 -translate-y-1/2 rounded-md p-1 text-[#A5A0A0] hover:text-[#F5F2F0]"
                    >
                      {showLoginPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                <div className="flex justify-end">
                  <button
                    type="button"
                    onClick={() => {
                      setFpEmail(loginEmail);
                      setFpError('');
                      setFpSuccess('');
                      setShowForgot(true);
                    }}
                    className="text-xs font-medium text-[#F5B6A9] hover:text-[#ECA99C]"
                  >
                    Forgot Password?
                  </button>
                </div>

                {loginError ? (
                  <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3.5 py-2.5 text-xs text-[#F5B6A9]">
                    {loginError}
                  </p>
                ) : null}

                <button
                  type="submit"
                  disabled={loginBusy}
                  className="h-12 w-full rounded-full bg-[#F5B6A9] px-6 text-sm font-semibold text-[#111111] transition-colors hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0"
                >
                  {loginBusy ? 'Signing in...' : 'Login'}
                </button>
              </form>

              <div className="my-6 flex items-center gap-3">
                <div className="h-px flex-1 bg-[#393333]" />
                <span className="text-xs text-[#A5A0A0]">or</span>
                <div className="h-px flex-1 bg-[#393333]" />
              </div>

              <button
                type="button"
                onClick={() => {
                  setLoginError('');
                  setMode('register');
                }}
                className="flex h-12 w-full items-center justify-center gap-2 rounded-full border border-[#ECA99C] bg-transparent px-6 text-sm font-medium text-[#F5F2F0] transition-colors hover:bg-[#252525] whitespace-nowrap shrink-0"
              >
                <Plus className="h-4 w-4 text-[#F5B6A9]" />
                Create New Account
              </button>
            </>
          ) : (
            <>
              <div>
                <h2 className="text-2xl font-bold tracking-tight text-[#F5F2F0]">
                  Create Account
                </h2>
                <p className="mt-1 text-sm text-[#A5A0A0]">
                  Register to stay safe and connected
                </p>
              </div>

              <form suppressHydrationWarning onSubmit={handleRegisterSubmit} className="mt-6 space-y-3.5">
                <div>
                  <label htmlFor="reg-name" className="mb-1 block text-xs font-medium text-[#A5A0A0]">
                    Full Name
                  </label>
                  <div className="relative">
                    <User className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
                    <input
                      id="reg-name"
                      suppressHydrationWarning
                      type="text"
                      required
                      value={regName}
                      onChange={(e) => setRegName(e.target.value)}
                      placeholder="Full Name"
                      className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] pr-4 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="reg-email" className="mb-1 block text-xs font-medium text-[#A5A0A0]">
                    Email
                  </label>
                  <div className="relative">
                    <Mail className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
                    <input
                      id="reg-email"
                      suppressHydrationWarning
                      type="email"
                      autoComplete="email"
                      autoCapitalize="none"
                      spellCheck={false}
                      required
                      value={regEmail}
                      onChange={(e) => setRegEmail(e.target.value)}
                      placeholder="Email"
                      className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] pr-4 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label htmlFor="reg-phone" className="mb-1 block text-xs font-medium text-[#A5A0A0]">
                    Phone Number (optional)
                  </label>
                  <input
                    id="reg-phone"
                    suppressHydrationWarning
                    type="tel"
                    value={regPhone}
                    onChange={(e) => setRegPhone(e.target.value)}
                    placeholder="+91 98765 43210"
                    className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="reg-password" className="mb-1 block text-xs font-medium text-[#A5A0A0]">
                    Password (min 6 characters)
                  </label>
                  <div className="relative">
                    <Lock className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
                    <input
                      id="reg-password"
                      suppressHydrationWarning
                      type={showRegPw ? 'text' : 'password'}
                      required
                      value={regPassword}
                      onChange={(e) => setRegPassword(e.target.value)}
                      placeholder="Password"
                      className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] pr-11 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                    />
                    <button
                      type="button"
                      aria-label={showRegPw ? 'Hide password' : 'Show password'}
                      onClick={() => setShowRegPw((v) => !v)}
                      className="absolute top-1/2 right-3 -translate-y-1/2 rounded-md p-1 text-[#A5A0A0] hover:text-[#F5F2F0]"
                    >
                      {showRegPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label htmlFor="reg-confirm" className="mb-1 block text-xs font-medium text-[#A5A0A0]">
                    Confirm Password
                  </label>
                  <div className="relative">
                    <Lock className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-[#A5A0A0]" />
                    <input
                      id="reg-confirm"
                      suppressHydrationWarning
                      type={showRegPw ? 'text' : 'password'}
                      required
                      value={regConfirm}
                      onChange={(e) => setRegConfirm(e.target.value)}
                      placeholder="Confirm Password"
                      className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] pr-4 pl-10 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
                    />
                  </div>
                </div>

                {regError ? (
                  <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3.5 py-2.5 text-xs text-[#F5B6A9]">
                    {regError}
                  </p>
                ) : null}

                <button
                  type="submit"
                  disabled={regBusy}
                  className="mt-2 h-12 w-full rounded-full bg-[#F5B6A9] px-6 text-sm font-semibold text-[#111111] transition-colors hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0"
                >
                  {regBusy ? 'Creating Account...' : 'Create Account'}
                </button>
              </form>

              <p className="mt-5 text-center text-xs text-[#A5A0A0]">
                Already have an account?{' '}
                <button
                  type="button"
                  onClick={() => {
                    setRegError('');
                    setMode('login');
                  }}
                  className="font-medium text-[#F5B6A9] hover:text-[#ECA99C]"
                >
                  Login
                </button>
              </p>
            </>
          )}
        </div>
      </div>

      {/* Forgot Password Modal */}
      {showForgot ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 px-4">
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="forgot-dialog-title"
            className="w-full max-w-md rounded-2xl border border-[#393333] bg-[#1C1C1C] p-6"
          >
            <div className="flex items-center justify-between">
              <h3 id="forgot-dialog-title" className="text-lg font-semibold text-[#F5F2F0]">
                Reset Password
              </h3>
              <button
                type="button"
                aria-label="Close reset password dialog"
                onClick={() => setShowForgot(false)}
                className="rounded-lg p-1.5 text-[#A5A0A0] hover:bg-[#252525] hover:text-[#F5F2F0]"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <p className="mt-1.5 text-xs text-[#A5A0A0]">
              Verify your registered email and full name to set a new password.
            </p>

            <form onSubmit={handleForgotSubmit} className="mt-4 space-y-3">
              <input
                type="email"
                required
                value={fpEmail}
                onChange={(e) => setFpEmail(e.target.value)}
                placeholder="Registered Email"
                className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
              />
              <input
                type="text"
                required
                value={fpName}
                onChange={(e) => setFpName(e.target.value)}
                placeholder="Registered Full Name"
                className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
              />
              <input
                type="password"
                required
                value={fpNewPw}
                onChange={(e) => setFpNewPw(e.target.value)}
                placeholder="New Password (min 6 characters)"
                className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
              />
              <input
                type="password"
                required
                value={fpConfirmPw}
                onChange={(e) => setFpConfirmPw(e.target.value)}
                placeholder="Confirm New Password"
                className="h-11 w-full rounded-xl border border-[#393333] bg-[#111111] px-3.5 text-sm text-[#F5F2F0] placeholder:text-[#777272] focus:border-[#ECA99C] focus:outline-none"
              />

              {fpError ? (
                <p role="alert" className="rounded-xl border border-[#D96B70]/40 bg-[#D96B70]/10 px-3 py-2 text-xs text-[#F5B6A9]">
                  {fpError}
                </p>
              ) : null}
              {fpSuccess ? (
                <p className="rounded-xl border border-[#8DBD87]/40 bg-[#8DBD87]/10 px-3 py-2 text-xs text-[#8DBD87]">
                  {fpSuccess}
                </p>
              ) : null}

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowForgot(false)}
                  className="rounded-full border border-[#393333] bg-[#252525] px-4 py-2 text-xs font-medium text-[#F5F2F0] hover:bg-[#2F2F2F] whitespace-nowrap shrink-0"
                >
                  Close
                </button>
                <button
                  type="submit"
                  disabled={fpBusy}
                  className="rounded-full bg-[#F5B6A9] px-5 py-2 text-xs font-semibold text-[#111111] hover:bg-[#ECA99C] disabled:opacity-60 whitespace-nowrap shrink-0"
                >
                  {fpBusy ? 'Updating...' : 'Update Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}
