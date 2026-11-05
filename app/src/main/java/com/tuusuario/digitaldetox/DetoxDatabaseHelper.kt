package com.tuusuario.digitaldetox

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetoxDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "digital_detox.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_SESSIONS = "sessions"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_DURATION_MS = "duration_ms"
        private const val COLUMN_IS_DEMO = "is_demo"
        private const val COLUMN_FORMATTED_DATE = "formatted_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_SESSIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_DURATION_MS INTEGER NOT NULL,
                $COLUMN_IS_DEMO INTEGER NOT NULL,
                $COLUMN_FORMATTED_DATE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSIONS")
        onCreate(db)
    }

    fun insertSession(durationMs: Long, isDemo: Boolean): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            put(COLUMN_TIMESTAMP, now)
            put(COLUMN_DURATION_MS, durationMs)
            put(COLUMN_IS_DEMO, if (isDemo) 1 else 0)
            put(COLUMN_FORMATTED_DATE, dateFormat.format(Date(now)))
        }
        return db.insert(TABLE_SESSIONS, null, values)
    }

    fun getAllSessions(): List<SessionRecord> {
        val list = mutableListOf<SessionRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_SESSIONS ORDER BY $COLUMN_ID DESC",
            null
        )
        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(COLUMN_ID)
            val tsIndex = c.getColumnIndexOrThrow(COLUMN_TIMESTAMP)
            val durIndex = c.getColumnIndexOrThrow(COLUMN_DURATION_MS)
            val demoIndex = c.getColumnIndexOrThrow(COLUMN_IS_DEMO)
            val dateIndex = c.getColumnIndexOrThrow(COLUMN_FORMATTED_DATE)

            while (c.moveToNext()) {
                list.add(
                    SessionRecord(
                        id = c.getLong(idIndex),
                        timestamp = c.getLong(tsIndex),
                        durationMs = c.getLong(durIndex),
                        isDemo = c.getInt(demoIndex) == 1,
                        formattedDate = c.getString(dateIndex)
                    )
                )
            }
        }
        return list
    }

    fun clearAllSessions() {
        val db = writableDatabase
        db.delete(TABLE_SESSIONS, null, null)
    }
}
