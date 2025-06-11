package com.cmp354.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PostEditActivity extends AppCompatActivity implements View.OnClickListener {
    private Spinner spinner;
    private EditText edTitle, edDesc, edDuration, edSalary;
    private Button btnSave;
    private MenuItem itemLogout;
    private FirebaseFirestore db;
    private Intent intent, i;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);
        db = FirebaseFirestore.getInstance(); // accessing the database on Firebase

        i = getIntent();
        name = i.getStringExtra("name"); // need this so that if we press on 'Home' from this activity,
        // we can set the title of the action bar

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.jobType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        edTitle = (EditText) findViewById(R.id.edTitle);
        edDesc = (EditText) findViewById(R.id.edDesc);
        edDuration = (EditText) findViewById(R.id.edDuration);
        edSalary = (EditText) findViewById(R.id.edSalary);
        btnSave = (Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // check for nulls before posting
        if (edTitle.getText().toString().isBlank() || edDesc.getText().toString().isBlank() ||
                edDuration.getText().toString().isBlank() || edSalary.getText().toString().isBlank())
        {
            Toast.makeText(this, "Please Enter All Fields!", Toast.LENGTH_LONG).show();
            return;
        }

        CollectionReference jobs = db.collection("jobs");
        Map<String, Object> data = new HashMap<>();
        data.put("title", edTitle.getText().toString());
        data.put("description", edDesc.getText().toString());
        data.put("type", (String) spinner.getSelectedItem());
        data.put("duration", edDuration.getText().toString());
        data.put("salary", edSalary.getText().toString());
        jobs.document().set(data); // creates a new document with an automatically-generated ID

        // save system time in a table; each job type has its own system time which will be compared
        // with the current time to determine whether a notification should be sent to the student
        CollectionReference updates = db.collection("updates");
        Map<String, Object> newJob = new HashMap<>();
        newJob.put((String) spinner.getSelectedItem(), System.currentTimeMillis());
        updates.document((String) spinner.getSelectedItem()).set(newJob);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success");
        builder.setMessage("\nJob Posted Successfully!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { finish(); }
        });
        builder.setIcon(R.drawable.success_icon);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        itemLogout = menu.findItem(R.id.itemLogout);
        itemLogout.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.itemAbout:
                AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
                aboutDialog.setTitle("About");
                aboutDialog.setMessage("Welcome to your admin page! You may view currently-" +
                        "offered jobs by different departments, make any modifications to them, and " +
                        "post new job offerings.");
                aboutDialog.show();
                return true;

            case R.id.itemHome:
                intent = new Intent(this, StudentAdminHomeActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("mode", 1); // to ensure home activity is customized accordingly
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}