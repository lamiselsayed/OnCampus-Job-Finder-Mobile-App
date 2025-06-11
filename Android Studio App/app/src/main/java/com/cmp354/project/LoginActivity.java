package com.cmp354.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private String logTAG = "Project", name;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText edUser, edPass;
    private TextView tvSignUp;
    private Button btnLogin;
    private Intent i;
    private Boolean found;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        name = sharedPrefs.getString("name", "");
        int mode = sharedPrefs.getInt("mode", -1);
        if(!name.isBlank() && mode != -1) {
            i = new Intent(this, StudentAdminHomeActivity.class);
            i.putExtra("name", name);
            i.putExtra("mode", mode);
            startActivity(i);
        }

        getSupportActionBar().setTitle("Login Page");

        mAuth = FirebaseAuth.getInstance(); // authentication
        db = FirebaseFirestore.getInstance(); // accessing the database on Firebase
        found = false; // flag used to search for the user attempting to log in the student & admin tables

        edUser = (EditText) findViewById(R.id.edUser);
        edPass = (EditText) findViewById(R.id.edPass);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvSignUp = (TextView) findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                // first, verify that both a username and password have been entered - cannot be empty or whitespaces
                // otherwise, generate a toast message
                if (edUser.getText().toString().isBlank() || edPass.getText().toString().isBlank()) {
                    Toast.makeText(this, "Please Enter Username & Password!", Toast.LENGTH_LONG).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(edUser.getText().toString().toLowerCase().trim(), edPass.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) // if login successful, user is taken to his/her corresponding activity
                                {
                                    Log.d(logTAG, "Login Successful");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    db.collection("studentTbl")
                                            .whereEqualTo("email", edUser.getText().toString().toLowerCase().trim())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                        QueryDocumentSnapshot document = task.getResult().iterator().next();
                                                        found = true;

                                                        name = (String) document.get("name");
                                                        editor = sharedPrefs.edit();
                                                        editor.putString("name", name);
                                                        editor.putInt("mode", 0);
                                                        editor.commit();
                                                        i = new Intent(LoginActivity.this, StudentAdminHomeActivity.class);
                                                        i.putExtra("name", name);
                                                        i.putExtra("mode", 0);
                                                        startActivity(i);
                                                    }
                                                }
                                            });
                                    if (!found) {
                                        db.collection("adminTbl")
                                                .whereEqualTo("email", edUser.getText().toString().toLowerCase().trim())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                            QueryDocumentSnapshot document = task.getResult().iterator().next();
                                                            found = true;

                                                            name = (String) document.get("name");
                                                            editor = sharedPrefs.edit();
                                                            editor.putString("name", name);
                                                            editor.putInt("mode", 1);
                                                            editor.commit();
                                                            i = new Intent(LoginActivity.this, StudentAdminHomeActivity.class);
                                                            i.putExtra("name", name);
                                                            i.putExtra("mode", 1);
                                                            startActivity(i);
                                                        }
                                                    }
                                                });
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                                    Log.w(logTAG, "Login Unsuccessful", task.getException());
                                }
                            }
                        });
                break;
            case R.id.tvSignUp:
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }
}