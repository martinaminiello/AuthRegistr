package com.example.authregistr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrazioneStaff extends AppCompatActivity {

    private static final String TAG = "AccessoRichiedenteAsilo"; // Add this line to define the TAG
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText email;
    private EditText password;

    private Spinner centroSpinner;

    private EditText cellulare;


    private Button register;
    private TextView loginRedirect;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione_staff);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.confirm_registration);
        loginRedirect = findViewById(R.id.alreadyRegistered);

        centroSpinner = findViewById(R.id.spinner_centro);
        cellulare = findViewById(R.id.cell);

        progressBar=findViewById(R.id.progressBar);



        mAuth = FirebaseAuth.getInstance();

        db.collection("CentroAccoglienza")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> centerNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming "Nome" is the field you want to retrieve
                            String nome = document.getString("Nome");
                            centerNames.add(nome);
                        }

                        // Populate Spinner with center names
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, centerNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        centroSpinner.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = email.getText().toString().trim();
                String userpass = password.getText().toString().trim();

                String cellulareValue = cellulare.getText().toString().trim();



                String centroValue = centroSpinner.getSelectedItem().toString();

                if (useremail.isEmpty()) {
                    email.setError("Email required");
                }
                if (userpass.isEmpty()) {
                    password.setError("Password required");
                }   if (userpass.length() < 6) {
                    password.setError( "La password deve essere di almeno 6 caratteri!");

                }
                if (useremail.isEmpty() || userpass.isEmpty() ||
                        cellulareValue.isEmpty() || centroValue.isEmpty()) {
                    Toast.makeText(RegistrazioneStaff.this, "Riempire tutti i campi!", Toast.LENGTH_SHORT).show();
                } if (!isValidEmail(useremail)) {
                    Toast.makeText(RegistrazioneStaff.this, "Email non valida!", Toast.LENGTH_SHORT).show();
                    return; // Stop registration if email is not valid
                }

                // Validate phone number format
                if (!isValidPhoneNumber(cellulareValue)) {
                    Toast.makeText(RegistrazioneStaff.this, "Cellulare non valido!", Toast.LENGTH_SHORT).show();
                    return; // Stop registration if phone number is not valid
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(useremail, userpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                String uid = currentUser.getUid();
                                DocumentReference documentStaff = db.collection("Staff").document(uid);

                                Map<String, Object> RichiedenteAsilo = new HashMap<>();
                                RichiedenteAsilo.put("ID_Staff", uid);
                                RichiedenteAsilo.put("Cellulare", cellulareValue);
                                RichiedenteAsilo.put("Centro", centroValue);
                                RichiedenteAsilo.put("Password", userpass);
                                RichiedenteAsilo.put("Email", useremail);
                                RichiedenteAsilo.put("Ruolo", "Staff");

                                documentStaff
                                        .set(RichiedenteAsilo)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(RegistrazioneStaff.this, "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegistrazioneStaff.this, AccessoStaff.class));
                                                Log.d(TAG, "FINISH successful");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Failed to write document", e);
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(RegistrazioneStaff.this, "Registrazione fallita" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Log.e(TAG, "Authentication failed", task.getException());
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegistrazioneStaff.this,   task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrazioneStaff.this, AccessoStaff.class));
            }
        });


    }



    // Email validation method
    private boolean isValidEmail(String email) {
        // Use a simple regex for email validation
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Phone number validation method
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Use a simple regex for phone number validation
        String phoneRegex = "^[+]?[0-9]{10,13}$";
        return phoneNumber.matches(phoneRegex);
    }
}