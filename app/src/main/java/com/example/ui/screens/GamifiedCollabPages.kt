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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

// ==========================================
// SCREEN: GAMIFIED LEARNING PATHWAYS
// ==========================================
@Composable
fun LearningPathPage(viewModel: StudyViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val pathways by viewModel.allLearningPaths.collectAsState()
    var selectedPath by remember { mutableStateOf<LearningPath?>(null) }
    
    val pathId = selectedPath?.id ?: -1L
    val milestones by viewModel.getMilestonesForPath(pathId).collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Pathway Explorer 🗺️",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = ActiveWhite
                )
                Text(
                    text = "Crack subject milestones, secure elite badges, and adapt courses to bolster identified weak areas.",
                    fontSize = 13.sp,
                    color = MutedSlate
                )
            }
        }

        item {
            LevelProgressHeader(profile = profile)
        }

        if (selectedPath == null) {
            item {
                Text(
                    text = "Select an Active Pathway",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ActiveWhite
                )
            }

            if (pathways.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active pathways mapped yet. Tap Syllabus to complete chapters!", color = MutedSlate, fontSize = 13.sp)
                    }
                }
            } else {
                items(pathways) { path ->
                    val isWeakSubject = profile.weakSubjects.contains(path.subject, ignoreCase = true)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isWeakSubject) 1.5.dp else 1.dp,
                                color = if (isWeakSubject) CoralOrange else BorderSlate,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedPath = path },
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isWeakSubject) CoralOrange.copy(alpha = 0.2f)
                                                else SpaceNeonTeal.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.School,
                                            contentDescription = "subject icon",
                                            tint = if (isWeakSubject) CoralOrange else SpaceNeonTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = path.subject.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isWeakSubject) CoralOrange else SpaceNeonTeal,
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SlateLight)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = path.difficultyLevel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ActiveWhite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = path.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite
                            )

                            Text(
                                text = path.description,
                                fontSize = 12.sp,
                                color = MutedSlate,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action & Milestone Status Ring
                            val progress = if (path.totalMilestones > 0) {
                                path.completedMilestones.toFloat() / path.totalMilestones.toFloat()
                            } else {
                                0f
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${path.completedMilestones} / ${path.totalMilestones} Milestones",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ActiveWhite
                                    )
                                    Text(
                                        text = "Rewards: +${path.xpReward} XP & Badge: ${path.badgeUnlocked}",
                                        fontSize = 11.sp,
                                        color = GoldYellow
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = String.format("%.0f%%", progress * 100),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpaceNeonTeal
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = "view path",
                                        tint = SpaceNeonTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = if (path.isCompleted) SpaceNeonTeal else NeonPurple,
                                trackColor = SlateLight
                            )
                        }
                    }
                }
            }
        } else {
            // Detailed Pathway Milestone View
            val curPath = selectedPath!!
            val isWeakSubject = profile.weakSubjects.contains(curPath.subject, ignoreCase = true)

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPath = null },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "back", tint = SpaceNeonTeal)
                    Text("Back to Pathways", color = SpaceNeonTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isWeakSubject) CoralOrange else BorderSlate, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE PATHWAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = SpaceNeonTeal,
                                letterSpacing = 1.sp
                            )
                            if (curPath.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SpaceNeonTeal.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("COMPLETED 🏅", color = SpaceNeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = curPath.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ActiveWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = curPath.description,
                            fontSize = 13.sp,
                            color = MutedSlate
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Completion Reward: +${curPath.xpReward} XP & Badge: \"${curPath.badgeUnlocked}\"",
                            fontSize = 12.sp,
                            color = GoldYellow,
                            fontWeight = FontWeight.Bold
                        )

                        // Adapt to Weak Areas Action Button
                        if (curPath.isAdaptable) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.adaptPathBasedOnWeakAreas(curPath.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Warning, "adapt pathway", tint = DeepSpace, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Adapt Pathway (Weaks Focus) 🚨", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepSpace)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateLight)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡ ADAPTED PATHWAY — Milestones customized for weak areas.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralOrange
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Milestone Milestones",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ActiveWhite
                )
            }

            if (milestones.isEmpty()) {
                item {
                    Text("Loading milestones...", color = MutedSlate, fontSize = 13.sp)
                }
            } else {
                items(milestones) { milestone ->
                    val mIcon = when (milestone.milestoneType) {
                        "Practice Questions" -> Icons.Outlined.Edit
                        "Mock Quiz" -> Icons.Outlined.Assignment
                        else -> Icons.Outlined.LibraryBooks
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (milestone.isCompleted) SpaceNeonTeal.copy(alpha = 0.5f) else BorderSlate, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.completeMilestone(milestone) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (milestone.isCompleted) SpaceNeonTeal.copy(alpha = 0.2f) else SlateLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = mIcon,
                                    contentDescription = "type icon",
                                    tint = if (milestone.isCompleted) SpaceNeonTeal else MutedSlate,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SlateLight)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(milestone.milestoneType.uppercase(), fontSize = 8.sp, color = MutedSlate, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "+${milestone.xpReward} XP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldYellow
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = milestone.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (milestone.isCompleted) SpaceNeonTeal else ActiveWhite
                                )
                                Text(
                                    text = milestone.description,
                                    fontSize = 11.sp,
                                    color = MutedSlate
                                )
                            }

                            // Interactive checkbox representation
                            Icon(
                                imageVector = if (milestone.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = "status check",
                                tint = if (milestone.isCompleted) SpaceNeonTeal else BorderSlate,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Glowing Badge Notification if fully complete
            val allComplete = milestones.isNotEmpty() && milestones.all { it.isCompleted }
            if (allComplete) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, SpaceNeonTeal, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏆 PATHWAY MASTERED",
                                fontWeight = FontWeight.Black,
                                color = SpaceNeonTeal,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Unlocked badge \"${curPath.badgeUnlocked}\"!",
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Completing this pathway has added massive confidence to your prep profile. Study areas successfully synchronized!",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN: COLLABORATIVE STUDY FEATURE
// ==========================================
@Composable
fun CollabStudyPage(viewModel: StudyViewModel) {
    val groups by viewModel.allStudyGroups.collectAsState(initial = emptyList())
    var activeGroup by remember { mutableStateOf<StudyGroup?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeGroup == null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Collaborative Studies 👥",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ActiveWhite
                        )
                        Text(
                            text = "Build deep custom revision channels, share research notes, and assign crew checklists.",
                            fontSize = 12.sp,
                            color = MutedSlate
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Cohorts/Groups",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActiveWhite
                    )

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Add, "create group", tint = DeepSpace, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Cohort", fontSize = 12.sp, color = DeepSpace, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showCreateDialog) {
                item {
                    CreateGroupFormCard(
                        onDismiss = { showCreateDialog = false },
                        onCreate = { name, desc, focus ->
                            viewModel.createStudyGroup(name, desc, focus)
                            showCreateDialog = false
                        }
                    )
                }
            }

            if (groups.isEmpty()) {
                item {
                    Text("No study groups joined yet. Create one above to invite friends!", color = MutedSlate, fontSize = 13.sp)
                }
            } else {
                items(groups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                            .clickable { activeGroup = group },
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonPurple.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = group.subjectFocus.uppercase(),
                                        color = NeonPurple,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.People, "members logo", tint = MutedSlate, modifier = Modifier.size(14.dp))
                                    Text("Active Crew", color = MutedSlate, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = group.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ActiveWhite
                            )

                            Text(
                                text = group.description,
                                fontSize = 12.sp,
                                color = MutedSlate,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Created by: ${group.authorName}",
                                    fontSize = 11.sp,
                                    color = MutedSlate
                                )

                                Text(
                                    text = "Enter Hub 💬",
                                    color = SpaceNeonTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Group Workspace Panel
            item {
                WorkspaceWrapper(
                    group = activeGroup!!,
                    viewModel = viewModel,
                    onBack = { activeGroup = null }
                )
            }
        }
    }
}


// Sub-component: Create Group Form Panel
@Composable
fun CreateGroupFormCard(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var focus by remember { mutableStateOf("Commerce") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, SpaceNeonTeal, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Launch New Cohort", fontWeight = FontWeight.Black, color = ActiveWhite, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Group Title / Cohort Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpaceNeonTeal,
                    unfocusedBorderColor = BorderSlate,
                    focusedLabelColor = SpaceNeonTeal
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Goal / Mission description") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpaceNeonTeal,
                    unfocusedBorderColor = BorderSlate,
                    focusedLabelColor = SpaceNeonTeal
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Subject/Theme Focus", fontSize = 12.sp, color = MutedSlate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Commerce", "Science", "Mathematics", "Exams").forEach { sub ->
                    val isSel = focus == sub
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) SpaceNeonTeal else SlateGray)
                            .clickable { focus = sub }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sub, fontSize = 11.sp, color = if (isSel) DeepSpace else ActiveWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Abort", color = CoralOrange)
                }

                Button(
                    onClick = { if (name.isNotBlank()) onCreate(name, desc, focus) },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal)
                ) {
                    Text("Launch", color = DeepSpace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// Workspace Panel displaying Group Chat, Tasks, and Documents
@Composable
fun WorkspaceWrapper(
    group: StudyGroup,
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    var activeTab by remember { mutableStateOf("chat") } // chat, tasks, docs

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "back", tint = SpaceNeonTeal)
            Text("Exit Cohort", color = SpaceNeonTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Group Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGray)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(group.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = ActiveWhite)
                    IconButton(onClick = { viewModel.deleteStudyGroup(group.id); onBack() }) {
                        Icon(Icons.Filled.Delete, "disband", tint = CoralOrange, modifier = Modifier.size(16.dp))
                    }
                }
                Text(group.description, fontSize = 12.sp, color = MutedSlate)
                Spacer(modifier = Modifier.height(10.dp))

                // Workspaces Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateLight)
                ) {
                    listOf("chat" to "💬 Chat", "tasks" to "🎯 To-Do", "docs" to "📂 Vault").forEach { (tabId, label) ->
                        val isSel = activeTab == tabId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) SpaceNeonTeal else Color.Transparent)
                                .clickable { activeTab = tabId }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) DeepSpace else ActiveWhite)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (activeTab) {
            "chat" -> GroupChatTab(groupId = group.id, viewModel = viewModel)
            "tasks" -> GroupTasksTab(groupId = group.id, viewModel = viewModel)
            "docs" -> GroupDocsTab(groupId = group.id, viewModel = viewModel)
        }
    }
}


// Workspace tab 1: Chat Message Feed
@Composable
fun GroupChatTab(groupId: Long, viewModel: StudyViewModel) {
    val messages by viewModel.getGroupMessages(groupId).collectAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                .background(SlateGray)
                .padding(10.dp)
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages in this chat. Write something!", color = MutedSlate, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isCurrentUser = msg.senderName == "Siddharth"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = msg.senderName,
                                fontSize = 10.sp,
                                color = if (isCurrentUser) SpaceNeonTeal else NeonPurple,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 8.dp,
                                            topEnd = 8.dp,
                                            bottomStart = if (isCurrentUser) 8.dp else 0.dp,
                                            bottomEnd = if (isCurrentUser) 0.dp else 8.dp
                                        )
                                    )
                                    .background(if (isCurrentUser) SpaceNeonTeal.copy(alpha = 0.15f) else SlateLight)
                                    .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = msg.messageText,
                                    fontSize = 12.sp,
                                    color = ActiveWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // Send Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Comment, update checklist, or ask a query...") },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpaceNeonTeal,
                    unfocusedBorderColor = BorderSlate,
                    focusedLabelColor = SpaceNeonTeal
                ),
                maxLines = 2
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendGroupMessage(groupId, "Siddharth", messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpaceNeonTeal)
            ) {
                Icon(Icons.Filled.Send, "send message", tint = DeepSpace, modifier = Modifier.size(16.dp))
            }
        }
    }
}


// Workspace tab 2: Group Task Checklists
@Composable
fun GroupTasksTab(groupId: Long, viewModel: StudyViewModel) {
    val tasks by viewModel.getGroupTasks(groupId).collectAsState(initial = emptyList())
    var taskTitle by remember { mutableStateOf("") }
    var taskAssignee by remember { mutableStateOf("All Members") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Active Shared Tasks Checklist", fontWeight = FontWeight.Bold, color = ActiveWhite, fontSize = 14.sp)

        // Add task card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGray)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Assign Crew Action Item", fontWeight = FontWeight.Bold, color = ActiveWhite, fontSize = 11.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        placeholder = { Text("Task description (e.g. solve curves page 50)") },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpaceNeonTeal,
                            unfocusedBorderColor = BorderSlate
                        )
                    )

                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                viewModel.createGroupTask(groupId, taskTitle, taskAssignee, 30)
                                taskTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Assign", color = DeepSpace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assigned To:", fontSize = 11.sp, color = MutedSlate)
                    listOf("All Members", "Siddharth", "Aarav").forEach { assignee ->
                        val isSel = taskAssignee == assignee
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) SpaceNeonTeal else SlateLight)
                                .clickable { taskAssignee = assignee }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(assignee, fontSize = 9.sp, color = if (isSel) DeepSpace else ActiveWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // List tasks
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No shared tasks assigned. Add some above!", color = MutedSlate, fontSize = 12.sp)
            }
        } else {
            tasks.forEach { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (task.isCompleted) SpaceNeonTeal.copy(alpha = 0.5f) else BorderSlate, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateGray)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.completeGroupTask(task) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "task check",
                            tint = if (task.isCompleted) SpaceNeonTeal else BorderSlate,
                            modifier = Modifier.size(20.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (task.isCompleted) SpaceNeonTeal else ActiveWhite
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SlateLight)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("ASSIGNED: ${task.assignedTo}", fontSize = 8.sp, color = MutedSlate, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "+${task.xpReward} XP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldYellow
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// Workspace tab 3: Shared Document Vault folder
@Composable
fun GroupDocsTab(groupId: Long, viewModel: StudyViewModel) {
    val documents by viewModel.getGroupDocuments(groupId).collectAsState(initial = emptyList())
    var docTitle by remember { mutableStateOf("") }
    var docContent by remember { mutableStateOf("") }
    var focusedDoc by remember { mutableStateOf<GroupDocument?>(null) }
    var isEditingDoc by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!isEditingDoc && focusedDoc == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Shared Repository Workspace", fontWeight = FontWeight.Bold, color = ActiveWhite, fontSize = 14.sp)
                Button(
                    onClick = { isEditingDoc = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Share, "publish file", tint = DeepSpace, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Doc", fontSize = 11.sp, color = DeepSpace, fontWeight = FontWeight.Bold)
                }
            }

            if (documents.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No documents uploaded yet. Share revision papers, formula books!", color = MutedSlate, fontSize = 12.sp)
                }
            } else {
                documents.forEach { doc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                            .clickable { focusedDoc = doc },
                        colors = CardDefaults.cardColors(containerColor = SlateGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SpaceNeonTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Description, "file representation", tint = SpaceNeonTeal, modifier = Modifier.size(18.dp))
                            }

                            Column {
                                Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ActiveWhite)
                                Text("Uploaded by: ${doc.sharedBy} • Tap to view full text", fontSize = 11.sp, color = MutedSlate)
                            }
                        }
                    }
                }
            }
        } else if (isEditingDoc) {
            // Upload form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, SpaceNeonTeal, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateLight)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Share Structured Document inside Cohort", fontWeight = FontWeight.Black, color = ActiveWhite, fontSize = 14.sp)
                    
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title (e.g. Accounts revision sheet)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpaceNeonTeal, unfocusedBorderColor = BorderSlate)
                    )

                    OutlinedTextField(
                        value = docContent,
                        onValueChange = { docContent = it },
                        label = { Text("Content / Notes Text") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpaceNeonTeal, unfocusedBorderColor = BorderSlate),
                        maxLines = 10
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { isEditingDoc = false; docTitle = ""; docContent = "" }) {
                            Text("Abort", color = CoralOrange)
                        }

                        Button(
                            onClick = {
                                if (docTitle.isNotBlank() && docContent.isNotBlank()) {
                                    viewModel.shareGroupDocument(groupId, docTitle, docContent, "Siddharth")
                                    isEditingDoc = false
                                    docTitle = ""
                                    docContent = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal)
                        ) {
                            Text("Upload File", color = DeepSpace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // View Focused document details
            val doc = focusedDoc!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(doc.title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = ActiveWhite)
                        IconButton(onClick = { focusedDoc = null }) {
                            Icon(Icons.Filled.Close, "close file details", tint = ActiveWhite)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateLight)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = doc.content,
                            fontSize = 12.sp,
                            color = ActiveWhite,
                            lineHeight = 18.sp
                        )
                    }

                    Text("Shared by crew member: ${doc.sharedBy}", fontSize = 11.sp, color = MutedSlate, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
