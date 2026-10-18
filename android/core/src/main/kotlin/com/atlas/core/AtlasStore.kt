package com.atlas.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** Everything the app persists, saved as one JSON file. */
@Serializable
data class AtlasData(
    val userName: String = "You",
    val journal: List<JournalEntry> = emptyList(),
    val diary: List<DiaryItem> = emptyList(),
    val calorieGoal: Int = 0,
    val checkIns: CheckInBoard = SampleData.checkIns,
    val sentFriendRequests: Set<String> = emptySet(),
)

/** Placeholder social data carried over from the Android prototype, which had no backend. */
object SampleData {
    val checkIns = CheckInBoard(
        mapOf(
            Gym.UBCO to listOf("Sahil", "Dhairya"),
            Gym.WORLD_GYM to listOf("Mehul", "Prithvi"),
            Gym.YMCA to listOf("Alex"),
        )
    )

    val leaderboard = listOf(
        LeaderboardEntry("Dhairya", 97),
        LeaderboardEntry("Mehul", 86),
        LeaderboardEntry("Sahil", 99),
        LeaderboardEntry("Prithvi", 79),
        LeaderboardEntry("Yegor", 80),
        LeaderboardEntry("Andrew", 100),
        LeaderboardEntry("Shaurya", 94),
    )

    val suggestedFriends = listOf("Dhairya", "Sahil", "Yegor")
}

/**
 * @param file where to persist; `null` keeps everything in memory (previews, tests).
 */
class AtlasStore(private val file: File?) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    private val _data = MutableStateFlow(load())
    val data: StateFlow<AtlasData> = _data.asStateFlow()

    private fun load(): AtlasData =
        file?.takeIf { it.exists() }
            ?.let { runCatching { json.decodeFromString<AtlasData>(it.readText()) }.getOrNull() }
            ?: AtlasData()

    private fun update(transform: (AtlasData) -> AtlasData) {
        val new = transform(_data.value)
        _data.value = new
        file?.let { target ->
            runCatching {
                val tmp = File(target.parentFile, "${target.name}.tmp")
                tmp.writeText(json.encodeToString(AtlasData.serializer(), new))
                tmp.renameTo(target)
            }.onFailure { System.err.println("Atlas: failed to save ${target.path}: $it") }
        }
    }

    // Profile

    fun setUserName(name: String) {
        val trimmed = name.trim()
        val current = _data.value.userName
        if (trimmed.isEmpty() || trimmed == current) return
        update { d ->
            val gym = d.checkIns.gymOf(current)
            var board = d.checkIns.checkOut(current)
            if (gym != null) board = board.checkIn(trimmed, gym)
            d.copy(userName = trimmed, checkIns = board)
        }
    }

    // Check-in

    val currentGym: Gym? get() = _data.value.let { it.checkIns.gymOf(it.userName) }

    fun checkIn(gym: Gym) = update { it.copy(checkIns = it.checkIns.checkIn(it.userName, gym)) }

    fun checkOut() = update { it.copy(checkIns = it.checkIns.checkOut(it.userName)) }

    // Journal

    /** Entries for one exercise, newest first. */
    fun entries(exercise: Exercise): List<JournalEntry> =
        _data.value.journal.filter { it.exercise == exercise }.sortedByDescending { it.dateMillis }

    fun personalBest(exercise: Exercise): Double? = entries(exercise).maxOfOrNull { it.maxWeight }

    fun addEntry(entry: JournalEntry) = update { it.copy(journal = it.journal + entry) }

    fun deleteEntries(ids: Set<String>) = update { d -> d.copy(journal = d.journal.filterNot { it.id in ids }) }

    // Calorie diary

    val calorieSummary: CalorieSummary get() = _data.value.let { CalorieSummary.of(it.calorieGoal, it.diary) }

    fun items(meal: Meal): List<DiaryItem> = _data.value.diary.filter { it.meal == meal }

    fun setCalorieGoal(goal: Int) = update { it.copy(calorieGoal = maxOf(0, goal)) }

    fun addDiaryItem(item: DiaryItem) = update { it.copy(diary = it.diary + item) }

    fun deleteDiaryItems(ids: Set<String>) = update { d -> d.copy(diary = d.diary.filterNot { it.id in ids }) }

    // Friends

    fun sendFriendRequest(name: String) = update { it.copy(sentFriendRequests = it.sentFriendRequests + name) }

    fun hasSentRequest(name: String): Boolean = name in _data.value.sentFriendRequests
}
