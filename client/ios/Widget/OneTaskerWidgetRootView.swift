import SwiftUI
import UIKit
import WidgetClient

private enum WidgetSection: String, CaseIterable, Identifiable {
    case listen = "Listen"
    case needs = "Needs"
    case profile = "Profile"

    var id: Self { self }
}

struct OneTaskerWidgetRootView: View {
    @State private var status = "Checking session…"
    @State private var needs: [NotificationInboxItem] = []
    @State private var callbackRequestedIds: Set<String> = []
    @State private var selectedSection: WidgetSection = .listen
    @State private var authenticated = false
    @State private var login = ""
    @State private var password = ""

    private func configuredURL(_ key: String) -> URL? {
        guard let raw = Bundle.main.object(forInfoDictionaryKey: key) as? String else { return nil }
        return URL(string: raw)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
            Text("OneTasker")
                .font(.title)

            Picker("Section", selection: $selectedSection) {
                ForEach(WidgetSection.allCases) { section in
                    Text(section.rawValue).tag(section)
                }
            }
            .pickerStyle(.segmented)

            switch selectedSection {
            case .listen:
                VStack(spacing: 12) {
                    Text("Describe what you need")
                    Button("Push to talk") {
                        if !authenticated {
                            selectedSection = .profile
                            status = "Sign in before describing a need"
                            return
                        }
                        Task {
                            let graph = WidgetApplicationGraph.current()
                            do {
                                let locale = Locale.current
                                let speech = try await graph.speechRecognition.recognizeOnce(locale: locale)
                                let transcript = speech.transcript.trimmingCharacters(in: .whitespacesAndNewlines)
                                guard !transcript.isEmpty else {
                                    status = "No speech recognized"
                                    return
                                }
                                let recognized = try await graph.needClassifier.classify(transcript: transcript, locale: locale)
                                guard recognized.isNeed else {
                                    status = "No service need recognized"
                                    return
                                }
                                guard let summary = recognized.summary?.trimmingCharacters(in: .whitespacesAndNewlines), !summary.isEmpty,
                                      let category = recognized.category?.trimmingCharacters(in: .whitespacesAndNewlines), !category.isEmpty else {
                                    status = "Need classifier returned incomplete result"
                                    return
                                }
                                let created = try await graph.notificationGateway.recognizedNeed(request: RecognizedNeedRequest(
                                    summary: summary,
                                    category: category,
                                    confidence: recognized.confidence,
                                    locale: locale.identifier,
                                    classifierVersion: recognized.classifierVersion
                                ))
                                guard created else {
                                    status = "Unable to save recognized need"
                                    return
                                }
                                let inbox = try await graph.notificationGateway.inbox()
                                needs = inbox.items.filter { $0.eventName == "need.recognized" && $0.topic == "one_tasker.need" }
                                callbackRequestedIds = Set(needs.filter { $0.status == "acked" }.map(\.id))
                                selectedSection = .needs
                                status = summary
                            } catch {
                                status = "Need classification unavailable"
                            }
                        }
                    }
                    Text("Audio is used for speech recognition and is not stored as a OneTasker business record.")
                        .foregroundStyle(.secondary)
                }

            case .needs:
                VStack(spacing: 12) {
                    if needs.isEmpty {
                        Text("No recognized needs yet")
                            .foregroundStyle(.secondary)
                    }
                    ForEach(needs) { need in
                        VStack(spacing: 8) {
                            Text(need.title)
                            if let category = need.needCategory {
                                Text("Category: \(category)")
                                    .foregroundStyle(.secondary)
                            }
                            if let confidence = need.needConfidence {
                                Text("Confidence: \(Int(min(1, max(0, confidence)) * 100))%")
                                    .foregroundStyle(.secondary)
                            }
                            if let locale = need.needLocale {
                                Text("Language: \(locale)")
                                    .foregroundStyle(.secondary)
                            }
                            if need.status == "new" {
                                Text("New")
                                    .foregroundStyle(.tint)
                            }
                            if need.status == "acked" || callbackRequestedIds.contains(need.id) {
                                Text("Callback requested")
                                    .foregroundStyle(.secondary)
                            } else {
                                Button("Call me") {
                                    Task {
                                        let graph = WidgetApplicationGraph.current()
                                        do {
                                            if try await graph.notificationGateway.requestNeedCallback(recipientEntryId: need.id) {
                                                callbackRequestedIds.insert(need.id)
                                                status = "Callback requested"
                                            } else {
                                                status = "Unable to request callback"
                                            }
                                        } catch {
                                            status = "Unable to request callback"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            case .profile:
                VStack(spacing: 12) {
                    Text(authenticated ? "Account connected" : "Account sign-in required")
                    if !authenticated {
                        TextField("Email", text: $login)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                        SecureField("Password", text: $password)
                        Button("Sign in") {
                            let normalizedLogin = login.trimmingCharacters(in: .whitespacesAndNewlines)
                            guard !normalizedLogin.isEmpty, !password.isEmpty else {
                                status = "Email and password are required"
                                return
                            }
                            Task {
                                let graph = WidgetApplicationGraph.current()
                                do {
                                    let session = try await graph.authGateway.startAuth(
                                        request: StartAuthRequest(
                                            login: normalizedLogin,
                                            password: password,
                                            deviceLabel: "OneTasker Widget"
                                        )
                                    )
                                    password = ""
                                    if session.authenticated {
                                        authenticated = true
                                        let inbox = try await graph.notificationGateway.inbox()
                                        needs = inbox.items.filter { $0.eventName == "need.recognized" && $0.topic == "one_tasker.need" }
                                        callbackRequestedIds = Set(needs.filter { $0.status == "acked" }.map(\.id))
                                        status = "\(needs.count) needs · \(inbox.unreadCount) unread"
                                    } else if session.requiresVerification {
                                        status = "Account verification required"
                                    } else if session.requiresSecondFactor {
                                        status = "Second-factor verification required"
                                    } else {
                                        status = "Unable to sign in"
                                    }
                                } catch {
                                    password = ""
                                    status = "Unable to sign in"
                                }
                            }
                        }
                    }
                    if authenticated {
                        Button("Sign out") {
                            Task {
                                let graph = WidgetApplicationGraph.current()
                                do {
                                    try await graph.authGateway.logoutAuth()
                                    authenticated = false
                                    needs = []
                                    callbackRequestedIds = []
                                    selectedSection = .listen
                                    status = "Signed out"
                                } catch {
                                    status = "Unable to sign out"
                                }
                            }
                        }
                    }
                    Text("Speech mode: native/system recognition; platform services may use network processing.")
                    Text("Need classification may send a bounded transcript fragment to the configured product AI service.")
                    Text("OneTasker does not durably store raw audio or unrestricted transcripts in the recognized-need contract.")
                        .foregroundStyle(.secondary)
                    Text("Language: \(Locale.current.identifier)")
                    if let notificationSettingsURL = URL(string: UIApplication.openSettingsURLString) {
                        Link("Notification settings", destination: notificationSettingsURL)
                    }
                    if let privacyURL = configuredURL("MOBILE_PRIVACY_POLICY_URL") {
                        Link("Privacy Policy", destination: privacyURL)
                    }
                    if let termsURL = configuredURL("MOBILE_TERMS_OF_SERVICE_URL") {
                        Link("Terms of Service", destination: termsURL)
                    }
                    if let aboutURL = configuredURL("MOBILE_ABOUT_URL") {
                        Link("About", destination: aboutURL)
                    }
                }
            }

            Text(status)
                .foregroundStyle(.secondary)
        }
        .padding(24)
        .task {
            let graph = WidgetApplicationGraph.current()
            do {
                let session = try await graph.authGateway.restoreAuth()
                authenticated = session.authenticated
                if session.authenticated {
                    let inbox = try await graph.notificationGateway.inbox()
                    needs = inbox.items.filter { $0.eventName == "need.recognized" && $0.topic == "one_tasker.need" }
                    callbackRequestedIds = Set(needs.filter { $0.status == "acked" }.map(\.id))
                    status = "\(needs.count) needs · \(inbox.unreadCount) unread"
                } else {
                    status = "Sign in to see your needs"
                }
            } catch {
                status = "Offline · tap Listen when ready"
            }
        }
        }
    }
}
