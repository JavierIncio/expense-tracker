import { environment } from '@env/environment';

export interface Month {
  year: number;
  month: number;
}

export function currentMonth(): Month {
  const now = new Date();
  return {
    year: now.getFullYear(),
    month: now.getMonth() + 1,
  };
}

/**
 * Shifts a Month object by a given number of months.
 *
 * @param param0 Month object
 * @param delta Number of months to shift back or forward
 * @returns
 */
export function shiftMonth({ month, year }: Month, delta: number): Month {
  const total = year * 12 + (month - 1) + delta;
  return {
    year: Math.floor(total / 12),
    month: (total % 12) + 1,
  };
}

/**
 * Formats a Month object into a human-readable string in the environment's locale language.
 *
 * @param param0 Month object
 * @returns string in the format "Month Year" (e.g., "September 2026")
 */
export function formatMonthLabel({ month, year }: Month): string {
  return monthName(month) + ' ' + year;
}

export function monthName(month: number): string {
  const name = new Intl.DateTimeFormat(environment.locale, { month: 'long' }).format(
    new Date(2000, month - 1, 1),
  );
  return name.charAt(0).toUpperCase() + name.slice(1);
}
