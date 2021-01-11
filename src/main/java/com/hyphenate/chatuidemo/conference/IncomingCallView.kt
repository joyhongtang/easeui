package com.hyphenate.chatuidemo.conference

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.hyphenate.easeui.R
import kotlinx.android.synthetic.main.em_incoming_call_view.view.*

/**
 * Created by zhangsong on 18-4-19.
 */
class IncomingCallView : FrameLayout {
    private val dotText =
        arrayOf(" . ", " . . ", " . . .")

    interface OnActionListener {
        fun onRejectClick(v: View)
        fun onPickupClick(v: View)
    }

    var drawableAnim: AnimationDrawable? = null
    var onActionListener: OnActionListener? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility == View.VISIBLE) {
            iv_call_anim.setBackgroundResource(R.drawable.ring_anim)
            drawableAnim = iv_call_anim.background as AnimationDrawable
            drawableAnim!!.isOneShot = false
            drawableAnim!!.start()
        } else {
            if (drawableAnim?.isRunning == true)
                drawableAnim?.stop()

            drawableAnim = null
        }
    }

    var valueAnimator: ValueAnimator ?= null
    fun setInviteInfo(inviteInfo: String) {
        tv_inviter_name.text = inviteInfo
        if (valueAnimator == null) {
            valueAnimator= ValueAnimator.ofInt(0, 3).setDuration(1000)
            valueAnimator!!.setRepeatCount(ValueAnimator.INFINITE)
            valueAnimator!!.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator?) {
                    val i = animation!!.getAnimatedValue() as Int
                    tv_update.text =  dotText[i % dotText.size]
                }
            })
        }
        valueAnimator!!.start()

    }

    fun init() {
        LayoutInflater.from(context).inflate(R.layout.em_incoming_call_view, this)
        // Reject the video call invite.
        btn_reject.setOnClickListener {
            onActionListener?.onRejectClick(btn_reject)
        }
        // Pickup the video call invite.
        btn_pickup.setOnClickListener {
            onActionListener?.onPickupClick(btn_pickup)
        }
    }
}