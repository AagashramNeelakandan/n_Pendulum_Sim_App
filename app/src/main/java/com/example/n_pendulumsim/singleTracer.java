package com.example.n_pendulumsim;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayDeque;
import java.util.Deque;

public class singleTracer {

    private Deque<Double> oneTracerX;
    private Deque<Double> oneTracerY;
    private int tracerLimit;
    private Paint oneTracerPaint;

    public singleTracer() {
    }

    public singleTracer(int tracerLimit, Paint oneTracerPaint) {
        this.tracerLimit = tracerLimit;
        oneTracerX = new ArrayDeque<Double>(tracerLimit);
        oneTracerY = new ArrayDeque<Double>(tracerLimit);

        this.oneTracerPaint = oneTracerPaint;
    }

    public Paint getOneTracerPaint() {
        return oneTracerPaint;
    }

    public void setOneTracerPaint(Paint oneTracerPaint) {
        this.oneTracerPaint = oneTracerPaint;
    }

    public Double[] getOneTracerX() {
        return oneTracerX.toArray(new Double[0]);
    }
    public Double[] getOneTracerY() {
        return oneTracerY.toArray(new Double[0]);
    }


    public void updateOneTracer(double posX,double posY){
        if (oneTracerX.size() >= tracerLimit) {
            //Removing the last Element
            oneTracerX.removeFirst();
            oneTracerY.removeFirst();

            //Adding to Last
        }
        oneTracerX.addLast(posX);
        oneTracerY.addLast(posY);
    }


    public void drawOneTracer(Canvas canvas){
        Double[] posXArray = getOneTracerX();
        Double[] posYArray = getOneTracerY();
        for(int index=0;index<(oneTracerX.size()-1);index++){
            canvas.drawLine((posXArray[index]).floatValue(),(posYArray[index]).floatValue(),(posXArray[index+1]).floatValue(),(posYArray[index+1]).floatValue(),getOneTracerPaint());
        }
    }

    public void drawOneTracer(Canvas canvas, Paint rainbowPaint){
        Double[] posXArray = getOneTracerX();
        Double[] posYArray = getOneTracerY();

        for(int index=0;index<(oneTracerX.size()-1);index++){
            canvas.drawLine((posXArray[index]).floatValue(),(posYArray[index]).floatValue(),(posXArray[index+1]).floatValue(),(posYArray[index+1]).floatValue(),rainbowPaint);
        }
    }
}
