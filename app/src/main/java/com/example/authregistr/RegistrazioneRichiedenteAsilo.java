package com.example.authregistr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrazioneRichiedenteAsilo extends AppCompatActivity {

    private static final String TAG = "AccessoRichiedenteAsilo"; // Add this line to define the TAG
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private Button register;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione_richiedente_asilo);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        register=findViewById(R.id.confirm_registration);
        loginRedirect=findViewById(R.id.alreadyRegistered);

        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail=email.getText().toString().trim();
                String userpass=password.getText().toString().trim();

                if(useremail.isEmpty()){
                    email.setError("Email required");
                }
                if(userpass.isEmpty()){
                    password.setError("Password required");
                } else {
                    mAuth.createUserWithEmailAndPassword(useremail,userpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegistrazioneRichiedenteAsilo.this,"Registrazione avvenuta con successo",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistrazioneRichiedenteAsilo.this,AccessoRichiedenteAsilo.class));

                                Log.d(TAG, "FINISH successful");
                            }else {
                                Toast.makeText(RegistrazioneRichiedenteAsilo.this,"Registrazione fallita"+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrazioneRichiedenteAsilo.this,AccessoRichiedenteAsilo.class));

            }
        });
    }
}
