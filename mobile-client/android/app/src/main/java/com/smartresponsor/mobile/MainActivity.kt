package com.smartresponsor.mobile
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartresponsor.mobile.app.dashboard.DashboardRouteMap
import com.smartresponsor.mobile.app.order.OrderOwnedRouteMap
import com.smartresponsor.mobile.app.vendor.VendorOwnedRouteMap
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { MaterializationScreen() } } }
    }
}
@Composable private fun MaterializationScreen() {
    val dashboard = DashboardRouteMap(); val vendor = VendorOwnedRouteMap(); val order = OrderOwnedRouteMap()
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SmartResponsor Mobiling", style = MaterialTheme.typography.headlineSmall)
        Text("Repository materialization baseline")
        Text("Dashboard: ${dashboard.primarySections().joinToString()}")
        Text("Entry flow: ${dashboard.entryFlows().joinToString()}")
        Text("Vendor owns: ${vendor.ownedFlows().joinToString()}")
        Text("Order owns: ${order.ownedFlows().joinToString()}")
        Text("Order embeds: ${order.embeddedCapabilities().joinToString()}")
    }
}
