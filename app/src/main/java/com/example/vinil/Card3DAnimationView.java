package com.example.vinil;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

/**
 * A custom view that applies 3D hover animation effects to a CardView
 */
public class Card3DAnimationView extends FrameLayout {

    private static final float MAX_ROTATION_ANGLE = 10.0f;
    private static final int ANIMATION_DURATION = 200;

    private CardView targetCard;
    private ImageView lightEffectView;
    private float centerX;
    private float centerY;
    private Camera camera;
    private Matrix matrix;
    private AnimatorSet currentAnimator;
    private float rotationX = 0f;
    private float rotationY = 0f;
    private boolean isAnimating = false;
    private float lightEffectAlpha = 0.0f;

    public Card3DAnimationView(@NonNull Context context) {
        super(context);
        init();
    }

    public Card3DAnimationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Card3DAnimationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        camera = new Camera();
        matrix = new Matrix();
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        // Create light effect overlay
        lightEffectView = new ImageView(getContext());
        lightEffectView.setImageResource(R.drawable.card_light_effect);
        lightEffectView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        lightEffectView.setAlpha(0.0f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Find the CardView child
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof CardView) {
                targetCard = (CardView) child;
                break;
            }
        }

        if (targetCard == null && getChildCount() > 0) {
            // If no CardView found, use the first child
            targetCard = (CardView) getChildAt(0);
        }
        
        // Add light effect overlay on top of the card
        if (targetCard != null) {
            addView(lightEffectView);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (targetCard == null) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Calculate rotation based on touch position relative to center
                float touchX = event.getX();
                float touchY = event.getY();

                // Calculate the rotation angles based on touch position
                // Invert Y axis for natural tilt feeling
                float newRotationY = (touchX - centerX) / centerX * MAX_ROTATION_ANGLE;
                float newRotationX = (centerY - touchY) / centerY * MAX_ROTATION_ANGLE;

                animateToRotation(newRotationX, newRotationY);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Reset to original position
                animateToRotation(0f, 0f);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void animateToRotation(float newRotationX, float newRotationY) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        ObjectAnimator rotateXAnimator = ObjectAnimator.ofFloat(this, "rotationX", rotationX, newRotationX);
        ObjectAnimator rotateYAnimator = ObjectAnimator.ofFloat(this, "rotationY", rotationY, newRotationY);
        
        // Calculate light effect alpha based on rotation magnitude
        float targetAlpha = (Math.abs(newRotationX) + Math.abs(newRotationY)) / (MAX_ROTATION_ANGLE * 2) * 0.7f;
        ObjectAnimator lightAlphaAnimator = ObjectAnimator.ofFloat(lightEffectView, "alpha", lightEffectView.getAlpha(), targetAlpha);
        
        // Add update listener to track rotation values
        rotateXAnimator.addUpdateListener(animation -> {
            rotationX = (float) animation.getAnimatedValue();
            updateLightEffect();
        });
        
        rotateYAnimator.addUpdateListener(animation -> {
            rotationY = (float) animation.getAnimatedValue();
            updateLightEffect();
        });
        
        lightAlphaAnimator.addUpdateListener(animation -> {
            lightEffectAlpha = (float) animation.getAnimatedValue();
        });
        
        // Configure and start the animator set
        currentAnimator = new AnimatorSet();
        currentAnimator.playTogether(rotateXAnimator, rotateYAnimator, lightAlphaAnimator);
        currentAnimator.setDuration(ANIMATION_DURATION);
        currentAnimator.setInterpolator(new DecelerateInterpolator());
        
        currentAnimator.start();
    }

    private void updateLightEffect() {
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (targetCard == null) {
            super.dispatchDraw(canvas);
            return;
        }

        // Save the canvas state
        canvas.save();

        // Apply 3D rotation transformation
        camera.save();
        camera.rotateX(rotationX);
        camera.rotateY(rotationY);
        camera.getMatrix(matrix);
        camera.restore();

        // Apply perspective transformation
        float[] values = new float[9];
        matrix.getValues(values);
        values[6] = values[6] / 1000f; // Adjust perspective
        values[7] = values[7] / 1000f; // Adjust perspective
        matrix.setValues(values);

        // Center the rotation point
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);

        // Apply the transformation to the canvas
        canvas.concat(matrix);

        // Draw the children with the applied transformation
        super.dispatchDraw(canvas);

        // Restore the canvas to its original state
        canvas.restore();
    }
}