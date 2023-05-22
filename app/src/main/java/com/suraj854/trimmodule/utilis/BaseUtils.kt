package com.suraj854.videotrimmerview.utilis

import android.content.Context
import java.lang.ref.WeakReference

object BaseUtils {
    private const val ERROR_INIT = "Initialize BaseUtils with invoke init()"
    private var mWeakReferenceContext: WeakReference<Context>? = null

    fun init(ctx: Context) {
        mWeakReferenceContext = WeakReference(ctx)

    }

    val context: Context
        get() {
            requireNotNull(mWeakReferenceContext) { ERROR_INIT }
            return mWeakReferenceContext!!.get()!!.applicationContext
        }
}
