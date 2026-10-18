import AtlasCore
import SwiftUI

/// Exercise picker (GymJournal) leading to one journal per lift
/// (benchJournal/squatJournal/... and their *AddJ screens).
struct JournalView: View {
    @Environment(AtlasStore.self) private var store

    var body: some View {
        NavigationStack {
            List(Exercise.allCases) { exercise in
                NavigationLink(value: exercise) {
                    HStack(spacing: 16) {
                        Image(exercise.imageName)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 64, height: 64)
                            .clipShape(.rect(cornerRadius: 12))
                        VStack(alignment: .leading) {
                            Text(exercise.displayName).font(.headline)
                            Text(subtitle(for: exercise))
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Gym Journal")
            .navigationDestination(for: Exercise.self) { ExerciseJournalView(exercise: $0) }
        }
    }

    private func subtitle(for exercise: Exercise) -> String {
        let count = store.entries(for: exercise).count
        guard let best = store.personalBest(for: exercise) else { return "No entries yet" }
        return "\(count) \(count == 1 ? "entry" : "entries") · best \(best.formatted())"
    }
}

struct ExerciseJournalView: View {
    let exercise: Exercise
    @Environment(AtlasStore.self) private var store
    @State private var adding = false

    var body: some View {
        let entries = store.entries(for: exercise)
        List {
            if entries.isEmpty {
                ContentUnavailableView(
                    "No \(exercise.displayName) entries",
                    systemImage: "dumbbell",
                    description: Text("Tap + to log your sets, reps and 1-rep max.")
                )
            }
            ForEach(entries) { entry in
                VStack(alignment: .leading, spacing: 4) {
                    Text(entry.date, format: .dateTime.day().month().year().hour().minute())
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    HStack {
                        stat("Sets", "\(entry.sets)")
                        stat("Reps", "\(entry.reps)")
                        stat("1RM", entry.maxWeight.formatted())
                    }
                }
            }
            .onDelete { offsets in
                store.deleteEntries(ids: Set(offsets.map { entries[$0].id }))
            }
        }
        .navigationTitle(exercise.displayName)
        .toolbar {
            Button("Add to journal", systemImage: "plus") { adding = true }
        }
        .sheet(isPresented: $adding) {
            AddEntryView(exercise: exercise)
        }
    }

    private func stat(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading) {
            Text(label).font(.caption2).foregroundStyle(.secondary)
            Text(value).font(.title3.monospacedDigit())
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct AddEntryView: View {
    let exercise: Exercise
    @Environment(AtlasStore.self) private var store
    @Environment(\.dismiss) private var dismiss
    @State private var sets = 3
    @State private var reps = 8
    @State private var maxWeight: Double?
    @State private var date = Date.now

    var body: some View {
        NavigationStack {
            Form {
                Stepper("Sets: \(sets)", value: $sets, in: 1...50)
                Stepper("Reps: \(reps)", value: $reps, in: 1...100)
                TextField("1-rep max weight", value: $maxWeight, format: .number)
                    .keyboardType(.decimalPad)
                DatePicker("Date", selection: $date)
            }
            .navigationTitle("Add \(exercise.displayName)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        guard let maxWeight else { return }
                        store.addEntry(JournalEntry(exercise: exercise, sets: sets, reps: reps, maxWeight: maxWeight, date: date))
                        dismiss()
                    }
                    .disabled((maxWeight ?? -1) < 0)
                }
            }
        }
    }
}

#Preview {
    JournalView().environment(AtlasStore(fileURL: nil))
}
