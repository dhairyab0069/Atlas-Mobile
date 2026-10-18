package com.atlas.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import com.atlas.app.ui.DiaryScreen
import com.atlas.app.ui.FriendsScreen
import com.atlas.app.ui.HomeScreen
import com.atlas.app.ui.JournalScreen
import com.atlas.app.ui.LeaderboardScreen
import com.atlas.core.AtlasStore
import java.io.File

class AtlasViewModel(app: Application) : AndroidViewModel(app) {
    val store = AtlasStore(File(app.filesDir, "atlas.json"))
}

class MainActivity : ComponentActivity() {
    private val viewModel: AtlasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtlasTheme { AtlasApp(viewModel.store) }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    JOURNAL("Journal", Icons.Filled.Book),
    DIARY("Diary", Icons.Filled.Restaurant),
    RANKINGS("Rankings", Icons.Filled.EmojiEvents),
    FRIENDS("Friends", Icons.Filled.Group),
}

@Composable
fun AtlasApp(store: AtlasStore) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach {
                    NavigationBarItem(
                        selected = tab == it,
                        onClick = { tab = it },
                        icon = { Icon(it.icon, contentDescription = null) },
                        label = { Text(it.label) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            Tab.HOME -> HomeScreen(store, modifier)
            Tab.JOURNAL -> JournalScreen(store, modifier)
            Tab.DIARY -> DiaryScreen(store, modifier)
            Tab.RANKINGS -> LeaderboardScreen(modifier)
            Tab.FRIENDS -> FriendsScreen(store, modifier)
        }
    }
}

@Composable
fun AtlasTheme(content: @Composable () -> Unit) {
    // Purple/teal carried over from the original app's colors.xml.
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFFBB86FC), secondary = Color(0xFF03DAC5))
    } else {
        lightColorScheme(primary = Color(0xFF6200EE), secondary = Color(0xFF018786))
    }
    MaterialTheme(colorScheme = colors, content = content)
}
