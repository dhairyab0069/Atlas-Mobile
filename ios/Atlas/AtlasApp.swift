import AtlasCore
import SwiftUI

@main
struct AtlasApp: App {
    @State private var store = AtlasStore.live()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(store)
        }
    }
}

struct RootView: View {
    var body: some View {
        TabView {
            Tab("Home", systemImage: "house") { HomeView() }
            Tab("Journal", systemImage: "book") { JournalView() }
            Tab("Diary", systemImage: "fork.knife") { DiaryView() }
            Tab("Rankings", systemImage: "trophy") { LeaderboardView() }
            Tab("Friends", systemImage: "person.2") { FriendsView() }
        }
    }
}

#Preview {
    RootView()
        .environment(AtlasStore(fileURL: nil))
}
