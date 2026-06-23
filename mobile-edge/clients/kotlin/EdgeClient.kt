
package mobile.edge.client
import java.net.HttpURLConnection
import java.net.URL

class EdgeClient(private val base:String, private val bearer:String? = null){
    private fun req(path:String, method:String="GET", body:String?=null, headers: Map<String,String> = emptyMap()): Pair<Int,String>{
        val url = URL(base.trimEnd('/') + path)
        val c = (url.openConnection() as HttpURLConnection)
        c.requestMethod = method
        for ((k,v) in headers) c.setRequestProperty(k, v)
        if (bearer != null) c.setRequestProperty("Authorization", bearer)
        if (body != null){
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.outputStream.use { it.write(body.toByteArray()) }
        }
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val txt = stream?.readBytes()?.toString(Charsets.UTF_8) ?: ""
        c.disconnect()
        return code to txt
    }
    fun config(): Pair<Int,String> = req("/config")
    fun entitlement(): Pair<Int,String> = req("/entitlement")
    fun pushRegister(deviceId:String, token:String, platform:String?=null): Pair<Int,String> {
        val b = "{"deviceId":"$deviceId","token":"$token","platform":"${platform ?: ""}"}"
        return req("/push/register", "POST", b)
    }
    fun receiptVerify(idem:String, transactionId:String, productId:String, platform:String, receipt:String): Pair<Int,String> {
        val b = "{"transactionId":"$transactionId","productId":"$productId","platform":"$platform","receipt":"$receipt"}"
        return req("/receipt/verify", "POST", b, headers = mapOf("Idempotency-Key" to idem))
    }
}
