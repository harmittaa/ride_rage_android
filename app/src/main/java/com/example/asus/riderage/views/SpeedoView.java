package com.example.asus.riderage.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.asus.riderage.R;

/**
 * Created by Daniel on 23/09/2016.
 */


public class SpeedoView extends View {

    private static final String TAG = "MainActivity";
    private Paint needlePaint; // holds the style and color information about how to draw geometries, text and bitmaps
    private Point viewCenter, needlePoint;
    private int radius;

    public SpeedoView(Context context) {
        super(context);
        Log.e(TAG, "SpeedoView: constructor called" );
        initView();
    }

    public SpeedoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG, "SpeedoView: constructor called" );
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SpeedoView,
                0, 0);
        initView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       /* float width = (float) getWidth();
        float height = (float) getHeight();
        float radius;

        if (width > height) {
            radius = height / 4;
        } else {
            radius = width / 4;
        }
        Path path = new Path();
        path.addCircle(width / 2,
                height / 2, radius,
                Path.Direction.CW);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);

        float center_x, center_y;
        final RectF oval = new RectF();

        paint.setStyle(Paint.Style.STROKE);
        center_x = width / 2;

        center_y = height / 2;

        oval.set(center_x - radius,
                center_y - radius,
                center_x + radius,
                center_y + radius);
        canvas.drawArc(oval, 90, 180, false, paint);
*/
        canvas.drawLine(viewCenter.x, viewCenter.y, needlePoint.x, needlePoint.y, needlePaint);
    }

    public void changeNeedlePosition(int newValue) {
        //this.needlePoint = new Point((int)(this.needlePoint.x*newValue),0);
        /*double radians = Math.toRadians((double)newValue/100*180);
        double suhde = Math.cos(radians);
        if(suhde<0) suhde = -suhde;
        double newHeight = this.getHeight() * suhde;
        if(newHeight < 0) newHeight = -newHeight;

        double newWidth = ((180*((double)newValue/100))/180)*this.getWidth();
        Log.e(TAG, "changeNeedlePosition: new width" + newWidth );
        this.needlePoint = new Point((int)newWidth, (int)newHeight);*/
        double newX = ((double)newValue/100)*this.getWidth();
        double newY = getYforX(newValue,this.getHeight());
        this.needlePoint = new Point((int)newX,(int)newY);
        Log.e(TAG, "changeNeedlePosition: new needle point: " + this.needlePoint.toString());
        invalidate();
    }

    public void initView() {
        Log.e(TAG, "initView: initview Called" );
        this.radius = this.getHeight();
        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG); // reduce the
        needlePaint.setColor(Color.BLACK); //jaggedness of lines in graphics
        needlePaint.setStrokeWidth(20f);
        viewCenter = new Point((this.getWidth() / 2), this.getHeight());//this.getHeight());
        needlePoint = new Point(0,this.getHeight());
        Log.e(TAG, "initView: pints inited:" + viewCenter.toString() + needlePoint.toString() );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.e(TAG, "onSizeChanged: size changed called");
        super.onSizeChanged(w, h, oldw, oldh);
        initView();
    }

    private double getYforX(double x,double radius){
        double radX = Math.toRadians(((x/100)*180));
        //if(lel > 90) lel = x-90;
        Log.e(TAG, "getYforX: x is " + radX );
        //double newx = Math.toRadians(lel);
        /*double y = Math.sqrt(1-(newx*newx));
        Log.e(TAG, "getYforX: result of calc= " + y);*/
        double y = Math.sin(radX)*getWidth()/2;
        Log.e(TAG, "getYforX: y is " + y );
        return -y+getHeight();
    }
}
