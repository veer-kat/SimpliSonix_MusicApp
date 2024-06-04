package com.example.simplisonix;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.simplisonix.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity
{
    Button save;
    EditText userName, emailId, password, confPassword;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        save=(Button) findViewById(R.id.register);
        userName = findViewById(R.id.regi_username);
        emailId = findViewById(R.id.regi_email);
        password = findViewById(R.id.regi_password);
        confPassword = findViewById(R.id.regi_confirmPassword);
        mAuth=FirebaseAuth.getInstance();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userName.getText().toString();
                String email = emailId.getText().toString();
                String pass = password.getText().toString();
                String confpass = confPassword.getText().toString();

                boolean check = validateinfo(name,email,pass,confpass);
                if(check==true)
                {
                    mAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(getApplicationContext(),"Registered successfully",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Register.this, Login.class));
                                        finish();
                                    }
                                    else
                                    {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Sorry check the Information again",Toast.LENGTH_SHORT).show();
                }



            }
        });

    }

    private Boolean validateinfo(String name, String email, String pass, String confpass) {
        if(name.length()==0)
        {
            userName.requestFocus();
            userName.setError("Field Cannot be Empty");
            return false;
        }

        if (!name.matches("[a-zA-Z]+")) {
            userName.requestFocus();
            userName.setError("Enter only alphabetical character");
            return false;
        }

        if (email.length()==0)
        {
            emailId.requestFocus();
            emailId.setError("Field cannot be empty");
            return false;
        }

        if (!email.matches("[a-zA-Z0-9._]+@[a-z]+\\.+[a-z]+"))
        {
            emailId.requestFocus();
            emailId.setError("Enter the Valid Email address");
            return false;
        }

        if (pass.length()==0)
        {
            password.requestFocus();
            password.setError("Field cannot be empty");
            return false;
        }

        if (pass.length() < 8 || pass.length() >= 18)
        {
            password.requestFocus();
            password.setError("Password length should be between 8 and 17 characters");
            return false;
        }


        if (!pass.equals(confpass))
        {
            confPassword.requestFocus();
            confPassword.setError("Password does not match");
            return false;
        }

        else
        {
            return true;
        }



    }

}