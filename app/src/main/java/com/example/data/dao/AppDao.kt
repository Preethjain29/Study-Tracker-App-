package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // --- Brain Items (Inbox / Top 3) ---
    @Query("SELECT * FROM brain_items ORDER BY dateCreated DESC")
    fun getAllBrainItems(): Flow<List<BrainItem>>

    @Query("SELECT * FROM brain_items WHERE id = :id LIMIT 1")
    suspend fun getBrainItemById(id: Long): BrainItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrainItem(item: BrainItem): Long

    @Update
    suspend fun updateBrainItem(item: BrainItem)

    @Query("DELETE FROM brain_items WHERE id = :id")
    suspend fun deleteBrainItemById(id: Long)

    @Query("SELECT * FROM brain_items WHERE isCompleted = 0 AND isTopPriority = 1 LIMIT 3")
    fun getTodayTop3(): Flow<List<BrainItem>>

    // --- Chapters ---
    @Query("SELECT * FROM chapters ORDER BY name ASC")
    fun getAllChapters(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: Long): Chapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapterById(id: Long)

    // --- Spaced Repetition Schedules ---
    @Query("SELECT * FROM spaced_rep_schedules ORDER BY targetDateMillis ASC")
    fun getAllSchedules(): Flow<List<SpacedRepSchedule>>

    @Query("SELECT * FROM spaced_rep_schedules WHERE targetDateMillis <= :maxDate AND isReviewed = 0")
    fun getDueSchedules(maxDate: Long): Flow<List<SpacedRepSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: SpacedRepSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<SpacedRepSchedule>)

    @Update
    suspend fun updateSchedule(schedule: SpacedRepSchedule)

    @Query("DELETE FROM spaced_rep_schedules WHERE chapterId = :chapterId")
    suspend fun deleteSchedulesForChapter(chapterId: Long)

    // --- Vault Documents ---
    @Query("SELECT * FROM vault_docs ORDER BY uploadDateMillis DESC")
    fun getAllVaultDocs(): Flow<List<VaultDoc>>

    @Query("SELECT * FROM vault_docs WHERE id = :id LIMIT 1")
    suspend fun getVaultDocById(id: Long): VaultDoc?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultDoc(doc: VaultDoc): Long

    @Query("DELETE FROM vault_docs WHERE id = :id")
    suspend fun deleteVaultDocById(id: Long)

    // --- Study Logs ---
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    fun getAllStudyLogs(): Flow<List<StudyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyLog(log: StudyLog): Long

    // --- Gamified Learning Path ---
    @Query("SELECT * FROM learning_paths ORDER BY id ASC")
    fun getAllLearningPaths(): Flow<List<LearningPath>>

    @Query("SELECT * FROM learning_paths WHERE id = :id LIMIT 1")
    suspend fun getLearningPathById(id: Long): LearningPath?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningPath(path: LearningPath): Long

    @Update
    suspend fun updateLearningPath(path: LearningPath)

    @Query("DELETE FROM learning_paths WHERE id = :id")
    suspend fun deleteLearningPathById(id: Long)

    @Query("SELECT * FROM path_milestones WHERE pathId = :pathId ORDER BY id ASC")
    fun getMilestonesForPath(pathId: Long): Flow<List<PathMilestone>>

    @Query("SELECT * FROM path_milestones WHERE pathId = :pathId ORDER BY id ASC")
    suspend fun getMilestonesForPathDirect(pathId: Long): List<PathMilestone>

    @Query("SELECT * FROM path_milestones WHERE id = :id LIMIT 1")
    suspend fun getMilestoneById(id: Long): PathMilestone?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<PathMilestone>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: PathMilestone): Long

    @Update
    suspend fun updateMilestone(milestone: PathMilestone)

    // --- Study Groups ---
    @Query("SELECT * FROM study_groups ORDER BY dateCreated DESC")
    fun getAllStudyGroups(): Flow<List<StudyGroup>>

    @Query("SELECT * FROM study_groups WHERE id = :id LIMIT 1")
    suspend fun getStudyGroupById(id: Long): StudyGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGroup(group: StudyGroup): Long

    @Query("DELETE FROM study_groups WHERE id = :id")
    suspend fun deleteStudyGroupById(id: Long)

    @Query("SELECT * FROM group_tasks WHERE groupId = :groupId ORDER BY id ASC")
    fun getGroupTasks(groupId: Long): Flow<List<GroupTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupTask(task: GroupTask): Long

    @Update
    suspend fun updateGroupTask(task: GroupTask)

    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getGroupMessages(groupId: Long): Flow<List<GroupMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessage(message: GroupMessage): Long

    @Query("SELECT * FROM group_documents WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getGroupDocuments(groupId: Long): Flow<List<GroupDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupDocument(doc: GroupDocument): Long

    // --- Database Clear Queries ---
    @Query("DELETE FROM brain_items")
    suspend fun clearBrainItems()

    @Query("DELETE FROM chapters")
    suspend fun clearChapters()

    @Query("DELETE FROM spaced_rep_schedules")
    suspend fun clearSchedules()

    @Query("DELETE FROM vault_docs")
    suspend fun clearVaultDocs()

    @Query("DELETE FROM study_logs")
    suspend fun clearStudyLogs()

    @Query("DELETE FROM learning_paths")
    suspend fun clearLearningPaths()

    @Query("DELETE FROM path_milestones")
    suspend fun clearPathMilestones()

    @Query("DELETE FROM study_groups")
    suspend fun clearStudyGroups()

    @Query("DELETE FROM group_tasks")
    suspend fun clearGroupTasks()

    @Query("DELETE FROM group_messages")
    suspend fun clearGroupMessages()

    @Query("DELETE FROM group_documents")
    suspend fun clearGroupDocuments()
}
