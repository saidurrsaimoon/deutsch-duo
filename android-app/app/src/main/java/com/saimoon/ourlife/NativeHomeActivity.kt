package com.saimoon.ourlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Rose = Color(0xFFFF82B3)
private val DeepRose = Color(0xFFB84579)
private val Plum = Color(0xFF1B0F1F)
private val DarkCard = Color(0xFF291829)
private val Cream = Color(0xFFFFF4F7)
private val Ink = Color(0xFF3D2030)
private val MutedDark = Color(0xFFC4A3B6)
private val MutedLight = Color(0xFF9B7889)

private val tabs = listOf(
    "Home" to Icons.Default.Home,
    "Chat" to Icons.Default.Favorite,
    "Learn" to Icons.Default.MenuBook,
    "Together" to Icons.Default.Event,
    "Profile" to Icons.Default.Person
)

class NativeHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(27, 15, 31)
        window.navigationBarColor = android.graphics.Color.rgb(27, 15, 31)
        setContent { OurLifeApp() }
    }
}

@Composable
private fun OurLifeApp() {
    var selected by remember { mutableStateOf("Home") }
    var theme by remember { mutableStateOf("Dark") }
    val dark = theme == "Dark"
    val bg = if (dark) Plum else Cream
    val card = if (dark) DarkCard else Color.White
    val fg = if (dark) Color(0xFFFFEFF5) else Ink
    val muted = if (dark) MutedDark else MutedLight
    val gradient = Brush.linearGradient(listOf(Rose, DeepRose))

    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(primary = Rose, secondary = DeepRose, background = bg, surface = card, onBackground = fg, onSurface = fg)
        else lightColorScheme(primary = DeepRose, secondary = Rose, background = bg, surface = card, onBackground = fg, onSurface = fg)
    ) {
        Scaffold(
            containerColor = bg,
            bottomBar = {
                NavigationBar(
                    containerColor = card.copy(alpha = .97f),
                    contentColor = fg,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    tabs.forEach { (label, icon) ->
                        NavigationBarItem(
                            selected = selected == label,
                            onClick = { selected = label },
                            icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (selected == label) FontWeight.Bold else FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DeepRose,
                                selectedTextColor = DeepRose,
                                indicatorColor = Rose.copy(alpha = .18f),
                                unselectedIconColor = muted,
                                unselectedTextColor = muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("OUR LITTLE LIFE", color = Rose, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.2.sp)
                            Text(pageTitle(selected), color = fg, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 31.sp)
                        }
                        Surface(shape = CircleShape, color = card, tonalElevation = 2.dp) {
                            IconButton(onClick = { theme = if (dark) "Light" else "Dark" }) {
                                Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Change theme", tint = Rose)
                            }
                        }
                    }
                }
                when (selected) {
                    "Home" -> {
                        item { CoupleHero(card, fg, muted, gradient) }
                        item { SectionHeading("Our little dashboard", "আজকের ছোট ছোট progress", fg, muted) }
                        item { ProgressStrip(card, fg, muted) }
                        item { NativeFeatureCard("Daily routine", "আজকের খাবার, নামাজ, college & self-care", "Shared daily checklist", "checklist", card, fg, muted, gradient) { selected = "Together" } }
                        item { NativeFeatureCard("Deutsch Duo", "German A1 • Vocabulary • Grammar", "Keep the original study routine unchanged", "book", card, fg, muted, gradient) { selected = "Learn" } }
                        item { NativeFeatureCard("Our private chat", "A quiet little place for just us two", "Messages and replies", "chat", card, fg, muted, gradient) { selected = "Chat" } }
                        item { NativeFeatureCard("Meet & memories", "Plan the next meet and save old moments", "Our shared timeline", "heart", card, fg, muted, gradient) { selected = "Together" } }
                    }
                    "Chat" -> {
                        item { SectionHeading("Just us, always. ♡", "Private conversation space", fg, muted) }
                        item { EmptyNativePanel("💌", "Our private chat", "এখানে native chat screen তৈরি হবে—message history, reply, delete ও reactions সহ।", card, fg, muted) }
                    }
                    "Learn" -> {
                        item { SectionHeading("Learn German together", "Deutsch Duo • A1 journey", fg, muted) }
                        item { NativeFeatureCard("Vocabulary", "Nouns • Articles • Plurals • Adjectives", "Word collection and practice", "book", card, fg, muted, gradient) {} }
                        item { NativeFeatureCard("Grammar tracker", "Track the grammar topics you practise", "Progress by topic", "checklist", card, fg, muted, gradient) {} }
                        item { NativeFeatureCard("Weekly study plan", "Your existing A1 days and tasks stay unchanged", "No routine edits without permission", "calendar", card, fg, muted, gradient) {} }
                    }
                    "Together" -> {
                        item { SectionHeading("Our memories & plans", "দুজনের জন্য shared space", fg, muted) }
                        item { NativeFeatureCard("Daily tasks", "Meals • Prayer • College • Bath • Custom tasks", "Visible to both accounts", "checklist", card, fg, muted, gradient) {} }
                        item { NativeFeatureCard("Meet schedule", "Date • Start/end time • Place", "Upcoming and past meetings", "calendar", card, fg, muted, gradient) {} }
                        item { NativeFeatureCard("Memories & letters", "Save moments that matter to both of you", "Our little timeline", "heart", card, fg, muted, gradient) {} }
                    }
                    else -> {
                        item { CoupleHero(card, fg, muted, gradient) }
                        item { SectionHeading("Two profiles, one journey", "Saimoon ♡ Nadiya", fg, muted) }
                        item { ProfileTile("Saimoon", "saimoon.png", card, fg, muted) }
                        item { ProfileTile("Nadiya", "nadiya.png", card, fg, muted) }
                        item { Text("Profile stats will be connected to the existing Firestore records.", color = muted, fontSize = 12.sp) }
                    }
                }
                item {
                    Text("Made with love  ♡  Our Life", color = muted.copy(alpha = .7f), fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

private fun pageTitle(page: String) = when (page) {
    "Home" -> "A little world for two ♡"
    "Chat" -> "Only you & me"
    "Learn" -> "Grow together"
    "Together" -> "Our little moments"
    else -> "Our two profiles"
}

@Composable
private fun CoupleHero(card: Color, fg: Color, muted: Color, gradient: Brush) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = card)) {
        Box(Modifier.fillMaxWidth().background(gradient).padding(1.dp)) {
            Column(
                Modifier.fillMaxWidth().background(card, RoundedCornerShape(27.dp)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    AvatarLetter("S", Rose)
                    Text("  ♡  ", color = Rose, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    AvatarLetter("N", DeepRose)
                }
                Text("Saimoon  &  Nadiya", color = fg, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("Two hearts, one little journey.", color = muted, fontSize = 13.sp)
                Surface(color = Rose.copy(alpha = .13f), shape = RoundedCornerShape(50)) {
                    Text("OUR LITTLE LIFE", color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
                }
            }
        }
    }
}

@Composable
private fun AvatarLetter(letter: String, color: Color) {
    Box(Modifier.size(58.dp).clip(CircleShape).background(color.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
        Text(letter, color = color, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String, fg: Color, muted: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = fg, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = muted, fontSize = 12.sp)
    }
}

@Composable
private fun ProgressStrip(card: Color, fg: Color, muted: Color) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = card)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Words" to "—", "Tasks" to "—", "Days" to "—").forEach { (label, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = Rose, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                    Text(label, color = muted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun NativeFeatureCard(
    title: String, description: String, note: String, type: String,
    card: Color, fg: Color, muted: Color, gradient: Brush, onClick: () -> Unit
) {
    val icon = when (type) {
        "book" -> Icons.Default.MenuBook
        "chat" -> Icons.Default.Forum
        "calendar" -> Icons.Default.CalendarMonth
        "heart" -> Icons.Default.Favorite
        else -> Icons.Default.CheckCircle
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(gradient), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = fg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(description, color = fg.copy(alpha = .8f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(note, color = muted, fontSize = 10.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Rose)
        }
    }
}

@Composable
private fun EmptyNativePanel(emoji: String, title: String, description: String, card: Color, fg: Color, muted: Color) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = card)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(emoji, fontSize = 42.sp)
            Text(title, color = fg, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(description, color = muted, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ProfileTile(name: String, image: String, card: Color, fg: Color, muted: Color) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = card)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AvatarLetter(name.first().toString(), Rose)
            Column {
                Text(name, color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(image, color = muted, fontSize = 11.sp)
            }
        }
    }
}
