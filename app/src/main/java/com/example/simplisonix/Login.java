package com.example.simplisonix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity
{
    Button proceed;
    EditText user, password;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = findViewById(R.id.login_username);
        proceed=(Button) findViewById(R.id.login);
        password = findViewById(R.id.login_password);
        mAuth=FirebaseAuth.getInstance();

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String name = user.getText().toString();
                String pass = password.getText().toString();

                boolean check = validation(name,pass);
                if(check==true)
                {
                    mAuth.signInWithEmailAndPassword(name, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(Login.this, "Login Successful",
                                                Toast.LENGTH_SHORT).show();
                                        Log.d("loggin","Logged in");
                                        startActivity(new Intent(Login.this, AudioPlayer.class));
                                    }
                                    else
                                    {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Login.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Username or Password is wrong",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validation(String name, String pass)
    {
        if(name.length()==0)
        {
            user.requestFocus();
            user.setError("Field Cannot be Empty");
            return false;
        }

        if (pass.length()==0)
        {
            password.requestFocus();
            password.setError("Field cannot be empty");
            return false;
        }

        else
        {
            return true;
        }

    }
}