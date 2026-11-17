import type { Metadata } from 'next';
import { JetBrains_Mono, Plus_Jakarta_Sans } from 'next/font/google';
import './globals.css';

const plusJakarta = Plus_Jakarta_Sans({
  subsets: ['latin'],
  weight: ['400', '500', '600', '700'],
  variable: '--font-jakarta',
  display: 'swap',
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ['latin'],
  weight: ['400', '500', '600'],
  variable: '--font-jetbrains-mono',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'Women Safety System',
  description:
    'Full-screen Women Safety System web application for emergency SOS activation, live and simulated location sharing, trusted contacts, and SOS history.',
  openGraph: {
    title: 'Women Safety System',
    description:
      'Full-screen Women Safety System web application for emergency SOS activation, live and simulated location sharing, trusted contacts, and SOS history.',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Women Safety System',
    description:
      'Full-screen Women Safety System web application for emergency SOS activation, live and simulated location sharing, trusted contacts, and SOS history.',
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html
      lang="en"
      suppressHydrationWarning
      className={`${plusJakarta.variable} ${jetbrainsMono.variable}`}
    >
      <body
        className="min-h-screen bg-[#080808] text-[#F5F2F0] antialiased selection:bg-[#F5B6A9]/30 selection:text-[#F5F2F0]"
        suppressHydrationWarning
      >
        {children}
      </body>
    </html>
  );
}
