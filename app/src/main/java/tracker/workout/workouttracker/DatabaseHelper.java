package tracker.workout.workouttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tracker.workout.workouttracker.dataContainers.Log;
import tracker.workout.workouttracker.dataContainers.Workout;
import tracker.workout.workouttracker.dataContainers.WorkoutExercise;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "workout_tracker.db";
	private static final String EXERCISE_TABLE = "exercise";
	private static final String CATEGORY_TABLE = "category";
	private static final String CATEGORY_EXERCISE_TABLE = "category_exercise";
	private static final String WORKOUT_TABLE = "workout";
	private static final String WORKOUT_EXERCISE_TABLE = "workout_exercise";
	private static final String LOG_TABLE = "log";
	private static final String LOG_WORKOUT_EXERCISE_TABLE = "log_workout_exercise";

	private Context context;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, context.getResources().getInteger(R.integer.database_version));
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String[] resExercises = context.getResources().getStringArray(R.array.exercises);
		String[] resCategories = context.getResources().getStringArray(R.array.categories);

		for (String string : context.getResources().getStringArray(R.array.create_statements)) {
			db.execSQL(string);
		}

		// Store categories in to db
		for (int i = 0; i < resCategories.length; i++) {
			ContentValues cv = new ContentValues();
			cv.put("name", resCategories[i]);
			db.insert(CATEGORY_TABLE, null, cv);
		}

		// Store exercises + category_exercise
		for (int i = 0; i < resExercises.length; i++) {
			String[] exercise = resExercises[i].split(":");
			int categoryId = Integer.parseInt(exercise[0]);
			ContentValues cv = new ContentValues();
			cv.put("name", exercise[1]);
			long exerciseId = db.insert(EXERCISE_TABLE, null, cv);

			cv.clear();

			cv.put("categoryId", categoryId);
			cv.put("exerciseId", exerciseId);
			db.insert(CATEGORY_EXERCISE_TABLE, null, cv);
		}

	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		reset(db, oldVersion, newVersion);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		reset(db, oldVersion, newVersion);
	}

	private void reset(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + LOG_WORKOUT_EXERCISE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + WORKOUT_EXERCISE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_EXERCISE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + WORKOUT_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + EXERCISE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE);
		onCreate(db);
	}

	public long createLog(String date) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("date", date);
		return db.insert(LOG_TABLE, null, cv);
	}

	public long insertLogWorkoutExercise(long logId, WorkoutExercise workoutExercise) {
		// workoutExerciseId == workout_exercise.id <- primary key
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("logId", logId);
		cv.put("workoutExerciseId", workoutExercise.getId());
		cv.put("sets", workoutExercise.getSets());
		cv.put("reps", workoutExercise.getReps());
		return db.insert(LOG_WORKOUT_EXERCISE_TABLE, null, cv);
	}

	// TODO: TEST THE METHOD!
	private Log getLoggedWorkout(long id) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(LOG_TABLE, new String[] {"date"}, null, null, null, null, null);
		c.moveToNext();
		String date = c.getString(0);

		c = db.query(LOG_WORKOUT_EXERCISE_TABLE, null, "logId = ?", new String[] {Long.toString(id)}, null, null, null);

		WorkoutExercise[] exercises = new WorkoutExercise[c.getCount()];

		long workoutId = -1;
		String workoutName = "";

		for (int i = 0; i < c.getCount(); i++) {
			c.moveToNext();

			long workoutExerciseId = c.getLong(2);
			int workoutExerciseSets = c.getInt(3);
			int workoutExerciseReps = c.getInt(4);

			Cursor workoutExerciseCursor = db.query(WORKOUT_EXERCISE_TABLE, null, "id = ?", new String[] {Long.toString(workoutExerciseId)}, null, null, null);
			workoutExerciseCursor.moveToNext();

			if (workoutId == -1 && workoutExerciseCursor.getCount() > 0) {
				workoutId = workoutExerciseCursor.getLong(1);
				workoutName = getWorkoutName(workoutId);
			}

			long exerciseId = workoutExerciseCursor.getLong(2);
			String exerciseName = getExerciseName(exerciseId);
			int exerciseDefaultSets = workoutExerciseCursor.getInt(3);
			int exerciseDefaultReps = workoutExerciseCursor.getInt(4);

			exercises[i] = new WorkoutExercise(exerciseId, exerciseName, (workoutExerciseSets == 0) ? exerciseDefaultSets : workoutExerciseSets, (workoutExerciseReps == 0) ? exerciseDefaultReps : workoutExerciseReps);
		}

		return new Log(id, date, new Workout(workoutId, workoutName, exercises));
	}

	public Log[] getLoggedWorkouts() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(LOG_TABLE, new String[] {"id"}, null, null, null, null, null);

		Log[] logs = new Log[c.getCount()];

		for (int i = 0; i < logs.length; i++) {
			c.moveToNext();
			logs[i] = getLoggedWorkout(c.getLong(0));
		}

		return logs;
	}

	private String getExerciseName(long id) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(EXERCISE_TABLE, new String[]{"name"}, "id = ?", new String[] {Long.toString(id)}, null, null, null);
		if (c.getCount() == 1) {
			c.moveToNext();
			return c.getString(0);
		}
		return null;
	}

	private long getExerciseId(String name) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(EXERCISE_TABLE, new String[]{"id"}, "name = ?", new String[] {name}, null, null, null);
		if (c.getCount() == 1) {
			c.moveToNext();
			return c.getLong(0);
		}
		return -1;
	}

	private long getWorkoutId(String name) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(WORKOUT_TABLE, new String[]{"id"}, "name = ?", new String[] {name}, null, null, null);
		if (c.getCount() == 1) {
			c.moveToNext();
			return c.getLong(0);
		}
		return -1;
	}

	private String getWorkoutName(long id) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(WORKOUT_TABLE, new String[]{"name"}, "id = ?", new String[] {Long.toString(id)}, null, null, null);
		if (c.getCount() == 1) {
			c.moveToNext();
			return c.getString(0);
		}
		return null;
	}

	// Can be used to change defaults in workout_exercise or logged_workout tables.
	// Returns if changed or not
	public boolean changeSets(String table, long rowId, int sets) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("sets", sets);
		return db.update(table, cv, (table.equals(WORKOUT_EXERCISE_TABLE)) ? "id" : "logId" + " = ?", new String[] {Long.toString(rowId)}) == 1;
	}


	// Can be used to change defaults in workout_exercise or logged_workout tables.
	// Returns if changed or not
	public boolean changeReps(String table, long rowId, int reps) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("reps", reps);
		return db.update(table, cv, (table.equals(WORKOUT_EXERCISE_TABLE)) ? "id" : "logId" + " = ?", new String[] {Long.toString(rowId)}) == 1;
	}

	public int getReps(long rowId) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(WORKOUT_EXERCISE_TABLE, new String[] {"reps"}, "id = ?", new String[] {Long.toString(rowId)}, null, null, null);
		if (c.getCount() == 1) {
			c.moveToNext();
			return c.getInt(0);
		}
		return -1;
	}

	public int getSets(long rowId) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(WORKOUT_EXERCISE_TABLE, new String[] {"sets"}, "id = ?", new String[] {Long.toString(rowId)}, null, null, null);
		if (c.getCount() == 1) {
			c.moveToNext();
			return c.getInt(0);
		}
		return -1;
	}

	public long createWorkout(String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		return db.insert(WORKOUT_TABLE, null, cv);
	}

	public long insertWorkout(String workoutName, String exercise, int sets, int reps) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put("workoutId", getWorkoutId(workoutName));
		cv.put("exerciseId", getExerciseId(exercise));
		cv.put("sets", sets);
		cv.put("reps", reps);
		return db.insert(WORKOUT_EXERCISE_TABLE, null, cv);
	}

	public Workout getWorkout(String name) {
		WorkoutExercise[] workoutExercises = getWorkoutExercises(name);
		return new Workout(getWorkoutId(name), name, workoutExercises);
	}

	public Workout[] getWorkouts() {
		String[] workoutNames = getWorkoutNames();

		Workout[] workouts = new Workout[workoutNames.length];

		for (int i = 0; i < workoutNames.length; i++) {
			String workoutName = workoutNames[i];
			workouts[i] = getWorkout(workoutName);
		}

		return workouts;
	}

	// Returns a String array of all of the workout names
	public String[] getWorkoutNames() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT name FROM " + WORKOUT_TABLE, null);
		String[] workouts = new String[c.getCount()];

		for (int i = 0; i < c.getCount(); i++) {
			c.moveToNext();
			workouts[i] = c.getString(0);
		}

		return workouts;
	}

	// Returns a String array of all exercises associated with a workout
	public WorkoutExercise[] getWorkoutExercises(String workoutName) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT we.id, e.name, we.sets, we.reps FROM " + WORKOUT_EXERCISE_TABLE + " we, " + WORKOUT_TABLE + " w, " + EXERCISE_TABLE + " e WHERE we.workoutId = w.id AND we.exerciseId = e.id AND w.name = '" + workoutName + "'", null);

		WorkoutExercise[] exercises = new WorkoutExercise[c.getCount()];

		for (int i = 0; i < c.getCount(); i++) {
			c.moveToNext();
			exercises[i] = new WorkoutExercise(c.getLong(0), c.getString(1), c.getInt(2), c.getInt(3));
		}

		return exercises;
	}

	// Returns all exercises associated with a particular category
	public String[] getExercisesForCategory(String category) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.rawQuery("SELECT e.name FROM " + CATEGORY_EXERCISE_TABLE + " ce, " + EXERCISE_TABLE + " e, " + CATEGORY_TABLE + " c WHERE ce.categoryId = c.id AND c.name = '" + category + "' AND ce.exerciseId = e.id", null);
		String[] exercises = new String[c.getCount()];

		for (int i = 0; i < c.getCount(); i++) {
			c.moveToNext();
			exercises[i] = c.getString(0);
		}

		return exercises;
	}

}