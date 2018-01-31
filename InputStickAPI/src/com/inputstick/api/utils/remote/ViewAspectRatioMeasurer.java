package com.inputstick.api.utils.remote;

import android.view.View.MeasureSpec;

public class ViewAspectRatioMeasurer {

	private Integer measuredWidth = null;

	public ViewAspectRatioMeasurer() {
	}

	public void measure(int widthMeasureSpec, int heightMeasureSpec, float aspectRatio) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec.getSize(heightMeasureSpec);

		if (heightMode == MeasureSpec.EXACTLY && widthMode == MeasureSpec.EXACTLY) {
			// Both width and height fixed
			measuredWidth = widthSize;
			measuredHeight = heightSize;

		} else if (heightMode == MeasureSpec.EXACTLY) {
			// Width dynamic, height fixed
			measuredWidth = (int) Math.min(widthSize, heightSize * aspectRatio);
			measuredHeight = (int) (measuredWidth / aspectRatio);

		} else if (widthMode == MeasureSpec.EXACTLY) {
			// Width fixed, height dynamic
			measuredHeight = (int) Math.min(heightSize, widthSize / aspectRatio);
			measuredWidth = (int) (measuredHeight * aspectRatio);

		} else {
			// Both width and height dynamic
			if (widthSize > heightSize * aspectRatio) {
				measuredHeight = heightSize;
				measuredWidth = (int) (measuredHeight * aspectRatio);
			} else {
				measuredWidth = widthSize;
				measuredHeight = (int) (measuredWidth / aspectRatio);
			}

		}
	}

	// Get the width measured in the latest call to <tt>measure()</tt>.
	public int getMeasuredWidth() {
		if (measuredWidth == null) {
			throw new IllegalStateException("You need to run measure() before trying to get measured dimensions");
		}
		return measuredWidth;
	}

	private Integer measuredHeight = null;

	// Get the height measured in the latest call to <tt>measure()</tt>.
	public int getMeasuredHeight() {
		if (measuredHeight == null) {
			throw new IllegalStateException("You need to run measure() before trying to get measured dimensions");
		}
		return measuredHeight;
	}

}