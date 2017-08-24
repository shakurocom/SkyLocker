package com.shakuro.skylocker.view;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;

public class DragContainer extends ViewGroup {
	private View target;
	private DragEventListener dragEventListener;

	public DragContainer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragContainer(Context context) {
		super(context, null);
	}

	public DragContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setDragEventListener(DragEventListener dragEventListener) {
		this.dragEventListener = dragEventListener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int i = getChildCount();
		if (i != 1) {
			throw new IllegalStateException("Only Support One Child");
		}
		measureChild(getChildAt(0), widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		View child = getChildAt(0);
		child.layout(l, t, r, b);
	}

	public boolean startDragChild(View child, ClipData data,
                                  Object myLocalState, int flags) {
		setDragTarget(child);
		return child.startDrag(data, new EmptyDragShadowBuilder(child),
				myLocalState, flags);
	}

	private void setDragTarget(View v) {
		target = v;
		onSetDragTarget(v);
	}

	/**
	 * this is similar to the constructor of DragShadowBuilder
	 *
	 * @param v
	 */
	protected void onSetDragTarget(View v) {

	}

	public View getDragTarget() {
		return target;
	}

	private float mDragX;
	private float mDragY;
	protected boolean mOnDrag;

	@Override
	protected void dispatchDraw(Canvas canvas) {

		super.dispatchDraw(canvas);
		if (mOnDrag && target != null) {
			canvas.save();
			drawDragShadow(canvas);
			canvas.restore();
		}
	}

	protected void drawDragShadow(Canvas canvas) {
		int h = target.getHeight();
		int w = target.getWidth();
		canvas.translate(mDragX - w / 2, mDragY - h / 2);
		target.draw(canvas);
	}

	protected float getDragX() {
		return mDragX;
	}

	protected float getDragY() {
		return mDragY;
	}

	@Override
	public boolean dispatchDragEvent(DragEvent event) {
		Log.v("DragContainer", event.toString());
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			int[] loc = new int[2];
			getLocationOnScreen(loc);
			mDragX = event.getX() - loc[0];
			mDragY = event.getY() - loc[1];
			mOnDrag = true;
			break;
		case DragEvent.ACTION_DROP:
		case DragEvent.ACTION_DRAG_ENDED:
			mOnDrag = false;
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			loc = new int[2];
			getLocationOnScreen(loc);
			mDragX = event.getX() - loc[0];
			mDragY = event.getY() - loc[1];
			break;
		default:
			mDragX = event.getX();
			mDragY = event.getY();
			break;
		}
		invalidate();

		if (dragEventListener != null) {
			dragEventListener.onDispatchDragEvent(event);
		}

		return super.dispatchDragEvent(event);
	}

	private static class EmptyDragShadowBuilder extends View.DragShadowBuilder {

		public EmptyDragShadowBuilder(View arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			super.onProvideShadowMetrics(size, touch);

		}

		@Override
		public void onDrawShadow(Canvas canvas) {
			// draw nothing
		}
	}

	public interface DragEventListener {
		void onDispatchDragEvent(DragEvent event);
	}
}
