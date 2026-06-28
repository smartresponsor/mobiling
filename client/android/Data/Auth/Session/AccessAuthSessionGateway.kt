package app.mobiling.client.data.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessConfirmVerificationRequest
import app.mobiling.client.contract.auth.session.AccessRegisterAuthRequest
import app.mobiling.client.contract.auth.session.AccessRequestRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessResetRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import app.mobiling.client.contract.auth.session.AccessVerifySecondFactorRequest

interface AccessAuthSessionGateway {
    suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload
    suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload
    suspend fun restoreAuth(): AccessAuthSessionPayload
    suspend fun logoutAuth()
    suspend fun resendVerification(): AccessAuthSessionPayload
    suspend fun confirmVerification(request: AccessConfirmVerificationRequest): AccessAuthSessionPayload
    suspend fun challengeSecondFactor(): AccessAuthSessionPayload
    suspend fun verifySecondFactor(request: AccessVerifySecondFactorRequest): AccessAuthSessionPayload
    suspend fun requestRecovery(request: AccessRequestRecoveryRequest): AccessAuthSessionPayload
    suspend fun resetRecovery(request: AccessResetRecoveryRequest): AccessAuthSessionPayload
}