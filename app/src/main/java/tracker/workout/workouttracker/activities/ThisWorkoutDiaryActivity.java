/*
 *  ThisWorkoutDiaryActivity - This Activity is where the user can view details
 *  about a particular logged workout. eg sets, reps for an exercise.
 */

package tracker.workout.workouttracker.activities;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import tracker.workout.workouttracker.R;
import tracker.workout.workouttracker.dataContainers.Workout;
import tracker.workout.workouttracker.dataContainers.WorkoutExercise;

public class ThisWorkoutDiaryActivity extends AppCompatActivity {

    private ListView gridView;
    private TextView textView;
    public Button addPhotoButton;
    public Button shareButton;
    private Workout workout;

    /*
     *	onCreate - Sets up a list view that contains all exercises in the workout.
     * 	Also shows the reps and sets for each exercise.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_this_workout_diary);
        gridView = (ListView) findViewById(R.id.thisWorkoutDiaryListView);
        textView = (TextView) findViewById(R.id.workoutNameTextView);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            workout = (Workout) extras.getSerializable("workout");
            textView.setText(workout.toString());

            WorkoutExercise[] workoutExercises = workout.getExercises();
            String[] exerciseDescriptions = new String[workoutExercises.length];

            for(int i=0; i<workoutExercises.length; i++) {
                exerciseDescriptions[i] = workoutExercises[i].toSetsRepsString();
            }

            ArrayAdapter<WorkoutExercise> adapter = new ArrayAdapter(this, R.layout.list_item, exerciseDescriptions);
            gridView.setAdapter(adapter);

        }

        init();
    }

    /*
     *  init - Sets up the buttons for adding a photo and sharing to Facebook.
     *  Also contains the click listeners for these.
     */
    public void init() {
        addPhotoButton = (Button) findViewById(R.id.addPhotoButton);
        shareButton = (Button) findViewById(R.id.shareButton);

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                // Required for saving the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, android.os.Environment.DIRECTORY_DCIM);
                startActivity(intent);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareHeading = "I just did my "+workout.toString()+" workout";
                String shareBody = "I just did my "+workout.toString()+" workout:";
                String endLine = ".\nAnd logged it using the WorkoutTracker app!";
                WorkoutExercise[] exercises = workout.getExercises();
                for (int i=0;i<exercises.length;i++){
                    if (shareBody.length()+exercises[i].toString().length() < 280-endLine.length()){
                        shareBody += (", "+exercises[i].toString());
                    }
                }
                shareBody += (endLine);

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareHeading);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share using:"));

            }
        });
    }
}