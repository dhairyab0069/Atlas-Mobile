import AtlasCore
import SwiftUI

/// Gym check-in and "See Who's Where" (MainActivity on Android).
struct HomeView: View {
    @Environment(AtlasStore.self) private var store
    @State private var selectedGym: Gym = .ubco
    @State private var editingName = false
    @State private var nameDraft = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Check in") {
                    if let gym = store.currentGym {
                        LabeledContent("You're at", value: gym.displayName)
                        Button("Check Out", role: .destructive) { store.checkOut() }
                    } else {
                        Picker("Gym", selection: $selectedGym) {
                            ForEach(Gym.allCases) { Text($0.displayName).tag($0) }
                        }
                        Button("Check In") { store.checkIn(at: selectedGym) }
                    }
                }

                Section("See who's where") {
                    ForEach(Gym.allCases) { gym in
                        let people = store.data.checkIns.people(at: gym)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(gym.displayName).font(.headline)
                            Text(people.isEmpty ? "Nobody yet" : people.joined(separator: ", "))
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Atlas")
            .toolbar {
                Button("Profile", systemImage: "person.crop.circle") {
                    nameDraft = store.data.userName
                    editingName = true
                }
            }
            .alert("Your name", isPresented: $editingName) {
                TextField("Name", text: $nameDraft)
                Button("Save") { store.setUserName(nameDraft) }
                Button("Cancel", role: .cancel) {}
            }
        }
    }
}

#Preview {
    HomeView().environment(AtlasStore(fileURL: nil))
}
