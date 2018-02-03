package com.inputstick.api.utils.remote;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class MousePadView extends View {
	
	protected int mWidth, mHeight;
	protected RectF mRect;
	protected Paint mPaint;
	
	protected ViewAspectRatioMeasurer mVarm = new ViewAspectRatioMeasurer();
	protected float mRatio; //aspect ratio to be respected by the measurer    
	 
    public MousePadView(Context context) {
        super(context);
        if( !isInEditMode()) {
        	mRatio = 0;
        }
    }
    
    public MousePadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if( !isInEditMode()) {
        	mRatio = 0;
        }
    }
    
    public MousePadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if( !isInEditMode()) {
        	mRatio = 0;
        }
    }
    
    public boolean refreshRatio(float newRatio) {
    	if (mRatio != newRatio) {
    		mRatio = newRatio;
    		return true;
    	} else {
    		return false;
    	}
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	if (mRatio > 0) {
    		mVarm.measure(widthMeasureSpec, heightMeasureSpec, mRatio);
        	setMeasuredDimension(mVarm.getMeasuredWidth(), mVarm.getMeasuredHeight() );
    	} else {
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	}    	
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
		
        mRect = new RectF(new Rect(0, 0, mWidth, mHeight));        
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);        
        mPaint.setStrokeWidth(w);
    }
    
    
    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);	
		if (isEnabled()) {
			mPaint.setColor(0xFF0080FF);
		} else {
			mPaint.setColor(Color.LTGRAY);
		}
		canvas.drawRect(mRect, mPaint);
		//canvas.drawRoundRect(mRect, 10, 10, mPaint);
	}
     
}