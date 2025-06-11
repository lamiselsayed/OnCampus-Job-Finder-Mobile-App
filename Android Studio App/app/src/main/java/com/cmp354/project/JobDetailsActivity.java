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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class JobDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private String logTAG = "Project", name, docID;
    private Intent intent, i;
    private int mode;
    private EditText edJobTitle, edDur, edSal, edJobDesc;
    private Spinner jobTypeSpinner;
    private MenuItem itemLogout;
    private FirebaseFirestore db;
    private LinearLayout layoutApply, layoutEdit;
    private Button btnAddWishlist, btnEdit, btnDelete;
    private CollectionReference jobs;
    private DatabaseConnector database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job_details);
        db = FirebaseFirestore.getInstance(); // accessing the database on Firebase
        jobs = db.collection("jobs");

        i = getIntent();
        name = i.getStringExtra("name");
        mode = i.getIntExtra("mode", 0);
        docID = i.getStringExtra("docID");

        layoutApply = (LinearLayout) findViewById(R.id.layoutApply);
        layoutEdit = (LinearLayout) findViewById(R.id.layoutEdit);
        layoutApply.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.GONE);

        btnAddWishlist = (Button) findViewById(R.id.btnAddWishlist);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        btnAddWishlist.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        jobTypeSpinner = (Spinner) findViewById(R.id.jobTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.jobType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobTypeSpinner.setAdapter(adapter);

        edJobTitle = (EditText) findViewById(R.id.edJobTitle);
        edDur = (EditText) findViewById(R.id.edDur);
        edSal = (EditText) findViewById(R.id.edSal);
        edJobDesc = (EditText) findViewById(R.id.edJobDesc);
        if(mode == 0) // student cannot modify job details, only admins can
        {
            layoutApply.setVisibility(View.VISIBLE);
            edJobTitle.setEnabled(false);
            jobTypeSpinner.setEnabled(false);
            edDur.setEnabled(false);
            edSal.setEnabled(false);
            edJobDesc.setEnabled(false);
        }
        else layoutEdit.setVisibility(View.VISIBLE);

        // get job details of the opportunity user clicked on and set all edit texts & the spinner
        db.collection("jobs").document(docID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                edJobTitle.setText((String) documentSnapshot.get("title"));
                jobTypeSpinner.setSelection(adapter.getPosition((String) documentSnapshot.get("type")));
                edDur.setText((String) documentSnapshot.get("duration"));
                edSal.setText((String) documentSnapshot.get("salary"));
                edJobDesc.setText((String) documentSnapshot.get("description"));
                Log.d(logTAG, "Job Details Retrieved from Firebase");
            }
        });
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
                if(mode == 0)
                    aboutDialog.setMessage("Welcome to your student page! You may view currently-" +
                            "offered jobs by different departments, add them to your wishlist, and also " +
                            "view your job wishlist. You may also customize the notifications you'd " +
                            "like to receive when a new job offering is posted.");
                else
                    aboutDialog.setMessage("Welcome to your admin page! You may view currently-" +
                            "offered jobs by different departments, make any modifications to them, and " +
                            "post new job offerings.");
                aboutDialog.show();
                return true;

            case R.id.itemHome:
                intent = new Intent(this, StudentAdminHomeActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("mode", mode); // to ensure home activity is customized accordingly
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnAddWishlist: // for student
                database = new DatabaseConnector(this, name);
                if(!database.checkIfExists(edJobTitle.getText().toString(),
                        (String) jobTypeSpinner.getSelectedItem(),
                        Float.parseFloat(edSal.getText().toString()),
                        edDur.getText().toString(),
                        edJobDesc.getText().toString())) {
                    database.insertJobWishlist(edJobTitle.getText().toString(),
                            (String) jobTypeSpinner.getSelectedItem(),
                            Float.parseFloat(edSal.getText().toString()),
                            edDur.getText().toString(),
                            edJobDesc.getText().toString()
                    );
                    Toast.makeText(this, "Job Successfully Added to Wishlist", Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(this, "Job Already in Wishlist", Toast.LENGTH_LONG).show();
                break;

            case R.id.btnEdit: // for admin
                Map<String, Object> details = new HashMap<>();
                details.put("title", edJobTitle.getText().toString());
                details.put("type", (String) jobTypeSpinner.getSelectedItem());
                details.put("duration", edDur.getText().toString());
                details.put("salary", edSal.getText().toString());
                details.put("description", edJobDesc.getText().toString());
                jobs.document(docID).set(details); // use the docID to ensure that the document attributes are overwritten
                Toast.makeText(this, "Job Details Updated Successfully", Toast.LENGTH_LONG).show();
                Log.d(logTAG, "Job Details Updated");
                finish(); // go back to the caller -> master (ViewJobsActivity)
                break;

            case R.id.btnDelete: // for admin
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Delete Job?");
                alert.setMessage("\nAre you sure you want to delete " + edJobTitle.getText().toString() + "?");
                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection("jobs").document(docID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(logTAG, "Job Deleted");
                                        Toast.makeText(JobDetailsActivity.this,
                                                "Job Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        intent = new Intent(JobDetailsActivity.this, ViewJobsActivity.class);
                                        intent.putExtra("name", name);
                                        intent.putExtra("mode", mode);
                                        startActivity(intent); // go back to the master (ViewJobsActivity)
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(logTAG, e.getMessage());
                                        Toast.makeText(JobDetailsActivity.this,
                                                "Job Deletion Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                alert.setNegativeButton("Cancel", null);
                alert.setIcon(R.drawable.warning_icon_yellow);
                alert.show();
                break;
        }
    }
}