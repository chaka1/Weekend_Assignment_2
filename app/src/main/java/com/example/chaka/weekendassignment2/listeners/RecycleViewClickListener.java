package com.example.chaka.weekendassignment2.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Chaka on 29/05/2015.
 */

    public class RecycleViewClickListener implements RecyclerView.OnItemTouchListener {

        private OnItemClickListener mOnClickListener;
        GestureDetector mGestureDetector;

        public interface OnItemClickListener {
            public void onItemClick(View view, int position);
        }

        public RecycleViewClickListener(Context context, OnItemClickListener listener) {
            mOnClickListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mOnClickListener != null && mGestureDetector.onTouchEvent(e)) {
                mOnClickListener.onItemClick(childView, view.getChildPosition(childView));
                return true;
            }
            return false;
        }

        @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

}

