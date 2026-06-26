import type { ProfileDetail } from '../../contract/profile/detail/ProfileDetail';
import { VendoringApiClient } from '../../../client/vendoring/vendoringApiClient.js';

const vendoringApiClient = new VendoringApiClient();

function isRecord(value: unknown): value is Record<string, unknown> {
  return null !== value && "object" === typeof value && !Array.isArray(value);
}

function stringValue(value: unknown): string | undefined {
  if ("string" === typeof value && "" !== value.trim()) {
    return value.trim();
  }

  if ("number" === typeof value && Number.isFinite(value)) {
    return String(value);
  }

  return undefined;
}

function nestedRecord(source: Record<string, unknown>, field: string): Record<string, unknown> {
  const value = source[field];

  return isRecord(value) ? value : {};
}

export async function loadProfileDetail(profileId: string): Promise<ProfileDetail> {
  const result = await vendoringApiClient.getProfile(profileId);

  if (result.status < 200 || result.status >= 300) {
    throw new Error(`Vendor profile request failed with HTTP ${result.status}.`);
  }

  const root = isRecord(result.body) ? result.body : {};
  const profile = nestedRecord(root, "profile");
  const publicProfile = nestedRecord(root, "publicProfile");
  const avatar = nestedRecord(publicProfile, "avatar");

  return {
    profileId: stringValue(root.vendorId) ?? profileId,
    displayName: stringValue(publicProfile.displayName) ?? stringValue(profile.displayName) ?? stringValue(publicProfile.publicName),
    email: stringValue(profile.email),
    phone: stringValue(profile.phone),
    avatarUrl: stringValue(avatar.url),
  };
}
