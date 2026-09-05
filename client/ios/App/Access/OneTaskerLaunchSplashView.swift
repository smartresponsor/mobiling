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
                    .offset(y: -3)

                TimelineView(.animation) { context in
                    HStack(alignment: .center, spacing: 0) {
                        ForEach(Array("tasker.com".enumerated()), id: \.offset) { index, character in
                            let isDomainSuffix = index >= 6
                            let waveOffset = wordWaveOffset(at: index, date: context.date)

                            if index == 8 {
                                Image("Mandala")
                                    .renderingMode(.template)
                                    .resizable()
                                    .scaledToFit()
                                    .foregroundStyle(
                                        Color(red: 45 / 255, green: 47 / 255, blue: 52 / 255)
                                            .opacity(0.46)
                                    )
                                    .frame(width: 25, height: 25)
                                    .offset(y: waveOffset)
                            } else {
                                Text(String(character))
                                    .font(
                                        .system(
                                            size: index == 9 ? 35 : (isDomainSuffix ? 34 : 44),
                                            weight: .regular,
                                            design: .default
                                        )
                                    )
                                    .foregroundStyle(
                                        Color(red: 45 / 255, green: 47 / 255, blue: 52 / 255)
                                            .opacity(isDomainSuffix ? 0.46 : 1)
                                    )
                                    .lineLimit(1)
                                    .fixedSize()
                                    .offset(y: waveOffset)
                            }
                        }
                    }
                    .frame(width: wordReady ? 214 : 0, alignment: .leading)
                    .clipped()
                    .opacity(wordReady ? 1 : 0)
                }
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

    private func wordWaveOffset(at index: Int, date: Date) -> CGFloat {
        guard !reduceMotion, wordReady else {
            return 0
        }

        let phase = date.timeIntervalSinceReferenceDate * (2 * Double.pi / 1.8)
        return CGFloat(sin(phase + Double(index) * 0.55) * 1.15)
    }
}
