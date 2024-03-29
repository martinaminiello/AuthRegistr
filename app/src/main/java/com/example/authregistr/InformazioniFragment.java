package com.example.authregistr;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.authregistr.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InformazioniFragment extends Fragment {

    private static final String TAG = "InformazioniFragment";

    EditText editNome;
    EditText editIndirizzo;
    EditText editLink;
    EditText editEmail;
    EditText editTel;
    EditText editDescr;

    View view;
    Button saveButton;


    private final FirebaseFirestore dbS = FirebaseFirestore.getInstance();
    DocumentReference documentStaff;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();



    DocumentReference documentRef;
    CollectionReference staffCollection = dbS.collection("Staff");

    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_informazioni, container, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        editNome = view.findViewById(R.id.editNome);
        editEmail = view.findViewById(R.id.editEmail);
        editIndirizzo = view.findViewById(R.id.editIndirizzo);
        editTel = view.findViewById(R.id.editTel);
        editLink = view.findViewById(R.id.editLink);
        editDescr = view.findViewById(R.id.editDescription);
        saveButton = view.findViewById(R.id.saveBtn);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();  // Update the class variable 'uid'
            documentStaff = dbS.collection("Staff").document(uid);
            fetchDataCentre();
            saveButton.setOnClickListener(v -> saveDataToFirestore());
        } else {
            // Handle the case where the current user is null
            Log.d(TAG, "Current user is null");
        }






        return view;
    }

    private void fetchDataCentre() {
        fetchField("Nome", editNome);
        fetchField("Indirizzo", editIndirizzo);
        fetchField("Sito web", editLink);
        fetchField("Email", editEmail);
        fetchField("Telefono", editTel);
        fetchField("Descrizione", editDescr);
    }

    private void fetchField(String fieldName, EditText editText) {
        documentStaff.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if the document exists
                        String centroValue = documentSnapshot.getString("Centro");
                        if (centroValue != null) {
                            fetchFieldFromCentroAccoglienza(fieldName, editText, centroValue);
                        } else {
                            // Handle the case where "Centro" field in "Staff" is null
                            Log.d(TAG, "Centro field is null for user with UID " + uid);
                        }
                    } else {
                        // Handle the case where the "Staff" document does not exist
                        Log.d(TAG, "No document found in Staff collection for user with UID " + uid);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving document from Staff collection: " + e.getMessage()));
    }

    private void fetchFieldFromCentroAccoglienza(String fieldName, EditText editText, String centroValue) {
        CollectionReference centroAccoglienzaCollection = dbS.collection("CentroAccoglienza");

        centroAccoglienzaCollection.whereEqualTo("Nome", centroValue)
                .limit(1) // Limit to one document, since we only need one
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // There is a matching document in "CentroAccoglienza"
                        DocumentSnapshot matchingDocument = queryDocumentSnapshots.getDocuments().get(0);
                        String fieldValue = matchingDocument.getString(fieldName);

                        // UPDATE EDITTEXT
                        if (fieldValue != null) {
                            editText.setText(fieldValue);
                        }
                    } else {
                        // No matching document found in "CentroAccoglienza"
                        Log.d(TAG, "No matching document found in CentroAccoglienza collection for Centro: " + centroValue);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error querying CentroAccoglienza collection: " + e.getMessage()));
    }

    private void saveDataToFirestore() {
        CollectionReference centroAccoglienzaCollection = dbS.collection("CentroAccoglienza");
        String nome = editNome.getText().toString();
        String indirizzo = editIndirizzo.getText().toString();
        String link = editLink.getText().toString();
        String email = editEmail.getText().toString();
        String tel = editTel.getText().toString();
        String descr = editDescr.getText().toString();

        documentStaff.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String centroValue = documentSnapshot.getString("Centro");
                        if (centroValue != null) {
                            centroAccoglienzaCollection.whereEqualTo("Nome", centroValue)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            DocumentSnapshot matchingDocument = queryDocumentSnapshots.getDocuments().get(0);
                                            DocumentReference documentRef = matchingDocument.getReference();
                                            Map<String, Object> newData = new HashMap<>();
                                            newData.put("Nome", nome);
                                            newData.put("Indirizzo", indirizzo);
                                            newData.put("Sito web", link);
                                            newData.put("Email", email);
                                            newData.put("Telefono", tel);
                                            newData.put("Descrizione", descr);

                                            documentRef.update(newData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Document updated successfully");
                                                        Toast.makeText(getContext(), "I dati sono stati aggiornati!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> Log.e(TAG, "Errore: " + e.getMessage()));

                                            // You can update other UI elements if needed
                                        } else {
                                            Log.d(TAG, "No matching document found in CentroAccoglienza collection for Centro: " + centroValue);
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error querying CentroAccoglienza collection: " + e.getMessage()));
                        } else {
                            Log.d(TAG, "Centro field is null for user with UID " + uid);
                        }
                    } else {
                        Log.d(TAG, "No document found in Staff collection for user with UID " + uid);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving document from Staff collection: " + e.getMessage()));
    }



}
