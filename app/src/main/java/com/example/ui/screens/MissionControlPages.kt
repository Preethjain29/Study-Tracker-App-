package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrainItem
import com.example.data.model.Chapter
import com.example.data.model.SpacedRepSchedule
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// SCREEN 1: HUB / DASHBOARD PAGE (REDESIGNED)
// ==========================================
@Composable
fun DashboardPage(viewModel: StudyViewModel, onNavigateTo: (ScreenTab) -> Unit) {
    val rawProfile by viewModel.userProfile.collectAsState()
    val profile = rawProfile ?: UserProfile()
    val allTasks by viewModel.allBrainItems.collectAsState()
    val logs by viewModel.allStudyLogs.collectAsState()
    val schedules by viewModel.allSchedules.collectAsState()

    val totalTimeFocused = logs.sumOf { it.durationMinutes }
    val activeTasks = allTasks.filter { !it.isCompleted }
    val top3Tasks = activeTasks.sortedByDescending { it.priorityScore }.take(3)
    val completedCount = allTasks.count { it.isCompleted }

    // Dynamic Calendar Greeting
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. HERO SECTION & GREETING
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$greeting, ${profile.name.ifBlank { "Scholar" }} 👋",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ActiveWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Level ${profile.level} • ${profile.careerGoal}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpaceNeonTeal
                        )
                    }
                    
                    // Simple, clean Flame Capsule
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateLight)
                            .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🔥", fontSize = 14.sp)
                        Text(
                            text = "${profile.streak} streak",
                            color = GoldYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // XP Progress Bar inside Hero
                val prevXpMax = when (profile.level) {
                    1 -> 0
                    2 -> 100
                    3 -> 300
                    4 -> 600
                    else -> 1000
                }
                val nextXpMax = when (profile.level) {
                    1 -> 100
                    2 -> 300
                    3 -> 600
                    4 -> 1000
                    else -> 2000
                }
                val xpInCurrentLevel = (profile.xp - prevXpMax).coerceAtLeast(0)
                val levelRange = nextXpMax - prevXpMax
                val progressFraction = (xpInCurrentLevel.toFloat() / levelRange.toFloat()).coerceIn(0f, 1f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateGray)
                        .border(1.5.dp, BorderSlate, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XP Progress",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedSlate
                        )
                        Text(
                            text = "${profile.xp} / $nextXpMax XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = SpaceNeonTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progressFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = SpaceNeonTeal,
                        trackColor = SlateLight
                    )
                }
            }
        }

        // 2. TODAY'S MISSION (TOP 3 TASKS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Today's Mission",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = ActiveWhite
                    )
                    Text(
                        text = "What should I do right now?",
                        fontSize = 12.sp,
                        color = MutedSlate,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (top3Tasks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = "All done",
                                tint = SpaceNeonTeal,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All missions completed!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite
                            )
                            Text(
                                text = "Add tasks to Brain Dump to populate.",
                                fontSize = 11.sp,
                                color = MutedSlate
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            top3Tasks.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SlateLight)
                                        .clickable { viewModel.completeBrainItem(task) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Circular checkbox button
                                    Icon(
                                        imageVector = Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = "Check task",
                                        tint = SpaceNeonTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ActiveWhite
                                        )
                                        Text(
                                            text = "${task.category} • ${task.estimatedDurationMinutes} mins",
                                            fontSize = 10.sp,
                                            color = MutedSlate
                                        )
                                    }
                                    if (task.priorityScore >= 80) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CoralOrange.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "High", color = CoralOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. QUICK ACTIONS (GRID)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Quick Actions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = ActiveWhite
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ACTION 1: Study
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateTo(ScreenTab.STUDY_PLANNER) }
                            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = "Study",
                                tint = SpaceNeonTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Study",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite
                            )
                            Text(
                                text = "Go to Planner",
                                fontSize = 10.sp,
                                color = MutedSlate
                            )
                        }
                    }

                    // ACTION 2: Revise
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateTo(ScreenTab.REVISION_CENTER) }
                            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Cached,
                                contentDescription = "Revise",
                                tint = NeonPurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Revise",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite
                            )
                            Text(
                                text = "Spaced repetition",
                                fontSize = 10.sp,
                                color = MutedSlate
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ACTION 3: Ask AI
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateTo(ScreenTab.AI_COACH) }
                            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AlternateEmail,
                                contentDescription = "Ask AI",
                                tint = CoralOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Ask AI",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite
                            )
                            Text(
                                text = "Talk to AI Coach",
                                fontSize = 10.sp,
                                color = MutedSlate
                            )
                        }
                    }

                    // ACTION 4: Brain Dump
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateTo(ScreenTab.BRAIN_DUMP) }
                            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Create,
                                contentDescription = "Brain Dump",
                                tint = GoldYellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Brain Dump",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite
                            )
                            Text(
                                text = "Manage checklist",
                                fontSize = 10.sp,
                                color = MutedSlate
                            )
                        }
                    }
                }
            }
        }

        // 4. PROGRESS SNAPSHOT
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Progress Snapshot",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = ActiveWhite,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Parameter 1: Study Time
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.AccessTime, "Focused time", tint = SpaceNeonTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Study Time", fontSize = 11.sp, color = MutedSlate)
                            Text(text = "${totalTimeFocused} Mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ActiveWhite)
                        }

                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(BorderSlate))

                        // Parameter 2: Tasks Completed
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.CheckCircle, "Tasks status", tint = NeonPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Completed", fontSize = 11.sp, color = MutedSlate)
                            Text(text = "${completedCount} Tasks", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ActiveWhite)
                        }

                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(BorderSlate))

                        // Parameter 3: Revision Due
                        val uncompletedSchedules = schedules.count { !it.isReviewed }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.HistoryEdu, "Revision status", tint = CoralOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Revision Due", fontSize = 11.sp, color = MutedSlate)
                            Text(
                                text = if (uncompletedSchedules > 0) "${uncompletedSchedules} Due" else "All Caught Up",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uncompletedSchedules > 0) CoralOrange else SpaceNeonTeal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: BRAIN DUMP PAGE
// ==========================================
@Composable
fun BrainDumpPage(viewModel: StudyViewModel) {
    var taskInput by remember { mutableStateOf("") }
    val allTasks by viewModel.allBrainItems.collectAsState()
    val loading by viewModel.aiLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Brain Dump Inbox 📥",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite
        )
        Text(
            text = "DUMP EVERYTHING: tasks, career notes, goals, accounting topics. The AI will instantly decode, value, and categorize them.",
            fontSize = 12.sp,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = taskInput,
                onValueChange = { taskInput = it },
                placeholder = { Text("E.g., Complete Economics Chapter 2 numericals", color = MutedSlate) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpaceNeonTeal,
                    unfocusedBorderColor = BorderSlate,
                    focusedTextColor = ActiveWhite,
                    unfocusedTextColor = ActiveWhite,
                    focusedContainerColor = SlateGray,
                    unfocusedContainerColor = SlateGray
                ),
                maxLines = 2,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (taskInput.isNotBlank()) {
                        viewModel.addBrainItem(taskInput)
                        taskInput = ""
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DeepSpace, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Add, contentDescription = "Add dump", tint = DeepSpace)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task Scroll list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pendingDumps = allTasks.filter { !it.isCompleted }
            val completedDumps = allTasks.filter { it.isCompleted }

            if (pendingDumps.isEmpty() && completedDumps.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = "Empty",
                            modifier = Modifier.size(48.dp),
                            tint = BorderSlate
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your inbox is clean!",
                            fontWeight = FontWeight.Bold,
                            color = MutedSlate
                        )
                        Text(
                            text = "Add tasks, research ideas, or math homework triggers to experience AI sorting.",
                            color = MutedSlate,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                if (pendingDumps.isNotEmpty()) {
                    item {
                        Text(text = "Unresolved Action Inbox", color = SpaceNeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(pendingDumps) { item ->
                        BrainItemRow(item, onChecked = { viewModel.completeBrainItem(item) }, onToggleFav = { viewModel.toggleBrainItemPriority(item) }, onDelete = { viewModel.deleteBrainItem(item.id) })
                    }
                }
                if (completedDumps.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Completed & Logged Items (+20 XP)", color = MutedSlate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    items(completedDumps) { item ->
                        BrainItemRow(item, onChecked = { viewModel.completeBrainItem(item) }, onToggleFav = { viewModel.toggleBrainItemPriority(item) }, onDelete = { viewModel.deleteBrainItem(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun BrainItemRow(
    item: BrainItem,
    onChecked: () -> Unit,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    val catColor = when (item.category) {
        "Study" -> NeonPurple
        "Personal" -> ActiveWhite
        "Health" -> SpaceNeonTeal
        "Business" -> GoldYellow
        "Finance" -> CoralOrange
        else -> MutedSlate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onChecked() },
                colors = CheckboxDefaults.colors(checkedColor = SpaceNeonTeal, uncheckedColor = BorderSlate)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCompleted) MutedSlate else ActiveWhite,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(catColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = item.category, fontSize = 9.sp, color = catColor, fontWeight = FontWeight.Bold)
                    }

                    // Priority Score
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SlateLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Score ${item.priorityScore}", fontSize = 9.sp, color = GoldYellow, fontWeight = FontWeight.Bold)
                    }

                    // Est Duration
                    Text(text = "⏳ ${item.estimatedDurationMinutes}m", fontSize = 10.sp, color = MutedSlate)
                }
            }

            IconButton(onClick = onToggleFav) {
                Icon(
                    imageVector = if (item.isTopPriority) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = "fav task",
                    tint = if (item.isTopPriority) GoldYellow else MutedSlate
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "delete", tint = CoralOrange, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ==========================================
// SCREEN 3: TODAY'S TOP 3 PAGE
// ==========================================
@Composable
fun TodayTop3Page(viewModel: StudyViewModel) {
    val allTasks by viewModel.allBrainItems.collectAsState()
    val top3 = allTasks.filter { !it.isCompleted && it.isTopPriority }.take(3)
    val remaining = allTasks.filter { !it.isCompleted && !it.isTopPriority }
    var hideRemaining by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Today's Top 3 Priorities 🎯",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite
        )
        Text(
            text = "Block everything else. Focus purely on these 3 critical tasks to reduce stress and boost study consistency.",
            fontSize = 12.sp,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Selected top 3 list
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            if (top3.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateGray)
                            .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.HotelClass, "Top icon", tint = SpaceNeonTeal, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No priorities selected for today", fontWeight = FontWeight.Bold, color = ActiveWhite, fontSize = 14.sp)
                        Text(
                            text = "To assign list items to your Daily Top 3, go to the Brain Dump Inbox and tap the outline Star ⭐️ icons on any tasks!",
                            color = MutedSlate,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                item {
                    Text(text = "Focus Items of the day", color = SpaceNeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                items(top3) { item ->
                    BrainItemRow(item, onChecked = { viewModel.completeBrainItem(item) }, onToggleFav = { viewModel.toggleBrainItemPriority(item) }, onDelete = { viewModel.deleteBrainItem(item.id) })
                }
            }

            // Accordion panel for Later Tasks
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hideRemaining = !hideRemaining }
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateLight)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Later Tasks (${remaining.size} items hidden)",
                        fontWeight = FontWeight.Bold,
                        color = ActiveWhite,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (hideRemaining) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                        contentDescription = "drawer",
                        tint = SpaceNeonTeal
                    )
                }
            }

            if (!hideRemaining) {
                if (remaining.isEmpty()) {
                    item {
                        Text(
                            text = "No other secondary tasks currently.",
                            color = MutedSlate,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(remaining) { item ->
                        BrainItemRow(item, onChecked = { viewModel.completeBrainItem(item) }, onToggleFav = { viewModel.toggleBrainItemPriority(item) }, onDelete = { viewModel.deleteBrainItem(item.id) })
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: STUDY PLANNER / SYLLABUS PAGE
// ==========================================
@Composable
fun StudyPlannerPage(viewModel: StudyViewModel) {
    val chapters by viewModel.allChapters.collectAsState()
    val loading by viewModel.aiLoading.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    var selectedChapterForAi by remember { mutableStateOf<Chapter?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Syllabus tracking 📚",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite
        )
        Text(
            text = "Track your chapters of ${profile.stream} Stream. Use AI features to build One-Page notes, mind maps, formula sheets, or Spaced Repetition reviews.",
            fontSize = 12.sp,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Syllabus Content List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            val streamChapters = chapters.filter { it.stream == profile.stream }
            
            items(streamChapters) { chapter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSlate, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateGray)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ActiveWhite
                                )
                                Text(
                                    text = "${chapter.subject} • Difficult: ${chapter.difficulty}",
                                    fontSize = 11.sp,
                                    color = MutedSlate
                                )
                            }

                            // Completion switch
                            Button(
                                onClick = {
                                    if (!chapter.isCompleted) {
                                        viewModel.completeChapter(chapter)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (chapter.isCompleted) SlateLight else SpaceNeonTeal
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (chapter.isCompleted) "Completed" else "Complete ✔",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (chapter.isCompleted) SpaceNeonTeal else DeepSpace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Current Retention: ${chapter.retentionScore}%",
                                color = if (chapter.retentionScore >= 70) SpaceNeonTeal else if (chapter.retentionScore >= 40) GoldYellow else CoralOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                selectedChapterForAi = if (selectedChapterForAi?.id == chapter.id) null else chapter
                            }) {
                                Icon(
                                    imageVector = if (selectedChapterForAi?.id == chapter.id) Icons.Filled.Close else Icons.Filled.AutoAwesome,
                                    contentDescription = "AI tool panel",
                                    tint = NeonPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // AI Generation Panels inside Syllabus
                        if (selectedChapterForAi?.id == chapter.id) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = BorderSlate)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(text = "⚡ Real-time Smart Note Systems (AI + Document grounded)", fontSize = 11.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                mapOf(
                                    "Formula" to { viewModel.generateQuickFormulaSheet(chapter.id) },
                                    "Notes" to { viewModel.generateChapterSummaryNotes(chapter.id) },
                                    "Mindmap" to { viewModel.generateChapterMindMap(chapter.id) },
                                    "Flashcards" to { viewModel.generateInteractiveFlashcards(chapter.id) }
                                ).forEach { (label, action) ->
                                    Button(
                                        onClick = action,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateLight),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = label, fontSize = 9.sp, color = ActiveWhite)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Podcast style Audio summary trigger
                            Button(
                                onClick = { viewModel.generateChapterPodcastTranscript(chapter.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SlateLight),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.Headset, "podcast", tint = SpaceNeonTeal, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Compose 2-Host Audio/Podcast Summary transcript", fontSize = 10.sp, color = SpaceNeonTeal)
                            }

                            // Show the outcomes if already created
                            if (chapter.formulaSheet.isNotEmpty()) {
                                CardItemPayload("Formula & Concepts Cheat Sheet:", chapter.formulaSheet)
                            }
                            if (chapter.quickNotes.isNotEmpty()) {
                                CardItemPayload("One Page Revision Notes:", chapter.quickNotes)
                            }
                            if (chapter.mindMapSummary.isNotEmpty()) {
                                CardItemPayload("Hierarchical Mind Map Summary:", chapter.mindMapSummary)
                            }
                            if (chapter.flashcardsRaw.isNotEmpty()) {
                                CardItemPayload("Exam Revision Flashcards:", chapter.flashcardsRaw)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardItemPayload(title: String, body: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = DeepSpace)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = body, color = ActiveWhite, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

// ==========================================
// SCREEN 5: REVISION CENTER / SPACED REP
// ==========================================
@Composable
fun RevisionCenterPage(viewModel: StudyViewModel) {
    val schedules by viewModel.allSchedules.collectAsState()
    
    // Filter out historical logs and pending ones
    val dueToday = schedules.filter { !it.isReviewed && it.targetDateMillis <= System.currentTimeMillis() }
    val futureSchedules = schedules.filter { !it.isReviewed && it.targetDateMillis > System.currentTimeMillis() }
    val historicalRevisions = schedules.filter { it.isReviewed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Revision Scheduler (Spaced Rep) Scheduled 🔄",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite
        )
        Text(
            text = "Automatically structures chapters for Day 1, Day 3, 7, 15, and 30 intervals upon completion. Rate your retrieval difficulty to auto-adjust retention scores.",
            fontSize = 12.sp,
            color = MutedSlate
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            // Due Today Section
            item {
                Text(text = "Revisions Due Today 🎯", color = SpaceNeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            if (dueToday.isEmpty()) {
                item {
                    Text(
                        text = "Nice task hygiene! No core revision sessions due right now. Go complete syllabus chapters to automatically spool up revision matrices.",
                        color = MutedSlate,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                items(dueToday) { schedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSlate, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = schedule.chapterName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ActiveWhite)
                                    Text(text = "${schedule.subject} • Scheduled Day ${schedule.scheduledDayIndex}", fontSize = 11.sp, color = MutedSlate)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CoralOrange.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Due Now", fontSize = 9.sp, color = CoralOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Rate your retrieval ease to lock in review accuracy: ", fontSize = 11.sp, color = MutedSlate)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(1, 2, 3, 4, 5).forEach { star ->
                                    Button(
                                        onClick = { viewModel.performSpacedRepetitionReview(schedule, star) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateLight),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(text = "⭐ $star", fontSize = 10.sp, color = GoldYellow, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Future Items Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Upcoming Scheduled Revisions", color = ActiveWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            if (futureSchedules.isEmpty()) {
                item {
                    Text(text = "No future sessions queued.", color = MutedSlate, fontSize = 11.sp, modifier = Modifier.padding(12.dp))
                }
            } else {
                items(futureSchedules) { schedule ->
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dateFormatted = sdf.format(Date(schedule.targetDateMillis))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateGray)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = schedule.chapterName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ActiveWhite)
                            Text(text = "${schedule.subject} • Interval Day ${schedule.scheduledDayIndex}", fontSize = 10.sp, color = MutedSlate)
                        }
                        Text(text = dateFormatted, fontSize = 10.sp, color = SpaceNeonTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Historical Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Revision Log History", color = MutedSlate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            if (historicalRevisions.isEmpty()) {
                item {
                    Text(text = "Completed logs will show up here.", color = MutedSlate, fontSize = 11.sp, modifier = Modifier.padding(12.dp))
                }
            } else {
                items(historicalRevisions) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateLight)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = log.chapterName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ActiveWhite)
                            Text(text = "Done on Day ${log.scheduledDayIndex}", fontSize = 10.sp, color = MutedSlate)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Rating: ", fontSize = 10.sp, color = MutedSlate)
                            Text(text = "⭐ ${log.reviewRating}", fontSize = 11.sp, color = GoldYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
