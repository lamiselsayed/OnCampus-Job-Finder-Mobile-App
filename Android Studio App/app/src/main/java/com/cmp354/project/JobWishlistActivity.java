package com.cmp354.project;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class JobWishlistActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private ListView wishlistListView;
    private Intent intent, i;
    private String name;
    private MenuItem itemAlerts, itemLogout;
    private SimpleCursorAdapter adapter;
    private int mode;
    private DatabaseConnector dbConnector;
    private Cursor result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wishlistListView = getListView(); // get the activity's list view widget
        wishlistListView.setOnItemClickListener(this);
        wishlistListView.setDividerHeight(20);
        getActionBar().setIcon(R.drawable.wishlist_icon);
        getActionBar().setTitle("\tMy Job Wishlist");

        i = getIntent();
        name = i.getStringExtra("name");
        mode = i.getIntExtra("mode", 0);

        dbConnector = new DatabaseConnector(this, name);

        String[] from = new String[] { "title", "type", "salary", "duration", "description" };
        int[] to = new int[] {R.id.tvWTitle, R.id.tvWType, R.id.tvWSalary, R.id.tvWDuration, R.id.tvWDesc};
        adapter = new SimpleCursorAdapter(this, R.layout.activity_job_wishlist, null, from, to, 0);
        setListAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayWishlist();
    }

    @Override
    protected void onStop() {
        Cursor cursor = adapter.getCursor();
        adapter.changeCursor(null);
        if(cursor != null)
            cursor.close();

        super.onStop();
    }

    public void displayWishlist() {
        dbConnector.open();
        result = dbConnector.getAllJobsinWishlist();
        adapter.changeCursor(result);
        dbConnector.close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView titleWishlist = (TextView) view.findViewById(R.id.tvWTitle);
        String message = titleWishlist.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Job from Wishlist?");
        builder.setMessage("Are you sure you want to delete " + message + "?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbConnector.deleteJobWishlist(id);
                displayWishlist();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setIcon(R.drawable.small_warning_icon);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student, menu);
        itemAlerts = menu.findItem(R.id.itemAlerts); // cannot use findViewById (menu items are not regular views part of the layout)
        itemAlerts.setVisible(false);
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
                aboutDialog.setMessage("Welcome to your student page! You may view currently-" +
                        "offered jobs by different departments, add them to your wishlist, and also " +
                        "view your job wishlist. You may also customize the notifications you'd " +
                        "like to receive when a new job offering is posted.");
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
}