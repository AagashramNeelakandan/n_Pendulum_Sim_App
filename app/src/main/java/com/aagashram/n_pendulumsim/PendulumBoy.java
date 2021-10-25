package com.aagashram.n_pendulumsim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import java.util.Arrays;

public class PendulumBoy {

    private final Paint paintBob;
    private final Paint paintLine;
    private final Paint paintRect;
    private final Paint paintText;
    private final Paint paintTracer;
    private int n;
    private double m;
    private double l;
    private double g;
    private double sA;
    private double sV;
    private double damping_percent;
    private double[] posX;
    private double[] posY;
    private double[] vel_S;
    private double[] acc_S;
    private double[] theta_S;
    private double[] cumMasses; //Cumulative Masses
    private double [][] massMatrix;
    private double [][] ForceMatrix;

    private double massScale = 10;
    private double lengthScaleX = 2.1250e2;
    private double lengthScaleY = -2.1250e2;
    double transX_Val = 500;
    double transY_Val = 500;
    double time = 0;
    double deltaTime = 1/(GameLoop.MAX_UPS);
    private GameLoop gameLoop;

    //Screen Size in pixels(So int)
    private int screenWidth=0;
    private int screenHeight=0;

    //Tracers
    private int tracerLimit = 40;
    private int tracerUpdateLimit = 1; //Limit to update frames To reduce the memory
    private int currTracerLimit = 0; //Limit to update frames To reduce the memory

    //Creating Tracer Class
    private Tracer tracers;




    public PendulumBoy(Context context, GameLoop gameLoop, int n, double length, double mass, double gravity, double startAngle, double startVelocity, double damping_percent, int screenWidth, int screenHeight) {
        this.n = n;
        this.m = mass;
        this.l = length;
        this.g = gravity;
        this.sA = startAngle;
        this.sV = startVelocity;
        this.damping_percent = damping_percent;
        this.gameLoop = gameLoop;

        //Declaring Paint
        paintBob = new Paint();
        paintLine = new Paint();
        paintRect = new Paint();
        paintText = new Paint();
        paintTracer = new Paint();


        int colorBob = ContextCompat.getColor(context, R.color.orangish_white_2000);
        int colorLine = ContextCompat.getColor(context, R.color.orangish_white_2000);
        int colorRect = ContextCompat.getColor(context, R.color.light_black_2000);
        int colorTracer = ContextCompat.getColor(context, R.color.orangish_white_2000);

        //Initializing Paint
        paintBob.setColor(colorBob);
        paintLine.setColor(colorLine);
        paintLine.setStrokeWidth(5);
        paintRect.setColor(colorRect);
        paintRect.setStrokeWidth(5);
        paintText.setColor(Color.GREEN);
        paintText.setTextSize(50);
        paintTracer.setColor(colorTracer);
        paintTracer.setStrokeWidth(4);


        //Declaring Arrays
        posX = new double[n];
        posY = new double[n];
        vel_S = new double[n];
        acc_S = new double[n];
        theta_S = new double[n];
        cumMasses = new double[n];
        massMatrix = new double[n][n];
        ForceMatrix = new double[n][1];

        //Initializing Arrays
        calCumSum(); //Calculating CumSum
        Arrays.fill(theta_S,Math.toRadians(sA));
        Arrays.fill(vel_S,sV);

        //Tracer
        tracers = new Tracer(context,n,tracerLimit,paintTracer);

        //Getting Screen Size
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;

        //Setting Offset to the Values
        this.transX_Val = this.screenWidth/2.0;
        this.transY_Val = this.screenWidth/2.0;

    }



    public void draw(Canvas canvas, boolean stats_toggle, int tracer_log) {
        canvas.drawRect(screenWidth*(2.0f/5),screenWidth*(2.0f/5),screenWidth*(3.0f/5),(screenWidth/2.0f),paintRect);
        scaleAndTranslatePos();

        //Drawing Tracers
        //tracers.updateTracer(posX,posY);
        if(currTracerLimit>tracerUpdateLimit){
            tracers.updateTracer(posX,posY);
            currTracerLimit = 0;
        }
        else{
            currTracerLimit++;
        }
        //Tracer Mode -0 No Tracer 1-black tracer 2. Rainbow Tracer
        if(tracer_log==0){
            //tracers.draw(canvas); No Tracer
        }
        else if(tracer_log==1){
            tracers.draw(canvas);
        }
        else if(tracer_log==2){
            tracers.drawRainbow(canvas);
        }


        //Replacing the above commented code with Line below drawing one
        canvas.drawLine((float)transX_Val,(float)transY_Val,(float)(posX[0]),(float)(posY[0]),paintLine);
        for(int i=1;i<n;i++){
            canvas.drawLine((float)(posX[i-1]),(float)(posY[i-1]),(float)(posX[i]),(float)(posY[i]),paintLine);
        }
        //canvas.drawCircle((float)(posX[0]),(float)(posY[0]),(float)(m*massScale),paintBob);
        for(int i=0;i<n;i++){
            canvas.drawCircle((float)(posX[i]),(float)(posY[i]),(float)(m*massScale),paintBob);
        }
        if(stats_toggle){
            canvas.drawText("Time: "+time,100,200,paintText);
            canvas.drawText("Delta Time: "+deltaTime,100,300,paintText);
        }

        //calPendulumPos(time,deltaTime); //Normal 4th order RK
        calPendulumPos_forRKF(time,deltaTime); // Adaptive RKF
        time+=deltaTime;




    }

    private void scaleAndTranslatePos(){

        double lengthScale = 100;
        double offset = screenWidth/2.0;
        for(int i=0;i<n;i++){
            if(i==0){
                posX[i] = l*lengthScale*Math.sin(theta_S[i])+offset;
                posY[i] = l*lengthScale*Math.cos(theta_S[i])+offset;
            }
            else{
                posX[i] =  posX[i-1] + l*lengthScale*Math.sin(theta_S[i]);
                posY[i] =  posY[i-1] + l*lengthScale*Math.cos(theta_S[i]);
            }
        }

    }

    public void update() {

    }


    private void calCumSum(){
        for(int i=0;i<n;i++){
            cumMasses[i] = n-i;
        }
    }


    private DMatrixRMaj calcMassMatrix(DMatrixRMaj y){
        for(int i=n;i<2*n;i++){
            theta_S[i-n] = y.get(i,0);
        }
        double temp_const_part = 0;
        double temp_cosine_part = 0;
        for(int i=1;i<=n;i++){
            for(int j=1;j<=n;j++){
                temp_const_part = cumMasses[Math.max(i,j)-1]*l;
                if(i==j){
                    temp_cosine_part = 1;
                }
                else{
                    temp_cosine_part = Math.cos(theta_S[i-1]-theta_S[j-1]);
                }

                massMatrix[i-1][j-1] = temp_const_part*temp_cosine_part;
            }
        }

        return new DMatrixRMaj(massMatrix);
    }


    private DMatrixRMaj velFMatrixCalc(){
        double [][] velFMatrix  = new double[n][n];
        double temp_const_part = 0;
        double temp_sine_part = 0;
        int negChanger = -1;
        for(int i=1;i<=n;i++){
            for(int j=1;j<=n;j++){
                temp_const_part = cumMasses[Math.max(i,j)-1]*l;
                if(i==j){
                    temp_sine_part = 0;
                }
                else{
                    temp_sine_part = negChanger*Math.sin(theta_S[i-1]-theta_S[j-1]);
                }

                velFMatrix[i-1][j-1] = temp_const_part*temp_sine_part;

            }
        }
        DMatrixRMaj velMat = new DMatrixRMaj(velFMatrix);
        return velMat;
    }


    private DMatrixRMaj calcFMatrix(DMatrixRMaj y){
        double[] temp_VelMat = new double[n];
        for(int i=0;i<1*n;i++){
            vel_S[i] = y.get(i,0);
            temp_VelMat[i] = (vel_S[i]*vel_S[i]);
        }
        for(int i=n;i<2*n;i++){
            theta_S[i-n] = y.get(i,0);
        }

        DMatrixRMaj velMat = velFMatrixCalc();

        DMatrixRMaj velSMat = new DMatrixRMaj(temp_VelMat);
        DMatrixRMaj multi_res1 = new DMatrixRMaj(velMat);

        CommonOps_DDRM.mult(velMat,velSMat,multi_res1);
        double [] gMat = new double[n];
        for(int i=0;i<n;i++){
            gMat[i] = (-1)*cumMasses[i]*g*Math.sin(theta_S[i]);
        }
        DMatrixRMaj FMat = new DMatrixRMaj(gMat);
        CommonOps_DDRM.add(multi_res1,new DMatrixRMaj(gMat),FMat);

        return FMat;
    }

    private DMatrixRMaj findAccel(DMatrixRMaj M, DMatrixRMaj F){
        DMatrixRMaj accMat = new DMatrixRMaj(acc_S);
        CommonOps_DDRM.invert(M);
        CommonOps_DDRM.mult(M,F,accMat);
        return accMat;
    }

    private DMatrixRMaj GMatrix(DMatrixRMaj y, double t){
        DMatrixRMaj acceleration_Matrix = findAccel(calcMassMatrix(y),calcFMatrix(y));
        DMatrixRMaj G = new DMatrixRMaj(new double[2*n]);
        for(int i=0;i<1*n;i++){
            vel_S[i] = y.get(i,0);
        }
        DMatrixRMaj velSMat = new DMatrixRMaj(vel_S);
        CommonOps_DDRM.concatRows(acceleration_Matrix,velSMat,G);


        return G;

    }

    private DMatrixRMaj RK_Method(DMatrixRMaj y,double t,double dt){

        //K1
        DMatrixRMaj k1 = GMatrix(y,t);
        //K2
        DMatrixRMaj temp1_forK = new DMatrixRMaj(k1);
        DMatrixRMaj temp2_forK = new DMatrixRMaj(k1);
        CommonOps_DDRM.scale(0.5*dt,k1,temp1_forK);
        CommonOps_DDRM.add(y,temp1_forK,temp2_forK);
        DMatrixRMaj k2 = GMatrix(temp2_forK,t+0.5*dt);
        //K3
        CommonOps_DDRM.scale(0.5*dt,k2,temp1_forK);
        CommonOps_DDRM.add(y,temp1_forK,temp2_forK);
        DMatrixRMaj k3 = GMatrix(temp2_forK,t+0.5*dt);
        //K4
        CommonOps_DDRM.scale(1*dt,k3,temp1_forK);
        CommonOps_DDRM.add(y,temp1_forK,temp2_forK);
        DMatrixRMaj k4 = GMatrix(temp2_forK,t+1*dt);

        //Add Terms
        CommonOps_DDRM.scale(2,k2);
        CommonOps_DDRM.scale(2,k3);
        CommonOps_DDRM.add(k1,k2,temp1_forK);
        CommonOps_DDRM.add(temp1_forK,k3,temp2_forK);
        CommonOps_DDRM.add(temp2_forK,k4,temp1_forK);
        //Multiply by Step Size
        CommonOps_DDRM.scale(dt,temp1_forK);
        CommonOps_DDRM.divide(temp1_forK,6);

        return temp1_forK;



    }

    private void calPendulumPos(double t,double dt){
        DMatrixRMaj y = new DMatrixRMaj(new double[2*n]);
        for(int i=0;i<1*n;i++){
            y.set(i,0,vel_S[i]);
        }
        for(int i=n;i<2*n;i++){
            y.set(i,0,theta_S[i-n]);
        }
        DMatrixRMaj temp1 = new DMatrixRMaj(new double[2*n]);
        CommonOps_DDRM.add(y,RK_Method(y,t,dt),temp1);

        for(int i=0;i<1*n;i++){
            vel_S[i] = temp1.get(i,0);
            vel_S[i] *= damping_percent/100; //Damping Factor
        }
        for(int i=n;i<2*n;i++){
            theta_S[i-n] = temp1.get(i,0);
        }
    }


    private void calPendulumPos_forRKF(double t,double dt){
        DMatrixRMaj y = new DMatrixRMaj(new double[2*n]);
        for(int i=0;i<1*n;i++){
            y.set(i,0,vel_S[i]);
        }
        for(int i=n;i<2*n;i++){
            y.set(i,0,theta_S[i-n]);
        }

        DMatrixRMaj temp1 = new DMatrixRMaj(RKF_Method(y,t,dt));

        for(int i=0;i<1*n;i++){
            vel_S[i] = temp1.get(i,0);
            vel_S[i] *= damping_percent/100.0; //Damping Factor
        }
        for(int i=n;i<2*n;i++){
            theta_S[i-n] = temp1.get(i,0);
        }

    }

    //Adaptive RKF Method
    private DMatrixRMaj RKF_Method(DMatrixRMaj y,double t,double dt) {
        double tolerance = 1e-4/5; //Epsilon
        double h = dt;
        double tou = 0; //Needed Time Step

        double safety_Factor = 0.9;
        double increase_Factor = 1.0;                              ;

        double hMin = 1e-6;
        //double hMax = 1/(GameLoop.MAX_UPS*(GameLoop.MAX_UPS/gameLoop.getAverageFPS()*1e3));
        double hMax = 1/(GameLoop.MAX_UPS*(GameLoop.MAX_UPS/gameLoop.getAverageFPS()));

        //Condition checking for Current time step
        if (h < hMin) {
            h = hMin;
        } else if (h > hMax) {
            h = hMax;
        }

        //K1
        DMatrixRMaj k1 = GMatrix(y, t);
        CommonOps_DDRM.scale(h, k1);


        //Temp Matrices for Storage
        DMatrixRMaj temp1_forK; //= new DMatrixRMaj(k1);
        DMatrixRMaj temp2_forK; //= new DMatrixRMaj(k1);
        DMatrixRMaj temp3_forK; //= new DMatrixRMaj(k1);
        DMatrixRMaj temp4_forK; //= new DMatrixRMaj(k1);
        DMatrixRMaj temp5_forK; //= new DMatrixRMaj(k1);
        DMatrixRMaj temp6_forK; //= new DMatrixRMaj(k1);

        //K2
        temp1_forK = new DMatrixRMaj(k1);

        CommonOps_DDRM.scale(0.25, temp1_forK);

        DMatrixRMaj k2 = GMatrix(CommonOps_DDRM.add(y, temp1_forK, null), t + 0.25 * h);
        CommonOps_DDRM.scale(h, k2);

        //K3
        temp1_forK = new DMatrixRMaj(k1);
        temp2_forK = new DMatrixRMaj(k2);

        //Scale_Variables in for k3
        double[] k3Coeff = {(3.0/32),(9.0/32)};

        CommonOps_DDRM.scale(k3Coeff[0], temp1_forK);
        CommonOps_DDRM.scale(k3Coeff[1], temp2_forK);

        DMatrixRMaj k3 = GMatrix(CommonOps_DDRM.add(CommonOps_DDRM.add(y, temp1_forK, null), temp2_forK, null), t + (3 / 8) * h);
        CommonOps_DDRM.scale(h, k3);

        //K4
        temp1_forK = new DMatrixRMaj(k1);
        temp2_forK = new DMatrixRMaj(k2);
        temp3_forK = new DMatrixRMaj(k3);

        //Scale_Variables in for k4
        double[] k4Coeff = {(1932.0/2197),(-7200.0/2197),(7296.0/2197)};

        CommonOps_DDRM.scale(k4Coeff[0], temp1_forK);
        CommonOps_DDRM.scale(k4Coeff[1], temp2_forK);
        CommonOps_DDRM.scale(k4Coeff[2], temp3_forK);

        DMatrixRMaj k4 = GMatrix(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(y, temp1_forK, null), temp2_forK, null), temp3_forK, null), t + (12 / 13) * h);
        CommonOps_DDRM.scale(h, k4);

        //K5
        temp1_forK = new DMatrixRMaj(k1);
        temp2_forK = new DMatrixRMaj(k2);
        temp3_forK = new DMatrixRMaj(k3);
        temp4_forK = new DMatrixRMaj(k4);

        //Scale_Variables in for k5
        double[] k5Coeff = {(439.0/216),(-8.0),(3680.0/513),(-845.0/4104)};

        CommonOps_DDRM.scale(k5Coeff[0], temp1_forK);
        CommonOps_DDRM.scale(k5Coeff[1], temp2_forK);
        CommonOps_DDRM.scale(k5Coeff[2], temp3_forK);
        CommonOps_DDRM.scale(k5Coeff[3], temp4_forK);

        DMatrixRMaj k5 = GMatrix(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(y, temp1_forK, null), temp2_forK, null), temp3_forK, null), temp4_forK, null), t + h);
        CommonOps_DDRM.scale(h, k5);

        //K6
        temp1_forK = new DMatrixRMaj(k1);
        temp2_forK = new DMatrixRMaj(k2);
        temp3_forK = new DMatrixRMaj(k3);
        temp4_forK = new DMatrixRMaj(k4);
        temp5_forK = new DMatrixRMaj(k5);

        //Scale_Variables in for k6
        double[] k6Coeff = {(-8.0/27),(2.0),(-3544.0/2565),(1859.0/4104),(-11.0/40)};

        CommonOps_DDRM.scale(k6Coeff[0], temp1_forK);
        CommonOps_DDRM.scale(k6Coeff[1], temp2_forK);
        CommonOps_DDRM.scale(k6Coeff[2], temp3_forK);
        CommonOps_DDRM.scale(k6Coeff[3], temp4_forK);
        CommonOps_DDRM.scale(k6Coeff[4], temp5_forK);

        DMatrixRMaj k6 = GMatrix(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(y, temp1_forK, null), temp2_forK, null), temp3_forK, null), temp4_forK, null), temp5_forK, null), t + (0.5) * h);
        CommonOps_DDRM.scale(h, k6);

        //4th Order Approximations
        temp1_forK = new DMatrixRMaj(k1);
        temp3_forK = new DMatrixRMaj(k3);
        temp4_forK = new DMatrixRMaj(k4);
        temp5_forK = new DMatrixRMaj(k5);

        //Scale_Variables in for Fourth Order
        double[] fourthOrderCoeff = {(25.0/216),(1408.0/2565),(2197.0/4104),(-1.0/5)};

        CommonOps_DDRM.scale(fourthOrderCoeff[0], temp1_forK);
        CommonOps_DDRM.scale(fourthOrderCoeff[1], temp3_forK);
        CommonOps_DDRM.scale(fourthOrderCoeff[2], temp4_forK);
        CommonOps_DDRM.scale(fourthOrderCoeff[3], temp5_forK);

        DMatrixRMaj fourthOrderApprox = new DMatrixRMaj(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(y, temp1_forK, null), temp3_forK, null), temp4_forK, null), temp5_forK, null));

        //5th Order Approximations
        temp1_forK = new DMatrixRMaj(k1);
        temp3_forK = new DMatrixRMaj(k3);
        temp4_forK = new DMatrixRMaj(k4);
        temp5_forK = new DMatrixRMaj(k5);
        temp6_forK = new DMatrixRMaj(k6);

        //Scale_Variables in for Fifth Order
        double[] fifthOrderCoeff = {(16.0/135),(6656.0/12825),(28561.0/56430),(-9.0/50),(2.0/55)};

        CommonOps_DDRM.scale(fifthOrderCoeff[0], temp1_forK);
        CommonOps_DDRM.scale(fifthOrderCoeff[1], temp3_forK);
        CommonOps_DDRM.scale(fifthOrderCoeff[2], temp4_forK);
        CommonOps_DDRM.scale(fifthOrderCoeff[3], temp5_forK);
        CommonOps_DDRM.scale(fifthOrderCoeff[4], temp6_forK);

        DMatrixRMaj fifthOrderApprox = new DMatrixRMaj(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(CommonOps_DDRM.add(y, temp1_forK, null), temp3_forK, null), temp4_forK, null), temp5_forK, null), temp6_forK, null));


        //Error and Step Size Calc
        DMatrixRMaj errorMat = new DMatrixRMaj(fourthOrderApprox);
        CommonOps_DDRM.subtract(fourthOrderApprox, fifthOrderApprox, errorMat);
        double error = CommonOps_DDRM.elementSumAbs(errorMat) / (2.0*n); //Average Error

        tou = h * Math.pow((tolerance/error), (1.0/5.0)); //StepSize we want

        //Condition For Recursion
        if (error > tolerance) {
            return RKF_Method(y, t, tou * safety_Factor);
        }
        else{
            if(dt<deltaTime){
                deltaTime = dt;
            }
            else{
                deltaTime = dt*increase_Factor;
            }

            return fifthOrderApprox;
        }
    }
}
