package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "New Recruit",
    val classLevel: String = "Class 11",
    val stream: String = "Commerce", // Commerce, Science, Arts
    val subjects: String = "",
    val careerGoal: String = "",
    val studyHoursAvailable: Int = 4,
    val wakeUpTime: String = "06:00 AM",
    val sleepTime: String = "11:00 PM",
    val examDate: String = "2026-12-31",
    val weakSubjects: String = "",
    val strongSubjects: String = "",
    val xp: Int = 0,
    val level: Int = 1, // 1/2/3/4/5
    val streak: Int = 0,
    val isEmergencyMode: Boolean = false,
    val isOnboarded: Boolean = false
)

@Entity(tableName = "brain_items")
data class BrainItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val category: String = "Uncategorized", // Study, Personal, Health, Business, Finance, Future Ideas
    val estimatedDurationMinutes: Int = 45,
    val priorityScore: Int = 50, // 1-100 calculated by AI
    val deadline: String = "No deadline",
    val dateCreated: Long = System.currentTimeMillis(),
    val isTopPriority: Boolean = false, // True if set inside top 3 priorities of today
    val isLater: Boolean = false,
    val procrastinationDays: Int = 0, // Days item was kept open
    val subtasksRaw: String = "" // Multi-line string or comma separated for decompressed tasks if procrastinated
)

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subject: String,
    val stream: String = "Commerce",
    val isCompleted: Boolean = false,
    val lastRevisedTime: Long = 0,
    val retentionScore: Int = 40, // 0-100 percentage
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val formulaSheet: String = "",
    val quickNotes: String = "",
    val mindMapSummary: String = "",
    val flashcardsRaw: String = "" // Custom simple key-value flashcards text
)

@Entity(tableName = "spaced_rep_schedules")
data class SpacedRepSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val chapterName: String,
    val subject: String,
    val scheduledDayIndex: Int, // Day 1, 3, 7, 15, 30
    val targetDateMillis: Long,
    val isReviewed: Boolean = false,
    val reviewRating: Int = 0 // 1 to 5 stars
)

@Entity(tableName = "vault_docs")
data class VaultDoc(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mimeType: String, // "pdf", "docx", "notes"
    val content: String, // Extracted raw content
    val uploadDateMillis: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = true,
    val audioSummaryUrl: String = "", // Simulated Audio summary podcast file path
    val audioDiscussion: String = "" // Transcript text or notes
)

@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val focusMode: String = "Pomodoro", // Pomodoro, Deep Work, Custom
    val energyState: String = "Fresh", // Fresh, Normal, Tired
    val category: String = "Study"
)

// --- Gamified Learning Path ---
@Entity(tableName = "learning_paths")
data class LearningPath(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val completedMilestones: Int = 0,
    val totalMilestones: Int = 3,
    val isCompleted: Boolean = false,
    val xpReward: Int = 100,
    val badgeUnlocked: String = "None",
    val isAdaptable: Boolean = true,
    val difficultyLevel: String = "Medium", // Easy, Medium, Hard
    val description: String = ""
)

@Entity(tableName = "path_milestones")
data class PathMilestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pathId: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val xpReward: Int = 25,
    val milestoneType: String = "Reading" // Reading, Practice Questions, Mastering Concept, Mock Quiz
)

// --- Collaborative Learning Hub ---
@Entity(tableName = "study_groups")
data class StudyGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val subjectFocus: String,
    val authorName: String = "Siddharth",
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "group_tasks")
data class GroupTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val assignedTo: String = "All Members", // E.g., "Siddharth", "Aarav", "All"
    val isCompleted: Boolean = false,
    val xpReward: Int = 30,
    val dueDate: String = "Due in 3 days"
)

@Entity(tableName = "group_messages")
data class GroupMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "group_documents")
data class GroupDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val content: String,
    val sharedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

