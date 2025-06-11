package com.cmp354.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewJobsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    private ListView jobsListView;
    private SearchView searchJobs;
    private FirebaseFirestore db;
    private Intent intent, i;
    private String name, docID, type;
    private MenuItem itemLogout;
    private SimpleAdapter adapter;
    private Spinner spin;
    private Button btn_filter;
    private int mode;

    private EditText ed_minSal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_jobs);

        jobsListView = (ListView) findViewById(R.id.jobsList);
        jobsListView.setOnItemClickListener(this);

        ed_minSal = (EditText) findViewById(R.id.ed_minSal);
        searchJobs = (SearchView) findViewById(R.id.searchJobs);
        btn_filter = (Button) findViewById(R.id.btn_Filter);

        spin = (Spinner) findViewById(R.id.SpinnerChoose);
        ArrayAdapter<CharSequence> adapt = ArrayAdapter.createFromResource(this,
                R.array.typeChoices, android.R.layout.simple_spinner_item);
        adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapt);
        spin.setSelection(0);

        db = FirebaseFirestore.getInstance(); // accessing the database on Firebase
        i = getIntent();
        name = i.getStringExtra("name"); // need this so that if we press on 'Home' from this activity,
        // we can set the title of the action bar
        mode = i.getIntExtra("mode", 0);
        type = i.getStringExtra("type"); // needed to determine whether to show the entire list or a filtered list based on type
        if(type != null) // i.e., job type was passed as an extra to the intent -> notification was generated -> show a filtered list
        {
            if(type.equals("Teaching Assistant")) spin.setSelection(1);
            else if (type.equals("Research Assistant")) spin.setSelection(2);
            else if (type.equals("IT (CEN-CAS)")) spin.setSelection(3);
            else if (type.equals("Library Assistant")) spin.setSelection(4);
            else spin.setSelection(5);
            displayJobs(searchJobs.getQuery().toString().trim(),
                    (String) spin.getSelectedItem(),
                    ed_minSal.getText().toString().trim());
        }
        else // i.e, no job type was passed as an extra -> show the entire list
            displayJobs(searchJobs.getQuery().toString().trim(),
                    (String) spin.getSelectedItem(),
                    ed_minSal.getText().toString().trim());
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayJobs(searchJobs.getQuery().toString().trim(),
                (String) spin.getSelectedItem(),
                ed_minSal.getText().toString().trim());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // we want to retrieve the ID of the item selected from the firebase table
        Map<String, String> selectedItem = (Map<String, String>) parent.getItemAtPosition(position);
        db.collection("jobs")
                .whereEqualTo("title", selectedItem.get("title"))
                .whereEqualTo("type", selectedItem.get("type"))
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty())
                            docID = queryDocumentSnapshots.getDocuments().get(0).getId();

                        intent = new Intent(ViewJobsActivity.this, JobDetailsActivity.class);
                        intent.putExtra("docID", docID);
                        intent.putExtra("name", name);
                        intent.putExtra("mode", mode);
                        startActivity(intent);
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

    public void onFilter(View view)  /* OnClick for Filter Button */ {
        if(view.getId() == R.id.btn_Filter)
            displayJobs(searchJobs.getQuery().toString().trim(),
                    (String) spin.getSelectedItem(),
                    ed_minSal.getText().toString().trim());
    }

    public void displayJobs(String key, String type, String sal) {
        double minSal;
        String searchKey = "";
        List<String> types = new ArrayList<String>();
        if(type == null || type.equals("All Jobs"))
        {
            types.add("Teaching Assistant");
            types.add("Research Assistant");
            types.add("IT (CEN-CAS)");
            types.add("Library Assistant");
            types.add("Event Volunteer");
        }
        else types.add(type);
        if(sal == null || sal.isBlank()) minSal = 0.0f;
        else minSal = Double.parseDouble(sal);

        if(key != null && !key.isBlank()) searchKey = key;
        String finalSearchKey = searchKey;

        // create a List of Map<String, ?> objects to display the items inside the list
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        db.collection("jobs")
                .whereIn("type", types)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(Double.parseDouble(document.get("salary").toString()) >= minSal
                                        && document.get("title").toString().toLowerCase()
                                        .contains(finalSearchKey.toLowerCase())){
                                    HashMap<String, String> job = new HashMap<String, String>();
                                    job.put("title", document.getString("title"));
                                    job.put("type", document.getString("type"));
                                    job.put("id", document.getId());
                                    data.add(job); // map object contains what we want to display for each item on the master
                                }
                            }

                            // create the resource, from, and to variables
                            int resource = R.layout.activity_view_job_in_list; // where we are going to display this info
                            String[] from = {"title", "type"}; // where's the info coming from
                            int[] to = {R.id.tvTitleList, R.id.tvTypeList}; // where is the info going to

                            // create and set the adapter
                            adapter = new SimpleAdapter(ViewJobsActivity.this, data, resource, from, to);
                            jobsListView.setAdapter(adapter);
                        }
                    }
                });

    }
}