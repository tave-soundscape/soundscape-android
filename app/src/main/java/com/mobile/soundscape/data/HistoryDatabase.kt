package com.mobile.soundscape.data

import android.content.Context
import androidx.room.*

// 1. 엔티티: DB에 저장할 데이터 형식
@Entity(tableName = "playlist_history")
data class PlaylistHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val place: String,      // "카페", "공원"
    val goal: String,       // "집중", "산책"
    val iconResName: String, // "place_icon1"
    val playlistId: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// 2. DAO: 데이터 접근 로직
@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PlaylistHistory)

    @Query("SELECT * FROM playlist_history ORDER BY timestamp DESC LIMIT 6")
    suspend fun getRecentHistory(): List<PlaylistHistory>

    @Query("DELETE FROM playlist_history")
    suspend fun clearAll()
}

// 3. Database: 데이터베이스 인스턴스
@Database(entities = [PlaylistHistory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soundscape_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}