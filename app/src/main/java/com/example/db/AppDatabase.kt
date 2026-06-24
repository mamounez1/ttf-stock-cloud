package com.example.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Users ---
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)

    @Query("UPDATE users SET passwordHash = :passwordHash, fullName = :fullName, role = :role WHERE id = :id")
    suspend fun updateUser(id: Int, passwordHash: String, fullName: String, role: String)

    // --- Articles ---
    @Query("SELECT * FROM articles ORDER BY reference ASC")
    fun getAllArticlesFlow(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE reference = :reference LIMIT 1")
    suspend fun getArticleByReference(reference: String): Article?

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Int): Article?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): Long

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteArticleById(articleId: Int)

    @Query("UPDATE articles SET name = :name, description = :description, reference = :reference WHERE id = :id")
    suspend fun updateArticle(id: Int, reference: String, name: String, description: String)

    @Query("UPDATE articles SET quantity = quantity + :delta WHERE id = :id")
    suspend fun updateArticleQuantity(id: Int, delta: Int)

    // --- Contacts ---
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContactsFlow(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: Int)

    @Query("UPDATE contacts SET name = :name, type = :type, phone = :phone, email = :email, address = :address WHERE id = :id")
    suspend fun updateContact(id: Int, name: String, type: String, phone: String, email: String, address: String)

    // --- Stock Entries ---
    @Query("SELECT * FROM stock_entries ORDER BY createdAt DESC")
    fun getAllStockEntriesFlow(): Flow<List<StockEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockEntry(entry: StockEntry): Long

    // --- Stock Exits ---
    @Query("SELECT * FROM stock_exits ORDER BY createdAt DESC")
    fun getAllStockExitsFlow(): Flow<List<StockExit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockExit(exit: StockExit): Long

    // --- Slot Assignments ---
    @Query("SELECT * FROM slot_assignments")
    fun getAllSlotAssignmentsFlow(): Flow<List<SlotAssignment>>

    @Query("SELECT * FROM slot_assignments")
    suspend fun getAllSlotAssignments(): List<SlotAssignment>

    @Query("SELECT * FROM slot_assignments WHERE locationCode = :code LIMIT 1")
    suspend fun getSlotAssignmentByCode(code: String): SlotAssignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlotAssignment(slot: SlotAssignment): Long

    @Query("DELETE FROM slot_assignments WHERE locationCode = :code")
    suspend fun deleteSlotAssignmentByCode(code: String)
}

@Database(
    entities = [
        User::class,
        Article::class,
        Contact::class,
        StockEntry::class,
        StockExit::class,
        SlotAssignment::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ttf_stock_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Auto populate admin database user with default admin/admin123
                            db.execSQL(
                                "INSERT OR IGNORE INTO users (username, passwordHash, fullName, role, createdAt) " +
                                "VALUES ('admin', 'admin123', 'Administrateur', 'admin', ${System.currentTimeMillis()})"
                            )
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
