package com.example.authregistr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegistrazioneRichiedenteAsilo extends AppCompatActivity {

    private static final String TAG = "AccessoRichiedenteAsilo"; // Add this line to define the TAG
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText email;
    private EditText password;
    private EditText nome;
    private EditText cognome;
    private Spinner genderSpinner;

    private EditText cellulare;
    private EditText luogonascita;
    private EditText datadinascita;

    private Button register;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione_richiedente_asilo);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.confirm_registration);
        loginRedirect = findViewById(R.id.alreadyRegistered);
        nome = findViewById(R.id.nome);
        cognome = findViewById(R.id.cognome);
        genderSpinner = findViewById(R.id.spinner_genere);
        cellulare = findViewById(R.id.cell);
        luogonascita = findViewById(R.id.luogoNascita);



        mAuth = FirebaseAuth.getInstance();

        datadinascita = findViewById(R.id.dataNascita);
        ImageView imageHome = findViewById(R.id.imageHome);
        imageHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = email.getText().toString().trim();
                String userpass = password.getText().toString().trim();
                String nomeValue = nome.getText().toString().trim();
                String cognomeValue = cognome.getText().toString().trim();
                String cellulareValue = cellulare.getText().toString().trim();
                String luogonascitaValue = luogonascita.getText().toString().trim();
                String nascitaValue = datadinascita.getText().toString().trim();


                String genereValue = genderSpinner.getSelectedItem().toString();

                if (useremail.isEmpty()) {
                    email.setError("Email required");
                }
                if (userpass.isEmpty()) {
                    password.setError("Password required");
                }  if (useremail.isEmpty() || userpass.isEmpty() || nomeValue.isEmpty() || cognomeValue.isEmpty() ||
                        cellulareValue.isEmpty() || luogonascitaValue.isEmpty() || nascitaValue.isEmpty() || genereValue.isEmpty()) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Riempire tutti i campi!", Toast.LENGTH_SHORT).show();
                } if (!isValidEmail(useremail)) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Email non valida!", Toast.LENGTH_SHORT).show();
                    return; // Stop registration if email is not valid
                }

                // Validate phone number format
                if (!isValidPhoneNumber(cellulareValue)) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Cellulare non valido!", Toast.LENGTH_SHORT).show();
                    return; // Stop registration if phone number is not valid
                }else {
                    mAuth.createUserWithEmailAndPassword(useremail, userpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                String uid = currentUser.getUid();
                                DocumentReference documentRichiedenteAsilo = db.collection("RichiedenteAsilo").document(uid);


                                Map<String, Object> RichiedenteAsilo = new HashMap<>();
                                RichiedenteAsilo.put("ID_RichiedenteAsilo", uid);
                                RichiedenteAsilo.put("Nome", nomeValue);
                                RichiedenteAsilo.put("Cognome", cognomeValue);
                                RichiedenteAsilo.put("Cellulare", cellulareValue);
                                RichiedenteAsilo.put("LuogoNascita", luogonascitaValue);
                                RichiedenteAsilo.put("DataNascita", nascitaValue);
                                RichiedenteAsilo.put("Genere", genereValue);
                                RichiedenteAsilo.put("Password", useremail);
                                RichiedenteAsilo.put("Email", useremail);

                                documentRichiedenteAsilo
                                        .set(RichiedenteAsilo)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegistrazioneRichiedenteAsilo.this, AccessoRichiedenteAsilo.class));
                                                Log.d(TAG, "FINISH successful");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Registrazione fallita" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrazioneRichiedenteAsilo.this, AccessoRichiedenteAsilo.class));
            }
        });


    }

    public void showDatePicker() {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        // Apply the custom style
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DatePickerStyle,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        datadinascita.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
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