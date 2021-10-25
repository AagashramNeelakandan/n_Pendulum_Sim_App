package com.aagashram.n_pendulumsim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class Game extends SurfaceView implements SurfaceHolder.Callback {

    private final PendulumBoy pendulumBoy;
    private GameLoop gameLoop;
    private double transXCurrPos = 0;
    private double transYCurrPos = 0;
    private double moveXCurrPos = 0;
    private double moveYCurrPos = 0;
    private double lastXPos = 0;
    private double lastYPos = 0;
    private double canvasXCurrPos = 0;
    private double canvasYCurrPos = 0;
    private double lastTouchedXPos = 0;
    private double lastTouchedYPos = 0;


    //Button Zoom Features
    private Button zoomInButton;
    private Button zoomOutButton;
    private Button zoomReset;
    private Button statsTog;
    private Button tracerLog;
    private boolean stats_toggle = false;
    private int tracer_log = 0;

    //Scale Values
    private float scale_factor_min = 0.10f;
    private float scale_factor_curr = 1.0f;
    private float scale_factor_max = 10.0f;
    private float scale_factor_step = 0.1f;
    private int screenWidth;
    private int screenHeight;


//TODO
// 1. Drag to Pan - Done
// 2. Pinch to Zoom - (Use either buttons or Pinch zoom feature [use buttons faster to implement])
// 3. Follow the last Bob - Get the Last bob location from PendulumBoy and make that as center of the Screen and have a flag which turn of pan drag in this mode, but has to work.
// 4. Tracer function - check for spline or create a queue in PendulumBoy, which stores last 10 (Change as per view) pos of each pendulum and draws the line between them and removes the first and adds to the last one as in queue. Make the paint variable.
// 5. Set the Main activity for variable selection.
//


    @SuppressLint("ResourceType")
    public Game(Context context, int n, double length, double mass, double gravity, double startAngle, double startVelocity, double damping_percent, int screenWidth, int screenHeight, Button[] zoomControls) {
        super(context);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        //Get Surface Holder and Get callback
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);



        gameLoop = new GameLoop(this,surfaceHolder);

        //player = new Player(getContext(),500,500,50, 1000,1500);
//        pendulumBoy = new PendulumBoy(getContext(),gameLoop,20,1,1, 9.81,70,0, 100.0);
        pendulumBoy = new PendulumBoy(getContext(),gameLoop,n,length, mass,gravity,startAngle,startVelocity, damping_percent,screenWidth, screenHeight);
//        sliderCOR = new SliderValue(getContext(),0.01,1.2);
//        panSurfaceView = new PanSurfaceView(1000,1000,200,100);
        setFocusable(true);


        //Button Setting
//        zoomInButton = (Button) findViewById(10001);
//        zoomOutButton = findViewById(10002);
        zoomInButton = zoomControls[0];
        zoomOutButton = zoomControls[1];
        zoomReset = zoomControls[2];

        statsTog = zoomControls[3];
        tracerLog = zoomControls[4];

        try {
            zoomInButton.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    set_scale_factor(1*scale_factor_step);
                }
            });

            zoomOutButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    set_scale_factor(-1*scale_factor_step);
                }
            });

            zoomReset.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    scale_factor_curr = 1.0f;
                    transXCurrPos = 0;
                    transYCurrPos = 0;
                }
            });

            statsTog.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    stats_toggle = !stats_toggle;
                }
            });

            tracerLog.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tracer_log+=1;
                    if(tracer_log>2){
                        tracer_log = 0;
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void set_scale_factor(float v) {
        scale_factor_curr+=v;
        if(scale_factor_curr<scale_factor_min){
            scale_factor_curr = scale_factor_min;
        }
        else if(scale_factor_curr>scale_factor_max){
            scale_factor_curr = scale_factor_max;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //player.setPosition((double)event.getX(),(double)event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                translateCanvas((double)event.getX(),(double)event.getY());
                //player.setPosition((double)event.getX(),(double)event.getY());
                return true;
//            case MotionEvent.ACTION_UP:
//                setLastPos((double)event.getX(),(double)event.getY());
//                return true;
        }
        return super.onTouchEvent(event);
    }

    private void setLastPos(double x, double y) {
//        lastXPos = x;
//        lastYPos = y;
//        canvasXCurrPos = x;
//        canvasYCurrPos = y;
        lastTouchedXPos = x;
        lastTouchedYPos = y;
//        transXCurrPos = x;
//        transYCurrPos = y;

//
    }

    private void translateCanvas(double x, double y) {
        double limitMovement = 25.0;
        moveXCurrPos = x;
        moveYCurrPos = y;
        if(Math.abs(moveXCurrPos-lastXPos)*(1/scale_factor_curr)<(limitMovement)*(1/scale_factor_curr) || Math.abs(moveYCurrPos-lastYPos)*(1/scale_factor_curr)<(limitMovement)*(1/scale_factor_curr)){
            transXCurrPos += (moveXCurrPos-lastXPos)*(1/scale_factor_curr);
            transYCurrPos += (moveYCurrPos-lastYPos)*(1/scale_factor_curr);
        }
//        transXCurrPos += moveXCurrPos-lastXPos;
//        transYCurrPos += moveYCurrPos-lastYPos;
        lastXPos = x;
        lastYPos = y;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Pendulum_Activity obj = new Pendulum_Activity();
        boolean stop = obj.getBoolean();

        if(stop){
            gameLoop.isRunning = false;
        }

        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void draw(Canvas canvas) {
//        canvas.drawRGB(255,250,235);



        canvasXCurrPos=transXCurrPos;
        canvasYCurrPos=transYCurrPos;

        //Scaling the Factor
//        canvas.scale(scale_factor_curr,scale_factor_curr,(float)lastTouchedXPos,(float)lastTouchedYPos);//,150,0);
        canvas.scale(scale_factor_curr,scale_factor_curr,(float)(screenWidth/2.0f),(float)(screenWidth/2.0f));//,150,0);

//        canvas.translate((float)canvasXCurrPos-canvas.getWidth()/2,(float)canvasYCurrPos-canvas.getHeight()/2);
        canvas.translate((float)canvasXCurrPos,(float)canvasYCurrPos);

        super.draw(canvas);
        if(stats_toggle){
            //drawUPS(canvas);
            drawFPS(canvas);
        }

//        panSurfaceView.draw(canvas);
        //player.draw(canvas);
        //sliderCOR.draw(canvas);
        //System.out.println(tracer_log);
        pendulumBoy.draw(canvas,stats_toggle,tracer_log);
//        transYCurrPos = 0;
//        transXCurrPos = 0;
//        canvasXCurrPos = 0;
//        canvasYCurrPos = 0;
//        System.out.println("transX&Y: "+transXCurrPos+" "+transYCurrPos+" moveX&Y: "+moveXCurrPos+" "+moveYCurrPos+" LastX&Y: "+lastXPos+" "+lastYPos);
    }

    public void drawUPS(Canvas canvas){
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(getContext(), R.color.magenta);
        paint.setColor(color);
        paint.setTextSize(50);
        canvas.drawText("UPS: "+averageUPS,100,100,paint);
    }

    public void drawFPS(Canvas canvas){
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(getContext(), R.color.magenta);
        paint.setColor(color);
        paint.setTextSize(50);
        canvas.drawText("FPS: "+averageFPS,100,100,paint);
    }

    public void update() {
        // Game Loop update will be done
//        panSurfaceView.update();
        //player.update();
        //player.ballBounce();
//        sliderCOR.update();
        pendulumBoy.update();
    }
}
