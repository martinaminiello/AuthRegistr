package com.example.authregistr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UserChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_choice);


        List<String> views_user=new ArrayList<>();
        views_user.add(0,"Scegli Utente");
        views_user.add("Richiedente Asilo");
        views_user.add("Staff");

        Spinner spinner_vista = findViewById(R.id.spinner_vista_Utente);



        // Create an ArrayAdapter using the options and a default style
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,getResources().getStringArray(R.array.vista_utente));
        // Apply the adapter to the Spinner
        spinner_vista.setAdapter(adapter);



        spinner_vista.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).equals("Scegli utente")){
                }

                else {
                    String item= parent.getItemAtPosition(position).toString();
                    //Toast.makeText(parent.getContext(),"selezionato "+item, Toast.LENGTH_SHORT).show();

                    if(parent.getItemAtPosition(position).equals("Richiedente Asilo")){
                        Intent intent=new Intent(UserChoiceActivity.this, AccessoRichiedenteAsilo.class);
                        startActivity(intent);
                        Toast.makeText(parent.getContext(),"Entra come "+item, Toast.LENGTH_SHORT).show();
                    }


                    if(parent.getItemAtPosition(position).equals("Staff")){
                        Intent intent=new Intent(UserChoiceActivity.this, AccessoStaff.class);
                        startActivity(intent);
                        Toast.makeText(parent.getContext(),"Entra come "+item, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }



}




