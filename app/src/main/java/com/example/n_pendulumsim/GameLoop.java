package com.example.n_pendulumsim;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameLoop extends Thread{
    public static final double MAX_UPS = 60.0;
    private static final double UPS_PERIOD = 1E-3/MAX_UPS;
    public volatile boolean isRunning = false;
    private Game game;
    private SurfaceHolder surfaceHolder;
    private double averageUPS;
    private double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
    }

    public void startLoop() {
        isRunning = true;
        start();
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    @Override
    public void run() {
        super.run();

        //Declaring Variables for Time and cycle count
        int updateCount = 0;
        int frameCount = 0;

        //Times in Millie Second
        long startTime;
        long elapsedTime;
        long sleepTime;

        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        while(isRunning){
            try{
                canvas = surfaceHolder.lockCanvas();
                if(canvas!=null){
                    synchronized (surfaceHolder){
//                        canvas.drawARGB(0,255,250,235);
                        game.update();
                        updateCount++;
                        game.draw(canvas);
                    }
                }


            }catch(IllegalArgumentException e){
                e.printStackTrace();
            }finally {
                if(canvas!=null){
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //Now we'll count the update and Frame Count and check if the time has elasped 1 secound , then we will divide total updates anddivide by time to get FPS and UPS




            // Pause the game loop to not exceed target UPS
            elapsedTime =  System.currentTimeMillis()-startTime;
            sleepTime = (long) (updateCount*UPS_PERIOD - elapsedTime);
            if(sleepTime>0){
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Skip frames to keep up with target UPS
            while(sleepTime<0 && updateCount<MAX_UPS-1){
                game.update();
                updateCount++;
                elapsedTime =  System.currentTimeMillis()-startTime;
                sleepTime = (long) (updateCount*UPS_PERIOD - elapsedTime);

            }

            elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime>=1000){
                averageUPS = updateCount/(1E-3*elapsedTime); //To convert ElapsedTime in MilliSec to Sec
                averageFPS = frameCount/(1E-3*elapsedTime); //To convert ElapsedTime in MilliSec to Sec
            }
        }
    }
}
