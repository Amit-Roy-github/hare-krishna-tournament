/**
 * Returns the display title for a given KeliKunj week number.
 *
 * Rules (in priority order):
 *  1. Week number is a multiple of 4 (4, 8, 12, 16 …) → "GRAND FINALE"
 *  2. Today is Sunday (last day of the week)            → "FINALE"
 *  3. Otherwise                                         → "Week N"
 */
export function getWeekTitle(weekNo) {
  if (!weekNo) return 'KeliKunj'
  if (weekNo % 4 === 0) return 'GRAND FINALE'
  if (new Date().getDay() === 0) return 'FINALE'
  return `Week ${weekNo}`
}
