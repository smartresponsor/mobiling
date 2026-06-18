import SwiftUI
import CoreConfig; import CoreEntitlement; import CoreBilling; import CoreAnalytic; import CorePush; import CoreSecurity
struct ContentView: View {
  @State private var status = "-"
  var body: some View {
    VStack(alignment: .leading, spacing: 12){
      Text("Mobile Phases XIV–XVIII — iOS")
      Button("Config+Entitlement"){
        Task { _ = try? await Config().refresh(); await Entitlement().grant(feature: "smart_reply", day: 7);
          let v = await Entitlement().isEntitled(feature: "smart_reply"); status = v ? "entitled" : "not entitled" }
      }
      HStack{
        Button("Upload receipt"){ Task { status = (try? await Billing().uploadReceipt(token: "demo-token", product: "sr.subs.pro")) == true ? "receipt=true" : "receipt=false" } }
        Button("Verify"){ Task { status = (try? await Billing().verify(token: "demo-token")) == true ? "verify=true" : "verify=false" } }
      }
      HStack{
        Button("Log event"){ Analytic().log(event: "opened", prop: ["screen":"main"]); status = "event logged" }
        Button("Flush"){ status = Analytic().flush() ? "flush=true" : "flush=false" }
      }
      HStack{
        Button("Register push"){ status = Push().register(token: "TEST_TOKEN") ? "push=true" : "push=false" }
        Button("Sign request"){ let req = URLRequest(url: URL(string: "https://httpbin.org/anything/mobile/verify")!); let s = Security().applySignature(to: req); status = s.value(forHTTPHeaderField: "X-SR-Signature") != nil ? "signed" : "no-sign" }
      }
      Text("status: \(status)")
    }.padding()
  }
}
