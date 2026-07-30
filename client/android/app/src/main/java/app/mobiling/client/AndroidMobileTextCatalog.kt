package app.mobiling.client

import android.content.Context

object AndroidMobileTextCatalog {
    fun current(context: Context): Map<String, String> = mapOf(
        MobileTextKey.Dashboard.semanticKey to context.getString(R.string.navigation_dashboard),
        MobileTextKey.Catalog.semanticKey to context.getString(R.string.navigation_catalog),
        MobileTextKey.Message.semanticKey to context.getString(R.string.navigation_message),
        MobileTextKey.Vendor.semanticKey to context.getString(R.string.navigation_vendor),
    )
}
