import Foundation

extension AuthSessionPayload {
    func toAccessScreen() -> AccessScreen {
        if requiresVerification {
            return .verificationRequired
        }
        if requiresSecondFactor {
            return .secondFactorRequired
        }
        return authenticated ? .welcome : .signIn
    }
}
