package app.mobiling.client

import android.content.Context

object AndroidMobileTextCatalog {
    fun current(context: Context): Map<String, String> = mapOf(
        MobileTextKey.Dashboard.semanticKey to context.getString(R.string.navigation_dashboard),
        MobileTextKey.Catalog.semanticKey to context.getString(R.string.navigation_catalog),
        MobileTextKey.Message.semanticKey to context.getString(R.string.navigation_message),
        MobileTextKey.Notification.semanticKey to context.getString(R.string.navigation_notification),
        MobileTextKey.Tasks.semanticKey to context.getString(R.string.navigation_tasks),
        MobileTextKey.Services.semanticKey to context.getString(R.string.navigation_services),
        MobileTextKey.Profile.semanticKey to context.getString(R.string.navigation_profile),
        MobileTextKey.Vendor.semanticKey to context.getString(R.string.navigation_vendor),
    )
}
