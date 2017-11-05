package tracker.workout.workouttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class CreateWorkoutActivity extends AppCompatActivity {

    public Button addExerciseButton;
    public Button saveExerciseButton;
    public ArrayList<String> workoutExercises = new ArrayList<String>();
    public DatabaseHelper dbHelper;
    private String inputText = "";
    private ListView workoutExercisesList;

    public void init() {
        addExerciseButton = (Button)findViewById(R.id.addExercise);
        saveExerciseButton = (Button)findViewById(R.id.save);

        addExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addActivity = new Intent(CreateWorkoutActivity.this, CategoriesActivity.class);
                addActivity.putExtra("workoutExercises", workoutExercises);
                startActivity(addActivity);
            }
        });

        saveExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateWorkoutActivity.this);
                builder.setTitle("Workout Name");
                final EditText input = new EditText(CreateWorkoutActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Setting up buttons
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputText = input.getText().toString();
                        String[] arr = workoutExercises.toArray(new String[workoutExercises.size()]);
                        dbHelper.insertWorkout(inputText, arr);
                        Intent mainActivity = new Intent(CreateWorkoutActivity.this, MainActivity.class);
                        startActivity(mainActivity);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);
        workoutExercisesList = (ListView) findViewById(R.id.listWorkoutExercises);
        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            workoutExercises = (ArrayList<String>) extras.get("workoutExercises");
            ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.list_item, workoutExercises);
            workoutExercisesList.setAdapter(adapter);
        }

        TextView empty=(TextView)findViewById(R.id.empty);
        workoutExercisesList.setEmptyView(empty);
        dbHelper = new DatabaseHelper(this);
        init();
    }
}