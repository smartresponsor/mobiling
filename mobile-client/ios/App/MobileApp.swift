import SwiftUI
import BackgroundTasks
import CoreConfig; import CoreEntitlement; import CoreBilling; import CoreAnalytic; import CorePush; import CoreSecurity
@main struct MobileApp: App {
  init(){
    BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.smartresponsor.mobile.refresh", using: nil){ task in
      Task { _ = try? await Config().refresh(); task.setTaskCompleted(success: true) }
    }
    BGTaskScheduler.shared.register(forTaskWithIdentifier: "dev.smartresponsor.mobile.flush", using: nil){ task in
      let ok = Analytic().flush(); task.setTaskCompleted(success: ok)
    }
  }
  var body: some Scene { WindowGroup { ContentView() } }
}
