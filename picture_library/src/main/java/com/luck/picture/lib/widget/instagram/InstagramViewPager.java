package com.luck.picture.lib.widget.instagram;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * ================================================
 * Created by JessYan on 2020/3/26 15:37
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class InstagramViewPager extends FrameLayout {
    private int startedTrackingX;
    private int startedTrackingY;
    private float scrollHorizontalPosition;
    private VelocityTracker velocityTracker;
    private android.view.animation.Interpolator Interpolator = new LinearInterpolator();
    private int interceptY;
    private int interceptX;
    private List<Page> mItems = new ArrayList<>();
    private List<View> mViews = new ArrayList<>();
    private int mCurrentPosition;
    private TabLayout mTabLayout;
    boolean click;
    int startClickX;
    int startClickY;
    long time;
    private AnimatorSet mAnimatorSet;

    public InstagramViewPager(@NonNull Context context) {
        super(context);
    }

    public InstagramViewPager(@NonNull Context context, List<Page> items) {
        super(context);
        if (items != null && !items.isEmpty()) {
            mItems.addAll(items);
            installView(items);
            mItems.get(0).init(0, this);
            mViews.get(0).setTag(true);
            mItems.get(0).refreshData(context);
            mTabLayout = new TabLayout(context, items);
            addView(mTabLayout);
        }
    }

    public void installView(List<Page> items) {
        for (Page item : items) {
            if (item != null) {
                View view = item.getView(getContext());
                if (view != null) {
                    addView(view);
                    mViews.add(view);
                } else {
                    throw new IllegalStateException("getView(Context) is null!");
                }
            }
        }
    }

    public void addPage(Page page) {
        if (page != null) {
            mItems.add(page);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int tabLayoutHeight = height;
        if (mTabLayout != null && mTabLayout.getVisibility() == VISIBLE) {
            measureChild(mTabLayout, widthMeasureSpec, heightMeasureSpec);
            tabLayoutHeight -= mTabLayout.getMeasuredHeight();
        }
        for (View view : mViews) {
            view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(tabLayoutHeight, MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        int viewTop = 0;
        int viewLeft;

        for (int i = 0; i < mViews.size(); i++) {
            viewLeft = i * getMeasuredWidth();
            View view = mViews.get(i);
            view.layout(viewLeft, viewTop, viewLeft + view.getMeasuredWidth(), viewTop + view.getMeasuredHeight());
        }

        if (mTabLayout != null && mTabLayout.getVisibility() == VISIBLE) {
            viewLeft = 0;
            viewTop = getMeasuredHeight() - mTabLayout.getMeasuredHeight();
            mTabLayout.layout(viewLeft, viewTop, viewLeft + mTabLayout.getMeasuredWidth(), viewTop + mTabLayout.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            interceptX = (int) ev.getX();
            interceptY = (int) ev.getY();
            return false;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (mViews.size() < 2) {
                return false;
            }

            Rect rect = mItems.get(mCurrentPosition).disallowInterceptTouchRect();
            if (rect != null && rect.contains((int) (ev.getX()), (int) (ev.getY()))) {
                return false;
            }

            float dx = (int) ev.getX() - interceptX;
            float dy = (int) ev.getY() - interceptY;
            if (Math.abs(dx) > 10 && Math.abs(dy) < 10) {
                startedTrackingX = (int) ev.getX();
                startedTrackingY = (int) ev.getY();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startedTrackingX = (int) event.getX();
            startedTrackingY = (int) event.getY();
            if (velocityTracker != null) {
                velocityTracker.clear();
            }

            click = true;
            startClickX = (int) event.getX();
            startClickY = (int) event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            float dx = (int) (event.getX() - startedTrackingX);
            float dy = (int) event.getY() - startedTrackingY;

            moveByX(dx * 1.1f);

            startedTrackingX = (int) event.getX();
            startedTrackingY = (int) event.getY();

            if (click && (Math.abs(event.getX() - startClickX) > 5 || Math.abs(event.getY() - startClickY) > 5)) {
                click = false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.computeCurrentVelocity(1000);

            float velX = velocityTracker.getXVelocity();

            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }

            int triggerValue = getMeasuredWidth() / 2;
            int position = (int) (Math.abs(scrollHorizontalPosition) / getMeasuredWidth());
            if (Math.abs(scrollHorizontalPosition) % getMeasuredWidth() >= triggerValue) {
                position++;
            }

            int destination = getDestination(position);

            if (Math.abs(velX) >= 500) {
                if (velX <= 0) {
                    startChildAnimation(getDestination(mCurrentPosition), 150);
                } else {
                    startChildAnimation(getDestination(mCurrentPosition - 1), 150);
                }
            } else {
                startChildAnimation(destination, 200);
            }

            if (click) {
                Rect rect = new Rect();
                mTabLayout.getHitRect(rect);
                if (rect.contains((int) (event.getX()), (int) (event.getY()))) {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    if (elapsedRealtime - time > 300) {
                        time = elapsedRealtime;
                        click = false;
                        if (mTabLayout.getTabSize() > 1) {
                            int tabWidth = getMeasuredWidth() / mTabLayout.getTabSize();
                            selectPagePosition((int) (event.getX() / tabWidth));
                        }
                    }
                }
            }
        }
        return true;
    }

    public void selectPagePosition(int position) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
        long duration = 150;
        int span = Math.abs(mCurrentPosition - position);
        if (span > 1) {
            duration += (span - 1) * 80;
        }
        startChildAnimation(getDestination(position), duration);
    }

    private int getDestination(int position) {
        if (position < 0) {
            position = 0;
        }
        return -(position * getMeasuredWidth());
    }

    private void startChildAnimation(float destination, long duration) {
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(
                ObjectAnimator.ofFloat(this, "scrollHorizontalPosition", scrollHorizontalPosition, destination));
        mAnimatorSet.setInterpolator(Interpolator);
        mAnimatorSet.setDuration(duration);
        mAnimatorSet.start();
    }

    public void moveByX(float dx) {
        setScrollHorizontalPosition(scrollHorizontalPosition + dx);
    }


    public void setScrollHorizontalPosition(float value) {
        if (mViews.size() < 2) {
            return;
        }
        float oldHorizontalPosition = scrollHorizontalPosition;
        if (value < -(getMeasuredWidth() * (mViews.size() - 1))) {
            scrollHorizontalPosition = -(getMeasuredWidth() * (mViews.size() - 1));
        } else if (value > 0) {
            scrollHorizontalPosition = 0;
        } else {
            scrollHorizontalPosition = value;
        }

        if (oldHorizontalPosition == scrollHorizontalPosition) {
            return;
        }

        for (View view : mViews) {
            view.setTranslationX(scrollHorizontalPosition);
        }

        int position = (int) (Math.abs(scrollHorizontalPosition) / getMeasuredWidth());
        float offset = Math.abs(scrollHorizontalPosition) % getMeasuredWidth();

        if (mTabLayout != null) {
            mTabLayout.setIndicatorPosition(position, offset / getMeasuredWidth());
            if (offset == 0) {
                mTabLayout.selectTab(position);
                mItems.get(position).refreshData(getContext());
            }
        }

        if (offset > 0) {
            position++;
        }

        mCurrentPosition = position;
        View currentView = mViews.get(position);
        Object tag = currentView.getTag();
        boolean isInti = false;
        if (tag instanceof Boolean) {
            isInti = (boolean) tag;
        }
        if (!isInti) {
            mItems.get(position).init(position, this);
            currentView.setTag(true);
        }
    }
}
