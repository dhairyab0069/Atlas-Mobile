package com.atlas.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Runs the cases in spec/fixtures.json, the same file the iOS tests read,
 * so both apps are held to identical behaviour.
 */
class SharedSpecTest {
    @Serializable
    private data class Fixtures(
        val sampleData: SampleDataSpec,
        val leaderboard: List<LeaderboardCase>,
        val calorieSummary: List<CalorieCase>,
        val checkIns: List<CheckInCase>,
    )

    @Serializable
    private data class SampleDataSpec(
        val leaderboard: List<LeaderboardEntry>,
        val checkIns: Map<Gym, List<String>>,
        val suggestedFriends: List<String>,
    )

    @Serializable
    private data class LeaderboardCase(
        val name: String,
        val useSampleData: Boolean = false,
        val entries: List<LeaderboardEntry> = emptyList(),
        val expected: List<String>,
    )

    @Serializable
    private data class CalorieCase(
        val name: String,
        val goal: Int,
        val items: List<DiaryItemSpec>,
        val food: Int,
        val exercise: Int,
        val remaining: Int,
    )

    @Serializable
    private data class DiaryItemSpec(val name: String, val calories: Int, val meal: Meal)

    @Serializable
    private data class CheckInCase(
        val name: String,
        val initial: Map<Gym, List<String>>,
        val steps: List<Step>,
        val expected: Map<Gym, List<String>>,
    )

    @Serializable
    private data class Step(val action: String, val person: String, val gym: Gym? = null)

    private val fixtures: Fixtures = Json.decodeFromString(
        File(System.getProperty("atlas.fixtures") ?: "../../spec/fixtures.json").readText()
    )

    @Test
    fun sampleDataMatchesSpec() {
        val spec = fixtures.sampleData
        assertEquals(spec.leaderboard, SampleData.leaderboard)
        assertEquals(spec.suggestedFriends, SampleData.suggestedFriends)
        for (gym in Gym.entries) {
            assertEquals(spec.checkIns[gym].orEmpty(), SampleData.checkIns.peopleAt(gym), "$gym")
        }
    }

    @Test
    fun leaderboard() {
        for (c in fixtures.leaderboard) {
            val entries = if (c.useSampleData) SampleData.leaderboard else c.entries
            assertEquals(c.expected, Leaderboard.ranked(entries).map { it.name }, c.name)
        }
    }

    @Test
    fun calorieSummary() {
        for (c in fixtures.calorieSummary) {
            val summary = CalorieSummary.of(c.goal, c.items.map { DiaryItem(it.name, it.calories, it.meal) })
            assertEquals(c.food, summary.food, c.name)
            assertEquals(c.exercise, summary.exercise, c.name)
            assertEquals(c.remaining, summary.remaining, c.name)
        }
    }

    @Test
    fun checkIns() {
        for (c in fixtures.checkIns) {
            var board = CheckInBoard(c.initial)
            for (step in c.steps) {
                board = when (step.action) {
                    "checkIn" -> board.checkIn(step.person, step.gym!!)
                    "checkOut" -> board.checkOut(step.person)
                    else -> fail("${c.name}: unknown action ${step.action}")
                }
            }
            for (gym in Gym.entries) {
                assertEquals(c.expected[gym].orEmpty(), board.peopleAt(gym), "${c.name} / $gym")
            }
        }
    }
}
