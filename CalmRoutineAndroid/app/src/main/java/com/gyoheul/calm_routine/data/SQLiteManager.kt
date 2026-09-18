package com.gyoheul.calm_routine.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gyoheul.calm_routine.common.Extension.getLogicalToday
import com.gyoheul.calm_routine.model.MoodRecord
import com.gyoheul.calm_routine.model.RoutineRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime

class SQLiteManager(context: Context) {
    companion object {
        private const val TABLE_NAME_ROUTINE = "routine"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_IS_DONE = "is_done"
        private const val COLUMN_SORT_INDEX = "sort_index"

        private const val TABLE_NAME_MOOD = "mood"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_EMOTION = "emotion"
        private const val COLUMN_MEMO = "memo"
        private const val COLUMN_PROMPT = "prompt"
        private const val COLUMN_AI_COMMENT = "ai_comment"
    }

    class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
        companion object {
            const val DB_NAME = "calm_routine.db"
            const val DB_VERSION = 1
        }

        private val sqlCreateRoutineEntries = """
            CREATE TABLE $TABLE_NAME_ROUTINE (
                $COLUMN_ID TEXT(100) PRIMARY KEY,
                $COLUMN_TITLE TEXT(200),
                $COLUMN_IS_DONE INTEGER DEFAULT 0,
                $COLUMN_SORT_INDEX INTEGER DEFAULT 0,
                $COLUMN_DATE DATE DEFAULT CURRENT_DATE NOT NULL,
                UNIQUE($COLUMN_DATE, $COLUMN_SORT_INDEX)
            )
        """

        private val sqlCreateMoodEntries = """
            CREATE TABLE $TABLE_NAME_MOOD (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE DATE DEFAULT CURRENT_DATE UNIQUE,
                $COLUMN_EMOTION TEXT(200),
                $COLUMN_MEMO TEXT(400),
                $COLUMN_PROMPT TEXT(1000),
                $COLUMN_AI_COMMENT TEXT(1000)
            )
        """

        private val sqlDeleteRoutineEntries = "DROP TABLE IF EXISTS $TABLE_NAME_ROUTINE"

        private val sqlDeleteMoodEntries = "DROP TABLE IF EXISTS $TABLE_NAME_MOOD"

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(sqlCreateRoutineEntries)
            db.execSQL(sqlCreateMoodEntries)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(sqlDeleteRoutineEntries)
            db.execSQL(sqlDeleteMoodEntries)
            onCreate(db)
        }
    }

    private val dbHelper = DatabaseHelper(context)
    private val json = Json {
        ignoreUnknownKeys = true // 今後のモデル変更に備える
        encodeDefaults = true    // デフォルト値を含めてシリアル化
        prettyPrint = false      // 保存用なのでfalse
    }

    suspend fun upsertRoutines(routines: List<RoutineRecord>, date: LocalDate = LocalDateTime.now().getLogicalToday()) {
        val database: SQLiteDatabase = dbHelper.writableDatabase
        database.use { db ->
            db.delete(
                TABLE_NAME_ROUTINE,
                "$COLUMN_DATE = ?",
                arrayOf(
                    date.toString()
                )
            )
        }

        withContext(Dispatchers.IO) {
            routines.map { routine ->
                async {
                    upsertRoutine(routine, date)
                }
            }.awaitAll()
        }
    }

    fun upsertRoutine(routine: RoutineRecord, date: LocalDate = LocalDateTime.now().getLogicalToday()) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val cursor = db.query(
            TABLE_NAME_ROUTINE,
            null,
            "$COLUMN_ID = ? AND $COLUMN_DATE = ?",
            arrayOf(
                routine.id,
                date.toString()
            ),
            null,
            null,
            "$COLUMN_SORT_INDEX ASC",
            "1"
        )

        try {
            if (cursor.count > 0) {
                val values = ContentValues().apply {
                    put(COLUMN_TITLE, routine.title)
                    put(COLUMN_IS_DONE, if (routine.isDone) 1 else 0)
                    put(COLUMN_SORT_INDEX, routine.sortIndex)
                }

                db.update(
                    TABLE_NAME_ROUTINE,
                    values,
                    "$COLUMN_ID = ? AND $COLUMN_DATE = ?",
                    arrayOf(
                        routine.id,
                        date.toString()
                    )
                )
            } else {
                val values = ContentValues().apply {
                    put(COLUMN_ID, routine.id)
                    put(COLUMN_TITLE, routine.title)
                    put(COLUMN_IS_DONE, if (routine.isDone) 1 else 0)
                    put(COLUMN_SORT_INDEX, routine.sortIndex)
                    put(COLUMN_DATE, date.toString())
                }

                db.insert(TABLE_NAME_ROUTINE, null, values)
            }
        } finally {
            LogModel.d(this, "upsertRoutine date: $date routine: $routine")
            cursor.close()
            db.close()
        }
    }

    fun getTodayRoutines(): List<RoutineRecord> {
        val routineMap: Map<LocalDate, List<RoutineRecord>> = getRoutinesWithDateGroup(limit = 1)
        return if (routineMap.containsKey(LocalDateTime.now().getLogicalToday())) {
            routineMap[LocalDate.now()] ?: emptyList()
        } else {
            routineMap.values.firstOrNull()?.map {
                it.copy(
                    isDone = false
                )
            } ?: emptyList()
        }
    }

    fun getRoutinesWithDateGroup(limit: Int = -1): Map<LocalDate, List<RoutineRecord>> {
        val database: SQLiteDatabase = dbHelper.readableDatabase
        database.use { db ->
            val dateCursor = db.query(
                true, // distinct
                TABLE_NAME_ROUTINE,
                arrayOf(COLUMN_DATE),  // 返却するカラム
                null,
                null,
                null,
                null,
                "$COLUMN_DATE DESC",
                if (limit > 0) {
                    limit.toString()
                } else {
                    null
                },
            )

            val dateList: List<String> = buildList {
                dateCursor.use { cursor ->
                    while (cursor.moveToNext()) {
                        val date: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                        add(date)
                    }
                }
            }
            return dateList.map { LocalDate.parse(it) }.associateWith { date ->
                val routineCursor = db.query(
                    TABLE_NAME_ROUTINE,
                    null,
                    "$COLUMN_DATE = ?",
                    arrayOf(
                        date.toString()
                    ),
                    null,
                    null,
                    "$COLUMN_SORT_INDEX ASC",
                    null
                )
                routineCursor.use { cursor ->
                    buildList {
                        while (cursor.moveToNext()) {
                            val id: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                            val title: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                            val isDone: Int = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DONE))
                            val sortIndex: Int = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SORT_INDEX))

                            add(
                                RoutineRecord(
                                    id = id,
                                    title = title,
                                    isDone = isDone != 0,
                                    sortIndex = sortIndex
                                )
                            )
                        }
                    }.sortedBy {
                        it.sortIndex
                    }.also {
                        LogModel.d(this, "getRoutinesWithDateGroup routines: $it")
                    }
                }
            }
        }
    }

    fun deleteRoutine(routine: RoutineRecord?, date: LocalDate = LocalDateTime.now().getLogicalToday()) {
        if (routine == null) return

        val database: SQLiteDatabase = dbHelper.writableDatabase
        database.use { db ->
            db.delete(
                TABLE_NAME_ROUTINE,
                "$COLUMN_ID = ? AND $COLUMN_DATE = ?",
                arrayOf(
                    routine.id,
                    date.toString()
                )
            )
            LogModel.d(this, "deleteRoutine date: $date routine: $routine")
        }
    }

    fun deleteRoutinesBeforeDate(date: LocalDate) {
        val database: SQLiteDatabase = dbHelper.writableDatabase
        database.use { db ->
            db.delete(
                TABLE_NAME_ROUTINE,
                "$COLUMN_DATE < ?",
                arrayOf(
                    date.toString()
                )
            )
            LogModel.d(this, "deleteRoutinesBeforeDate date: $date")
        }
    }

    suspend fun upsertMoods(moods: List<MoodRecord>) {
        withContext(Dispatchers.IO) {
            moods.map { mood ->
                async {
                    upsertMood(mood)
                }
            }.awaitAll()
        }
    }

    fun upsertMood(mood: MoodRecord) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val cursor = db.query(
            TABLE_NAME_MOOD,
            null, // 全カラム
            "$COLUMN_DATE = ?",
            arrayOf(
                mood.date
            ),
            null,
            null,
            null,
            "1"
        )

        try {
            if (cursor.count > 0) {
                val values = ContentValues().apply {
                    put(COLUMN_EMOTION, json.encodeToString(mood.emotion))
                    put(COLUMN_MEMO, mood.memo)
                    put(COLUMN_PROMPT, mood.prompt)
                    put(COLUMN_AI_COMMENT, mood.aiComment)
                }

                db.update(
                    TABLE_NAME_MOOD,
                    values,
                    "$COLUMN_DATE = ?",
                    arrayOf(
                        mood.date
                    )
                )
            } else {
                val values = ContentValues().apply {
                    put(COLUMN_DATE, mood.date)
                    put(COLUMN_EMOTION, json.encodeToString(mood.emotion))
                    put(COLUMN_MEMO, mood.memo)
                    put(COLUMN_PROMPT, mood.prompt)
                    put(COLUMN_AI_COMMENT, mood.aiComment)
                }

                db.insert(TABLE_NAME_MOOD, null, values)
            }
        } finally {
            LogModel.d(this, "upsertMood: $mood")
            cursor.close()
            db.close()
        }
    }

    fun getMoods(limit: Int = -1, desc: Boolean = true): List<MoodRecord> {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_NAME_MOOD,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE ${if (desc) "DESC" else "ASC"}",
            if (limit > 0) {
                limit.toString()
            } else {
                null
            },
        )

        return try {
            buildList {
                while (cursor.moveToNext()) {
                    val date: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                    val emotion: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMOTION))
                    val memo: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO))
                    val prompt: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROMPT))
                    val aiComment: String = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AI_COMMENT))

                    add(
                        MoodRecord(
                            date = date,
                            emotion = json.decodeFromString(emotion),
                            memo = memo,
                            prompt = prompt,
                            aiComment = aiComment
                        )
                    )
                }
            }.also {
                LogModel.d(this, "getMoods: $it")
            }
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun deleteMoodsBeforeDate(date: LocalDate) {
        val database: SQLiteDatabase = dbHelper.writableDatabase
        database.use { db ->
            db.delete(
                TABLE_NAME_MOOD,
                "$COLUMN_DATE < ?",
                arrayOf(
                    date.toString()
                )
            )
            LogModel.d(this, "deleteMoodsBeforeDate date: $date")
        }
    }

    fun clearAllData() {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_ROUTINE")
        db.execSQL("DELETE FROM $TABLE_NAME_MOOD")
        db.close()
    }
}