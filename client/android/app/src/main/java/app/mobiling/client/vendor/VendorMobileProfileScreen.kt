package app.mobiling.client.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.data.vendor.profile.VendorProfileGateway
import app.mobiling.client.ui.vendor.profile.VendorMobileProfileScreenContract
import app.mobiling.client.usecase.vendor.profile.VendorLoadProfileUseCase

@Composable
fun VendorMobileProfileScreen(
    vendorId: String?,
    vendorProfileGateway: VendorProfileGateway?,
) {
    var profile by remember { mutableStateOf<VendorMobileProfileScreenContract?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vendorId, vendorProfileGateway) {
        profile = null
        errorMessage = null

        if (vendorId.isNullOrBlank()) {
            errorMessage = "Profile requires an active vendor session."
            return@LaunchedEffect
        }

        if (vendorProfileGateway == null) {
            errorMessage = "Vendor profile gateway is not available."
            return@LaunchedEffect
        }

        try {
            profile = VendorMobileProfileScreenContract.from(
                VendorLoadProfileUseCase(vendorProfileGateway).invoke(vendorId),
            )
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Vendor profile is temporarily unavailable."
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("My Profile", fontWeight = FontWeight.Bold)
        }

        when {
            errorMessage != null -> item {
                Text(errorMessage ?: "Vendor profile is temporarily unavailable.")
            }

            profile == null -> item {
                Text("Loading vendor profile...")
            }

            else -> {
                val currentProfile = profile ?: return@LazyColumn

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(currentProfile.title, fontWeight = FontWeight.SemiBold)
                        ProfileField("Vendor ID", currentProfile.vendorId)
                        ProfileField("Brand", currentProfile.brandName)
                        ProfileField("Status", currentProfile.status)
                        ProfileField("Publication", currentProfile.publicationStatus)
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Completion: ${currentProfile.completionPercent}%")
                        LinearProgressIndicator(
                            progress = currentProfile.completionPercent / 100f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(if (currentProfile.readyForPublishing) "Ready for publishing" else "Not ready yet")
                            },
                        )
                    }
                }

                item {
                    ProfileField("Next action", currentProfile.nextAction)
                    ProfileField("About", currentProfile.about)
                    ProfileField("Website", currentProfile.website)
                    ProfileField("Avatar URL", currentProfile.avatarUrl)
                    ProfileField("Cover URL", currentProfile.coverUrl)
                }
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String?) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value?.takeIf { it.isNotBlank() } ?: "—")
    }
}
