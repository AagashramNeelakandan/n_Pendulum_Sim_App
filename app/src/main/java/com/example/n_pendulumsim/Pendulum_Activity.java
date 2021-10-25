package com.example.n_pendulumsim;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class Pendulum_Activity extends AppCompatActivity {

    Game gameView; // View Containing Game
    RelativeLayout gameButtonsView; // View Containing Game Buttons
    FrameLayout joinView; //View for joining the above two
    volatile boolean stopThread = false;
    Dialog infoDialog;

    public Boolean getBoolean(){
        return stopThread;
    }

    public void setBoolean(boolean stopValue){
        stopThread=stopValue;
    }



    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Dialog
        infoDialog = new Dialog(this);


        //Intent and value from other Activity
        Intent intent = getIntent();
        int n = intent.getIntExtra("n",1);
        double l = intent.getDoubleExtra("l",1.0);
        double m = intent.getDoubleExtra("m",1.0);
        double g = intent.getDoubleExtra("g",9.81);
        double theta = intent.getDoubleExtra("theta",10.0);
        double v = intent.getDoubleExtra("v",0.0);
        double damp = intent.getDoubleExtra("damp",100.0);

        //Setting Default Values
        if(n<=0){
            n=1;
        }
        if(l<=0.0){
            l=1.0;
        }
        if(m<=0.0){
            m=1.0;
        }
        if(g<=0.0){
            g=9.81;
        }
        if(theta<=0.0){
            theta=10.0;
        }
        if(v<=0.0){
            v=0.0;
        }
        if(damp<=0.0){
            damp=100.0;
        }

        //Creating Buttons
        Button zoomInButton = generateButtons("+",30,10001, Color.rgb(255,250,235),Color.TRANSPARENT);
        Button zoomOutButton = generateButtons("-",30,10002,Color.rgb(255,250,235),Color.TRANSPARENT);
        Button zoomReset = generateButtons("Reset",20,10003,Color.rgb(255,250,235),Color.TRANSPARENT);

        Button statsToggle = generateButtons("Stats",20,10004,Color.rgb(255,250,235),Color.TRANSPARENT);
        Button tracerToggle = generateButtons("Trace",20,10005,Color.rgb(255,250,235),Color.TRANSPARENT);

        // Button Array
        Button[] zoomControls = {zoomInButton,zoomOutButton,zoomReset,statsToggle,tracerToggle};

        //Getting Screen Size

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);



        //Since button is needed in GameView
        gameView = new Game(this, n, l, m, g, theta, v, damp, dm.widthPixels, dm.heightPixels, zoomControls);
        gameButtonsView = new RelativeLayout(this);
        joinView = new FrameLayout(this);


        //RelativeLayout Params for the Button
        RelativeLayout.LayoutParams b1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams b2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams b3_Reset = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        //Stas and Tracer Params
        RelativeLayout.LayoutParams b4_stats = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams b5_tracer = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        //Pos for the Button Position
        b1.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        //b1.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        zoomInButton.setLayoutParams(b1);

        b2.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        //b2.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        zoomOutButton.setLayoutParams(b2);

        b3_Reset.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        //b2.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        zoomReset.setLayoutParams(b3_Reset);

        b4_stats.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        //b2.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        statsToggle.setLayoutParams(b4_stats);

        b5_tracer.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        //b2.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        tracerToggle.setLayoutParams(b5_tracer);


        //Linear Layout for the Buttons
        LinearLayout outerVertical = new LinearLayout(this);
        LinearLayout innerHorizontal_B1 = new LinearLayout(this);
        LinearLayout innerHorizontal_B2 = new LinearLayout(this);
        LinearLayout innerHorizontal_B3_R = new LinearLayout(this);

        //Side Stats and Tracer Layout
        LinearLayout stats = new LinearLayout(this);
        LinearLayout Tracer = new LinearLayout(this);
        LinearLayout stats_Tracer = new LinearLayout(this);

        //Orientation set on the LinearLayout Object
        outerVertical.setOrientation(LinearLayout.VERTICAL);
        innerHorizontal_B1.setOrientation(LinearLayout.HORIZONTAL);
        innerHorizontal_B2.setOrientation(LinearLayout.HORIZONTAL);
        innerHorizontal_B3_R.setOrientation(LinearLayout.HORIZONTAL);

        stats.setOrientation(LinearLayout.HORIZONTAL);
        Tracer.setOrientation(LinearLayout.HORIZONTAL);
        stats_Tracer.setOrientation(LinearLayout.HORIZONTAL);


        //Adding Button to Linear Layout Horizontal View
        innerHorizontal_B1.addView(zoomInButton);
        innerHorizontal_B2.addView(zoomOutButton);
        innerHorizontal_B3_R.addView(zoomReset);

        stats.addView(statsToggle);
        Tracer.addView(tracerToggle);

        //Adding Horizontal LinearLayout to Vertical One
        outerVertical.addView(innerHorizontal_B1);
        outerVertical.addView(innerHorizontal_B2);
        outerVertical.addView(innerHorizontal_B3_R);

        //Another Relative View For Linear Layout
        RelativeLayout linearLayout_Combiner_RelativeLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams linearLayout_Combiner_Relative = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        linearLayout_Combiner_Relative.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        linearLayout_Combiner_Relative.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);

        linearLayout_Combiner_RelativeLayout.setLayoutParams(linearLayout_Combiner_Relative);

        linearLayout_Combiner_RelativeLayout.addView(outerVertical);


        stats_Tracer.addView(stats);
        stats_Tracer.addView(Tracer);



        //Another Relative View For Linear Layout for the Stats and Tracer
        RelativeLayout stats_tracer_combiner = new RelativeLayout(this);

        RelativeLayout.LayoutParams stats_tracer_combiner_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        stats_tracer_combiner_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        stats_tracer_combiner_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);

        stats_tracer_combiner.setLayoutParams(stats_tracer_combiner_params);

        stats_tracer_combiner.addView(stats_Tracer);
        //stats_tracer_combiner.addView(Tracer);


        //Button Wrapper
        RelativeLayout.LayoutParams buttonsWrapper = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);

        //Conditions for Layout Param

        buttonsWrapper.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
        buttonsWrapper.addRule(RelativeLayout.ALIGN_BOTTOM,RelativeLayout.TRUE);


        Button info = generateButtons("info",20,10006,Color.rgb(255,250,235),Color.TRANSPARENT);
        info.setTypeface(Typeface.SERIF,Typeface.ITALIC);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInfoDialog();
            }
        });

        RelativeLayout.LayoutParams b6_info = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        info.setLayoutParams(b6_info);

        RelativeLayout info_master = new RelativeLayout(this);

        RelativeLayout.LayoutParams info_master_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        info_master_params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
        info_master_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);

        info_master.setLayoutParams(info_master_params);

        info_master.addView(info);

        gameButtonsView.setLayoutParams(buttonsWrapper);

        //Adding Linear Button to GamButton View
        gameButtonsView.addView(linearLayout_Combiner_RelativeLayout);
        //For the stats and tracer combiner
        gameButtonsView.addView(stats_tracer_combiner);
        //Info Master
        gameButtonsView.addView(info_master);


        joinView.addView(gameView);
        joinView.addView(gameButtonsView);


        getSupportActionBar().hide();
        //setContentView(R.layout.activity_pendulum_);

        // Set window to full screen so no back bar
        Window window = getWindow();

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        setContentView(joinView);

    }


    private Button generateButtons(String buttonText, int textSize, int buttonId, int textColor_rgb, int bgColortransparent) {
        Button newButton = new Button(this);
        newButton.setText(buttonText);
        newButton.setTextSize(textSize);
        newButton.setId(buttonId);
        newButton.setTextColor(textColor_rgb);
        newButton.setBackgroundColor(bgColortransparent);
        newButton.setTransformationMethod(null);

        return newButton;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopThread=true;
        setBoolean(stopThread);

        try{
            Thread.sleep(200); // Be safeside and wait for Game thread to finish
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    private void openInfoDialog() {
        infoDialog.setContentView(R.layout.info_popup_layout);
        infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        infoDialog.show();
    }

}
