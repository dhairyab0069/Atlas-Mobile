package com.atlas.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.core.AtlasStore
import com.atlas.core.Leaderboard
import com.atlas.core.SampleData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(modifier: Modifier = Modifier) {
    val ranked = Leaderboard.ranked(SampleData.leaderboard)
    Column(modifier) {
        TopAppBar(title = { Text("Rankings") })
        LazyColumn {
            itemsIndexed(ranked, key = { _, e -> e.name }) { index, entry ->
                ListItem(
                    leadingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(28.dp))
                            if (index == 0) Icon(Icons.Filled.WorkspacePremium, contentDescription = "First place", tint = Color(0xFFFFC107))
                        }
                    },
                    headlineContent = {
                        Text(entry.name, fontWeight = if (index < 3) FontWeight.SemiBold else FontWeight.Normal)
                    },
                    trailingContent = { Text("${entry.score} / 100") },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(store: AtlasStore, modifier: Modifier = Modifier) {
    val data by store.data.collectAsState()
    var search by rememberSaveable { mutableStateOf("") }
    val query = search.trim()
    val matches = SampleData.suggestedFriends.filter { query.isEmpty() || it.contains(query, ignoreCase = true) }

    Column(modifier) {
        TopAppBar(title = { Text("Add Friends") })
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        SectionHeader("Contacts using Atlas")
        LazyColumn {
            items(matches, key = { it }) { name ->
                val sent = name in data.sentFriendRequests
                ListItem(
                    headlineContent = { Text(name) },
                    trailingContent = {
                        if (sent) {
                            Icon(Icons.Filled.Check, contentDescription = "Request sent")
                        } else {
                            IconButton(onClick = { store.sendFriendRequest(name) }) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = "Add $name")
                            }
                        }
                    },
                )
            }
        }
    }
}
