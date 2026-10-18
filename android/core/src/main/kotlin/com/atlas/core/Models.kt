package com.atlas.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/** The four lifts the original Android app kept a journal for. */
@Serializable
enum class Exercise(val displayName: String) {
    @SerialName("benchPress") BENCH_PRESS("Bench Press"),
    @SerialName("squat") SQUAT("Squat"),
    @SerialName("deadlift") DEADLIFT("Deadlift"),
    @SerialName("pullUps") PULL_UPS("Pull-ups"),
}

@Serializable
data class JournalEntry(
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    /** One-rep max weight, in the user's unit of choice. */
    val maxWeight: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString(),
)

@Serializable
enum class Gym(val displayName: String) {
    @SerialName("ubco") UBCO("UBCO Gym"),
    @SerialName("anytimeFitness") ANYTIME_FITNESS("Anytime Fitness"),
    @SerialName("worldGym") WORLD_GYM("World Gym"),
    @SerialName("ymca") YMCA("YMCA"),
}

@Serializable
enum class Meal(val displayName: String) {
    @SerialName("breakfast") BREAKFAST("Breakfast"),
    @SerialName("lunch") LUNCH("Lunch"),
    @SerialName("dinner") DINNER("Dinner"),
    @SerialName("exercise") EXERCISE("Exercise"),
}

@Serializable
data class DiaryItem(
    val name: String,
    val calories: Int,
    val meal: Meal,
    val id: String = UUID.randomUUID().toString(),
)

/** "Calories Remaining" card: goal - food + exercise = remaining. */
data class CalorieSummary(val goal: Int, val food: Int, val exercise: Int) {
    val remaining: Int get() = goal - food + exercise

    companion object {
        fun of(goal: Int, items: List<DiaryItem>) = CalorieSummary(
            goal = goal,
            food = items.filter { it.meal != Meal.EXERCISE }.sumOf { it.calories },
            exercise = items.filter { it.meal == Meal.EXERCISE }.sumOf { it.calories },
        )
    }
}

@Serializable
data class LeaderboardEntry(val name: String, val score: Int)

object Leaderboard {
    /** Highest score first; ties broken alphabetically so the order is stable. */
    fun ranked(entries: List<LeaderboardEntry>): List<LeaderboardEntry> =
        entries.sortedWith(compareByDescending<LeaderboardEntry> { it.score }.thenBy { it.name })
}

/** Who is checked in at which gym ("See Who's Where"). */
@Serializable
data class CheckInBoard(val people: Map<Gym, List<String>> = emptyMap()) {
    fun peopleAt(gym: Gym): List<String> = people[gym].orEmpty()

    fun gymOf(name: String): Gym? = Gym.entries.firstOrNull { name in peopleAt(it) }

    /** Checks [name] in at [gym], moving them out of any other gym first. */
    fun checkIn(name: String, gym: Gym): CheckInBoard {
        val out = checkOut(name)
        return CheckInBoard(out.people + (gym to out.peopleAt(gym) + name))
    }

    fun checkOut(name: String): CheckInBoard =
        CheckInBoard(people.mapValues { (_, names) -> names.filter { it != name } })
}
