import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

// Conventional shadcn/ui helper: merges Tailwind class strings and dedupes
// conflicting utilities (e.g. p-2 + p-4 -> p-4).
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
