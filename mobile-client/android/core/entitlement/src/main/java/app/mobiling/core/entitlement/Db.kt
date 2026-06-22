package app.mobiling.core.entitlement

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "entitlement", indices = [Index("featureKey", unique = true)])
data class EntitlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val featureKey: String,
    val status: String,
    val grantedUntil: Long?,
    val grantedAt: Long,
    val source: String,
)

@Dao
interface EntitlementDao {
    @Query("SELECT * FROM entitlement WHERE featureKey=:k LIMIT 1")
    suspend fun byKey(k: String): EntitlementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: EntitlementEntity)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entitlement ADD COLUMN grantedAt INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(entities = [EntitlementEntity::class], version = 2, exportSchema = false)
abstract class EntitlementDb : RoomDatabase() {
    abstract fun dao(): EntitlementDao

    companion object {
        @Volatile
        private var instance: EntitlementDb? = null

        fun get(context: Context): EntitlementDb =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    EntitlementDb::class.java,
                    "ent.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
