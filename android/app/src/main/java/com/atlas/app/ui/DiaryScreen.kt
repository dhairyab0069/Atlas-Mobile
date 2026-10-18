package com.atlas.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.core.AtlasStore
import com.atlas.core.CalorieSummary
import com.atlas.core.DiaryItem
import com.atlas.core.Meal

/** Calorie diary (fragment_diary.xml, which the Java app laid out but never wired up). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(store: AtlasStore, modifier: Modifier = Modifier) {
    val data by store.data.collectAsState()
    var addingTo by remember { mutableStateOf<Meal?>(null) }
    var editingGoal by remember { mutableStateOf(false) }

    Column(modifier) {
        TopAppBar(title = { Text("Diary") })
        LazyColumn {
            item { SectionHeader("Calories remaining") }
            item { Summary(CalorieSummary.of(data.calorieGoal, data.diary)) }
            item {
                TextButton(onClick = { editingGoal = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text("Set goal")
                }
            }
            Meal.entries.forEach { meal ->
                item(key = meal.name) { SectionHeader(meal.displayName) }
                items(data.diary.filter { it.meal == meal }, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text("${item.calories} kcal") },
                        trailingContent = {
                            IconButton(onClick = { store.deleteDiaryItems(setOf(item.id)) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        },
                    )
                }
                item(key = "${meal.name}-add") {
                    TextButton(onClick = { addingTo = meal }, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("Add item")
                    }
                }
            }
        }
    }

    if (editingGoal) {
        FormDialog(
            title = "Daily calorie goal",
            fields = listOf(FieldSpec("Goal", data.calorieGoal.toString(), numeric = true)),
            onDismiss = { editingGoal = false },
            onConfirm = { (goal) ->
                val g = goal.trim().toIntOrNull() ?: return@FormDialog false
                store.setCalorieGoal(g)
                true
            },
        )
    }

    addingTo?.let { meal ->
        FormDialog(
            title = "Add to ${meal.displayName}",
            fields = listOf(
                FieldSpec(if (meal == Meal.EXERCISE) "Activity" else "Food name"),
                FieldSpec("Calories", numeric = true),
            ),
            confirmLabel = "Add",
            onDismiss = { addingTo = null },
            onConfirm = { (name, calories) ->
                val c = calories.trim().toIntOrNull()
                if (name.isBlank() || c == null || c < 0) return@FormDialog false
                store.addDiaryItem(DiaryItem(name.trim(), c, meal))
                true
            },
        )
    }
}

@Composable
private fun Summary(s: CalorieSummary) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryColumn(s.goal, "Goal")
        Text("−")
        SummaryColumn(s.food, "Food")
        Text("+")
        SummaryColumn(s.exercise, "Exercise")
        Text("=")
        SummaryColumn(s.remaining, "Remaining", negative = s.remaining < 0)
    }
}

@Composable
private fun SummaryColumn(value: Int, label: String, negative: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (negative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
