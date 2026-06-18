package com.smartresponsor.mobile
import android.os.Bundle; import androidx.activity.ComponentActivity; import androidx.activity.compose.setContent
import androidx.compose.material3.*; import androidx.compose.runtime.*; import androidx.compose.foundation.layout.*; import androidx.compose.ui.Modifier; import androidx.compose.ui.unit.dp
import androidx.work.*; import java.util.concurrent.TimeUnit
import com.smartresponsor.core.config.Config; import com.smartresponsor.core.entitlement.Entitlement; import com.smartresponsor.core.analytic.Analytic
import com.smartresponsor.core.billing.BillingVerifier; import com.smartresponsor.core.push.PushRegistrar; import com.smartresponsor.core.security.RequestSigner
import kotlinx.coroutines.launch
class MainActivity: ComponentActivity(){
  override fun onCreate(b:Bundle?){ super.onCreate(b)
    val cfg = Config(this); val ent = Entitlement(this); val an = Analytic(this); val bill = BillingVerifier(); val push = PushRegistrar(); val signer = RequestSigner("secret")
    // Background: config refresh hourly, analytic flush every 15 min
    WorkManager.getInstance(this).enqueueUniquePeriodicWork("cfg", ExistingPeriodicWorkPolicy.KEEP, PeriodicWorkRequestBuilder<ConfigWorker>(1, TimeUnit.HOURS).build())
    WorkManager.getInstance(this).enqueueUniquePeriodicWork("an", ExistingPeriodicWorkPolicy.KEEP, PeriodicWorkRequestBuilder<AnalyticWorker>(15, TimeUnit.MINUTES).build())
    setContent{
      MaterialTheme{ Surface{
        val scope = rememberCoroutineScope()
        var status by remember { mutableStateOf("-") }
        Column(Modifier.padding(16.dp)){
          Text("Mobile Phases XIV–XVIII — Android")
          Spacer(Modifier.height(8.dp))
          Button(onClick={ scope.launch { cfg.refresh(); ent.grant("smart_reply"); status = if (ent.isEntitled("smart_reply")) "entitled" else "not entitled" } }){ Text("Config+Entitlement") }
          Spacer(Modifier.height(8.dp))
          Row{
            Button(onClick={ scope.launch { status = "receipt="+ bill.uploadReceipt("demo-token","sr.subs.pro").toString() } }){ Text("Upload receipt") }
            Spacer(Modifier.width(8.dp))
            Button(onClick={ scope.launch { status = "verify="+ bill.verify("demo-token").toString() } }){ Text("Verify") }
          }
          Spacer(Modifier.height(8.dp))
          Row{
            Button(onClick={ an.log("opened", mapOf("screen" to "main")); status = "event logged" }){ Text("Log event") }
            Spacer(Modifier.width(8.dp))
            Button(onClick={ status = "flush="+an.flush().toString() }){ Text("Flush analytic") }
          }
          Spacer(Modifier.height(8.dp))
          Row{
            Button(onClick={ status = "push="+push.register("TEST_TOKEN") }){ Text("Register push") }
            Spacer(Modifier.width(8.dp))
            Button(onClick={ val req = okhttp3.Request.Builder().url("https://httpbin.org/anything/mobile/verify").build(); val s = signer.apply(req); status = if (s.header("X-SR-Signature")!=null) "signed" else "no-sign" }){ Text("Sign request") }
          }
          Spacer(Modifier.height(8.dp)); Text("status: "+status)
        }
      } }
    }
  }
}
class ConfigWorker(appContext: android.content.Context, params: WorkerParameters): CoroutineWorker(appContext, params){
  override suspend fun doWork(): Result { return if (Config(applicationContext).refresh()) Result.success() else Result.retry() }
}
class AnalyticWorker(appContext: android.content.Context, params: WorkerParameters): Worker(appContext, params){
  override fun doWork(): Result { return if (com.smartresponsor.core.analytic.Analytic(applicationContext).flush()) Result.success() else Result.retry() }
}
