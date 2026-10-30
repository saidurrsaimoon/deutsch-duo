package com.saimoon.ourlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Rose = Color(0xFFE8A0B5)
private val Plum = Color(0xFF241827)
private val CardDark = Color(0xFF35243A)
private val Cream = Color(0xFFFFF5EC)

class NativeHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(36, 24, 39)
        window.navigationBarColor = android.graphics.Color.rgb(36, 24, 39)
        setContent { OurLifeApp() }
    }
}

@Composable
private fun OurLifeApp() {
    var selected by remember { mutableStateOf("Home") }
    var dark by remember { mutableStateOf(true) }
    val bg = if (dark) Plum else Cream
    val card = if (dark) CardDark else Color.White
    val fg = if (dark) Cream else Color(0xFF35243A)
    MaterialTheme(colorScheme = darkColorScheme(primary = Rose, background = bg, surface = card, onBackground = fg, onSurface = fg)) {
        Scaffold(
            containerColor = bg,
            bottomBar = {
                NavigationBar(containerColor = card, contentColor = fg) {
                    listOf("Home" to Icons.Default.Home, "Chat" to Icons.Default.Favorite, "Learn" to Icons.Default.MenuBook, "Together" to Icons.Default.Event, "Profile" to Icons.Default.Person).forEach { (label, icon) ->
                        NavigationBarItem(selected = selected == label, onClick = { selected = label }, icon = { Icon(icon, contentDescription = label) }, label = { Text(label, fontSize = 10.sp) })
                    }
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("OUR LITTLE LIFE", color = Rose, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text(when (selected) { "Home" -> "A little world for two ♡"; "Chat" -> "Just us, always."; "Learn" -> "Learn German together"; "Together" -> "Our memories & plans"; else -> "Your little corner" }, color = fg, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { dark = !dark }) { Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle theme", tint = Rose) }
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(26.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Saimoon  ♡  Nadiya", color = fg, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("Two hearts, one little journey.", color = fg.copy(alpha = .72f), fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = { selected = "Learn" }, label = { Text("German A1") })
                                AssistChip(onClick = { selected = "Together" }, label = { Text("Our plans") })
                            }
                        }
                    }
                }
                item { Text(if (selected == "Home") "Today, together" else selected, color = fg, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                when (selected) {
                    "Home" -> {
                        item { FeatureCard("Daily routine", "Meals • Prayer • College • Self care", "Keep our little habits going", card, fg) }
                        item { FeatureCard("German learning", "A1 vocabulary, grammar & weekly tasks", "Your existing study plan stays unchanged", card, fg) }
                        item { FeatureCard("Our next meet", "Save a date, time and place together", "Every memory belongs to both of you", card, fg) }
                    }
                    "Chat" -> item { FeatureCard("Private chat", "Native chat interface is being prepared", "Firebase connection is the next step", card, fg) }
                    "Learn" -> item { FeatureCard("Deutsch Duo", "Vocabulary • Articles • Plurals • Grammar", "The original A1 routine will be preserved", card, fg) }
                    "Together" -> item { FeatureCard("Meet & memories", "Plan a meeting or save a past memory", "Shared with both accounts", card, fg) }
                    else -> item { FeatureCard("Saimoon & Nadiya", "Two profiles • Shared progress", "Profile statistics will connect to Firestore", card, fg) }
                }
                item { Text("Native Android preview • Version 1.1", color = fg.copy(alpha = .5f), fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun FeatureCard(title: String, subtitle: String, note: String, card: Color, fg: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = fg, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = fg.copy(alpha = .82f), fontSize = 13.sp)
            Text(note, color = Rose, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
