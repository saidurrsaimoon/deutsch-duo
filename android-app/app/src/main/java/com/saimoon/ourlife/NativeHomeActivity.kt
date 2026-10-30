package com.saimoon.ourlife

import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBwsCFD4YzcoCD_vbEHm5jfxStzcdamLHU")
                .setApplicationId("1:660528509397:android:03d9d4897ccd5f88818d5c")
                .setProjectId("deutsch-duo-8f489")
                .setGcmSenderId("660528509397")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
        setContent { OurLifeApp(FirebaseAuth.getInstance()) }
    }
}

@Composable
private fun OurLifeApp(auth: FirebaseAuth) {
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    if (currentUser == null) {
        LoginScreen(auth)
    } else {
        AuthenticatedOurLifeApp(auth)
    }
}

@Composable
private fun LoginScreen(auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val gradient = Brush.linearGradient(listOf(Rose, DeepRose))
    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF59304A), Plum))
        ).padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Text("♡", color = Rose, fontSize = 48.sp)
                Text("OUR LITTLE LIFE", color = Rose, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text("Welcome back", color = Color(0xFFFFEFF5), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text("শুধু Saimoon & Nadiya-র জন্য", color = MutedDark, fontSize = 13.sp)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Email") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                if (error != null) {
                    Text(error!!, color = Color(0xFFFF8298), fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            error = "Email ও password দুটোই লিখো"
                        } else {
                            busy = true
                            error = null
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    busy = false
                                    if (!task.isSuccessful) {
                                        error = task.exception?.localizedMessage ?: "Login হয়নি। আবার চেষ্টা করো।"
                                    }
                                }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRose),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (busy) "Signing in…" else "Sign in", modifier = Modifier.padding(vertical = 5.dp), fontWeight = FontWeight.Bold)
                }
                Text("নতুন account তৈরি করা যাবে না", color = MutedDark, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AuthenticatedOurLifeApp(auth: FirebaseAuth) {
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
                        item { NativeChatPanel(auth, card, fg, muted) }
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


private data class NativeChatMessage(
    val id: String, val uid: String, val name: String, val text: String, val time: Long,
    val replyName: String?, val replyText: String?, val deliveredAt: Long?, val seenAt: Long?,
    val reactions: Map<String, String>, val edited: Boolean
)

@Composable
private fun NativeChatPanel(auth: FirebaseAuth, card: Color, fg: Color, muted: Color) {
    val db = remember { FirebaseFirestore.getInstance() }
    val me = auth.currentUser
    val myName = when (me?.uid) {
        "R0xCX48rkDa9VyyK7tDkyyH5qIk1" -> "Saimoon"
        "lGnuAexcJzaJLMsIjYKeR4bzyXJ3" -> "Nadiya"
        else -> me?.email?.substringBefore("@") ?: "You"
    }
    val partnerUid = if (me?.uid == "R0xCX48rkDa9VyyK7tDkyyH5qIk1") "lGnuAexcJzaJLMsIjYKeR4bzyXJ3" else "R0xCX48rkDa9VyyK7tDkyyH5qIk1"
    var partnerStatus by remember { mutableStateOf<Map<String, Any>?>(null) }
    var messages by remember { mutableStateOf<List<NativeChatMessage>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var replyTo by remember { mutableStateOf<NativeChatMessage?>(null) }
    var actionMessage by remember { mutableStateOf<NativeChatMessage?>(null) }
    var editingMessage by remember { mutableStateOf<NativeChatMessage?>(null) }
    val clipboard = LocalClipboardManager.current
    var lastTypingWrite by remember { mutableLongStateOf(0L) }

    DisposableEffect(me?.uid) {
        var chatReg: ListenerRegistration? = null
        var userReg: ListenerRegistration? = null
        if (me != null) {
            db.collection("users").document(me.uid).set(
                mapOf("vis" to true, "inChat" to true, "seen" to System.currentTimeMillis()),
                com.google.firebase.firestore.SetOptions.merge()
            )
            userReg = db.collection("users").document(partnerUid).addSnapshotListener { snap, _ ->
                partnerStatus = snap?.data
            }
            chatReg = db.collection("chat").orderBy("t", Query.Direction.DESCENDING).limit(100)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        error = exception.localizedMessage ?: "Chat load হয়নি"
                    } else if (snapshot != null) {
                        val now = System.currentTimeMillis()
                        snapshot.documents.forEach { doc ->
                            if (doc.getString("uid") != me.uid) {
                                if (doc.getLong("dAt") == null) doc.reference.update("dAt", now)
                                if (doc.getLong("sAt") == null) doc.reference.update("sAt", now)
                            }
                        }
                        messages = snapshot.documents.mapNotNull { doc ->
                            val uid = doc.getString("uid") ?: return@mapNotNull null
                            val reply = doc.get("re") as? Map<*, *>
                            val rx = doc.get("rx") as? Map<*, *> ?: emptyMap<Any, Any>()
                            NativeChatMessage(
                                doc.id, uid, doc.getString("name") ?: "Unknown",
                                doc.getString("text") ?: "", doc.getLong("t") ?: 0L,
                                reply?.get("name") as? String, reply?.get("text") as? String,
                                doc.getLong("dAt"), doc.getLong("sAt"),
                                rx.entries.mapNotNull { e -> (e.key as? String)?.let { k -> (e.value as? String)?.let { k to it } } }.toMap(),
                                doc.getLong("edAt") != null
                            )
                        }
                        error = null
                    }
                }
        }
        onDispose {
            chatReg?.remove()
            userReg?.remove()
            if (me != null) db.collection("users").document(me.uid).set(
                mapOf("vis" to false, "inChat" to false, "seen" to System.currentTimeMillis(), "typ" to 0L),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    val online = partnerStatus?.get("vis") == true &&
        System.currentTimeMillis() - ((partnerStatus?.get("seen") as? Number)?.toLong() ?: 0L) < 70000
    val typing = online && System.currentTimeMillis() - ((partnerStatus?.get("typ") as? Number)?.toLong() ?: 0L) < 4500
    val status = when {
        typing -> "typing…"
        online && partnerStatus?.get("inChat") == true -> "online · in chat"
        online -> "online"
        (partnerStatus?.get("seen") as? Number)?.toLong() != null -> "last seen " +
            java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(
                java.util.Date((partnerStatus?.get("seen") as Number).toLong()))
        else -> "offline"
    }

    Column(Modifier.fillMaxWidth().heightIn(min = 460.dp, max = 680.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text("Just us, always. ♡", color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Saimoon & Nadiya  ·  $status", color = if (online) Rose else muted, fontSize = 11.sp)
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = card)) {
            if (messages.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(330.dp), contentAlignment = Alignment.Center) {
                    Text("Say hi 👋", color = muted)
                }
            } else {
                LazyColumn(Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 500.dp).padding(12.dp),
                    reverseLayout = true, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(messages, key = { it.id }) { message ->
                        val mine = message.uid == me?.uid
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                                Column(
                                    Modifier.widthIn(max = 292.dp)
                                        .pointerInput(message.id) {
                                            var drag = 0f
                                            detectHorizontalDragGestures(
                                                onHorizontalDrag = { _, amount -> drag += amount },
                                                onDragEnd = { if (drag > 55f) replyTo = message; drag = 0f },
                                                onDragCancel = { drag = 0f }
                                            )
                                        }
                                        .pointerInput(message.id) {
                                            detectTapGestures(onLongPress = { actionMessage = message })
                                        }
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (mine) DeepRose else card.copy(alpha = .88f))
                                        .padding(horizontal = 13.dp, vertical = 9.dp)
                                ) {
                                    if (message.replyText != null) {
                                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                            .background(Color.Black.copy(alpha = .13f)).padding(8.dp)) {
                                            Text(message.replyName ?: "Reply", color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(message.replyText, color = if (mine) Color.White.copy(alpha = .85f) else muted,
                                                fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                    }
                                    if (!mine) Text(message.name, color = Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(message.text, color = if (mine) Color.White else fg, fontSize = 14.sp)
                                    Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (message.edited) Text("edited", color = Color.White.copy(alpha = .6f), fontSize = 8.sp)
                                        Text(if (message.time > 0) java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                            .format(java.util.Date(message.time)) else "",
                                            color = if (mine) Color.White.copy(alpha = .72f) else muted, fontSize = 9.sp)
                                        if (mine) {
                                            val tickColor = if (message.seenAt != null) Color(0xFF8DE5FF) else Color.White.copy(alpha = .75f)
                                            Text(if (message.seenAt != null) "✓✓" else if (message.deliveredAt != null) "✓✓" else "✓",
                                                color = tickColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            if (message.reactions.isNotEmpty()) {
                                Text(message.reactions.values.joinToString("  "), color = Rose, fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }
            }
        }
        if (error != null) Text(error!!, color = Color(0xFFFF8298), fontSize = 11.sp)
        if (replyTo != null) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(card).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Replying to ${replyTo!!.name}", color = Rose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(replyTo!!.text, color = muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { replyTo = null }) { Icon(Icons.Default.Close, "Cancel reply", tint = muted) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = draft, onValueChange = {
                draft = it
                val now = System.currentTimeMillis()
                if (me != null && now - lastTypingWrite > 2500) {
                    lastTypingWrite = now
                    db.collection("users").document(me.uid).set(
                        mapOf("typ" to now, "vis" to true, "inChat" to true, "seen" to now),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                }
            }, modifier = Modifier.weight(1f), placeholder = { Text("Write a message…") },
                shape = RoundedCornerShape(24.dp), maxLines = 4)
            Button(onClick = {
                val body = draft.trim()
                if (body.isNotEmpty() && me != null && !sending) {
                    sending = true
                    draft = ""
                    val editing = editingMessage
                    val reply = replyTo?.let { mapOf("id" to it.id, "name" to it.name, "text" to it.text.take(80)) }
                    replyTo = null
                    val write = if (editing != null) {
                        db.collection("chat").document(editing.id).update("text", body, "edAt", System.currentTimeMillis())
                    } else {
                        db.collection("chat").add(mapOf("uid" to me.uid, "name" to myName,
                            "text" to body, "t" to System.currentTimeMillis(), "re" to reply))
                    }
                    write.addOnCompleteListener { task ->
                        sending = false
                        if (!task.isSuccessful) {
                            draft = body
                            error = task.exception?.localizedMessage ?: "Message পাঠানো যায়নি"
                        } else {
                            editingMessage = null
                            db.collection("users").document(me.uid).set(mapOf("typ" to 0L),
                                com.google.firebase.firestore.SetOptions.merge())
                        }
                    }
                }
            }, enabled = draft.isNotBlank() && !sending, modifier = Modifier.size(52.dp),
                shape = CircleShape, contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepRose)) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }

    if (actionMessage != null) {
        val selectedMessage = actionMessage!!
        val mine = selectedMessage.uid == me?.uid
        AlertDialog(
            onDismissRequest = { actionMessage = null },
            title = { Text(if (mine) "Your message" else selectedMessage.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(selectedMessage.text, color = fg)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("❤️", "😂", "😮", "😢", "👍", "🙏").forEach { emoji ->
                            TextButton(onClick = {
                                db.collection("chat").document(selectedMessage.id)
                                    .update("rx.${me?.uid}", emoji)
                                actionMessage = null
                            }) { Text(emoji, fontSize = 19.sp) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { replyTo = selectedMessage; actionMessage = null }) { Text("Reply") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        clipboard.setText(AnnotatedString(selectedMessage.text))
                        actionMessage = null
                    }) { Text("Copy") }
                    if (mine) TextButton(onClick = {
                        editingMessage = selectedMessage
                        draft = selectedMessage.text
                        actionMessage = null
                    }) { Text("Edit") }
                    if (mine) TextButton(onClick = {
                        db.collection("chat").document(selectedMessage.id).delete()
                        actionMessage = null
                    }) { Text("Delete", color = Color(0xFFE46A82)) }
                    TextButton(onClick = { actionMessage = null }) { Text("Close") }
                }
            },
            containerColor = card,
            titleContentColor = fg,
            textContentColor = muted
        )
    }
}
