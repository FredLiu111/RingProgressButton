package ring.fredliu.com.library; /**
 * Copyright (C) 2016 fantianwen <twfan_09@hotmail.com>
 * <p>
 * also you can see {@link https://github.com/fantianwen/CircleProgressButton}
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;



/**
 * 圆形进度按钮
 */
public class CircleProgressButton extends View {


    public String Tag = getClass().getSimpleName();
    /**
     * 进度正在增加
     */
    private final static int PROGRESS_PLUS = 0;

    /**
     * 进度正在减少
     */
    private final static int PROGRESS_REDUCE = 1;

    /**
     * 圆的半径减少
     */
    private final static int RADIUS_PLUS = 2;

    /**
     * 圆的半径增加
     */
    private final static int RADIUS_REDUCE = 3;

    private Context mContext;

    private static final long TIME_INTERVAL = 1;
    private Paint mPaint;
    private Paint mProgressPaint;
    private Paint mProgressBack;
    private Paint mRingPaint;

    private int mWidth, mHeight;
    private float sweepAngle;

    int colorSweep[] = {getResources().getColor(R.color.blue_20), getResources().getColor(R.color.map_search_blue), getResources().getColor(R.color.blue_20)};
    float position[] = {0.4f, 0.75f, 1.0f};

    /**
     * 进度条的宽度
     */
    private float mProgressWidth;

    private float mBouncedWidth;

    private float mAnimatedWidth;

    /**
     * 结束标志位
     */
    private boolean isEnd;

    /**
     * 手放开之后，判断有没有回到progress为0的情况
     */
    private boolean isEndOk;

    /**
     * 按下动画结束标志
     */
    private boolean isPressedOk;

    /**
     * 按下松开动画结束标志
     */
    private boolean ifPressedBackOk;


    /**
     * 圆弧渐变的角度增加
     */
    private int everyIntervalAngle = 5;

    /**
     * 监听进度情况
     */
    private CircleProcessListener mCircleProcessListener;

    private int mSize;

    private int mRadius;
    private BounceY mBounceY;


    public CircleProgressButton(Context context) {
        this(context, null);
    }

    public CircleProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        mSize = (width > height ? height : width);
        mRadius = (mSize / 2) - 15;

        setMeasuredDimension(mSize + 50, mSize + 50);
    }

    private Handler mLongPressedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PROGRESS_PLUS:
                    isEnd = sweepAngle == 260;
                    if (isEnd) {
                        if (mCircleProcessListener != null) {
                            mCircleProcessListener.onFinished();
//                            TODO: 2018/9/7 进度复位
                            sweepAngle = 0;
                            invalidate();
                        }
                        removeMessages(PROGRESS_PLUS);
                    } else {
                        sweepAngle += everyIntervalAngle;
                        Log.d(Tag, "handleMessage: " + sweepAngle);
                        mCircleProcessListener.onStarting();
                        invalidate();
                        sendEmptyMessageDelayed(PROGRESS_PLUS, TIME_INTERVAL);
                    }
                    break;
                case PROGRESS_REDUCE:
                    isEndOk = sweepAngle == 0;
                    if (!isEndOk) {
                        sweepAngle -= everyIntervalAngle;
                        invalidate();
                        sendEmptyMessageDelayed(PROGRESS_REDUCE, TIME_INTERVAL);
                    } else {
                        if (mCircleProcessListener != null) {
                            mCircleProcessListener.onCancelOk();
                        }
                        removeMessages(PROGRESS_REDUCE);
                    }

                    break;
                case RADIUS_PLUS:
                    isPressedOk = mBouncedWidth - mAnimatedWidth <= 0;
                    if (!isPressedOk) {
                        mAnimatedWidth += 0.5;
                        invalidate();
                        sendEmptyMessageDelayed(RADIUS_PLUS, 1);
                    } else {
                        removeMessages(RADIUS_PLUS);
                    }

                    break;
                case RADIUS_REDUCE:
                    ifPressedBackOk = mAnimatedWidth <= 0;

                    if (!ifPressedBackOk) {
                        mAnimatedWidth -= 0.5;
                        invalidate();
                        sendEmptyMessageDelayed(RADIUS_REDUCE, 1);
                    } else {
                        removeMessages(RADIUS_REDUCE);
                    }

                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                // 按下的动画
                if (isEnd) {
                    sweepAngle = 0;
                }

                if (!ifPressedBackOk) {
                    mLongPressedHandler.sendEmptyMessage(RADIUS_REDUCE);
                }
                mLongPressedHandler.sendEmptyMessage(RADIUS_PLUS);

                if (!isEndOk) {
                    if (mCircleProcessListener != null) {
                        mCircleProcessListener.onReStart();
                    }
                    mLongPressedHandler.removeMessages(PROGRESS_REDUCE);
                }

                mLongPressedHandler.sendEmptyMessage(PROGRESS_PLUS);

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (!isPressedOk) {
                    mLongPressedHandler.sendEmptyMessage(RADIUS_PLUS);
                }
                mLongPressedHandler.sendEmptyMessage(RADIUS_REDUCE);

                if (!isEnd) {
                    if (mCircleProcessListener != null) {
                        mCircleProcessListener.onCancel();
                    }
                    mLongPressedHandler.sendEmptyMessage(PROGRESS_REDUCE);
                }

                mLongPressedHandler.removeMessages(PROGRESS_PLUS);

                break;
        }

        return true;

    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressBack = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBouncedWidth = mProgressWidth / 2;

        mBounceY = new BounceY(mBouncedWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.rotate(-220);

        //画进度条
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(16);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStrokeJoin(Paint.Join.BEVEL);
        mProgressPaint.setPathEffect(new DashPathEffect(new float[]{5f, 6f}, 0));
        final RectF rectF = new RectF(-mRadius + mBouncedWidth, -mRadius + mBouncedWidth, mRadius - mBouncedWidth, mRadius - mBouncedWidth);
        //画渐变颜色
        SweepGradient mProgressShader = new SweepGradient(mRadius, mRadius, colorSweep, position);
        mProgressPaint.setShader(mProgressShader);
        canvas.drawArc(rectF, sweepAngle - 30, sweepAngle, false, mProgressPaint);


        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.color_circle));
        canvas.drawCircle(0, 0, mRadius - mAnimatedWidth-10, mPaint);
//
//
        //画外环
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setColor(getResources().getColor(R.color.color_ring));
        mRingPaint.setStrokeWidth(12);
        canvas.drawCircle(0, 0, mRadius - mAnimatedWidth + 15, mRingPaint);

    }

    public void setCircleProcessListener(CircleProcessListener circleProcessListener) {
        this.mCircleProcessListener = circleProcessListener;
    }

    public interface CircleProcessListener {

        void onFinished();

        void onCancel();

        /**
         * 取消后进度转到0
         */
        void onCancelOk();

        void onReStart();

        void onStarting();
    }


}
