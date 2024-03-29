package com.example.authregistr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccessoStaff extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private Button login;
    private TextView registerRedirect;

    private TextView forgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accesso_staff);

        email=findViewById(R.id.Logemail);
        password=findViewById(R.id.Logpassword);
        login=findViewById(R.id.loginButton);
        registerRedirect=findViewById(R.id.notyetRegistered);
        forgotPassword=findViewById(R.id.password_persa);

        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail=email.getText().toString().trim();
                String userpass=password.getText().toString().trim();

                if(!useremail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(useremail).matches()) {
                    if (!userpass.isEmpty()) {
                        mAuth.signInWithEmailAndPassword(useremail, userpass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(AccessoStaff.this, "Accesso avvenuto con successo", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AccessoStaff.this, HomeS.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AccessoStaff.this, "Accesso fallito", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        password.setError("Password required!");
                    }

                } else if(useremail.isEmpty()){
                    email.setError("Email required!");
                }
                else{
                    email.setError("Email non valida!");
                }

            }
        });

        registerRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccessoStaff.this,RegistrazioneStaff.class));

            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString().trim();

                if (!userEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    mAuth.sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AccessoStaff.this, "Controlla le email per resettare la password", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AccessoStaff.this, "Impossibile resettare la password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    email.setError("Inserisci un indirizzo email valido");
                }
            }
        });
    }
}


