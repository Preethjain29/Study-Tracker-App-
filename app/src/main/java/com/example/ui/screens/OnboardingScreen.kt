package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(viewModel: StudyViewModel) {
    var step by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Onboarding Form States
    var fullName by remember { mutableStateOf("") }
    var classLevel by remember { mutableStateOf("Class 11") }
    var stream by remember { mutableStateOf("Commerce") }
    var subjects by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf("2026-07-15") }
    var studyGoalHours by remember { mutableStateOf(4) }
    var careerGoal by remember { mutableStateOf("") }

    // Generation State
    var isGenerating by remember { mutableStateOf(false) }
    var generationProgress by remember { mutableStateOf(0f) }
    var generationStatusMessage by remember { mutableStateOf("Initializing desktop workspace...") }

    // Soft animated card scaling
    val containerTransition = updateTransition(targetState = step, label = "step_transition")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isGenerating) {
            // STEP 7: Personalized Generation Screen
            LaunchedEffect(Unit) {
                scope.launch {
                    val messages = listOf(
                        "Analyzing Chosen Stream curriculum...",
                        "Clearing older database caches...",
                        "Seeding uncompleted review chapters...",
                        "Synthesizing customized learning pathways...",
                        "Customizing cohort collaboration desk...",
                        "Compiling Daily Mission control logs..."
                    )
                    for (i in 0..100) {
                        delay(25)
                        generationProgress = i / 100f
                        val msgIndex = (generationProgress * (messages.size - 1)).toInt()
                        generationStatusMessage = messages[msgIndex.coerceIn(0, messages.size - 1)]
                    }
                    // Trigger dynamic onboarding seed
                    viewModel.onboardUser(
                        name = if (fullName.isNotBlank()) fullName else "Preeth",
                        classLevel = classLevel,
                        stream = stream,
                        subjects = if (subjects.isNotBlank()) subjects else {
                            if (stream == "Commerce") "Accountancy, Economics"
                            else if (stream == "Science") "Physics, Chemistry"
                            else "History, Geography"
                        },
                        examDate = examDate,
                        studyGoalHours = studyGoalHours,
                        careerGoal = if (careerGoal.isNotBlank()) careerGoal else {
                            if (stream == "Commerce") "Chartered Accountant"
                            else if (stream == "Science") "Research Specialist"
                            else "Civil Services"
                        }
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(SpaceNeonTeal, NeonPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudSync,
                        contentDescription = "Sync",
                        tint = DeepSpace,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = "Generating Personalized Dashboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ActiveWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = generationStatusMessage,
                    fontSize = 14.sp,
                    color = MutedSlate,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = generationProgress,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .testTag("onboarding_progress"),
                    color = SpaceNeonTeal,
                    trackColor = SlateLight
                )

                Text(
                    text = "${(generationProgress * 100).toInt()}% Completed",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpaceNeonTeal
                )
            }
        } else {
            // Main Onboarding content
            Column(
                modifier = Modifier.fillMaxWidth(if (LocalConfiguration.current.screenWidthDp >= 600) 0.6f else 1f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Step indicator
                if (step > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = MutedSlate)
                        }
                        Text(
                            text = "Step $step of 6",
                            fontSize = 13.sp,
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(48.dp)) // Equalizer
                    }
                }

                // Step specific card content with entry transition animations
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                ) {
                    when (step) {
                        1 -> WelcomeStep(
                            fullName = fullName,
                            onNameChange = { fullName = it },
                            onNext = { step++ }
                        )
                        2 -> ClassSelectionStep(
                            selectedClass = classLevel,
                            onClassSelected = { classLevel = it },
                            onNext = { step++ }
                        )
                        3 -> StreamSelectionStep(
                            selectedStream = stream,
                            onStreamSelected = {
                                stream = it
                                // Set default subjects based on stream
                                subjects = when (it) {
                                    "Science" -> "Physics, Chemistry"
                                    "Arts" -> "History, Geography"
                                    else -> "Accountancy, Economics"
                                }
                            },
                            onNext = { step++ }
                        )
                        4 -> SubjectSelectionStep(
                            subjects = subjects,
                            onSubjectsChange = { subjects = it },
                            careerGoal = careerGoal,
                            onCareerGoalChange = { careerGoal = it },
                            onNext = { step++ }
                        )
                        5 -> ExamDateStep(
                            examDate = examDate,
                            onExamDateChange = { examDate = it },
                            onNext = { step++ }
                        )
                        6 -> DailyGoalStep(
                            studyGoalHours = studyGoalHours,
                            onStudyHoursChange = { studyGoalHours = it },
                            onNext = { isGenerating = true }
                        )
                    }
                }
            }
        }
    }
}

// --- Step 1: Welcome Screen ---
@Composable
fun WelcomeStep(fullName: String, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(SpaceNeonTeal, NeonPurple))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.OfflineBolt,
                contentDescription = "Mission Control icon",
                tint = DeepSpace,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mission Control",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your AI Study Operating System",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SpaceNeonTeal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGray)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Welcome to a clean study slate. No distraction, zero clutter, 100% focus. Tell us your name to ignite your study spaceship launch pad.",
                    fontSize = 13.sp,
                    color = MutedSlate,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = onNameChange,
                    label = { Text("Enter Your Name") },
                    placeholder = { Text("Preeth") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpaceNeonTeal,
                        unfocusedBorderColor = BorderSlate,
                        focusedLabelColor = SpaceNeonTeal,
                        unfocusedLabelColor = MutedSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { onNext() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("welcome_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Ignite Operating System 🚀",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepSpace
                    )
                }
            }
        }
    }
}

// --- Step 2: Class Selection ---
@Composable
fun ClassSelectionStep(selectedClass: String, onClassSelected: (String) -> Unit, onNext: () -> Unit) {
    val classes = listOf("Class 11", "Class 12", "Undergraduate / UG")
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "What class are you in? 🎓",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Mission Control will configure the syllabus, question depth, and schedules matching your academic standard.",
            fontSize = 13.sp,
            color = MutedSlate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        classes.forEach { classItem ->
            val isSelected = selectedClass == classItem
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SpaceNeonTeal else BorderSlate,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onClassSelected(classItem) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SlateLight else SlateGray
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = classItem,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SpaceNeonTeal else ActiveWhite
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onClassSelected(classItem) },
                        colors = RadioButtonDefaults.colors(selectedColor = SpaceNeonTeal)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("class_next_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepSpace
            )
        }
    }
}

// --- Step 3: Stream Selection ---
@Composable
fun StreamSelectionStep(selectedStream: String, onStreamSelected: (String) -> Unit, onNext: () -> Unit) {
    val streams = listOf(
        Triple("Commerce", "Accountancy & Economics", "📊"),
        Triple("Science", "Physics, Chemistry & Math/Bio", "🔬"),
        Triple("Arts", "History, Political Sci & Geography", "🎨")
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Your Academic Stream 🌟",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite,
            textAlign = TextAlign.Center
        )

        Text(
            text = "We instantly create dedicated study chapters, schedules, and pathways for your selected stream.",
            fontSize = 13.sp,
            color = MutedSlate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        streams.forEach { (streamName, details, emoji) ->
            val isSelected = selectedStream == streamName
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SpaceNeonTeal else BorderSlate,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onStreamSelected(streamName) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SlateLight else SlateGray
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) SpaceNeonTeal else SlateLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = streamName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SpaceNeonTeal else ActiveWhite
                        )
                        Text(
                            text = details,
                            fontSize = 11.sp,
                            color = MutedSlate
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onStreamSelected(streamName) },
                        colors = RadioButtonDefaults.colors(selectedColor = SpaceNeonTeal)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("stream_next_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Set Theme Stream",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepSpace
            )
        }
    }
}

// --- Step 4: Subject Selection & Career Goal ---
@Composable
fun SubjectSelectionStep(
    subjects: String,
    onSubjectsChange: (String) -> Unit,
    careerGoal: String,
    onCareerGoalChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Subjects & Career Ambitions 🚀",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Enter subjects separated by comma. Provide your ultimate exam target or career goal for the AI Coach.",
            fontSize = 13.sp,
            color = MutedSlate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGray)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = subjects,
                    onValueChange = onSubjectsChange,
                    label = { Text("Core Subjects") },
                    placeholder = { Text("Accountancy, Economics") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpaceNeonTeal,
                        unfocusedBorderColor = BorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SpaceNeonTeal,
                        unfocusedLabelColor = MutedSlate
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = careerGoal,
                    onValueChange = onCareerGoalChange,
                    label = { Text("Desired Career / Exam Target") },
                    placeholder = { Text("Chartered Accountant (CA)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpaceNeonTeal,
                        unfocusedBorderColor = BorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SpaceNeonTeal,
                        unfocusedLabelColor = MutedSlate
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("subjects_next_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepSpace
            )
        }
    }
}

// --- Step 5: Exam Dates Setup ---
@Composable
fun ExamDateStep(examDate: String, onExamDateChange: (String) -> Unit, onNext: () -> Unit) {
    val datePresets = listOf(
        "Next 30 Days" to "2026-07-03",
        "Next 3 Months" to "2026-09-03",
        "Mid-Syllabus Term" to "2026-10-15",
        "Final Term Exams" to "2026-12-20"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "When is your next Exam? 📅",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Exam date configures the revision engine. If exams are in under 30 days, emergency booster rules activate automatically.",
            fontSize = 13.sp,
            color = MutedSlate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = examDate,
            onValueChange = onExamDateChange,
            label = { Text("Target Exam Date (YYYY-MM-DD)") },
            placeholder = { Text("2026-12-31") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SpaceNeonTeal,
                unfocusedBorderColor = BorderSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = SpaceNeonTeal,
                unfocusedLabelColor = MutedSlate
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            datePresets.take(2).forEach { (label, presetDate) ->
                val isSel = examDate == presetDate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) SlateLight else SlateGray)
                        .border(1.dp, if (isSel) SpaceNeonTeal else BorderSlate, RoundedCornerShape(12.dp))
                        .clickable { onExamDateChange(presetDate) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label, color = if (isSel) SpaceNeonTeal else ActiveWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            datePresets.drop(2).forEach { (label, presetDate) ->
                val isSel = examDate == presetDate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) SlateLight else SlateGray)
                        .border(1.dp, if (isSel) SpaceNeonTeal else BorderSlate, RoundedCornerShape(12.dp))
                        .clickable { onExamDateChange(presetDate) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label, color = if (isSel) SpaceNeonTeal else ActiveWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("exam_next_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepSpace
            )
        }
    }
}

// --- Step 6: Study Daily Goal Setup ---
@Composable
fun DailyGoalStep(studyGoalHours: Int, onStudyHoursChange: (Int) -> Unit, onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Study Target 🎯",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = ActiveWhite,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Set how many hours you want to invest in deep focused study sessions daily.",
            fontSize = 13.sp,
            color = MutedSlate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, BorderSlate, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateGray)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${studyGoalHours} Hours / Day",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = SpaceNeonTeal
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (studyGoalHours > 1) onStudyHoursChange(studyGoalHours - 1) },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SlateLight)
                    ) {
                        Icon(Icons.Filled.Remove, "Less hours", tint = ActiveWhite, modifier = Modifier.size(24.dp))
                    }

                    IconButton(
                        onClick = { if (studyGoalHours < 12) onStudyHoursChange(studyGoalHours + 1) },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SlateLight)
                    ) {
                        Icon(Icons.Filled.Add, "More hours", tint = ActiveWhite, modifier = Modifier.size(24.dp))
                    }
                }

                Text(
                    text = when {
                        studyGoalHours <= 2 -> "🌱 Casual Learner - Great for general study tracking."
                        studyGoalHours <= 4 -> "🔥 Focused Achiever - Perfect balance of exams and school."
                        else -> "⚡ Academic Champion - Elite high-frequency booster pace!"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedSlate,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_generate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SpaceNeonTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Generate Personalized Dashboard ✨",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepSpace
            )
        }
    }
}
