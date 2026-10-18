import AtlasCore
import SwiftUI

struct FriendsView: View {
    @Environment(AtlasStore.self) private var store
    @State private var search = ""

    private var matches: [String] {
        let query = search.trimmingCharacters(in: .whitespaces)
        guard !query.isEmpty else { return SampleData.suggestedFriends }
        return SampleData.suggestedFriends.filter { $0.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            List {
                Section("Contacts using Atlas") {
                    ForEach(matches, id: \.self) { name in
                        HStack {
                            Text(name)
                            Spacer()
                            if store.hasSentRequest(to: name) {
                                Label("Sent", systemImage: "checkmark")
                                    .foregroundStyle(.secondary)
                            } else {
                                Button("Add", systemImage: "person.badge.plus") {
                                    store.sendFriendRequest(to: name)
                                }
                                .buttonStyle(.borderless)
                            }
                        }
                    }
                }
            }
            .searchable(text: $search)
            .navigationTitle("Add Friends")
        }
    }
}

#Preview {
    FriendsView().environment(AtlasStore(fileURL: nil))
}
