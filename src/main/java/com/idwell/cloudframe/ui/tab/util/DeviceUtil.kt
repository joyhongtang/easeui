package com.idwell.cloudframe.ui.tab.util

import com.blankj.utilcode.util.SPUtils
private const val IS_VISIT_IM_SERVICE = "isvisitim"
object  DeviceUtil {
    var isVisitedImService: Boolean
        get() = SPUtils.getInstance().getBoolean(IS_VISIT_IM_SERVICE, false)
        set(value) = SPUtils.getInstance().put(IS_VISIT_IM_SERVICE, value)

}