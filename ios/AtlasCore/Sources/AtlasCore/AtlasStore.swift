import Foundation
import Observation

/// Everything the app persists, saved as one JSON file.
public struct AtlasData: Codable, Equatable, Sendable {
    public var userName: String
    public var journal: [JournalEntry]
    public var diary: [DiaryItem]
    public var calorieGoal: Int
    public var checkIns: CheckInBoard
    public var sentFriendRequests: Set<String>

    public init(
        userName: String = "You",
        journal: [JournalEntry] = [],
        diary: [DiaryItem] = [],
        calorieGoal: Int = 0,
        checkIns: CheckInBoard = SampleData.checkIns,
        sentFriendRequests: Set<String> = []
    ) {
        self.userName = userName
        self.journal = journal
        self.diary = diary
        self.calorieGoal = calorieGoal
        self.checkIns = checkIns
        self.sentFriendRequests = sentFriendRequests
    }
}

/// Placeholder social data carried over from the Android prototype, which had no backend.
public enum SampleData {
    public static let checkIns = CheckInBoard(people: [
        .ubco: ["Sahil", "Dhairya"],
        .worldGym: ["Mehul", "Prithvi"],
        .ymca: ["Alex"],
    ])

    public static let leaderboard: [LeaderboardEntry] = [
        .init(name: "Dhairya", score: 97),
        .init(name: "Mehul", score: 86),
        .init(name: "Sahil", score: 99),
        .init(name: "Prithvi", score: 79),
        .init(name: "Yegor", score: 80),
        .init(name: "Andrew", score: 100),
        .init(name: "Shaurya", score: 94),
    ]

    public static let suggestedFriends = ["Dhairya", "Sahil", "Yegor"]
}

@MainActor
@Observable
public final class AtlasStore {
    public private(set) var data: AtlasData {
        didSet { save() }
    }

    private let fileURL: URL?

    /// - Parameter fileURL: where to persist; `nil` keeps everything in memory (previews, tests).
    public init(fileURL: URL?) {
        self.fileURL = fileURL
        if let fileURL,
           let bytes = try? Data(contentsOf: fileURL),
           let decoded = try? JSONDecoder().decode(AtlasData.self, from: bytes) {
            data = decoded
        } else {
            data = AtlasData()
        }
    }

    public static func live() -> AtlasStore {
        let dir = URL.applicationSupportDirectory
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        return AtlasStore(fileURL: dir.appending(path: "atlas.json"))
    }

    private func save() {
        guard let fileURL else { return }
        do {
            let encoder = JSONEncoder()
            encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
            try encoder.encode(data).write(to: fileURL, options: .atomic)
        } catch {
            print("Atlas: failed to save \(fileURL.path): \(error)")
        }
    }

    // MARK: Profile

    public func setUserName(_ name: String) {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty, trimmed != data.userName else { return }
        let gym = data.checkIns.gym(of: data.userName)
        data.checkIns.checkOut(data.userName)
        if let gym { data.checkIns.checkIn(trimmed, at: gym) }
        data.userName = trimmed
    }

    // MARK: Check-in

    public var currentGym: Gym? { data.checkIns.gym(of: data.userName) }

    public func checkIn(at gym: Gym) {
        data.checkIns.checkIn(data.userName, at: gym)
    }

    public func checkOut() {
        data.checkIns.checkOut(data.userName)
    }

    // MARK: Journal

    /// Entries for one exercise, newest first.
    public func entries(for exercise: Exercise) -> [JournalEntry] {
        data.journal
            .filter { $0.exercise == exercise }
            .sorted { $0.date > $1.date }
    }

    public func personalBest(for exercise: Exercise) -> Double? {
        entries(for: exercise).map(\.maxWeight).max()
    }

    public func addEntry(_ entry: JournalEntry) {
        data.journal.append(entry)
    }

    public func deleteEntries(ids: Set<UUID>) {
        data.journal.removeAll { ids.contains($0.id) }
    }

    // MARK: Calorie diary

    public var calorieSummary: CalorieSummary {
        CalorieSummary(goal: data.calorieGoal, items: data.diary)
    }

    public func items(for meal: Meal) -> [DiaryItem] {
        data.diary.filter { $0.meal == meal }
    }

    public func setCalorieGoal(_ goal: Int) {
        data.calorieGoal = max(0, goal)
    }

    public func addDiaryItem(_ item: DiaryItem) {
        data.diary.append(item)
    }

    public func deleteDiaryItems(ids: Set<UUID>) {
        data.diary.removeAll { ids.contains($0.id) }
    }

    // MARK: Friends

    public func sendFriendRequest(to name: String) {
        data.sentFriendRequests.insert(name)
    }

    public func hasSentRequest(to name: String) -> Bool {
        data.sentFriendRequests.contains(name)
    }
}
