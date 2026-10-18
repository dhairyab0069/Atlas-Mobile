package com.atlas.core

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AtlasStoreTest {
    @Test
    fun journalEntriesAreNewestFirstPerExercise() {
        val store = AtlasStore(null)
        val old = JournalEntry(Exercise.SQUAT, sets = 3, reps = 5, maxWeight = 100.0, dateMillis = 0)
        val new = JournalEntry(Exercise.SQUAT, sets = 5, reps = 5, maxWeight = 120.0)
        store.addEntry(old)
        store.addEntry(new)
        store.addEntry(JournalEntry(Exercise.BENCH_PRESS, sets = 3, reps = 8, maxWeight = 80.0))

        assertEquals(listOf(new.id, old.id), store.entries(Exercise.SQUAT).map { it.id })
        assertEquals(120.0, store.personalBest(Exercise.SQUAT))
        assertNull(store.personalBest(Exercise.DEADLIFT))

        store.deleteEntries(setOf(new.id))
        assertEquals(listOf(old.id), store.entries(Exercise.SQUAT).map { it.id })
    }

    @Test
    fun renamingKeepsCheckIn() {
        val store = AtlasStore(null)
        store.checkIn(Gym.ANYTIME_FITNESS)
        store.setUserName("Yegor")
        assertEquals(Gym.ANYTIME_FITNESS, store.currentGym)
        assertEquals(listOf("Yegor"), store.data.value.checkIns.peopleAt(Gym.ANYTIME_FITNESS))
    }

    @Test
    fun persistsAcrossLaunches() {
        val file = File.createTempFile("atlas", ".json").apply { delete(); deleteOnExit() }

        val first = AtlasStore(file)
        first.addEntry(JournalEntry(Exercise.DEADLIFT, sets = 1, reps = 1, maxWeight = 180.0))
        first.setCalorieGoal(2200)
        first.addDiaryItem(DiaryItem("Eggs", 150, Meal.BREAKFAST))
        first.sendFriendRequest("Sahil")
        first.checkIn(Gym.YMCA)

        val second = AtlasStore(file)
        assertEquals(first.data.value, second.data.value)
        assertEquals(Gym.YMCA, second.currentGym)
    }
}
