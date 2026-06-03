package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        BrainItem::class,
        Chapter::class,
        SpacedRepSchedule::class,
        VaultDoc::class,
        StudyLog::class,
        LearningPath::class,
        PathMilestone::class,
        StudyGroup::class,
        GroupTask::class,
        GroupMessage::class,
        GroupDocument::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mission_control_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.dao()
                    // Populate empty default profile (unonboarded)
                    dao.insertOrUpdateProfile(
                        UserProfile(
                            id = 1,
                            name = "New Recruit",
                            classLevel = "Class 11",
                            stream = "Commerce",
                            subjects = "",
                            careerGoal = "",
                            studyHoursAvailable = 4,
                            wakeUpTime = "06:00 AM",
                            sleepTime = "11:00 PM",
                            examDate = "2026-12-31",
                            weakSubjects = "",
                            strongSubjects = "",
                            xp = 0,
                            level = 1,
                            streak = 0,
                            isEmergencyMode = false,
                            isOnboarded = false
                        )
                    )
                }
            }
        }
    }
}
