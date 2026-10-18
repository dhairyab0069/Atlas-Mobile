package com.atlas.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atlas.core.AtlasStore
import com.atlas.core.Gym
import androidx.compose.foundation.layout.Column

/** Gym check-in and "See Who's Where" (MainActivity in the Java app). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(store: AtlasStore, modifier: Modifier = Modifier) {
    val data by store.data.collectAsState()
    val currentGym = data.checkIns.gymOf(data.userName)
    var selectedGym by remember { mutableStateOf(Gym.UBCO) }
    var pickerOpen by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(false) }

    Column(modifier) {
        TopAppBar(
            title = { Text("Atlas") },
            actions = {
                IconButton(onClick = { editingName = true }) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                }
            },
        )
        LazyColumn {
            item { SectionHeader("Check in") }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentGym != null) {
                        Text("You're at ${currentGym.displayName}", Modifier.weight(1f))
                        OutlinedButton(onClick = { store.checkOut() }) { Text("Check Out") }
                    } else {
                        Box(Modifier.weight(1f)) {
                            OutlinedButton(onClick = { pickerOpen = true }) { Text(selectedGym.displayName) }
                            DropdownMenu(expanded = pickerOpen, onDismissRequest = { pickerOpen = false }) {
                                Gym.entries.forEach { gym ->
                                    DropdownMenuItem(
                                        text = { Text(gym.displayName) },
                                        onClick = { selectedGym = gym; pickerOpen = false },
                                    )
                                }
                            }
                        }
                        Button(onClick = { store.checkIn(selectedGym) }) { Text("Check In") }
                    }
                }
            }
            item { SectionHeader("See who's where") }
            items(Gym.entries) { gym ->
                val people = data.checkIns.peopleAt(gym)
                ListItem(
                    headlineContent = { Text(gym.displayName) },
                    supportingContent = { Text(if (people.isEmpty()) "Nobody yet" else people.joinToString(", ")) },
                )
            }
        }
    }

    if (editingName) {
        FormDialog(
            title = "Your name",
            fields = listOf(FieldSpec("Name", data.userName)),
            onDismiss = { editingName = false },
            onConfirm = { (name) -> store.setUserName(name); true },
        )
    }
}
