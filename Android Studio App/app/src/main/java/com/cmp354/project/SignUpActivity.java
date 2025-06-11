package com.cmp354.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private String logTAG = "Project";
    private FirebaseAuth mAuth;
    private EditText edName, edEmail, edPassS;
    private Button btnSignUp;
    private RadioGroup rgMode;
    private RadioButton rbStudent, rbAdmin;
    private FirebaseFirestore db;
    private CollectionReference studentTbl, adminTbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setTitle("Sign Up Page");

        mAuth = FirebaseAuth.getInstance(); // authentication
        db = FirebaseFirestore.getInstance(); // accessing the database on Firebase

        edName = (EditText) findViewById(R.id.edName);
        edEmail = (EditText) findViewById(R.id.edEmail);
        edPassS = (EditText) findViewById(R.id.edPassS);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        rgMode = (RadioGroup) findViewById(R.id.rgMode);
        rbStudent = (RadioButton) findViewById(R.id.rbStudent);
        rbAdmin = (RadioButton) findViewById(R.id.rbAdmin);

        btnSignUp.setOnClickListener(this);
        rgMode.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if(rbStudent.isChecked() == false && rbAdmin.isChecked() == false) // user must first select mode
        {
            Toast.makeText(this, "Please Select a User Mode", Toast.LENGTH_LONG).show();
            return;
        }
        // verify that all fields (name, username, password) have been entered - cannot be empty or whitespaces
        // otherwise, generate a toast message
        if (edName.getText().toString().isBlank() || edEmail.getText().toString().isBlank() || edPassS.getText().toString().isBlank())
        {
            Toast.makeText(this, "Please Enter All Fields!", Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(edEmail.getText().toString().toLowerCase().trim(), edPassS.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                            builder.setTitle("Success"); // so the icon shows
                            builder.setMessage("\nSign Up Successful! Please Login");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // user redirected to login page after successfully signing up
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // add user to their respective tables (student / admin)
                                    // depending on which mode was selected
                                    // will be used to customize their view/functionalities later on
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", edName.getText().toString());
                                    data.put("email", edEmail.getText().toString().toLowerCase().trim());
                                    if(rbStudent.isChecked()) {
                                        studentTbl = db.collection("studentTbl");
                                        studentTbl.document().set(data);
                                    }
                                    else {
                                        adminTbl = db.collection("adminTbl");
                                        adminTbl.document().set(data);
                                    }

                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.setIcon(R.drawable.success_icon); // not showing for some reason
                            builder.show();
                            Log.d(logTAG, "Sign Up Successful");
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Sign Up Failed", Toast.LENGTH_SHORT).show();
                            Log.w(logTAG, "Sign Up Unsuccessful", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) { }
}