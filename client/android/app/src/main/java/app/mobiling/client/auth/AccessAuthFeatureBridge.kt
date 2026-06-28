package app.mobiling.client.auth

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessConfirmVerificationRequest
import app.mobiling.client.contract.auth.session.AccessRegisterAuthRequest
import app.mobiling.client.contract.auth.session.AccessRequestRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessResetRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import app.mobiling.client.contract.auth.session.AccessVerifySecondFactorRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway
import app.mobiling.client.usecase.auth.session.AccessChallengeSecondFactorUseCase
import app.mobiling.client.usecase.auth.session.AccessConfirmVerificationUseCase
import app.mobiling.client.usecase.auth.session.AccessLogoutUseCase
import app.mobiling.client.usecase.auth.session.AccessRegisterUseCase
import app.mobiling.client.usecase.auth.session.AccessRequestRecoveryUseCase
import app.mobiling.client.usecase.auth.session.AccessResendVerificationUseCase
import app.mobiling.client.usecase.auth.session.AccessResetRecoveryUseCase
import app.mobiling.client.usecase.auth.session.AccessRestoreUseCase
import app.mobiling.client.usecase.auth.session.AccessStartUseCase
import app.mobiling.client.usecase.auth.session.AccessVerifySecondFactorUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for auth entry flow.
 *
 * It keeps Auth as the dedicated entry/auth surface while delegating
 * behavior into canonical Contract/Data/UseCase slices.
 */
class AccessAuthFeatureBridge(
    private val gateway: AccessAuthSessionGateway,
) {
    suspend fun start(request: AccessStartAuthRequest): AccessAuthSessionPayload =
        AccessStartUseCase(gateway).invoke(request)

    suspend fun register(request: AccessRegisterAuthRequest): AccessAuthSessionPayload =
        AccessRegisterUseCase(gateway).invoke(request)

    suspend fun restore(): AccessAuthSessionPayload =
        AccessRestoreUseCase(gateway).invoke()

    suspend fun logout() =
        AccessLogoutUseCase(gateway).invoke()

    suspend fun resendVerification(): AccessAuthSessionPayload =
        AccessResendVerificationUseCase(gateway).invoke()

    suspend fun confirmVerification(request: AccessConfirmVerificationRequest): AccessAuthSessionPayload =
        AccessConfirmVerificationUseCase(gateway).invoke(request)

    suspend fun challengeSecondFactor(): AccessAuthSessionPayload =
        AccessChallengeSecondFactorUseCase(gateway).invoke()

    suspend fun verifySecondFactor(request: AccessVerifySecondFactorRequest): AccessAuthSessionPayload =
        AccessVerifySecondFactorUseCase(gateway).invoke(request)

    suspend fun requestRecovery(request: AccessRequestRecoveryRequest): AccessAuthSessionPayload =
        AccessRequestRecoveryUseCase(gateway).invoke(request)

    suspend fun resetRecovery(request: AccessResetRecoveryRequest): AccessAuthSessionPayload =
        AccessResetRecoveryUseCase(gateway).invoke(request)
}