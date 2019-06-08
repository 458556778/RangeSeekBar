package com.jaygoo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2018/5/10
 * 描    述:
 * ================================================
 */
public class VerticalRangeSeekBar extends RangeSeekBar {

    //text direction of VerticalRangeSeekBar. include indicator and tickMark
    /** @hide */
    @IntDef({TEXT_DIRECTION_VERTICAL, TEXT_DIRECTION_HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextDirectionDef {}
    public final static int TEXT_DIRECTION_VERTICAL = 1;
    public final static int TEXT_DIRECTION_HORIZONTAL = 2;

    //direction of VerticalRangeSeekBar
    /** @hide */
    @IntDef({DIRECTION_LEFT, DIRECTION_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DirectionDef {}
    public final static int DIRECTION_LEFT = 1;
    public final static int DIRECTION_RIGHT = 2;

    private int orientation = DIRECTION_LEFT;
    private int tickMarkDirection = TEXT_DIRECTION_VERTICAL;

    private int maxTickMarkWidth;

    public VerticalRangeSeekBar(Context context) {
        this(context, null);
    }

    public VerticalRangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initSeekBar(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        try {
            TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalRangeSeekBar);
            orientation = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_orientation, DIRECTION_LEFT);
            tickMarkDirection = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_tick_mark_orientation, TEXT_DIRECTION_VERTICAL);
            t.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void initSeekBar(AttributeSet attrs) {
        leftSB = new VerticalSeekBar(this, attrs, true);
        rightSB = new VerticalSeekBar(this, attrs, false);
        rightSB.setVisible(seekBarMode != SEEKBAR_MODE_SINGLE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        /*
         * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
         * MeasureSpec.EXACTLY 是精确尺寸
         * MeasureSpec.AT_MOST 是最大尺寸
         * MeasureSpec.UNSPECIFIED 是未指定尺寸
         */

        if (widthMode == MeasureSpec.EXACTLY) {
            widthSize = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        } else if (widthMode == MeasureSpec.AT_MOST && getParent() instanceof ViewGroup
                && widthSize == ViewGroup.LayoutParams.MATCH_PARENT) {
            widthSize = MeasureSpec.makeMeasureSpec(((ViewGroup) getParent()).getMeasuredHeight(), MeasureSpec.AT_MOST);
        } else {
            int heightNeeded;
            if (gravity == Gravity.CENTER) {
                heightNeeded = 2 * getProgressTop() + progressHeight;
            } else {
                heightNeeded = (int) getRawHeight();
            }
            widthSize = MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthSize, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (orientation == DIRECTION_LEFT) {
            canvas.rotate(-90);
            canvas.translate(-getHeight(), 0);
        } else {
            canvas.rotate(90);
            canvas.translate(0, -getWidth());
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onDrawTickMark(Canvas canvas) {
        if (tickMarkTextArray != null) {
            int trickPartWidth = progressWidth / (tickMarkTextArray.length - 1);
            for (int i = 0; i < tickMarkTextArray.length; i++) {
                final String text2Draw = tickMarkTextArray[i].toString();
                if (TextUtils.isEmpty(text2Draw)) continue;
                paint.getTextBounds(text2Draw, 0, text2Draw.length(), tickMarkTextRect);
                paint.setColor(tickMarkTextColor);
                //平分显示
                float x;
                if (tickMarkMode == TRICK_MARK_MODE_OTHER) {
                    if (tickMarkGravity == TRICK_MARK_GRAVITY_RIGHT) {
                        x = getProgressLeft() + i * trickPartWidth - tickMarkTextRect.width();
                    } else if (tickMarkGravity == TRICK_MARK_GRAVITY_CENTER) {
                        x = getProgressLeft() + i * trickPartWidth - tickMarkTextRect.width() / 2f;
                    } else {
                        x = getProgressLeft() + i * trickPartWidth;
                    }
                } else {
                    float num = Utils.parseFloat(text2Draw);
                    SeekBarState[] states = getRangeSeekBarState();
                    if (Utils.compareFloat(num, states[0].value) != -1 && Utils.compareFloat(num, states[1].value) != 1 && (seekBarMode == SEEKBAR_MODE_RANGE)) {
                        paint.setColor(tickMarkInRangeTextColor);
                    }
                    //按实际比例显示
                    x = getProgressLeft() + progressWidth * (num - minProgress) / (maxProgress - minProgress)
                            - tickMarkTextRect.width() / 2f;
                }
                float y;
                if (tickMarkLayoutGravity == Gravity.TOP) {
                    y = getProgressTop() - tickMarkTextMargin;
                } else {
                    y = getProgressBottom() + tickMarkTextMargin + tickMarkTextRect.height();
                }
                int degrees = 0;
                float rotateX = (x + tickMarkTextRect.width() / 2f);
                float rotateY = (y - tickMarkTextRect.height() / 2f);
                if (tickMarkDirection == TEXT_DIRECTION_VERTICAL) {
                    if (orientation == DIRECTION_LEFT) {
                        degrees = 90;
                    } else if (orientation == DIRECTION_RIGHT) {
                        degrees = -90;
                    }
                }
                if (degrees != 0) {
                    canvas.rotate(degrees, rotateX, rotateY);
                }
                canvas.drawText(text2Draw, x, y, paint);
                if (degrees != 0) {
                    canvas.rotate(-degrees, rotateX, rotateY);
                }
            }
        }

    }


    @Override
    protected int getTickMarkRawHeight() {
        if (maxTickMarkWidth > 0)return tickMarkTextMargin+ maxTickMarkWidth;
        if (tickMarkTextArray != null && tickMarkTextArray.length > 0) {
            maxTickMarkWidth = Utils.measureText(String.valueOf(tickMarkTextArray[0]), tickMarkTextSize).width();
            for (int i = 1; i < tickMarkTextArray.length; i++){
                int width = Utils.measureText(String.valueOf(tickMarkTextArray[i]), tickMarkTextSize).width();
                if (maxTickMarkWidth < width){
                    maxTickMarkWidth = width;
                }
            }
            return tickMarkTextMargin+ maxTickMarkWidth;
        }
        return 0;
    }

    @Override
    public void setTickMarkTextSize(int tickMarkTextSize) {
        super.setTickMarkTextSize(tickMarkTextSize);
        maxTickMarkWidth = 0;
    }

    @Override
    public void setTickMarkTextArray(CharSequence[] tickMarkTextArray) {
        super.setTickMarkTextArray(tickMarkTextArray);
        maxTickMarkWidth = 0;
    }

    @Override
    protected float getEventX(MotionEvent event) {
        if (orientation == DIRECTION_LEFT) {
            return getHeight() - event.getY();
        } else {
            return event.getY();
        }
    }

    @Override
    protected float getEventY(MotionEvent event) {
        if (orientation == DIRECTION_LEFT) {
            return event.getX();
        } else {
            return -event.getX() + getWidth();
        }
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(@DirectionDef int orientation) {
        this.orientation = orientation;
    }

    public int getTickMarkDirection() {
        return tickMarkDirection;
    }

    public void setTickMarkDirection(@TextDirectionDef int tickMarkDirection) {
        this.tickMarkDirection = tickMarkDirection;
    }
}
