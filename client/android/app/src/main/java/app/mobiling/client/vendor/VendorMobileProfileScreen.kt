package app.mobiling.client.vendor

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mobiling.client.attachment.AttachmentFeatureBridge
import app.mobiling.client.contract.attachment.AttachmentItemPayload
import app.mobiling.client.contract.attachment.AttachmentLinkRequest
import app.mobiling.client.contract.attachment.AttachmentUploadHandoffRequest
import app.mobiling.client.data.vendor.profile.VendorProfileGateway
import coil.compose.AsyncImage
import com.yalantis.ucrop.UCrop
import java.io.File
import kotlinx.coroutines.launch
import app.mobiling.client.ui.vendor.profile.VendorMobileProfileScreenContract
import app.mobiling.client.usecase.vendor.profile.VendorLoadProfileUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorMobileProfileScreen(
    vendorId: String?,
    vendorProfileGateway: VendorProfileGateway?,
    attachmentFeatureBridge: AttachmentFeatureBridge? = null,
) {
    var profile by remember { mutableStateOf<VendorMobileProfileScreenContract?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mediaSlot by remember { mutableStateOf<String?>(null) }
    var mediaItems by remember { mutableStateOf<List<AttachmentItemPayload>>(emptyList()) }
    var mediaError by remember { mutableStateOf<String?>(null) }
    var mediaLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            UCrop.getError(result.data ?: return@rememberLauncherForActivityResult)?.let {
                mediaError = it.message ?: "Unable to crop the selected image."
            }
            return@rememberLauncherForActivityResult
        }
        val croppedUri = UCrop.getOutput(result.data ?: return@rememberLauncherForActivityResult)
            ?: return@rememberLauncherForActivityResult
        val slot = mediaSlot ?: return@rememberLauncherForActivityResult
        if (attachmentFeatureBridge == null || vendorId.isNullOrBlank() || vendorProfileGateway == null) {
            mediaError = "Profile upload requires an active vendor session."
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            mediaLoading = true
            mediaError = null
            try {
                val bytes = context.contentResolver.openInputStream(croppedUri)?.use { it.readBytes() }
                    ?: error("Unable to read the cropped image.")
                attachmentFeatureBridge.upload(
                    request = AttachmentUploadHandoffRequest(
                        ownerType = "vendor",
                        ownerId = vendorId,
                        context = "profile",
                        slot = slot,
                        isPrimary = true,
                        title = if (slot == "avatar") "Profile avatar" else "Profile cover",
                        altText = if (slot == "avatar") "Vendor profile avatar" else "Vendor profile cover",
                    ),
                    fileName = "vendor-${slot}-${System.currentTimeMillis()}.jpg",
                    mimeType = "image/jpeg",
                    bytes = bytes,
                )
                profile = VendorMobileProfileScreenContract.from(
                    VendorLoadProfileUseCase(vendorProfileGateway).invoke(vendorId),
                )
                mediaSlot = null
            } catch (exception: Exception) {
                mediaError = exception.message ?: "Unable to upload profile image."
            } finally {
                mediaLoading = false
            }
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { sourceUri: Uri? ->
        val slot = mediaSlot
        if (sourceUri == null || slot == null) return@rememberLauncherForActivityResult
        val destination = Uri.fromFile(File(context.cacheDir, "profile-${slot}-${System.currentTimeMillis()}.jpg"))
        val crop = UCrop.of(sourceUri, destination)
            .withAspectRatio(if (slot == "avatar") 1f else 8f, if (slot == "avatar") 1f else 3f)
            .withMaxResultSize(if (slot == "avatar") 1200 else 2400, if (slot == "avatar") 1200 else 900)
            .withOptions(UCrop.Options().apply {
                setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                setCompressionQuality(92)
                setFreeStyleCropEnabled(false)
                setHideBottomControls(false)
            })
        cropLauncher.launch(crop.getIntent(context))
    }

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
            errorMessage = "Vendor ${vendorId ?: "unknown"}: ${exception.message?.takeIf(String::isNotBlank) ?: "Vendor profile is temporarily unavailable."}"
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
                    VendorProfileMediaHeader(
                        profile = currentProfile,
                        onEdit = { slot ->
                            mediaSlot = slot
                            mediaError = null
                            mediaItems = emptyList()
                            if (attachmentFeatureBridge == null || vendorId.isNullOrBlank()) {
                                mediaError = "Profile media requires an active attachment bridge and vendor session."
                            } else {
                                scope.launch {
                                    mediaLoading = true
                                    try {
                                        mediaItems = attachmentFeatureBridge.list("vendor", vendorId, "profile", slot).items
                                            .filter { it.mimeType?.startsWith("image/") == true && !it.downloadUrl.isNullOrBlank() }
                                    } catch (exception: Exception) {
                                        mediaError = exception.message ?: "Media library is unavailable."
                                    } finally {
                                        mediaLoading = false
                                    }
                                }
                            }
                        },
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(currentProfile.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        currentProfile.brandName?.takeIf { it.isNotBlank() && it != currentProfile.title }?.let {
                            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text(currentProfile.publicationStatus?.takeIf(String::isNotBlank) ?: currentProfile.status?.takeIf(String::isNotBlank) ?: "Profile") },
                        )
                    }
                }

                currentProfile.about?.takeIf(String::isNotBlank)?.let { about ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(about, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                currentProfile.website?.takeIf(String::isNotBlank)?.let { website ->
                    item { ProfileField("Website", website) }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp) {
                            Text(
                                "Posts and vendor updates will appear here.",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    mediaSlot?.let { slot ->
        ModalBottomSheet(onDismissRequest = { mediaSlot = null }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(if (slot == "avatar") "Choose avatar" else "Choose cover", fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    enabled = !mediaLoading,
                ) {
                    Text("Choose from device")
                }
                when {
                    mediaLoading -> Text("Loading media library...")
                    mediaError != null -> Text(mediaError ?: "Media library is unavailable.")
                    mediaItems.isEmpty() -> Text("No uploaded images yet.")
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(mediaItems, key = { it.attachmentId }) { item ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val numericId = item.attachmentId.toLongOrNull()
                                    if (numericId == null || attachmentFeatureBridge == null || vendorId.isNullOrBlank()) {
                                        mediaError = "This attachment cannot be selected."
                                    } else {
                                        scope.launch {
                                            try {
                                                attachmentFeatureBridge.attach(
                                                    AttachmentLinkRequest(
                                                        attachmentId = numericId,
                                                        ownerType = "vendor",
                                                        ownerId = vendorId,
                                                        context = "profile",
                                                        slot = slot,
                                                        isPrimary = true,
                                                    ),
                                                )
                                                profile = vendorProfileGateway?.let {
                                                    VendorMobileProfileScreenContract.from(VendorLoadProfileUseCase(it).invoke(vendorId))
                                                }
                                                mediaSlot = null
                                            } catch (exception: Exception) {
                                                mediaError = exception.message ?: "Unable to select profile image."
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 1.dp,
                            ) {
                                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    AsyncImage(
                                        model = item.downloadUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Text("Attachment ${item.attachmentId}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorProfileMediaHeader(
    profile: VendorMobileProfileScreenContract,
    onEdit: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
        AsyncImage(
            model = profile.coverUrl,
            contentDescription = "Vendor cover",
            modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
        if (profile.canEditProfileMedia) {
            IconButton(
                onClick = { onEdit("cover") },
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = .82f), CircleShape),
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = "Change cover")
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp).size(112.dp),
        ) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = "Vendor avatar",
                modifier = Modifier.matchParentSize().clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            if (profile.canEditProfileMedia) {
                IconButton(
                    onClick = { onEdit("avatar") },
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = 8.dp).size(36.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = .88f), CircleShape),
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Change avatar", modifier = Modifier.size(18.dp))
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
