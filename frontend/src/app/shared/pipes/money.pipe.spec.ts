import { MoneyPipe } from './money.pipe';

describe('MoneyPipe', () => {
  const pipe = new MoneyPipe();

  it("format with locale & environment's currency (es-ES, EUR)", () => {
    expect(pipe.transform(1234.5)).toBe('1234,50\u00A0€');
  });

  it('format negative values', () => {
    expect(pipe.transform(-25)).toBe('-25,00\u00A0€');
  });

  it('returns dash for null/undefined', () => {
    expect(pipe.transform(null)).toBe('—');
    expect(pipe.transform(undefined)).toBe('—');
  });
});
