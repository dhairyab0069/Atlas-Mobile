import Foundation
import Testing
@testable import AtlasCore

@MainActor
@Suite struct AtlasStoreTests {
    @Test func journalEntriesAreNewestFirstPerExercise() {
        let store = AtlasStore(fileURL: nil)
        let old = JournalEntry(exercise: .squat, sets: 3, reps: 5, maxWeight: 100, date: .distantPast)
        let new = JournalEntry(exercise: .squat, sets: 5, reps: 5, maxWeight: 120)
        store.addEntry(old)
        store.addEntry(new)
        store.addEntry(JournalEntry(exercise: .benchPress, sets: 3, reps: 8, maxWeight: 80))

        #expect(store.entries(for: .squat).map(\.id) == [new.id, old.id])
        #expect(store.personalBest(for: .squat) == 120)
        #expect(store.personalBest(for: .deadlift) == nil)

        store.deleteEntries(ids: [new.id])
        #expect(store.entries(for: .squat).map(\.id) == [old.id])
    }

    @Test func renamingKeepsCheckIn() {
        let store = AtlasStore(fileURL: nil)
        store.checkIn(at: .anytimeFitness)
        store.setUserName("Yegor")
        #expect(store.currentGym == .anytimeFitness)
        #expect(store.data.checkIns.people(at: .anytimeFitness) == ["Yegor"])
    }

    @Test func persistsAcrossLaunches() throws {
        let url = FileManager.default.temporaryDirectory.appending(path: "atlas-\(UUID()).json")
        defer { try? FileManager.default.removeItem(at: url) }

        let first = AtlasStore(fileURL: url)
        first.addEntry(JournalEntry(exercise: .deadlift, sets: 1, reps: 1, maxWeight: 180))
        first.setCalorieGoal(2200)
        first.addDiaryItem(.init(name: "Eggs", calories: 150, meal: .breakfast))
        first.sendFriendRequest(to: "Sahil")
        first.checkIn(at: .ymca)

        let second = AtlasStore(fileURL: url)
        #expect(second.data == first.data)
        #expect(second.currentGym == .ymca)
    }
}
