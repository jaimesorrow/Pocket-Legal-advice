import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Entity(
    tableName = "legal_violations",
    indices = [
        Index(value = ["jurisdiction", "violationType"], unique = true),
        Index(value = ["violationType"])
    ]
)
data class LegalViolation(
    @PrimaryKey
    val id: Long,
    val jurisdiction: String,
    val violationType: String,
    val statutoryBasis: String,
    val requiredFormName: String
)

@Entity(
    tableName = "action_steps",
    foreignKeys = [
        ForeignKey(
            entity = LegalViolation::class,
            parentColumns = ["id"],
            childColumns = ["violationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["violationId"]),
        Index(value = ["violationId", "stepNumber"], unique = true)
    ]
)
data class ActionStep(
    @PrimaryKey
    val id: Long,
    val violationId: Long,
    val stepNumber: Int,
    val stepDescription: String
)

data class ViolationWithSteps(
    @Embedded
    val violation: LegalViolation,
    @Relation(
        parentColumn = "id",
        entityColumn = "violationId"
    )
    val actionSteps: List<ActionStep>
) {
    val orderedActionSteps: List<ActionStep>
        get() = actionSteps.sortedBy(ActionStep::stepNumber)
}

data class ViolationSeed(
    val violation: LegalViolation,
    val steps: List<ActionStep>
)

@Dao
interface LegalActionDao {
    @Transaction
    @Query(
        """
        SELECT * FROM legal_violations
        WHERE violationType IN (:violationTypes)
        ORDER BY jurisdiction ASC, violationType ASC
        """
    )
    suspend fun getViolationsWithStepsByTypesInternal(
        violationTypes: List<String>
    ): List<ViolationWithSteps>

    suspend fun getViolationsWithStepsByTypes(
        violationTypes: List<String>
    ): List<ViolationWithSteps> {
        val normalizedTypes = violationTypes
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()

        if (normalizedTypes.isEmpty()) {
            return emptyList()
        }

        return getViolationsWithStepsByTypesInternal(normalizedTypes)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertViolations(violations: List<LegalViolation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActionSteps(steps: List<ActionStep>)

    @Query("DELETE FROM action_steps")
    suspend fun clearActionSteps()

    @Query("DELETE FROM legal_violations")
    suspend fun clearViolations()

    @Transaction
    suspend fun replaceAll(seedData: List<ViolationSeed>) {
        clearActionSteps()
        clearViolations()

        if (seedData.isEmpty()) {
            return
        }

        upsertViolations(seedData.map(ViolationSeed::violation))
        upsertActionSteps(seedData.flatMap(ViolationSeed::steps))
    }
}

@Database(
    entities = [LegalViolation::class, ActionStep::class],
    version = 1,
    exportSchema = true
)
abstract class LegalActionDatabase : RoomDatabase() {
    abstract fun legalActionDao(): LegalActionDao

    data class Configuration(
        val databaseName: String = "legal_actions.db",
        val inMemory: Boolean = false,
        val prepopulatedAssetPath: String? = null,
        val seedDataProvider: suspend () -> List<ViolationSeed> = { emptyList() }
    )

    companion object {
        @Volatile
        private var INSTANCE: LegalActionDatabase? = null

        private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(
            context: Context,
            configuration: Configuration = Configuration()
        ): LegalActionDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(
                    context = context.applicationContext,
                    configuration = configuration
                ).also { INSTANCE = it }
            }
        }

        fun newInMemoryInstance(
            context: Context,
            seedDataProvider: suspend () -> List<ViolationSeed> = { emptyList() }
        ): LegalActionDatabase {
            return buildDatabase(
                context = context.applicationContext,
                configuration = Configuration(
                    inMemory = true,
                    seedDataProvider = seedDataProvider
                )
            )
        }

        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }

        private fun buildDatabase(
            context: Context,
            configuration: Configuration
        ): LegalActionDatabase {
            val instanceHolder = DatabaseHolder()
            val builder = if (configuration.inMemory) {
                Room.inMemoryDatabaseBuilder(
                    context,
                    LegalActionDatabase::class.java
                )
            } else {
                Room.databaseBuilder(
                    context,
                    LegalActionDatabase::class.java,
                    configuration.databaseName
                ).apply {
                    configuration.prepopulatedAssetPath
                        ?.takeIf(String::isNotBlank)
                        ?.let(::createFromAsset)
                }
            }

            val configuredBuilder = builder.apply {
                if (!configuration.inMemory) {
                    setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                }

                fallbackToDestructiveMigrationOnDowngrade()
                addCallback(
                    SeedCallback(
                        databaseHolder = instanceHolder,
                        seedDataProvider = configuration.seedDataProvider
                    )
                )
            }

            return configuredBuilder
                .build()
                .also { instanceHolder.database = it }
        }
    }

    private class DatabaseHolder {
        @Volatile
        var database: LegalActionDatabase? = null
    }

    private class SeedCallback(
        private val databaseHolder: DatabaseHolder,
        private val seedDataProvider: suspend () -> List<ViolationSeed>
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val database = databaseHolder.database ?: return
            databaseScope.launch {
                val seedData = seedDataProvider()
                if (seedData.isNotEmpty()) {
                    database.legalActionDao().replaceAll(seedData)
                }
            }
        }
    }
}
