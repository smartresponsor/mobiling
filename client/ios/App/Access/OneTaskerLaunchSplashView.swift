import SwiftUI

public struct OneTaskerLaunchSplashView: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var oneReady: Bool = false
    @State private var wordReady: Bool = false
    private let isVisible: Bool

    public init(isVisible: Bool) {
        self.isVisible = isVisible
    }

    public var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

            HStack(alignment: .center, spacing: 0) {
                Text("1")
                    .font(.system(size: 72, weight: .medium, design: .default))
                    .foregroundStyle(Color(red: 45 / 255, green: 47 / 255, blue: 52 / 255))
                    .opacity(oneReady ? 1 : 0)
                    .scaleEffect(x: oneReady ? 0.72 : 0.66, y: oneReady ? 1.34 : 1.23)

                HStack(alignment: .center, spacing: 0) {
                    Text("tasker")
                        .font(.system(size: 44, weight: .regular, design: .default))
                        .foregroundStyle(Color(red: 45 / 255, green: 47 / 255, blue: 52 / 255))
                        .lineLimit(1)
                        .fixedSize()

                    Text(".com")
                        .font(.system(size: 34, weight: .regular, design: .default))
                        .foregroundStyle(Color(red: 45 / 255, green: 47 / 255, blue: 52 / 255).opacity(0.46))
                        .lineLimit(1)
                        .fixedSize()
                }
                .frame(width: wordReady ? 214 : 0, alignment: .leading)
                .clipped()
                .opacity(wordReady ? 1 : 0)
            }
        }
        .opacity(isVisible ? 1 : 0)
        .accessibilityHidden(true)
        .onAppear { startAnimation() }
    }

    private func startAnimation() {
        if reduceMotion {
            oneReady = true
            wordReady = true
            return
        }

        withAnimation(.easeOut(duration: 0.26)) {
            oneReady = true
        }

        Task { @MainActor in
            try? await Task.sleep(nanoseconds: 480_000_000)
            withAnimation(.easeInOut(duration: 0.62)) {
                wordReady = true
            }
        }
    }
}
