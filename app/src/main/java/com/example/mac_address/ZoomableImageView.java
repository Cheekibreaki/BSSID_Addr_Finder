package com.example.mac_address;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


public class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Matrix matrix;
    private ScaleGestureDetector scaleGestureDetector;
    private PointF lastTouchPoint; // For tracking the last touch point during drag
    private float[] matrixValues; // To store matrix values during transformations

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        lastTouchPoint = new PointF();
        matrixValues = new float[9];
        // Set initial zoom level (e.g., 0.5 for 50% size)
        float initialZoom = 0.1f;
        matrix.setScale(initialZoom, initialZoom);
        setImageMatrix(matrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastTouchPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scaleGestureDetector.isInProgress()) {
                    float dx = event.getX() - lastTouchPoint.x;
                    float dy = event.getY() - lastTouchPoint.y;
                    matrix.postTranslate(dx, dy);
                    setImageMatrix(matrix);
                    lastTouchPoint.set(event.getX(), event.getY());
                }
                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            matrix.getValues(matrixValues);
            float currentScale = matrixValues[Matrix.MSCALE_X];

            // Adjust these values as needed for minimum and maximum zoom levels
            float minScale = 0.1f; // Minimum zoom level (e.g., 30% of original size)
            float maxScale = 5f;   // Maximum zoom level

            // Check if scaling is within bounds
            if ((scaleFactor < 1f && currentScale > minScale) || (scaleFactor > 1f && currentScale < maxScale)) {
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(matrix);
            }
            return true;
        }
    }
}