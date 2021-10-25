package com.example.n_pendulumsim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Tracer {

    private Context context;
    private int n;
    private int tracerLimit;
    private Paint paintTracer;

    private singleTracer[] tracersQueue;
    //Creating Field Variables for the Tracer



    public Tracer(Context context, int n, int tracerLimit, Paint paintTracer) {
        this.context = context;
        this.n = n;
        this.tracerLimit = tracerLimit;
        this.paintTracer = paintTracer;


        tracersQueue = new singleTracer[n];
        for (int index=0;index<n;index++) {
            tracersQueue[index] = new singleTracer(tracerLimit,paintTracer); //(new Paint()).setColor(ContextCompat.getColor(context,R.color.yellow)));
        }
    }

    //Will draw the tracer in the screen
    public void draw(Canvas canvas) {
        for (singleTracer alpha: tracersQueue) {
            alpha.drawOneTracer(canvas);
        }
    }

    //Will draw the tracer in the screen
    public void drawRainbow(Canvas canvas) {
        float[] hsvColor = new float[]{0, 1, 1};
        Paint paintRainbow = new Paint();
        paintRainbow.setStrokeWidth(4);
        float colorIncrementStep = (float)360.0/n;
        for (singleTracer alpha: tracersQueue) {
            paintRainbow.setColor(Color.HSVToColor(hsvColor));
            alpha.drawOneTracer(canvas, paintRainbow);
            hsvColor[0]+=colorIncrementStep;
        }
    }


    //Will enqueue the tracer with new values and dequeue the old values
    public void updateTracer(double[] posX, double[] posY) {
        int index = 0;
        for (singleTracer alpha: tracersQueue) {
            alpha.updateOneTracer(posX[index],posY[index]);
            index++;
        }
    }
}
