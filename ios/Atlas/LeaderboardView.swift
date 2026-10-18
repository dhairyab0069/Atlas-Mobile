import AtlasCore
import SwiftUI

struct LeaderboardView: View {
    private let ranked = Leaderboard.ranked(SampleData.leaderboard)

    var body: some View {
        NavigationStack {
            List(Array(ranked.enumerated()), id: \.element.id) { index, entry in
                HStack {
                    Text("\(index + 1)")
                        .font(.headline.monospacedDigit())
                        .frame(width: 28)
                    if index == 0 {
                        Image(systemName: "crown.fill").foregroundStyle(.yellow)
                    }
                    Text(entry.name).fontWeight(index < 3 ? .semibold : .regular)
                    Spacer()
                    Text("\(entry.score) / 100")
                        .monospacedDigit()
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Rankings")
        }
    }
}

#Preview {
    LeaderboardView()
}
