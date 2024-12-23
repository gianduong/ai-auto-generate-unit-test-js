
// func-chiu.test.js
const { nhan } = require('./func-chiu');

describe('nhan function', () => {
  it('should return 0 when multiplying by 0', () => {
    expect(nhan(5, 0)).toBe(0);
    expect(nhan(0, 5)).toBe(0);
    expect(nhan(0, 0)).toBe(0);
  });

  it('should return the correct product for positive integers', () => {
    expect(nhan(2, 3)).toBe(6);
    expect(nhan(10, 5)).toBe(50);
    expect(nhan(7, 7)).toBe(49);
  });

  it('should return the correct product for negative integers', () => {
    expect(nhan(-2, 3)).toBe(-6);
    expect(nhan(2, -3)).toBe(-6);
    expect(nhan(-2, -3)).toBe(6);
  });

  it('should return the correct product for floating-point numbers', () => {
    expect(nhan(2.5, 3)).toBeCloseTo(7.5);
    expect(nhan(2, 3.5)).toBeCloseTo(7);
    expect(nhan(2.5, 3.5)).toBeCloseTo(8.75);
  });

  it('should handle large numbers', () => {
    expect(nhan(1000000, 1000000)).toBe(1000000000000);
  });

  it('should return NaN for non-numeric inputs', () => {
    expect(nhan('a', 5)).toBeNaN();
    expect(nhan(5, 'a')).toBeNaN();
    expect(nhan('a', 'b')).toBeNaN();
    expect(nhan(5, NaN)).toBeNaN();
    expect(nhan(NaN, 5)).toBeNaN();

  });

});

