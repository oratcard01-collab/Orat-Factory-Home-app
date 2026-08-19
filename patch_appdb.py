import re

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.data.model.User',
    'import com.example.data.model.User\nimport androidx.room.migration.Migration\nimport androidx.sqlite.db.SupportSQLiteDatabase'
)

content = content.replace(
    'version = 3',
    'version = 4'
)

migration_code = """
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE deduction_logs ADD COLUMN invoiceNumber TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
"""

content = content.replace(
    '        fun getDatabase(context: Context): AppDatabase {',
    migration_code
)

content = content.replace(
    '.fallbackToDestructiveMigration()',
    '.addMigrations(MIGRATION_3_4)\n                .fallbackToDestructiveMigration()'
)

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'w') as f:
    f.write(content)
