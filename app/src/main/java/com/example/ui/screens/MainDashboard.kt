package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

// Tab Definitions
enum class ScreenTab(val title: String, val iconSelected: androidx.compose.ui.graphics.vector.ImageVector, val iconUnselected: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Home", Icons.Filled.Home, Icons.Outlined.Home),
    LEARNING_PATH("Pathways", Icons.Filled.Map, Icons.Outlined.Map),
    COLLAB_STUDY("Collab Hub", Icons.Filled.Groups, Icons.Outlined.Groups),
    BRAIN_DUMP("Brain Dump", Icons.Filled.Inbox, Icons.Outlined.Inbox),
    TODAY_TOP_3("Top 3", Icons.Filled.HotelClass, Icons.Outlined.HotelClass),
    STUDY_PLANNER("Planner", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    REVISION_CENTER("Spaced Rep", Icons.Filled.Update, Icons.Outlined.Update),
    KNOWLEDGE_VAULT("Vault", Icons.Filled.FolderZip, Icons.Outlined.FolderZip),
    AI_COACH("AI Coach", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent),
    COMMERCE_COMPANION("Commerce", Icons.Filled.QueryStats, Icons.Outlined.QueryStats),
    ANALYTICS("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainDashboardScreen(viewModel: StudyViewModel) {
    val currentTab = remember { mutableStateOf(ScreenTab.DASHBOARD) }
    val profile by viewModel.userProfile.collectAsState()
    
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Gamification level and streak triggers
    val levelUpEvent by viewModel.showLevelUpDialog.collectAsState()
    val streakEvent by viewModel.showStreakCongrats.collectAsState()

    // 5 main bottom tab limits 
    val navTabs = listOf(
        ScreenTab.DASHBOARD,
        ScreenTab.STUDY_PLANNER,
        ScreenTab.KNOWLEDGE_VAULT,
        ScreenTab.AI_COACH,
        ScreenTab.PROFILE
    )

    if (!profile.isOnboarded) {
        // Render step by step Onboarding flow on clean install
        OnboardingScreen(viewModel = viewModel)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DeepSpace,
                            titleContentColor = ActiveWhite
                        ),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.linearGradient(listOf(SpaceNeonTeal, NeonPurple))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "MC",
                                        fontWeight = FontWeight.Bold,
                                        color = DeepSpace,
                                        fontSize = 16.sp
                                    )
                                }
                                Column {
                                    Text(
                                        text = "MISSION CONTROL",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${profile.stream} • ${profile.classLevel}",
                                        fontSize = 10.sp,
                                        color = MutedSlate,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        },
                        actions = {
                            // Streaking Flame
                            Row(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SlateLight)
                                    .border(1.dp, BorderSlate, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.awardXp(15) } // Reward debug clicks
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "🔥", fontSize = 14.sp)
                                Text(
                                    text = "${profile.streak} Days",
                                    color = GoldYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            // Level Badge
                            Row(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.linearGradient(listOf(NeonPurple, SlateLight)))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Lvl ${profile.level}",
                                    color = ActiveWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!isTablet) {
                        // Customized Bottom Navigation Bar restricting to EXTREMELY clean 5 tabs
                        Column(
                            modifier = Modifier
                                .background(NavDarkBg)
                                .navigationBarsPadding()
                        ) {
                            HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NavDarkBg)
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                navTabs.forEach { tab ->
                                    val isSelected = currentTab.value == tab
                                    val color = if (isSelected) SpaceNeonTeal else MutedSlate
                                    val bg = if (isSelected) SlateLight else Color.Transparent

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bg)
                                            .clickable { currentTab.value = tab }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("nav_tab_${tab.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) tab.iconSelected else tab.iconUnselected,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(20.dp),
                                                tint = color
                                            )
                                            Text(
                                                text = tab.title,
                                                fontSize = 9.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) ActiveWhite else MutedSlate
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                containerColor = DeepSpace
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Adaptive Rail navigation for Wide layout tablet screens
                    if (isTablet) {
                        NavigationRail(
                            containerColor = SlateGray,
                            header = {
                                Text(
                                    text = "DECK",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedSlate,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            navTabs.forEach { tab ->
                                val isSelected = currentTab.value == tab
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { currentTab.value = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.iconSelected else tab.iconUnselected,
                                            contentDescription = tab.title,
                                            tint = if (isSelected) SpaceNeonTeal else MutedSlate
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp,
                                            color = if (isSelected) ActiveWhite else MutedSlate,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        indicatorColor = SlateLight
                                    )
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(0.5.dp)
                                .background(BorderSlate)
                        )
                    }

                    // Core Page Routing
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(DeepSpace)
                    ) {
                        AnimatedContent(
                            targetState = currentTab.value,
                            transitionSpec = {
                                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                            },
                            label = "tab_crossfade"
                        ) { targetState ->
                            when (targetState) {
                                ScreenTab.DASHBOARD -> DashboardPage(viewModel, onNavigateTo = { currentTab.value = it })
                                ScreenTab.LEARNING_PATH -> LearningPathPage(viewModel)
                                ScreenTab.COLLAB_STUDY -> CollabStudyPage(viewModel)
                                ScreenTab.BRAIN_DUMP -> BrainDumpPage(viewModel)
                                ScreenTab.TODAY_TOP_3 -> TodayTop3Page(viewModel)
                                ScreenTab.STUDY_PLANNER -> StudyPlannerPage(viewModel)
                                ScreenTab.REVISION_CENTER -> RevisionCenterPage(viewModel)
                                ScreenTab.KNOWLEDGE_VAULT -> KnowledgeVaultPage(viewModel)
                                ScreenTab.AI_COACH -> AiCoachPage(viewModel)
                                ScreenTab.COMMERCE_COMPANION -> CommerceCompanionPage(viewModel)
                                ScreenTab.ANALYTICS -> AnalyticsPage(viewModel)
                                ScreenTab.PROFILE -> ProfileSettingsPage(viewModel)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // DELIGHTFUL GAMIFICATION OVERLAYS
            // ==========================================

            // 1. Level-Up Full-screen Overlay Dialog
            levelUpEvent?.let { nextLevel ->
                val levelName = when (nextLevel) {
                    1 -> "New Recruit"
                    2 -> "Focused Learner"
                    3 -> "Consistent Performer"
                    4 -> "Academic Warrior"
                    else -> "Elite Scholar"
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(enabled = true, onClick = {}) // Block background clicks
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, SpaceNeonTeal, RoundedCornerShape(24.dp))
                            .testTag("level_up_dialog"),
                        colors = CardDefaults.cardColors(containerColor = SlateGray),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🎉 LEVEL UP! 🎉",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = SpaceNeonTeal
                            )

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(SpaceNeonTeal, NeonPurple))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Lvl\n$nextLevel",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DeepSpace,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }

                            Text(
                                text = "Rank Achieved: $levelName",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Awesome progress! Your AI guidance operating systems have advanced. Daily goal limits have auto-scaled.",
                                fontSize = 13.sp,
                                color = MutedSlate,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = { viewModel.clearLevelUpDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("level_up_close")
                            ) {
                                Text("Back to Deck", color = DeepSpace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Streak Celebrate Dialog
            streakEvent?.let { day ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.80f))
                        .clickable(enabled = true, onClick = {})
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, GoldYellow, RoundedCornerShape(24.dp))
                            .testTag("streak_celebrate_dialog"),
                        colors = CardDefaults.cardColors(containerColor = SlateGray),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "STREAK ENGAGED! 🔥",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldYellow
                            )

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(SlateLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🔥", fontSize = 42.sp)
                            }

                            Text(
                                text = "Day $day Study Lock activated!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveWhite,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Your streak is now ignited! Consistent study patterns enable dynamic memory retention bonuses (+10% auto-scaling XP increments)!",
                                fontSize = 12.sp,
                                color = MutedSlate,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Button(
                                onClick = { viewModel.clearStreakCongrats() },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("streak_close")
                            ) {
                                Text("Keep Winning ⚡", color = DeepSpace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// XP Tracker Level Meter Top Row inside pages
@Composable
fun LevelProgressHeader(profile: UserProfile) {
    val levelName = when (profile.level) {
        1 -> "Beginner"
        2 -> "Focused Learner"
        3 -> "Consistent Performer"
        4 -> "Top Student"
        else -> "Elite Achiever"
    }

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

    val xpRange = (nextXpMax - prevXpMax).toFloat()
    val progress = ((profile.xp - prevXpMax).toFloat() / xpRange).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Level ${profile.level}: $levelName",
                        color = ActiveWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Next tier at $nextXpMax XP • Earn XP from tasks & revision!",
                        color = MutedSlate,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${profile.xp} / $nextXpMax XP",
                    color = GoldYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = progress,
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
