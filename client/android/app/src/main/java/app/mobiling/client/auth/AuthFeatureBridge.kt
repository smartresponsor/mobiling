package app.mobiling.client.auth

import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.contract.auth.session.ConfirmVerificationRequest
import app.mobiling.client.contract.auth.session.RegisterAuthRequest
import app.mobiling.client.contract.auth.session.StartAuthRequest
import app.mobiling.client.contract.auth.session.VerifySecondFactorRequest
import app.mobiling.client.data.auth.session.AuthSessionGateway
import app.mobiling.client.usecase.auth.session.ChallengeSecondFactorUseCase
import app.mobiling.client.usecase.auth.session.ConfirmVerificationUseCase
import app.mobiling.client.usecase.auth.session.LogoutAuthUseCase
import app.mobiling.client.usecase.auth.session.RegisterAuthUseCase
import app.mobiling.client.usecase.auth.session.ResendVerificationUseCase
import app.mobiling.client.usecase.auth.session.RestoreAuthUseCase
import app.mobiling.client.usecase.auth.session.StartAuthUseCase
import app.mobiling.client.usecase.auth.session.VerifySecondFactorUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for auth entry flow.
 *
 * It keeps Auth as the dedicated entry/auth surface while delegating
 * behavior into canonical Contract/Data/UseCase slices.
 */
class AuthFeatureBridge(
    private val gateway: AuthSessionGateway,
) {
    suspend fun start(request: StartAuthRequest): AuthSessionPayload =
        StartAuthUseCase(gateway).invoke(request)

    suspend fun register(request: RegisterAuthRequest): AuthSessionPayload =
        RegisterAuthUseCase(gateway).invoke(request)

    suspend fun restore(): AuthSessionPayload =
        RestoreAuthUseCase(gateway).invoke()

    suspend fun logout() =
        LogoutAuthUseCase(gateway).invoke()

    suspend fun resendVerification(): AuthSessionPayload =
        ResendVerificationUseCase(gateway).invoke()

    suspend fun confirmVerification(request: ConfirmVerificationRequest): AuthSessionPayload =
        ConfirmVerificationUseCase(gateway).invoke(request)

    suspend fun challengeSecondFactor(): AuthSessionPayload =
        ChallengeSecondFactorUseCase(gateway).invoke()

    suspend fun verifySecondFactor(request: VerifySecondFactorRequest): AuthSessionPayload =
        VerifySecondFactorUseCase(gateway).invoke(request)
}