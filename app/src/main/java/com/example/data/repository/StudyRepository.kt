package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val dao: AppDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    suspend fun getUserProfileDirect(): UserProfile? = dao.getUserProfileDirect()
    suspend fun saveProfile(profile: UserProfile) = dao.insertOrUpdateProfile(profile)

    val allBrainItems: Flow<List<BrainItem>> = dao.getAllBrainItems()
    val todayTop3: Flow<List<BrainItem>> = dao.getTodayTop3()
    suspend fun getBrainItemById(id: Long): BrainItem? = dao.getBrainItemById(id)
    suspend fun saveBrainItem(item: BrainItem): Long = dao.insertBrainItem(item)
    suspend fun updateBrainItem(item: BrainItem) = dao.updateBrainItem(item)
    suspend fun deleteBrainItem(id: Long) = dao.deleteBrainItemById(id)

    val allChapters: Flow<List<Chapter>> = dao.getAllChapters()
    suspend fun getChapterById(id: Long): Chapter? = dao.getChapterById(id)
    suspend fun saveChapter(chapter: Chapter): Long = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: Chapter) = dao.updateChapter(chapter)
    suspend fun deleteChapter(id: Long) = dao.deleteChapterById(id)

    val allSchedules: Flow<List<SpacedRepSchedule>> = dao.getAllSchedules()
    fun getDueSchedules(maxDate: Long): Flow<List<SpacedRepSchedule>> = dao.getDueSchedules(maxDate)
    suspend fun saveSchedule(schedule: SpacedRepSchedule): Long = dao.insertSchedule(schedule)
    suspend fun saveSchedules(schedules: List<SpacedRepSchedule>) = dao.insertSchedules(schedules)
    suspend fun updateSchedule(schedule: SpacedRepSchedule) = dao.updateSchedule(schedule)
    suspend fun deleteSchedulesForChapter(chapterId: Long) = dao.deleteSchedulesForChapter(chapterId)

    val allVaultDocs: Flow<List<VaultDoc>> = dao.getAllVaultDocs()
    suspend fun getVaultDocById(id: Long): VaultDoc? = dao.getVaultDocById(id)
    suspend fun saveVaultDoc(doc: VaultDoc): Long = dao.insertVaultDoc(doc)
    suspend fun deleteVaultDoc(id: Long) = dao.deleteVaultDocById(id)

    val allStudyLogs: Flow<List<StudyLog>> = dao.getAllStudyLogs()
    suspend fun saveStudyLog(log: StudyLog): Long = dao.insertStudyLog(log)

    // --- Gamified Learning Path ---
    val allLearningPaths: Flow<List<LearningPath>> = dao.getAllLearningPaths()
    suspend fun getLearningPathById(id: Long): LearningPath? = dao.getLearningPathById(id)
    suspend fun saveLearningPath(path: LearningPath): Long = dao.insertLearningPath(path)
    suspend fun updateLearningPath(path: LearningPath) = dao.updateLearningPath(path)
    suspend fun deleteLearningPath(id: Long) = dao.deleteLearningPathById(id)

    fun getMilestonesForPath(pathId: Long): Flow<List<PathMilestone>> = dao.getMilestonesForPath(pathId)
    suspend fun getMilestonesForPathDirect(pathId: Long): List<PathMilestone> = dao.getMilestonesForPathDirect(pathId)
    suspend fun getMilestoneById(id: Long): PathMilestone? = dao.getMilestoneById(id)
    suspend fun saveMilestones(milestones: List<PathMilestone>) = dao.insertMilestones(milestones)
    suspend fun saveMilestone(milestone: PathMilestone): Long = dao.insertMilestone(milestone)
    suspend fun updateMilestone(milestone: PathMilestone) = dao.updateMilestone(milestone)

    // --- Study Groups ---
    val allStudyGroups: Flow<List<StudyGroup>> = dao.getAllStudyGroups()
    suspend fun getStudyGroupById(id: Long): StudyGroup? = dao.getStudyGroupById(id)
    suspend fun saveStudyGroup(group: StudyGroup): Long = dao.insertStudyGroup(group)
    suspend fun deleteStudyGroup(id: Long) = dao.deleteStudyGroupById(id)

    fun getGroupTasks(groupId: Long): Flow<List<GroupTask>> = dao.getGroupTasks(groupId)
    suspend fun saveGroupTask(task: GroupTask): Long = dao.insertGroupTask(task)
    suspend fun updateGroupTask(task: GroupTask) = dao.updateGroupTask(task)

    fun getGroupMessages(groupId: Long): Flow<List<GroupMessage>> = dao.getGroupMessages(groupId)
    suspend fun saveGroupMessage(message: GroupMessage): Long = dao.insertGroupMessage(message)

    fun getGroupDocuments(groupId: Long): Flow<List<GroupDocument>> = dao.getGroupDocuments(groupId)
    suspend fun saveGroupDocument(doc: GroupDocument): Long = dao.insertGroupDocument(doc)

    suspend fun clearAllData() {
        dao.clearBrainItems()
        dao.clearChapters()
        dao.clearSchedules()
        dao.clearVaultDocs()
        dao.clearStudyLogs()
        dao.clearLearningPaths()
        dao.clearPathMilestones()
        dao.clearStudyGroups()
        dao.clearGroupTasks()
        dao.clearGroupMessages()
        dao.clearGroupDocuments()
    }
}
