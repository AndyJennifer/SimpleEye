package com.jennifer.andy.simpleeyes.ui.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.StringRes
import com.jennifer.andy.simpleeyes.R
import com.jennifer.andy.simpleeyes.manager.ActivityManager
import com.jennifer.andy.simpleeyes.utils.showKeyboard
import com.jennifer.andy.simpleeyes.widget.font.CustomFontTextView
import com.jennifer.andy.simpleeyes.widget.font.FontType
import com.jennifer.andy.simpleeyes.widget.state.MultipleStateView
import me.yokeyword.fragmentation.SupportActivity


/**
 * Author:  andy.xwt
 * Date:    2017/8/10 22:37
 * Description:
 */

abstract class BaseAppCompatActivity : SupportActivity() {

    /**
     * 上下文对象
     */
    protected lateinit var mContext: Context
    protected lateinit var mMultipleStateView: MultipleStateView


    /**
     * 跳转到其他Activity启动或者退出的模式
     */
    enum class TransitionMode {
        LEFT, RIGHT, TOP, BOTTOM, SCALE, FADE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initView(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        ActivityManager.getInstance().addActivity(this)
    }

    private fun initData() {
        overrideTransitionAnimation()
        val extras = intent.extras
        if (extras != null) {
            getBundleExtras(extras)
        }
        mContext = this
        //添加相应的布局
        if (getContentViewLayoutId() != 0) {
            mMultipleStateView = MultipleStateView(this)
            val view = View.inflate(this, getContentViewLayoutId(), mMultipleStateView)
            setContentView(view)
        } else {
            throw  IllegalArgumentException("You must return layout id")
        }

    }

    /**
     * 设置activity进入动画
     */
    private fun overrideTransitionAnimation() {
        if (toggleOverridePendingTransition()) {
            when (getOverridePendingTransition()) {
                TransitionMode.TOP -> overridePendingTransition(R.anim.top_in, R.anim.no_anim)
                TransitionMode.BOTTOM -> overridePendingTransition(R.anim.bottom_in, R.anim.no_anim)
                TransitionMode.LEFT -> overridePendingTransition(R.anim.left_in, R.anim.no_anim)
                TransitionMode.RIGHT -> overridePendingTransition(R.anim.right_in, R.anim.no_anim)
                TransitionMode.FADE -> overridePendingTransition(R.anim.fade_in, R.anim.no_anim)
                TransitionMode.SCALE -> overridePendingTransition(R.anim.scale_in, R.anim.no_anim)
            }
        }
    }

    override fun finish() {
        super.finish()
        if (toggleOverridePendingTransition()) {
            when (getOverridePendingTransition()) {
                TransitionMode.TOP -> overridePendingTransition(0, R.anim.top_out)
                TransitionMode.BOTTOM -> overridePendingTransition(0, R.anim.bottom_out)
                TransitionMode.LEFT -> overridePendingTransition(0, R.anim.left_out)
                TransitionMode.RIGHT -> overridePendingTransition(0, R.anim.right_out)
                TransitionMode.FADE -> overridePendingTransition(0, R.anim.fade_out)
                TransitionMode.SCALE -> overridePendingTransition(0, R.anim.scale_out)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        ActivityManager.getInstance().removeActivity(this)
    }


    /**
     * 初始化工具栏,默认情况下加粗
     */
    protected fun initToolBar(toolbar: ViewGroup, title: String? = null, fontType: FontType = FontType.BOLD) {
        val ivBack = toolbar.findViewById<ImageView>(R.id.iv_back)
        ivBack.setOnClickListener {
            showKeyboard(false)
            finish()
        }

        val tvTitle = toolbar.findViewById<CustomFontTextView>(R.id.tv_title)
        tvTitle.setFontType(fontType)
        tvTitle.text = title

    }

    /**
     * 初始化工具栏，默认情况下加粗
     */
    protected fun initToolBar(toolbar: ViewGroup, @StringRes id: Int? = null, fontType: FontType = FontType.BOLD) {
        val ivBack = toolbar.findViewById<ImageView>(R.id.iv_back)
        ivBack.setOnClickListener {
            showKeyboard(false)
            finish()
        }

        val tvTitle = toolbar.findViewById<CustomFontTextView>(R.id.tv_title)
        tvTitle.setFontType(fontType)
        tvTitle.setText(id!!)

    }


    abstract fun initView(savedInstanceState: Bundle?)

    /**
     *  获取bundle 中的数据
     */
    open fun getBundleExtras(extras: Bundle) {}


    /**
     * 是否有切换动画
     */
    protected open fun toggleOverridePendingTransition() = false

    /**
     * 获得切换动画的模式
     */
    protected open fun getOverridePendingTransition(): TransitionMode? = null

    /**
     * 获取当前布局id
     */
    abstract fun getContentViewLayoutId(): Int


}


