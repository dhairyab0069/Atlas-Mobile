import Foundation

/// The four lifts the original Android app kept a journal for.
public enum Exercise: String, Codable, CaseIterable, Identifiable, Sendable {
    case benchPress
    case squat
    case deadlift
    case pullUps

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .benchPress: "Bench Press"
        case .squat: "Squat"
        case .deadlift: "Deadlift"
        case .pullUps: "Pull-ups"
        }
    }

    /// Name of the photo in the app's asset catalog.
    public var imageName: String {
        switch self {
        case .benchPress: "bench"
        case .squat: "squat"
        case .deadlift: "deadlift"
        case .pullUps: "pullup"
        }
    }
}

public struct JournalEntry: Codable, Identifiable, Hashable, Sendable {
    public var id: UUID
    public var exercise: Exercise
    public var sets: Int
    public var reps: Int
    /// One-rep max weight, in the user's unit of choice.
    public var maxWeight: Double
    public var date: Date

    public init(id: UUID = UUID(), exercise: Exercise, sets: Int, reps: Int, maxWeight: Double, date: Date = .now) {
        self.id = id
        self.exercise = exercise
        self.sets = sets
        self.reps = reps
        self.maxWeight = maxWeight
        self.date = date
    }
}

public enum Gym: String, Codable, CaseIterable, Identifiable, Sendable {
    case ubco
    case anytimeFitness
    case worldGym
    case ymca

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .ubco: "UBCO Gym"
        case .anytimeFitness: "Anytime Fitness"
        case .worldGym: "World Gym"
        case .ymca: "YMCA"
        }
    }
}

public enum Meal: String, Codable, CaseIterable, Identifiable, Sendable {
    case breakfast
    case lunch
    case dinner
    case exercise

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .breakfast: "Breakfast"
        case .lunch: "Lunch"
        case .dinner: "Dinner"
        case .exercise: "Exercise"
        }
    }
}

public struct DiaryItem: Codable, Identifiable, Hashable, Sendable {
    public var id: UUID
    public var name: String
    public var calories: Int
    public var meal: Meal

    public init(id: UUID = UUID(), name: String, calories: Int, meal: Meal) {
        self.id = id
        self.name = name
        self.calories = calories
        self.meal = meal
    }
}

/// "Calories Remaining" card: goal - food + exercise = remaining.
public struct CalorieSummary: Equatable, Sendable {
    public var goal: Int
    public var food: Int
    public var exercise: Int

    public var remaining: Int { goal - food + exercise }

    public init(goal: Int, items: [DiaryItem]) {
        self.goal = goal
        self.food = items.filter { $0.meal != .exercise }.reduce(0) { $0 + $1.calories }
        self.exercise = items.filter { $0.meal == .exercise }.reduce(0) { $0 + $1.calories }
    }
}

public struct LeaderboardEntry: Codable, Identifiable, Hashable, Sendable {
    public var name: String
    public var score: Int

    public var id: String { name }

    public init(name: String, score: Int) {
        self.name = name
        self.score = score
    }
}

public enum Leaderboard {
    /// Highest score first; ties broken alphabetically so the order is stable.
    public static func ranked(_ entries: [LeaderboardEntry]) -> [LeaderboardEntry] {
        entries.sorted { a, b in
            a.score != b.score ? a.score > b.score : a.name < b.name
        }
    }
}

/// Who is checked in at which gym ("See Who's Where").
public struct CheckInBoard: Codable, Equatable, Sendable {
    public private(set) var people: [Gym: [String]]

    public init(people: [Gym: [String]] = [:]) {
        self.people = people
    }

    public func people(at gym: Gym) -> [String] {
        people[gym] ?? []
    }

    public func gym(of name: String) -> Gym? {
        Gym.allCases.first { people(at: $0).contains(name) }
    }

    /// Checks `name` in at `gym`, moving them out of any other gym first.
    public mutating func checkIn(_ name: String, at gym: Gym) {
        checkOut(name)
        people[gym, default: []].append(name)
    }

    public mutating func checkOut(_ name: String) {
        for gym in Gym.allCases {
            people[gym]?.removeAll { $0 == name }
        }
    }
}
