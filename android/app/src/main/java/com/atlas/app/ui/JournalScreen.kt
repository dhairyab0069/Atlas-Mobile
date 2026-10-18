package com.atlas.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.atlas.app.R
import com.atlas.core.AtlasStore
import com.atlas.core.Exercise
import com.atlas.core.JournalEntry
import java.text.DateFormat
import java.util.Date

private val Exercise.imageRes: Int
    get() = when (this) {
        Exercise.BENCH_PRESS -> R.drawable.bench
        Exercise.SQUAT -> R.drawable.squat
        Exercise.DEADLIFT -> R.drawable.deadlift
        Exercise.PULL_UPS -> R.drawable.pullup
    }

private fun Double.pretty(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()

/** Exercise picker (GymJournal) leading to one journal per lift. */
@Composable
fun JournalScreen(store: AtlasStore, modifier: Modifier = Modifier) {
    var selected by rememberSaveable { mutableStateOf<Exercise?>(null) }
    val exercise = selected
    if (exercise == null) {
        ExercisePicker(store, modifier) { selected = it }
    } else {
        BackHandler { selected = null }
        ExerciseJournal(store, exercise, modifier) { selected = null }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePicker(store: AtlasStore, modifier: Modifier, onSelect: (Exercise) -> Unit) {
    // Re-read on every change so subtitles stay current.
    store.data.collectAsState().value
    Column(modifier) {
        TopAppBar(title = { Text("Gym Journal") })
        LazyColumn {
            items(Exercise.entries) { exercise ->
                val entries = store.entries(exercise)
                val best = store.personalBest(exercise)
                ListItem(
                    leadingContent = {
                        Image(
                            painterResource(exercise.imageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                        )
                    },
                    headlineContent = { Text(exercise.displayName) },
                    supportingContent = {
                        Text(
                            if (best == null) "No entries yet"
                            else "${entries.size} ${if (entries.size == 1) "entry" else "entries"} · best ${best.pretty()}"
                        )
                    },
                    modifier = Modifier.clickable { onSelect(exercise) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseJournal(store: AtlasStore, exercise: Exercise, modifier: Modifier, onBack: () -> Unit) {
    store.data.collectAsState().value
    val entries = store.entries(exercise)
    var adding by rememberSaveable { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                title = { Text(exercise.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
            if (entries.isEmpty()) {
                Text(
                    "No ${exercise.displayName} entries yet.\nTap + to log your sets, reps and 1-rep max.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
            }
            LazyColumn {
                items(entries, key = { it.id }) { entry ->
                    EntryRow(entry) { store.deleteEntries(setOf(entry.id)) }
                }
            }
        }
        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Filled.Add, contentDescription = "Add to journal") }
    }

    if (adding) {
        FormDialog(
            title = "Add ${exercise.displayName}",
            fields = listOf(FieldSpec("Sets", "3", numeric = true), FieldSpec("Reps", "8", numeric = true), FieldSpec("1-rep max weight", numeric = true)),
            onDismiss = { adding = false },
            onConfirm = { (sets, reps, max) ->
                val s = sets.trim().toIntOrNull()
                val r = reps.trim().toIntOrNull()
                val m = max.trim().toDoubleOrNull()
                if (s == null || r == null || m == null || s < 1 || r < 1 || m < 0) return@FormDialog false
                store.addEntry(JournalEntry(exercise, sets = s, reps = r, maxWeight = m))
                true
            },
        )
    }
}

@Composable
private fun EntryRow(entry: JournalEntry, onDelete: () -> Unit) {
    ListItem(
        overlineContent = {
            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(entry.dateMillis)))
        },
        headlineContent = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("Sets", entry.sets.toString())
                Stat("Reps", entry.reps.toString())
                Stat("1RM", entry.maxWeight.pretty())
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
        },
    )
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
