package com.example.mac_address;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
public class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Matrix matrix;
    private ScaleGestureDetector scaleGestureDetector;
    private PointF lastTouchPoint; // For tracking the last touch point during drag
    private float[] matrixValues; // To store matrix values during transformations

    private GestureDetector gestureDetector;

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

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                handleLongPress(e);
            }
        });

        longPressPoint = new PointF();
        paint = new Paint();
        paint.setColor(Color.RED); // Set the color of the dot
        paint.setStyle(Paint.Style.FILL);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
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


    private void handleLongPress(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        // Convert touch coordinates into image coordinates
        PointF imagePoint = transformTouchCoordinates(touchX, touchY);

        // Create a message with the coordinates
        String message = String.format("Image coordinates: (%.2f, %.2f)",
                imagePoint.x, imagePoint.y);

        // Show the message in a Toast
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

        longPressPoint.set(imagePoint.x, imagePoint.y);
        drawDot = true;

        // Redraw the view to show the dot
        invalidate();
    }

    private PointF transformTouchCoordinates(float touchX, float touchY) {
        Matrix inverse = new Matrix();
        matrix.invert(inverse);

        // Transform touch coordinates to match the image matrix
        float[] touchPoint = new float[]{touchX, touchY};
        inverse.mapPoints(touchPoint);

        Drawable drawable = getDrawable();
        if (drawable == null) {
            return new PointF(0, 0);
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        // Calculate the scale factors
        float scaleX = getWidth() / (float) drawableWidth;
        float scaleY = getHeight() / (float) drawableHeight;

        // Adjust scale factors based on the ImageView's scaling logic (e.g., centerCrop, fitXY, etc.)
        // For simplicity, assuming fitCenter or similar. Adjust as necessary for your specific use case.
        float actualScale = Math.min(scaleX, scaleY);

        // Adjust the coordinates to get the relative position on the original image
        float adjustedX = (touchPoint[0] / actualScale);
        float adjustedY = (touchPoint[1] / actualScale);

        // Clamp values to the image's bounds
        adjustedX = Math.max(0, Math.min(adjustedX, drawableWidth));
        adjustedY = Math.max(0, Math.min(adjustedY, drawableHeight));

        return new PointF(adjustedX, adjustedY);
    }

    private PointF longPressPoint; // For storing the long press location
    private boolean drawDot = false; // Flag to indicate whether to draw the dot
    private Paint paint; // Paint object to draw the dot





    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Only draw the dot if the flag is set and the image is present
        if (drawDot && getDrawable() != null) {
            float[] mappedPoints = new float[2];
            // Map the long press point to the current image matrix
            matrix.mapPoints(mappedPoints, new float[]{longPressPoint.x, longPressPoint.y});
            float dotRadius = 10; // Radius of the dot
            canvas.drawCircle(mappedPoints[0], mappedPoints[1], dotRadius, paint);
        }
    }

    // Add a method to clear the dot when needed
    public void clearDot() {
        drawDot = false;
        invalidate();
    }



}