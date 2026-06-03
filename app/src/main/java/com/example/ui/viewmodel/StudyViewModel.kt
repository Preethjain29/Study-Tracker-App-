package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = StudyRepository(database.dao())

    // --- State Streams ---
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val allBrainItems: StateFlow<List<BrainItem>> = repository.allBrainItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allChapters: StateFlow<List<Chapter>> = repository.allChapters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSchedules: StateFlow<List<SpacedRepSchedule>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allVaultDocs: StateFlow<List<VaultDoc>> = repository.allVaultDocs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allStudyLogs: StateFlow<List<StudyLog>> = repository.allStudyLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLearningPaths: StateFlow<List<LearningPath>> = repository.allLearningPaths
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allStudyGroups: StateFlow<List<StudyGroup>> = repository.allStudyGroups
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Energy Planner State ---
    private val _userEnergy = MutableStateFlow("Normal") // Fresh, Normal, Tired
    val userEnergy: StateFlow<String> = _userEnergy.asStateFlow()

    // --- Timer States ---
    private val _timerDuration = MutableStateFlow(25 * 60) // in seconds
    val timerDuration: StateFlow<Int> = _timerDuration.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerMode = MutableStateFlow("Pomodoro") // Pomodoro, Deep Work, Custom
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    // --- AI Operations States ---
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiAnswer = MutableStateFlow("")
    val aiAnswer: StateFlow<String> = _aiAnswer.asStateFlow()

    private val _decodedResult = MutableStateFlow("")
    val decodedResult: StateFlow<String> = _decodedResult.asStateFlow()

    private val _ragResult = MutableStateFlow("")
    val ragResult: StateFlow<String> = _ragResult.asStateFlow()

    // Chat History
    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Hello! I am your Mission Control AI Study Coach. Let me know how I can assist with Class 11 preparation, exam targets, or general productivity!" to false
        )
    )
    val chatHistory: StateFlow<List<Pair<String, Boolean>>> = _chatHistory.asStateFlow()

    init {
        // Start simple background ticker for study timer
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (_timerRunning.value) {
                    _timerDuration.update {
                        if (it > 1) {
                            it - 1
                        } else {
                            _timerRunning.value = false
                            // Log focused time
                            logFocusSessionFinished()
                            25 * 60
                        }
                    }
                }
            }
        }
    }

    // --- Core Operations ---

    // 1. Brain Dump Categorizer
    fun addBrainItem(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val prompt = """
                The student has inputted this task in their study operating system brain dump inbox: "$title"
                Determine a brief study duration (in minutes, e.g., 30, 45, 60, 90), priority score (between 1 and 100), and auto-categorize it into exactly one of: Study, Personal, Health, Business, Finance, Future Ideas.
                Format the answer strictly in JSON without any markdown formatting wrappers (just the raw JSON string):
                {
                  "category": "Study",
                  "priorityScore": 75,
                  "estimatedDurationMinutes": 45
                }
            """.trimIndent()

            val aiResponse = GeminiClient.generateContent(
                prompt = prompt,
                systemInstruction = "You are a senior productivity categorizer. Always respond in strict raw JSON format without backticks or markdown wrap."
            )

            var category = "Study"
            var priorityScore = 50
            var duration = 45

            try {
                // Strip markdown formatting if any present
                val cleanedJson = aiResponse.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = JSONObject(cleanedJson)
                category = json.optString("category", "Study")
                priorityScore = json.optInt("priorityScore", 50)
                duration = json.optInt("estimatedDurationMinutes", 45)
            } catch (e: Exception) {
                Log.e("StudyViewModel", "Error parsing AI JSON, using defaults", e)
                // Fallback analysis manually
                if (title.contains("Accounts", true) || title.contains("Chapter", true) || title.contains("Learn", true) || title.contains("Revise", true)) {
                    category = "Study"
                    priorityScore = 80
                } else if (title.contains("money", true) || title.contains("buy", true) || title.contains("Finance", true)) {
                    category = "Finance"
                    priorityScore = 50
                } else if (title.contains("Run", true) || title.contains("Gym", true) || title.contains("Health", true)) {
                    category = "Health"
                    priorityScore = 40
                }
            }

            val newItem = BrainItem(
                title = title,
                category = category,
                priorityScore = priorityScore,
                estimatedDurationMinutes = duration,
                isTopPriority = false
            )
            repository.saveBrainItem(newItem)
            _aiLoading.value = false
        }
    }

    fun toggleBrainItemPriority(item: BrainItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBrainItem(item.copy(isTopPriority = !item.isTopPriority))
        }
    }

    fun completeBrainItem(item: BrainItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(isCompleted = !item.isCompleted)
            repository.updateBrainItem(updated)
            if (updated.isCompleted) {
                awardXp(20)
            }
        }
    }

    fun deleteBrainItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBrainItem(id)
        }
    }

    fun updateEnergy(energy: String) {
        _userEnergy.value = energy
    }

    // 2. Active Spaced Repetition Loader
    fun completeChapter(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = chapter.copy(
                isCompleted = true,
                lastRevisedTime = System.currentTimeMillis()
            )
            repository.updateChapter(updated)
            
            // Delete old schedules if any
            repository.deleteSchedulesForChapter(chapter.id)

            // Auto schedule Day 1, 3, 7, 15, 30
            val now = System.currentTimeMillis()
            val schedules = listOf(
                SpacedRepSchedule(chapterId = chapter.id, chapterName = chapter.name, subject = chapter.subject, scheduledDayIndex = 1, targetDateMillis = now + 86400000 * 1),
                SpacedRepSchedule(chapterId = chapter.id, chapterName = chapter.name, subject = chapter.subject, scheduledDayIndex = 3, targetDateMillis = now + 86400000 * 3),
                SpacedRepSchedule(chapterId = chapter.id, chapterName = chapter.name, subject = chapter.subject, scheduledDayIndex = 7, targetDateMillis = now + 86400000 * 7),
                SpacedRepSchedule(chapterId = chapter.id, chapterName = chapter.name, subject = chapter.subject, scheduledDayIndex = 15, targetDateMillis = now + 86400000 * 15),
                SpacedRepSchedule(chapterId = chapter.id, chapterName = chapter.name, subject = chapter.subject, scheduledDayIndex = 30, targetDateMillis = now + 86400000 * 30)
            )

            repository.saveSchedules(schedules)
            awardXp(40)
        }
    }

    fun performSpacedRepetitionReview(schedule: SpacedRepSchedule, ratingStar: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSchedule(schedule.copy(isReviewed = true, reviewRating = ratingStar))
            
            // Update Chapter retention score based on review rating
            val chapter = repository.getChapterById(schedule.chapterId)
            if (chapter != null) {
                val addedScore = when (ratingStar) {
                    5 -> 25
                    4 -> 15
                    3 -> 5
                    else -> -10
                }
                val newScore = (chapter.retentionScore + addedScore).coerceIn(0, 100)
                repository.updateChapter(chapter.copy(
                    lastRevisedTime = System.currentTimeMillis(),
                    retentionScore = newScore
                ))
            }
            awardXp(30)
        }
    }

    // 3. AI Question Decoder
    fun analyzeAndDecodeQuestion(question: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            _decodedResult.value = ""
            val prompt = """
                Decode and explain this exam question for Class 11 students:
                "$question"

                Format your response beautiful and clear, matching these headings:
                ### Question Analysis
                **Subject:** 
                **Chapter:** 
                **Topic:** 
                **Difficulty:** 

                #### What is the examiner asking?
                (Explain the core catch of the question elegantly)

                #### Important Concepts
                *   (Concept 1 details)
                *   (Concept 2 details)

                #### Step-by-Step Solving Approach
                1. 
                2. 
                3. 

                #### Common Mistakes
                *   (Common pitfall details)

                #### Similar Questions
                1. 
                2. 
            """.trimIndent()

            val response = GeminiClient.generateContent(
                prompt = prompt,
                systemInstruction = "You are a top education coach specialized in STEM and Commerce subjects. Always write precise, source-grounded material."
            )
            _decodedResult.value = response
            _aiLoading.value = false
        }
    }

    // 4. NotebookLM grounded RAG search
    fun uploadDocumentMock(name: String, notesText: String, type: String = "Notes") {
        viewModelScope.launch(Dispatchers.IO) {
            val fileDoc = VaultDoc(
                title = name,
                mimeType = type,
                content = notesText,
                isProcessed = true
            )
            repository.saveVaultDoc(fileDoc)
            awardXp(25)
        }
    }

    fun deleteDocument(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVaultDoc(id)
        }
    }

    fun groundedQueryNotebookLM(userQuery: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            _ragResult.value = ""

            // Retrieve all local files in standard vault
            val docs = repository.allVaultDocs.first()
            
            // Build the Grounding context
            val groundBuilder = StringBuilder()
            if (docs.isEmpty()) {
                groundBuilder.append("User has no uploaded files in Vault. Use default scientific/commerce general principles.")
            } else {
                docs.forEach { doc ->
                    groundBuilder.append("Uploaded Doc: [${doc.title}] (${doc.mimeType})\nContent:\n${doc.content}\n\n")
                }
            }

            val prompt = """
                You are playing the role of NotebookLM. The student asks: "$userQuery"

                Use the following User-Uploaded Document contexts as your ABSOLUTE source of truth. Prioritize them over general internet knowledge:
                
                $groundBuilder

                Every claim must cite page number, chapter, document name, confidence score, and references.
                Format layout:
                ### Source Grounded Answer
                **Source Document Name:** (Name)
                **Confidence Score:** (0-100%)
                **Reference Snippet:** (Exact exact text excerpt)

                #### Explanation
                (Clear explanations)
            """.trimIndent()

            val response = GeminiClient.generateContent(prompt)
            _ragResult.value = response
            _aiLoading.value = false
        }
    }

    // 5. Smart Study Timers
    fun startTimer() {
        _timerRunning.value = true
    }

    fun pauseTimer() {
        _timerRunning.value = false
    }

    fun resetTimer() {
        _timerRunning.value = false
        _timerDuration.value = when (_timerMode.value) {
            "Pomodoro" -> 25 * 60
            "Deep Work" -> 50 * 60
            else -> 15 * 60
        }
    }

    fun updateTimerMode(mode: String) {
        _timerMode.value = mode
        _timerRunning.value = false
        _timerDuration.value = when (mode) {
            "Pomodoro" -> 25 * 60
            "Deep Work" -> 50 * 60
            else -> 15 * 60
        }
    }

    private fun logFocusSessionFinished() {
        viewModelScope.launch(Dispatchers.IO) {
            val minutes = when (_timerMode.value) {
                "Pomodoro" -> 25
                "Deep Work" -> 50
                else -> 15
            }
            repository.saveStudyLog(
                StudyLog(
                    durationMinutes = minutes,
                    focusMode = _timerMode.value,
                    energyState = _userEnergy.value,
                    category = "Study"
                )
            )
            awardXp(minutes)
        }
    }

    // --- Celebration and Alert States ---
    private val _showLevelUpDialog = MutableStateFlow<Int?>(null)
    val showLevelUpDialog: StateFlow<Int?> = _showLevelUpDialog.asStateFlow()

    private val _showStreakCongrats = MutableStateFlow<Int?>(null)
    val showStreakCongrats: StateFlow<Int?> = _showStreakCongrats.asStateFlow()

    fun clearLevelUpDialog() { _showLevelUpDialog.value = null }
    fun clearStreakCongrats() { _showStreakCongrats.value = null }

    // 6. Gamification System & Profile Edit
    fun awardXp(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileDirect() ?: UserProfile()
            val nextXp = profile.xp + amount
            
            // Re-calculate Level
            // Level 1: <100 XP
            // Level 2: 100-300 XP
            // Level 3: 300-600 XP
            // Level 4: 600-1000 XP
            // Level 5: 1000+ XP
            val nextLevel = when {
                nextXp < 100 -> 1
                nextXp < 300 -> 2
                nextXp < 600 -> 3
                nextXp < 1000 -> 4
                else -> 5
            }
            
            if (nextLevel > profile.level) {
                _showLevelUpDialog.value = nextLevel
            }
            
            repository.saveProfile(profile.copy(xp = nextXp, level = nextLevel))
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveProfile(profile)
        }
    }

    fun onboardUser(
        name: String,
        classLevel: String,
        stream: String,
        subjects: String,
        examDate: String,
        studyGoalHours: Int,
        careerGoal: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Guarantee fresh slate
            repository.clearAllData()
            
            // Seed Chapters based on Stream choice
            when (stream) {
                "Science" -> {
                    repository.saveChapter(Chapter(name = "Units & Measurements", subject = "Physics", stream = "Science", isCompleted = false, difficulty = "Easy", retentionScore = 0))
                    repository.saveChapter(Chapter(name = "Laws of Motion", subject = "Physics", stream = "Science", isCompleted = false, difficulty = "Hard", retentionScore = 0))
                    repository.saveChapter(Chapter(name = "Chemical Bonding", subject = "Chemistry", stream = "Science", isCompleted = false, difficulty = "Medium", retentionScore = 0))
                    repository.saveChapter(Chapter(name = "Structure of Atom", subject = "Chemistry", stream = "Science", isCompleted = false, difficulty = "Hard", retentionScore = 0))
                }
                "Arts" -> {
                    repository.saveChapter(Chapter(name = "Early Empires & Trade Routes", subject = "History", stream = "Arts", isCompleted = false, difficulty = "Medium", retentionScore = 0))
                    repository.saveChapter(Chapter(name = "Constitutional Design Principles", subject = "Political Science", stream = "Arts", isCompleted = false, difficulty = "Easy", retentionScore = 0))
                    repository.saveChapter(Chapter(name = "Geomorphic Processes & Landforms", subject = "Geography", stream = "Arts", isCompleted = false, difficulty = "Hard", retentionScore = 0))
                }
                else -> { // Commerce
                    repository.saveChapter(Chapter(name = "Introduction to Accounting", subject = "Accountancy", stream = "Commerce", isCompleted = false, difficulty = "Easy", retentionScore = 0))
                    repository.saveChapter(Chapter(name = "Recording of Transactions (Ledgers)", subject = "Accountancy", stream = "Commerce", isCompleted = false, difficulty = "Hard", retentionScore = 0))
                    repository.saveChapter(Chapter(name = "Consumer's Demand & Equilibrium", subject = "Economics", stream = "Commerce", isCompleted = false, difficulty = "Medium", retentionScore = 0))
                }
            }

            // Seed initial personalized Learning Path
            val primarySubject = if (subjects.contains(",")) subjects.split(",")[0].trim() else if (subjects.isNotBlank()) subjects else "Core Focus"
            val pathId = repository.saveLearningPath(
                LearningPath(
                    title = "Alpha $primarySubject Pathway",
                    subject = primarySubject,
                    completedMilestones = 0,
                    totalMilestones = 3,
                    isCompleted = false,
                    xpReward = 150,
                    badgeUnlocked = "Gold Star Pathfinder",
                    description = "Master your stream's flagship concepts step-by-step with guided review lessons."
                )
            )
            repository.saveMilestones(
                listOf(
                    PathMilestone(pathId = pathId, title = "Read Core Concept Summary", description = "Understand primary formulas and definitions", isCompleted = false, xpReward = 20, milestoneType = "Reading"),
                    PathMilestone(pathId = pathId, title = "Attempt 5 Assessment Exercises", description = "Apply concept knowledge through guided exercises", isCompleted = false, xpReward = 30, milestoneType = "Practice Questions"),
                    PathMilestone(pathId = pathId, title = "Draft Diagnostic Formula Summary", description = "Grasp final revision summary points", isCompleted = false, xpReward = 50, milestoneType = "Mastering Concept")
                )
            )

            // Seed clean starter Tasks (Today's Mission Top 3!)
            repository.saveBrainItem(
                BrainItem(
                    title = "Set up Day 1 Study Plan for $primarySubject",
                    isCompleted = false,
                    category = "Study",
                    estimatedDurationMinutes = 30,
                    priorityScore = 95,
                    isTopPriority = true
                )
            )
            repository.saveBrainItem(
                BrainItem(
                    title = "Schedule introductory review session",
                    isCompleted = false,
                    category = "Study",
                    estimatedDurationMinutes = 45,
                    priorityScore = 85,
                    isTopPriority = true
                )
            )
            repository.saveBrainItem(
                BrainItem(
                    title = "Draft list of study resources & exam syllabus",
                    isCompleted = false,
                    category = "Personal",
                    estimatedDurationMinutes = 15,
                    priorityScore = 70,
                    isTopPriority = true
                )
            )

            // Seed standard initial study cohort group for collaborative interactions
            repository.saveStudyGroup(
                StudyGroup(
                    name = "$stream Study Cohort Alpha 🚀",
                    description = "Warm welcome! Collaborative team workspace for stream chapter discussions.",
                    subjectFocus = stream
                )
            )

            // Save Onboarded User Profile
            repository.saveProfile(
                UserProfile(
                    id = 1,
                    name = name,
                    classLevel = classLevel,
                    stream = stream,
                    subjects = subjects,
                    studyHoursAvailable = studyGoalHours,
                    examDate = examDate,
                    careerGoal = if (careerGoal.isNotBlank()) careerGoal else "Dynamic Achiever",
                    xp = 0,
                    level = 1,
                    streak = 1, // Start streak at 1 on onboarding!
                    isOnboarded = true
                )
            )
            
            // Trigger onboarding/streak celebrate!
            _showStreakCongrats.value = 1
        }
    }

    // 7. AI Study Coach chat
    fun sendCoachMessage(text: String) {
        if (text.isBlank()) return
        
        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add(text to true)
        _chatHistory.value = currentHistory

        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            
            val profile = repository.getUserProfileDirect() ?: UserProfile()
            val taskPrompt = """
                User Profile Info: Class info: ${profile.classLevel}, Stream: ${profile.stream}, Taraget Career Goal: ${profile.careerGoal}.
                Recent weak subjects: ${profile.weakSubjects}, strong areas: ${profile.strongSubjects}.
                XP Score: ${profile.xp}, Study hours target: ${profile.studyHoursAvailable}h.
                Is exam backup mode active: ${profile.isEmergencyMode}.

                Chat History Context:
                ${currentHistory.takeLast(6).joinToString("\n") { (msg, isUser) -> if (isUser) "Student: $msg" else "Coach: $msg" }}

                Student Message: "$text"
            """.trimIndent()

            val aiResponse = GeminiClient.generateContent(
                prompt = taskPrompt,
                systemInstruction = "You are the Mission Control AI Study Coach. Your personality is friendly, encouraging, direct, practical, and highly concise. Never speak in long, repetitive paragraphs; keep your answers conversational, ultra-practical, and action-oriented. Provide precise, gamified study tips and advice tailored to the student's profile, subjects (e.g., Accountancy, Economics, or Science), and current XP score, like an elite mentor."
            )

            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add(aiResponse to false)
            _chatHistory.value = updatedHistory
            
            _aiLoading.value = false
            awardXp(10)
        }
    }

    fun runExamEmergencyMode(activate: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileDirect() ?: UserProfile()
            repository.saveProfile(profile.copy(isEmergencyMode = activate))
        }
    }

    // Custom helper generators
    fun generateQuickFormulaSheet(chapterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val chapter = repository.getChapterById(chapterId)
            if (chapter != null) {
                val prompt = "Create a high-yield Cheat Sheet and accounting/mathematics Formulas & Concepts list for Chapter: '${chapter.name}' of Subject: '${chapter.subject}'. Focus strictly on Exam questions preparation tips."
                val response = GeminiClient.generateContent(prompt)
                repository.updateChapter(chapter.copy(formulaSheet = response))
            }
            _aiLoading.value = false
            awardXp(15)
        }
    }

    fun generateChapterSummaryNotes(chapterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val chapter = repository.getChapterById(chapterId)
            if (chapter != null) {
                val prompt = "Write a comprehensive Chapter Summary, key numerical Ledger principles, and Quick Revision notes for Chapter: '${chapter.name}' in '${chapter.subject}'."
                val response = GeminiClient.generateContent(prompt)
                repository.updateChapter(chapter.copy(quickNotes = response))
            }
            _aiLoading.value = false
            awardXp(15)
        }
    }

    fun generateChapterMindMap(chapterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val chapter = repository.getChapterById(chapterId)
            if (chapter != null) {
                val prompt = "Produce a hierarchical text-based Chapter Mindmap layout (with root nodes, branches, and leaf formulas) for Chapter: '${chapter.name}' in subject: '${chapter.subject}'."
                val response = GeminiClient.generateContent(prompt)
                repository.updateChapter(chapter.copy(mindMapSummary = response))
            }
            _aiLoading.value = false
            awardXp(15)
        }
    }

    fun generateInteractiveFlashcards(chapterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val chapter = repository.getChapterById(chapterId)
            if (chapter != null) {
                val prompt = "Generate 5 interactive Student Flashcards (Front: Concept Question, Back: Precise Exam Answer) for Chapter: '${chapter.name}' in subject: '${chapter.subject}'. Formatted nicely."
                val response = GeminiClient.generateContent(prompt)
                repository.updateChapter(chapter.copy(flashcardsRaw = response))
            }
            _aiLoading.value = false
            awardXp(15)
        }
    }

    fun generateChapterPodcastTranscript(chapterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val chapter = repository.getChapterById(chapterId)
            if (chapter != null) {
                val prompt = "Create a 2-host podcast style audio script discussion focusing on the core concept and exam focus of Chapter: '${chapter.name}' in '${chapter.subject}'."
                val response = GeminiClient.generateContent(prompt)
                val modifiedDoc = VaultDoc(
                    title = "Audio Podcast: ${chapter.name}",
                    mimeType = "Audio/Podcast",
                    content = response,
                    audioSummaryUrl = "podcast_ch_${chapterId}.mp3",
                    audioDiscussion = response
                )
                repository.saveVaultDoc(modifiedDoc)
            }
            _aiLoading.value = false
            awardXp(25)
        }
    }

    // --- Gamified Learning Path Operations ---
    fun getMilestonesForPath(pathId: Long): Flow<List<PathMilestone>> = repository.getMilestonesForPath(pathId)

    fun completeMilestone(milestone: PathMilestone) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMilestone = milestone.copy(isCompleted = !milestone.isCompleted)
            repository.updateMilestone(updatedMilestone)
            
            if (updatedMilestone.isCompleted) {
                awardXp(updatedMilestone.xpReward)
            } else {
                awardXp(-updatedMilestone.xpReward) // Reverse reward if toggled off
            }

            // Recalculate completed count
            val allMilestones = repository.getMilestonesForPathDirect(milestone.pathId)
            val total = allMilestones.size
            val completedCount = allMilestones.count { if (it.id == milestone.id) updatedMilestone.isCompleted else it.isCompleted }
            
            val path = repository.getLearningPathById(milestone.pathId)
            if (path != null) {
                val pathWasCompleted = path.isCompleted
                val pathIsCompleted = completedCount == total
                
                val updatedPath = path.copy(
                    completedMilestones = completedCount,
                    totalMilestones = total,
                    isCompleted = pathIsCompleted
                )
                repository.updateLearningPath(updatedPath)
                
                // Extra bonus for completing the entire pathway!
                if (pathIsCompleted && !pathWasCompleted) {
                    awardXp(path.xpReward)
                }
            }
        }
    }

    fun adaptPathBasedOnWeakAreas(pathId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val path = repository.getLearningPathById(pathId)
            val profile = repository.getUserProfileDirect() ?: UserProfile()
            if (path != null) {
                val nextDifficulty = "Adapted (Weak Areas)"
                val updatedPath = path.copy(
                    difficultyLevel = nextDifficulty, 
                    title = "Adapted: " + path.title,
                    isAdaptable = false
                )
                repository.updateLearningPath(updatedPath)
                
                // Append custom adaptive milestone targeting the student's weak subject area (e.g. Accountancy)
                val weakMilestone = PathMilestone(
                    pathId = pathId,
                    title = "Targeted Study: ${profile.weakSubjects} Booster",
                    description = "Custom milestone generated to tackle user's recorded weak subject of ${profile.weakSubjects}.",
                    isCompleted = false,
                    xpReward = 50,
                    milestoneType = "Practice Questions"
                )
                repository.saveMilestone(weakMilestone)
                
                // Refresh parent pathway completed stats
                val allMilestones = repository.getMilestonesForPathDirect(pathId)
                repository.updateLearningPath(updatedPath.copy(
                    totalMilestones = allMilestones.size,
                    completedMilestones = allMilestones.count { it.isCompleted }
                ))
                _aiAnswer.value = "Pathway adapted for ${profile.weakSubjects}! We inserted a tailored practice milestone."
            }
            _aiLoading.value = false
        }
    }

    // --- Collaborative Study Operations ---
    fun createStudyGroup(name: String, description: String, subjectFocus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newGroup = StudyGroup(
                name = name,
                description = description,
                subjectFocus = subjectFocus
            )
            repository.saveStudyGroup(newGroup)
            awardXp(30)
        }
    }

    fun deleteStudyGroup(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudyGroup(id)
        }
    }

    fun getGroupTasks(groupId: Long): Flow<List<GroupTask>> = repository.getGroupTasks(groupId)
    fun getGroupMessages(groupId: Long): Flow<List<GroupMessage>> = repository.getGroupMessages(groupId)
    fun getGroupDocuments(groupId: Long): Flow<List<GroupDocument>> = repository.getGroupDocuments(groupId)

    fun sendGroupMessage(groupId: Long, senderName: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveGroupMessage(
                GroupMessage(
                    groupId = groupId,
                    senderName = senderName,
                    messageText = text
                )
            )
            awardXp(5) // Group interaction XP
        }
    }

    fun shareGroupDocument(groupId: Long, title: String, content: String, sharedBy: String) {
        if (title.isBlank() || content.contentEquals("")) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveGroupDocument(
                GroupDocument(
                    groupId = groupId,
                    title = title,
                    content = content,
                    sharedBy = sharedBy
                )
            )
            awardXp(20) // Sharing documents grants nicer rewards
        }
    }

    fun completeGroupTask(task: GroupTask) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateGroupTask(updated)
            if (updated.isCompleted) {
                awardXp(task.xpReward)
            } else {
                awardXp(-task.xpReward)
            }
        }
    }

    fun createGroupTask(groupId: Long, title: String, assignedTo: String, xpReward: Int) {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveGroupTask(
                GroupTask(
                    groupId = groupId,
                    title = title,
                    assignedTo = assignedTo,
                    xpReward = xpReward
                )
            )
        }
    }
}

class StudyViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
