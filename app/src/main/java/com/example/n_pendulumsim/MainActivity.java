package com.example.n_pendulumsim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        // Set window to full screen so no backbar
        Window window = getWindow();

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        //Set the view to Gamer View
        setContentView(R.layout.activity_main);
    }


    public void runSimulationMethod(View view) {

        //Get the Values
        EditText n = findViewById(R.id.nop_Value);
        EditText length = findViewById(R.id.length_Value);
        EditText mass = findViewById(R.id.mass_Value);
        EditText gravity = findViewById(R.id.gravity_Value);
        EditText startAngle = findViewById(R.id.angle_Value);
        EditText initVelocity = findViewById(R.id.startVelocity_Value);
        EditText damping = findViewById(R.id.dampingPercentage_Value);

        //Get Create Intent
        Intent intent = new Intent(this,Pendulum_Activity.class);

//        //Make Intent
//        intent.putExtra("n", Integer.parseInt(n.getText().toString()));
//        intent.putExtra("l", Double.parseDouble(length.getText().toString()));
//        intent.putExtra("m", Double.parseDouble(mass.getText().toString()));
//        intent.putExtra("g", Double.parseDouble(gravity.getText().toString()));
//        intent.putExtra("theta", Double.parseDouble(startAngle.getText().toString()));
//        intent.putExtra("v", Double.parseDouble(initVelocity.getText().toString()));
//        intent.putExtra("damp", Double.parseDouble(damping.getText().toString()));

        //Make Intent
        intent.putExtra("n", getIntValFromEditText(n));
        intent.putExtra("l", getDoubleValFromEditText(length));
        intent.putExtra("m", getDoubleValFromEditText(mass));
        intent.putExtra("g", getDoubleValFromEditText(gravity));
        intent.putExtra("theta", getDoubleValFromEditText(startAngle));
        intent.putExtra("v", getDoubleValFromEditText(initVelocity));
        intent.putExtra("damp", getDoubleValFromEditText(damping));

        //Starting the Pendulum Activity
        startActivity(intent);
    }

    public int getIntValFromEditText(EditText text){
        if(text.getText().toString().isEmpty()){
            return 1;
        }
        else {
            return Integer.parseInt(text.getText().toString());
        }
    }

    public double getDoubleValFromEditText(EditText text){
        Editable t = text.getText();
        String val =  text.getText().toString();
        if(text.getText().toString().isEmpty()){
            return 0.0;
        }
        else {
            return Double.parseDouble(text.getText().toString());
        }
    }
}