package app.mobiling.client.data.auth.session

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.ConfirmVerificationRequest
import app.mobiling.client.contract.auth.session.RegisterAuthRequest
import app.mobiling.client.contract.auth.session.StartAuthRequest
import app.mobiling.client.contract.auth.session.VerifySecondFactorRequest

interface AuthSessionGateway {
    suspend fun startAuth(request: StartAuthRequest): AuthSessionPayload
    suspend fun registerAuth(request: RegisterAuthRequest): AuthSessionPayload
    suspend fun restoreAuth(): AuthSessionPayload
    suspend fun logoutAuth()
    suspend fun resendVerification(): AuthSessionPayload
    suspend fun confirmVerification(request: ConfirmVerificationRequest): AuthSessionPayload
    suspend fun challengeSecondFactor(): AuthSessionPayload
    suspend fun verifySecondFactor(request: VerifySecondFactorRequest): AuthSessionPayload
}