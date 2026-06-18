import { ENV } from "../env.js";
import { M } from "../metrics.js";
let state = "closed";
let fails = 0;
let openedAt = 0;
export function allow() {
    if (state === "open") {
        if (Date.now() - openedAt > ENV.CB_COOLDOWN_MS) {
            state = "half_open";
            M.circuit("half_open");
            return true;
        }
        return false;
    }
    return true;
}
export function onSuccess() { if (state !== "closed") {
    state = "closed";
    fails = 0;
    M.circuit("closed");
} }
export function onFail() { fails++; if (state === "half_open") {
    state = "open";
    openedAt = Date.now();
    M.circuit("open");
    return;
} if (fails >= ENV.CB_FAILS_OPEN) {
    state = "open";
    openedAt = Date.now();
    M.circuit("open");
} }
export function status() { return { state, fails, openedAt }; }
