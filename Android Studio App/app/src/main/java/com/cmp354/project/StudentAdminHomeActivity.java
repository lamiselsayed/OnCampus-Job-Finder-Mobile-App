package com.cmp354.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentAdminHomeActivity extends AppCompatActivity implements View.OnClickListener {
    private String logTAG = "Project", name;
    private Intent i, intent, serviceIntent;
    private Uri viewURI;
    private ImageView ivWebsite, ivBanner;
    private Button btnSearch, btnWishlist, btnPost, btnView;
    private int mode;
    private LinearLayout layoutStudent, layoutAdmin;
    private MenuItem itemHome;
    private CheckBox cbTA, cbRA, cbLA, cbIT, cbEV;
    private FirebaseFirestore db;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    private boolean checkTA, checkRA, checkLA, checkIT, checkEV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_admin_home);

        i = getIntent();
        name = i.getStringExtra("name");
        mode = i.getIntExtra("mode", 0);
        getSupportActionBar().setTitle("Welcome " + name + "!");
        db = FirebaseFirestore.getInstance(); // accessing the database on Firebase

        ivWebsite = (ImageView) findViewById(R.id.ivWebsite);
        ivBanner = (ImageView) findViewById(R.id.ivBanner);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnWishlist = (Button) findViewById(R.id.btnWishlist);
        btnPost = (Button) findViewById(R.id.btnPost);
        btnView = (Button) findViewById(R.id.btnView);
        layoutStudent = (LinearLayout) findViewById(R.id.layoutStudent);
        layoutAdmin = (LinearLayout) findViewById(R.id.layoutAdmin);

        ivWebsite.setOnClickListener(this);
        ivBanner.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnWishlist.setOnClickListener(this);
        btnPost.setOnClickListener(this);
        btnView.setOnClickListener(this);

        layoutStudent.setVisibility(View.GONE);
        layoutAdmin.setVisibility(View.GONE);

        invalidateOptionsMenu(); // this ensures the Action Bar is adjusted accordingly (calls onCreateOptionsMenu)

        sharedPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        if(mode == 0)
        {
            layoutStudent.setVisibility(View.VISIBLE);
            checkTA = sharedPrefs.getBoolean("cbTA" + name, true);
            checkRA = sharedPrefs.getBoolean("cbRA" + name, true);
            checkLA = sharedPrefs.getBoolean("cbLA" + name, true);
            checkIT = sharedPrefs.getBoolean("cbIT" + name, true);
            checkEV = sharedPrefs.getBoolean("cbEV" + name, true);
        }
        else layoutAdmin.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mode == 0)
        {
            serviceIntent = new Intent(this, JobsService.class);
            serviceIntent.putExtra("name", name);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mode == 0) getMenuInflater().inflate(R.menu.menu_student, menu);
        else getMenuInflater().inflate(R.menu.menu_admin, menu);
        itemHome = menu.findItem(R.id.itemHome); // cannot use findViewById (menu items are not regular views part of the layout)
        itemHome.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.itemAbout: // both (w/ customized messages for each)
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

            case R.id.itemAlerts: // student
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View viewInflated = inflater.inflate(R.layout.custom_dialog_alerts, null);

                cbTA = viewInflated.findViewById(R.id.cbTA);
                cbRA = viewInflated.findViewById(R.id.cbRA);
                cbLA = viewInflated.findViewById(R.id.cbLA);
                cbIT = viewInflated.findViewById(R.id.cbIT);
                cbEV = viewInflated.findViewById(R.id.cbEV);

                // student can view their settings whenever they press on the menu item
                cbTA.setChecked(checkTA);
                cbRA.setChecked(checkRA);
                cbLA.setChecked(checkLA);
                cbIT.setChecked(checkIT);
                cbEV.setChecked(checkEV);

                // now associate the view with the dialog
                builder.setView(viewInflated);
                builder.setTitle("Job Alerts Settings");
                builder.setMessage("\nCheck the job types you would like to receive notifications for:");
                builder.setPositiveButton("Save Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // save student's notifications settings into shared preferences
                        editor = sharedPrefs.edit();
                        editor.putBoolean("cbTA" + name, cbTA.isChecked());
                        editor.putBoolean("cbRA" + name, cbRA.isChecked());
                        editor.putBoolean("cbLA" + name, cbLA.isChecked());
                        editor.putBoolean("cbIT" + name, cbIT.isChecked());
                        editor.putBoolean("cbEV" + name, cbEV.isChecked());
                        editor.commit();
                        Log.d(logTAG, "Notification Settings Saved");
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.setIcon(R.drawable.notifications_icon);
                builder.show();
                return true;

            case R.id.itemLogout: // both
                intent = new Intent(this, LoginActivity.class);
                editor = sharedPrefs.edit();
                editor.putString("name", "");
                editor.putInt("mode", -1);
                editor.commit();
                if(serviceIntent != null) stopService(serviceIntent);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.ivWebsite: // both
                viewURI = Uri.parse("https://www.aus.edu/");
                intent = new Intent(Intent.ACTION_VIEW, viewURI);
                startActivity(intent);
                break;

            case R.id.ivBanner: // both
                viewURI = Uri.parse("https://banner.aus.edu/");
                intent = new Intent(Intent.ACTION_VIEW, viewURI);
                startActivity(intent);
                break;

            case R.id.btnSearch: // student
                intent = new Intent(this, ViewJobsActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("mode", mode);
                startActivity(intent);
                break;

            case R.id.btnWishlist: // student
                intent = new Intent(this, JobWishlistActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("mode", mode);
                startActivity(intent);
                break;

            case R.id.btnPost: // admin
                intent = new Intent(this, PostEditActivity.class);
                intent.putExtra("name", name);
                startActivity(intent);
                break;

            case R.id.btnView: // admin
                intent = new Intent(this, ViewJobsActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("mode", mode);
                startActivity(intent);
                break;
        }
    }
}