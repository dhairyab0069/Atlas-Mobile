import AtlasCore
import SwiftUI

/// Calorie diary (fragment_diary.xml, which the Android app laid out but never wired up).
struct DiaryView: View {
    @Environment(AtlasStore.self) private var store
    @State private var addingTo: Meal?
    @State private var editingGoal = false
    @State private var goalDraft: Int?

    var body: some View {
        NavigationStack {
            List {
                Section("Calories remaining") {
                    summary
                    Button("Set goal") {
                        goalDraft = store.data.calorieGoal
                        editingGoal = true
                    }
                }

                ForEach(Meal.allCases) { meal in
                    let items = store.items(for: meal)
                    Section {
                        ForEach(items) { item in
                            LabeledContent(item.name, value: "\(item.calories) kcal")
                        }
                        .onDelete { offsets in
                            store.deleteDiaryItems(ids: Set(offsets.map { items[$0].id }))
                        }
                        Button("Add item", systemImage: "plus") { addingTo = meal }
                    } header: {
                        Text(meal.displayName)
                    }
                }
            }
            .navigationTitle("Diary")
            .sheet(item: $addingTo) { AddDiaryItemView(meal: $0) }
            .alert("Daily calorie goal", isPresented: $editingGoal) {
                TextField("Goal", value: $goalDraft, format: .number)
                    .keyboardType(.numberPad)
                Button("Save") { store.setCalorieGoal(goalDraft ?? 0) }
                Button("Cancel", role: .cancel) {}
            }
        }
    }

    private var summary: some View {
        let s = store.calorieSummary
        return HStack {
            column("Goal", s.goal)
            Text("−")
            column("Food", s.food)
            Text("+")
            column("Exercise", s.exercise)
            Text("=")
            column("Remaining", s.remaining)
                .foregroundStyle(s.remaining < 0 ? .red : .primary)
        }
        .font(.headline.monospacedDigit())
    }

    private func column(_ label: String, _ value: Int) -> some View {
        VStack {
            Text("\(value)")
            Text(label).font(.caption2).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

struct AddDiaryItemView: View {
    let meal: Meal
    @Environment(AtlasStore.self) private var store
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var calories: Int?

    var body: some View {
        NavigationStack {
            Form {
                TextField(meal == .exercise ? "Activity" : "Food name", text: $name)
                TextField("Calories", value: $calories, format: .number)
                    .keyboardType(.numberPad)
            }
            .navigationTitle("Add to \(meal.displayName)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        guard let calories else { return }
                        store.addDiaryItem(DiaryItem(name: name.trimmingCharacters(in: .whitespaces), calories: calories, meal: meal))
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty || (calories ?? -1) < 0)
                }
            }
        }
    }
}

#Preview {
    DiaryView().environment(AtlasStore(fileURL: nil))
}
