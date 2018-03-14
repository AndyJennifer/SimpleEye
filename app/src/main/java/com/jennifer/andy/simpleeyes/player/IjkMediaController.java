package com.jennifer.andy.simpleeyes.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.jennifer.andy.simpleeyes.player.controllerview.ControllerView;
import com.jennifer.andy.simpleeyes.player.controllerview.FullScreenControllerView;
import com.jennifer.andy.simpleeyes.player.controllerview.TinyControllerView;

/**
 * Author:  andy.xwt
 * Date:    2018/1/9 22:29
 * Description:
 */


public class IjkMediaController extends FrameLayout {

    private android.widget.MediaController.MediaPlayerControl mPlayer;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mDecorLayoutParams;
    private Window mWindow;

    private View mDecor;
    private ViewGroup mRoot;

    private boolean mShowing;
    private View mAnchor;
    private final Context mContext;
    private ControllerView mControllerView;

    private static final int sDefaultTimeout = 4000;
    private LayoutParams mTinyParams;


    public IjkMediaController(@NonNull Context context) {
        super(context);
        mContext = context;
        mRoot = this;
        initFloatingWindowLayout();
        initFloatingWindow();
    }


    // TODO: 2018/3/13 xwt 全屏后退出全屏，没显示时间
    // TODO: 2018/3/13 xwt 拖动的时候还是会更新进度条
    // TODO: 2018/3/13 xwt 切换视频的时候,视频进度条还是会闪烁

    /**
     * 初始化悬浮window布局
     */
    private void initFloatingWindowLayout() {
        mDecorLayoutParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.gravity = Gravity.TOP | Gravity.LEFT;
        p.height = LayoutParams.WRAP_CONTENT;
        p.x = 0;
        p.format = PixelFormat.TRANSLUCENT;
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
        p.token = null;
        p.windowAnimations = 0; // android.R.style.DropDownAnimationDown;
    }

    /**
     * 初始化悬浮window
     */
    private void initFloatingWindow() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindow = PolicyCompat.createPhoneWindow(mContext);
        mWindow.setWindowManager(mWindowManager, null, null);
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        mDecor = mWindow.getDecorView();
        mDecor.setOnTouchListener(mTouchListener);
        mWindow.setContentView(this);
        mWindow.setBackgroundDrawableResource(android.R.color.transparent);
        mWindow.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        requestFocus();
    }

    //动态更新根布局的高度与宽度，注意：需要mAnchor != NULL
    private void updateFloatingWindowLayout() {
        int[] anchorPos = new int[2];
        mAnchor.getLocationOnScreen(anchorPos);

        mDecor.measure(MeasureSpec.makeMeasureSpec(mAnchor.getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(mAnchor.getHeight(), MeasureSpec.AT_MOST));

        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.width = mAnchor.getWidth();
        p.x = anchorPos[0] + (mAnchor.getWidth() - p.width) / 2;
        p.y = anchorPos[1] + mAnchor.getHeight() - mDecor.getMeasuredHeight();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null) {
            mControllerView.initControllerListener();
        }
    }

    // 当锚点view布局发生改变的时候会调用
    private final OnLayoutChangeListener mLayoutChangeListener =
            new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight,
                                           int oldBottom) {
                    updateFloatingWindowLayout();
                    if (mShowing) {
                        mWindowManager.updateViewLayout(mDecor, mDecorLayoutParams);
                    }
                }
            };

    /**
     * 触摸监听，如果按下且正在播放，隐藏
     */
    private final OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                }
            }
            return false;
        }
    };


    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
        if (mControllerView != null) {
            mControllerView.togglePause();
        }
    }


    /**
     * 将控制层view与视屏播放view进行关联，并且将视屏播放view添加进控制层view
     */
    public void setAnchorView(View view) {
        if (mAnchor != null) {
            mAnchor.removeOnLayoutChangeListener(mLayoutChangeListener);
        }
        mAnchor = (View) view.getParent();
        if (mAnchor != null) {
            mAnchor.addOnLayoutChangeListener(mLayoutChangeListener);
        }

        ViewGroup.LayoutParams mAnchorLayoutParams = mAnchor.getLayoutParams();
        mTinyParams = new LayoutParams(mAnchorLayoutParams.width, mAnchorLayoutParams.height);
        removeAllViews();
        mControllerView = new TinyControllerView(mPlayer, this, mContext);
        addView(mControllerView.getRootView(), mTinyParams);
    }


    /**
     * 隐藏控制层view
     */
    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            if (mShowing) {
                hide();
            }
        }
    };

    /**
     * 第一次显示，不显示控制层
     */
    public void firstShow() {
        if (!mShowing && mAnchor != null) {
            mControllerView.show();
        }
    }

    /**
     * 显示当前控制层view，默认时间内会自动消失
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * 将控制器显示在屏幕上，当到达过期时间时会自动消失。
     *
     * @param timeout 过期时间(毫秒) 如果设置为0直到调用hide方法才会消失
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            updateFloatingWindowLayout();
            mWindowManager.addView(mDecor, mDecorLayoutParams);
            mShowing = true;
        }
        if (timeout != 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
        mControllerView.show();
    }

    /**
     * 当前Window是否显示
     */
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * 隐藏控制器
     */
    public void hide() {
        if (mAnchor == null)
            return;
        if (mShowing) {
            try {
                mWindowManager.removeView(mDecor);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }

    /**
     * 取消延时隐藏
     */
    public void cancelFadeOut() {
        removeCallbacks(mFadeOut);
    }

    /**
     * 进入全屏
     */
    public void changeControllerView(ControllerView controllerView) {
        mRoot.removeAllViews();
        if (controllerView instanceof TinyControllerView) {
            mRoot.addView(controllerView.getRootView(), mTinyParams);
        } else {
            mRoot.addView(controllerView.getRootView());
        }
        if (mControllerListener != null) {
            if (controllerView instanceof FullScreenControllerView) {
                mControllerListener.onFullScreenClick();
            } else {
                mControllerListener.onTinyScreenClick();
            }
        }
        controllerView.show();
        mControllerView = controllerView;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
                mControllerView.cancelProgressRunnable();
                //如果传入的是activity直接退出
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    public void hideNextButton() {
        mControllerView.hideNextButton();
    }

    /**
     * 控制层监听
     */
    private ControllerListener mControllerListener;

    public ControllerListener getControllerListener() {
        return mControllerListener;
    }

    public void setControllerListener(ControllerListener controllerListener) {
        mControllerListener = controllerListener;
    }


    public interface ControllerListener {

        //退出点击
        void onBackClick();

        //下一页点击
        void onNextClick();

        //全屏点击
        void onFullScreenClick();

        //退出全屏
        void onTinyScreenClick();

    }
}
