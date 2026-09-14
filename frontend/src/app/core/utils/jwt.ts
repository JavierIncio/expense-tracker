export interface JwtPayload {
  sub: string;
  type: string;
  email: string;
  roles: string[];
  jti: string;
  iss: string;
  iat: number;
  exp: number;
}

/**
 * Decodes a JWT token and returns the payload as a JwtPayload object.
 *
 * @param token  The JWT token to decode.
 * @returns The decoded JWT payload, or null if the token is invalid.
 */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');

    if (parts.length !== 3) return null;

    const base64Url = parts[1];

    // Convert Base64URL -> Base64
    const base64 = base64Url
      .replace(/-/g, '+')
      .replace(/_/g, '/');

    // Add padding if necessary
    const padded = base64.padEnd(
      Math.ceil(base64.length / 4) * 4,
      '='
    );

    const json = atob(padded);
    return JSON.parse(json) as JwtPayload;

  } catch {
    return null;
  }
}

/**
 * Checks if a JWT token is expired based on its payload and an optional buffer time.
 *
 * @param payload   The decoded JWT payload to check for expiration.
 * @param bufferSec Optional buffer time in seconds to account for clock skew. Default is 5 seconds.
 * @returns
 */
export function isTokenExpired(
  payload: JwtPayload,
  bufferSec = 5
): boolean {
  return payload.exp <= Math.floor(Date.now() / 1000) + bufferSec;
}
