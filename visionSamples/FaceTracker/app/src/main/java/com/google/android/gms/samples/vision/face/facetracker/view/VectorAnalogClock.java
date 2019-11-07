package com.google.android.gms.samples.vision.face.facetracker.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.samples.vision.face.facetracker.R;

import java.util.Calendar;


public abstract class VectorAnalogClock extends RelativeLayout {

    private static final String TAG = "VectorAnalogClock";

    private AppCompatImageView analogFace;
    private AppCompatImageView analogHour;
    private AppCompatImageView analogMinute;
    private AppCompatImageView analogSecond;

    @DrawableRes
    private int faceId;
    @DrawableRes
    private int hourId;
    @DrawableRes
    private int minuteId;
    @DrawableRes
    private int secondId;

    private Context ctx;

    private Handler mTickHandler = null;
    private Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            tickTick();
        }
    };

    public VectorAnalogClock(Context ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    public VectorAnalogClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    public VectorAnalogClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.ctx = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VectorAnalogClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.ctx = context;
    }

    /**
     *  A simple initialization with default assets
     */
    public void initializeSimple(){
        mTickHandler = new Handler();

        this.faceId = R.drawable.clock_face;
        this.hourId = R.drawable.hours_hand;
        this.minuteId = R.drawable.minutes_hand;
        this.secondId = R.drawable.second_hand;

        main(ctx);
    }

    /** Intitializes the view. If you want to provide your own vector assets.(You will have a hard time configuring that)
     *
     * @param faceId: the clock face vector svg resource.
     * @param hourId: the hours clock hand vector svg resource.
     * @param minuteId: the minutes clock hand vector svg resource.
     * @param secondId: the seconds clock hand vector svg resource.
     */
    public void initializeCustom(@DrawableRes int faceId, @DrawableRes int hourId, @DrawableRes int minuteId, @DrawableRes int secondId){
        this.faceId = faceId;
        this.hourId = hourId;
        this.minuteId = minuteId;
        this.secondId = secondId;

        main(ctx);
    }

    private boolean showSeconds = true;
    private int color = 0xff000000;
    private float scale = 1.0f;
    private float opacity = 1.0f;
    private Calendar calendar;

    private int dp;
    private int sizeInDp;
    private int sizeInPixels;
    private int diameterInPixels = 0;
    private float diameterInDp = 0;
    private float scaleMultiplier = 1.0f;

    //-------------- Getters --------------\\

    /**
     * @return the calendar that the clock is operating with
     */
    public Calendar getCalendar() {
        if(calendar == null)
            calendar = Calendar.getInstance();

        return calendar;
    }

    /**
     * @return the diameter in pixels set by the user explicitly in setDiameterInPixels()
     */
    public int getDiameterInPixels() {
        return diameterInPixels;
    }

    /**
     * @return the diameter in dp set by the user explicitly in setDiameterInDp()
     */
    public float getDiameterInDp() {
        return diameterInDp;
    }

    /**
     * @return the scale set by setScale()
     */
    public float getScale() {
        return scale;
    }

    /**
     * @return [0, 1.0]
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * @return hexadecimal integer color
     */
    public int getColor() {
        return color;
    }

    /**
     * @return boolean indicating if the clock is currently showing the seconds hand
     */
    public boolean isShowingSeconds() {
        return showSeconds;
    }

    //-------------- Setters --------------\\

    /**
     *  Sets the timing of the clock from the calendar object
     */
    public VectorAnalogClock setCalendar(Calendar calendar) {
        this.calendar = calendar;
        tickTick();

        return this;
    }

    /**
     * Sets the scale of the view.
     */
    public VectorAnalogClock setScale(float scale) {
        this.scale = scale;
        this.setScaleY(scale * scaleMultiplier);
        this.setScaleX(scale * scaleMultiplier);

        return this;
    }

    /**
     * @param diameterInDp: the desired diameter in dp
     */
    public VectorAnalogClock setDiameterInDp(float diameterInDp){
        this.diameterInDp = diameterInDp;
        //scaleMultiplier = newSize / oldSize
        scaleMultiplier = diameterInDp / this.sizeInDp;
        setScale(scale);

        return this;
    }

    /**
     * @param diameterInPixels: the desired diameter in pixels
     */
    public VectorAnalogClock setDiameterInPixels(int diameterInPixels){
        this.diameterInPixels = diameterInPixels;
        //scaleMultiplier = newSize / oldSize
        scaleMultiplier = (diameterInPixels+0.0f) / (this.sizeInPixels+0.0f);
        Log.d("xx",scaleMultiplier+"");
        setScale(scale);

        return this;
    }

    /**
     * @param opacity: ranges from 0 (transparent) to 1.0 (opaque)
     *
     *               Default: 1.0f
     */
    public VectorAnalogClock setOpacity(float opacity) {
        this.opacity = opacity;
        main(ctx);

        return this;
    }

    /**
     * @param color: hexadecimal color (ex: 0xff000000)
     */
    public VectorAnalogClock setColor(int color) {
        this.color = color;
        main(ctx);

        return this;
    }

    /**
     * @param showSeconds: controls whether to show the seconds hand or not.
     */
    public VectorAnalogClock setShowSeconds(boolean showSeconds) {
        this.showSeconds = showSeconds;
        main(ctx);

        return this;
    }

    /**
     *  Black Box
     */
    ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private void main(Context ctx) {

        Drawable face = AppCompatResources.getDrawable(ctx, faceId);
        Drawable hour = AppCompatResources.getDrawable(ctx, hourId);
        Drawable minute = AppCompatResources.getDrawable(ctx, minuteId);
        Drawable second = AppCompatResources.getDrawable(ctx, secondId);

        int alpha255 = (int)(opacity * 255);
        face.setAlpha(alpha255);
        hour.setAlpha(alpha255);
        minute.setAlpha(alpha255);
        second.setAlpha(alpha255);

        face = DrawableCompat.wrap(face);
        hour = DrawableCompat.wrap(hour);
        minute = DrawableCompat.wrap(minute);
        second = DrawableCompat.wrap(second);

//        DrawableCompat.setTint(face.mutate(),color);
//        DrawableCompat.setTint(hour.mutate(),color);
//        DrawableCompat.setTint(minute.mutate(),color);
//        DrawableCompat.setTint(second.mutate(),color);

        inflate(ctx,R.layout.analog_clock,this);

        analogFace = findViewById(R.id.face);
        analogHour = findViewById(R.id.hour);
        analogMinute = findViewById(R.id.minute);
        analogSecond = findViewById(R.id.second);

        if(!showSeconds){
            analogSecond.setVisibility(GONE);
        }

        //square it
        analogFace.setAdjustViewBounds(true);
        analogHour.setAdjustViewBounds(true);
        analogMinute.setAdjustViewBounds(true);
        analogSecond.setAdjustViewBounds(true);

        analogHour.setScaleType(ImageView.ScaleType.FIT_END);
        analogMinute.setScaleType(ImageView.ScaleType.FIT_END);
        analogSecond.setScaleType(ImageView.ScaleType.FIT_END);

        sizeInDp = 40;//why 40 ? cause it works.
        sizeInDp = (sizeInDp + 25) * 4;
        sizeInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,sizeInDp,getResources().getDisplayMetrics());
        ViewGroup.LayoutParams layoutParams = analogFace.getLayoutParams();
        layoutParams.width = sizeInPixels;
        layoutParams.height = sizeInPixels;
        analogFace.setLayoutParams(layoutParams);

        layoutParams = analogSecond.getLayoutParams();
        float minutesHeight = (sizeInPixels/2) - (sizeInPixels/5.5f);
        layoutParams.height = (int)minutesHeight;
        analogSecond.setLayoutParams(layoutParams);

        layoutParams = analogMinute.getLayoutParams();
        layoutParams.height = (int)minutesHeight;
        analogMinute.setLayoutParams(layoutParams);

        layoutParams = analogHour.getLayoutParams();
        layoutParams.height = (sizeInPixels) / 5;
        analogHour.setLayoutParams(layoutParams);

        analogFace.setImageDrawable(face);
        analogHour.setImageDrawable(hour);
        analogMinute.setImageDrawable(minute);
        analogSecond.setImageDrawable(second);

        dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,3.5f,getResources().getDisplayMetrics());
        layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setPositionFor(analogSecond);
                setPositionFor(analogMinute);
                setPositionFor(analogHour);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
                }

            }
        };
        //the coolest line
        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        // Align Time
        Calendar cal = Calendar.getInstance();
        long curTimeStamp = cal.getTimeInMillis();
        cal.add(Calendar.SECOND, 1);
        cal.set(Calendar.MILLISECOND, 0);
        long diffMillis = cal.getTimeInMillis() - curTimeStamp;
        mTickHandler.postDelayed(mTickRunnable, diffMillis);
    }

    private void setPositionFor(View v) {
        v.setTranslationY(- v.getHeight()/2 + dp);
    }

    /**
     *  Positions clock hands correctly(hopefully), and the rest is clockwork
     */
    private void tickTick() {
        Calendar calendar = getCalendar();

        long curMillis = calendar.get(Calendar.MILLISECOND);
        int curSecond = calendar.get(Calendar.SECOND);
        int curMinute = calendar.get(Calendar.MINUTE);
        int curHour = calendar.get(Calendar.HOUR);

        //every 1 second moves 6 degrees every second
        //every 1 minute moves 6 degrees every minute
        //every 1 hour moves by 30 degrees every 1 hour
        //Seconds Degrees
        float degrees = curSecond * 6 + 0.006f * curMillis;
        long duration = 1000 * 60;

        Animation positioner = new RotateAnimation(0,degrees, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        positioner.setFillAfter(true);
        positioner.setDuration(0);

        Animation rotator = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        rotator.setDuration(duration);
        rotator.setRepeatMode(Animation.RESTART);
        rotator.setRepeatCount(Animation.INFINITE);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(positioner);
        animationSet.addAnimation(rotator);
        animationSet.setInterpolator(new LinearInterpolator());

        analogSecond.startAnimation(animationSet);

        //Minutes Degree
        degrees = 6 * curMinute + 0.1f * curSecond+ (1e-5f) * curMillis;
        duration *= 60;

        positioner = new RotateAnimation(0,degrees, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        positioner.setFillAfter(true);
        positioner.setDuration(0);

        rotator = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        rotator.setDuration(duration);
        rotator.setRepeatMode(Animation.RESTART);
        rotator.setRepeatCount(Animation.INFINITE);

        animationSet = new AnimationSet(true);
        animationSet.addAnimation(positioner);
        animationSet.addAnimation(rotator);
        animationSet.setInterpolator(new LinearInterpolator());

        analogMinute.startAnimation(animationSet);

        //every 1 hour moves by 0.00166667 degree every 1 second
        //every 1 hour moves by 0.1 degree every 1 minute

        //Hours Degree
        degrees = 1.66667e-6f * curMillis + 1.66667e-3f * curSecond + 0.5f * curMinute + 30 * curHour;
        duration *= 12;

        positioner = new RotateAnimation(0,degrees, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        positioner.setFillAfter(true);
        positioner.setDuration(0);

        rotator = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        rotator.setDuration(duration);
        rotator.setRepeatMode(Animation.RESTART);
        rotator.setRepeatCount(Animation.INFINITE);

        animationSet = new AnimationSet(true);
        animationSet.addAnimation(positioner);
        animationSet.addAnimation(rotator);
        animationSet.setInterpolator(new LinearInterpolator());

        analogHour.startAnimation(animationSet);

        //move analogSecond by 360 degree every 1 minute
        //move analogMinute by 360 degrees every 60 minute
        //move analogHour by 360 degrees every 3600 minutes
    }
}