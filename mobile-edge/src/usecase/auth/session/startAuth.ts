import type { StartAuthBody } from '../../contract/auth/session/StartAuthBody';

export async function startAuth(_body: StartAuthBody): Promise<never> {
  throw new Error('Legacy auth session start is retired; use /access/* session transport.');
}
