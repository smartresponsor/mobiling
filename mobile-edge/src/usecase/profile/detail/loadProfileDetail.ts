import type { ProfileDetail } from '../../contract/profile/detail/ProfileDetail';

export async function loadProfileDetail(profileId: string): Promise<ProfileDetail> {
  return {
    profileId,
    displayName: 'Stub Profile',
    email: 'stub@example.test',
  };
}
