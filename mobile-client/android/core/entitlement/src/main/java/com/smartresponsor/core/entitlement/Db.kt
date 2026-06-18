package com.smartresponsor.core.entitlement
import android.content.Context; import androidx.room.*
@Entity(tableName="entitlement", indices=[Index("featureKey", unique=true)])
data class EntitlementEntity(@PrimaryKey(autoGenerate=true) val id:Long=0, val featureKey:String, val status:String, val grantedUntil:Long?, val grantedAt:Long, val source:String)
@Dao interface EntitlementDao {
  @Query("SELECT * FROM entitlement WHERE featureKey=:k LIMIT 1") suspend fun byKey(k:String):EntitlementEntity?
  @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(e:EntitlementEntity)
}
val MIGRATION_1_2 = object: Migration(1,2){
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE entitlement ADD COLUMN grantedAt INTEGER NOT NULL DEFAULT 0")
  }
}
@Database(entities=[EntitlementEntity::class], version=2, exportSchema=false)
abstract class EntitlementDb:RoomDatabase(){ abstract fun dao():EntitlementDao; companion object{ @Volatile private var I:EntitlementDb?=null; fun get(c:Context)= I?: synchronized(this){ I?: Room.databaseBuilder(c, EntitlementDb::class.java,"ent.db").addMigrations(MIGRATION_1_2).build().also{I=it}}}}
