import Foundation
import Testing
@testable import AtlasCore

/// Runs the cases in spec/fixtures.json, the same file the Android tests read,
/// so both apps are held to identical behaviour.
private struct Fixtures: Decodable {
    struct SampleData: Decodable {
        var leaderboard: [LeaderboardEntry]
        var checkIns: [String: [String]]
        var suggestedFriends: [String]
    }

    struct LeaderboardCase: Decodable, CustomTestStringConvertible {
        var name: String
        var useSampleData: Bool?
        var entries: [LeaderboardEntry]?
        var expected: [String]
        var testDescription: String { name }
    }

    struct CalorieCase: Decodable, CustomTestStringConvertible {
        var name: String
        var goal: Int
        var items: [DiaryItemFixture]
        var food: Int
        var exercise: Int
        var remaining: Int
        var testDescription: String { name }
    }

    struct DiaryItemFixture: Decodable {
        var name: String
        var calories: Int
        var meal: Meal
    }

    struct CheckInCase: Decodable, CustomTestStringConvertible {
        struct Step: Decodable {
            var action: String
            var person: String
            var gym: Gym?
        }
        var name: String
        var initial: [String: [String]]
        var steps: [Step]
        var expected: [String: [String]]
        var testDescription: String { name }
    }

    var sampleData: SampleData
    var leaderboard: [LeaderboardCase]
    var calorieSummary: [CalorieCase]
    var checkIns: [CheckInCase]

    static let shared: Fixtures = {
        // <repo>/ios/AtlasCore/Tests/AtlasCoreTests/SharedSpecTests.swift -> <repo>
        var url = URL(filePath: #filePath)
        for _ in 0..<5 { url.deleteLastPathComponent() }
        let file = url.appending(path: "spec/fixtures.json")
        return try! JSONDecoder().decode(Fixtures.self, from: Data(contentsOf: file))
    }()
}

private func board(_ raw: [String: [String]]) -> CheckInBoard {
    CheckInBoard(people: Dictionary(uniqueKeysWithValues: raw.map { (Gym(rawValue: $0.key)!, $0.value) }))
}

@Suite struct SharedSpecTests {
    @Test func sampleDataMatchesSpec() {
        let spec = Fixtures.shared.sampleData
        #expect(SampleData.leaderboard == spec.leaderboard)
        #expect(SampleData.suggestedFriends == spec.suggestedFriends)
        let expected = board(spec.checkIns)
        for gym in Gym.allCases {
            #expect(SampleData.checkIns.people(at: gym) == expected.people(at: gym))
        }
    }

    @Test(arguments: Fixtures.shared.leaderboard)
    fileprivate func leaderboard(_ c: Fixtures.LeaderboardCase) {
        let entries = c.useSampleData == true ? SampleData.leaderboard : c.entries ?? []
        #expect(Leaderboard.ranked(entries).map(\.name) == c.expected)
    }

    @Test(arguments: Fixtures.shared.calorieSummary)
    fileprivate func calorieSummary(_ c: Fixtures.CalorieCase) {
        let items = c.items.map { DiaryItem(name: $0.name, calories: $0.calories, meal: $0.meal) }
        let summary = CalorieSummary(goal: c.goal, items: items)
        #expect(summary.food == c.food)
        #expect(summary.exercise == c.exercise)
        #expect(summary.remaining == c.remaining)
    }

    @Test(arguments: Fixtures.shared.checkIns)
    fileprivate func checkIns(_ c: Fixtures.CheckInCase) {
        var b = board(c.initial)
        for step in c.steps {
            switch step.action {
            case "checkIn": b.checkIn(step.person, at: step.gym!)
            case "checkOut": b.checkOut(step.person)
            default: Issue.record("unknown action \(step.action)")
            }
        }
        let expected = board(c.expected)
        for gym in Gym.allCases {
            #expect(b.people(at: gym) == expected.people(at: gym), "\(gym)")
        }
    }
}
