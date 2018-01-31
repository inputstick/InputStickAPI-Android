package com.inputstick.api.utils.remote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MouseScrollView extends View {
	
	private int mWidth, mHeight;
	private Paint mPaint;
	private int x1,x2;
		 
    public MouseScrollView(Context context) {
        super(context);
    }
    
    public MouseScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public MouseScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);   	
    }
    
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        mWidth = xNew;
        mHeight = yNew;
        
		int w = mHeight / 100;
		if (mWidth > mHeight) {
			w = mWidth / 100;
		}
		if (w < 1) {
			w = 1;
		}  
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);        
        
        x1 = mWidth/4;
        x2 = 3*x1;
    }
    
    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isEnabled()) {
			mPaint.setColor(0xFF0080FF);
		} else {
			mPaint.setColor(Color.LTGRAY);
		}
		
		int y = 0;
		while(true) {
			canvas.drawRect(0, y, mWidth, y+5, mPaint);
			y += 10;
			if (y > mHeight) {
				break;
			}
			
			canvas.drawRect(x1, y, x2, y+5, mPaint);
			y += 10;
			if (y > mHeight) {
				break;
			}
		}		
	}	

}
