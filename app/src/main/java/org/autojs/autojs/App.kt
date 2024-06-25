package org.autojs.autojs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.baidu.mobstat.StatService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.flurry.android.FlurryAgent
import com.stardust.app.GlobalAppContext
import com.stardust.autojs.core.ui.inflater.ImageLoader
import com.stardust.autojs.core.ui.inflater.util.Drawables
import com.stardust.theme.ThemeColor
import com.tencent.bugly.Bugly
import com.tencent.bugly.crashreport.CrashReport
import org.autojs.autojs.autojs.AutoJs
import org.autojs.autojs.autojs.key.GlobalKeyObserver
import org.autojs.autojs.external.receiver.DynamicBroadcastReceivers
import org.autojs.autojs.theme.ThemeColorManagerCompat
import org.autojs.autojs.timing.TimedTaskManager
import org.autojs.autojs.timing.TimedTaskScheduler
import org.autojs.autojs.tool.CrashHandler
import org.autojs.autojs.ui.error.ErrorReportActivity
import org.autojs.autoxjs.BuildConfig
import org.autojs.autoxjs.R
import java.lang.ref.WeakReference

/**
 * Created by Stardust on 2017/1/27.
 */

class App : MultiDexApplication(), Configuration.Provider {
    lateinit var dynamicBroadcastReceivers: DynamicBroadcastReceivers
        private set

    override fun onCreate() {
        super.onCreate()
        GlobalAppContext.set(
            this, com.stardust.app.BuildConfig.generate(BuildConfig::class.java)
        )
        instance = WeakReference(this)
        setBaiduUpStaticsTool(this)
        //setUpStaticsTool()
        setUpDebugEnvironment()
        init()
    }

    private fun setBaiduUpStaticsTool(context: Context) {

        // SDK初始化，该函数不会采集用户个人信息，也不会向百度移动统计后台上报数据
        StatService.init( context,  "dbcfccb460",  "test");

        // 通过该接口可以控制敏感数据采集，true表示可以采集，false表示不可以采集，
        // 该方法一定要最优先调用，请在StatService.start(this)之前调用，采集这些数据可以帮助App运营人员更好的监控App的使用情况，
        // 建议有用户隐私策略弹窗的App，用户未同意前设置false,同意之后设置true
        StatService.setAuthorizedState( this, true)

        // setSendLogStrategy已经@deprecated，建议使用新的start接口
        // 如果没有页面和自定义事件统计埋点，此代码一定要设置，否则无法完成统计
        // 进程第一次执行此代码，会导致发送上次缓存的统计数据；若无上次缓存数据，则发送空启动日志
        // 由于多进程等可能造成Application多次执行，建议此代码不要埋点在Application中，否则可能造成启动次数偏高
        // 建议此代码埋点在统计路径触发的第一个页面中，若可能存在多个则建议都埋点
        StatService.start(this);
    }
    private fun setUpStaticsTool() {
        if (BuildConfig.DEBUG)
            return
        FlurryAgent.Builder()
            .withLogEnabled(BuildConfig.DEBUG)
            .build(this, "D42MH48ZN4PJC5TKNYZD")
    }

    private fun setUpDebugEnvironment() {
        Bugly.isDev = false
        val crashHandler = CrashHandler(ErrorReportActivity::class.java)

        val strategy = CrashReport.UserStrategy(applicationContext)
        strategy.setCrashHandleCallback(crashHandler)

        CrashReport.initCrashReport(applicationContext, BUGLY_APP_ID, false, strategy)

        crashHandler.setBuglyHandler(Thread.getDefaultUncaughtExceptionHandler())
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
    }

    private fun init() {
        ThemeColorManagerCompat.init(
            this,
            ThemeColor(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark),
                ContextCompat.getColor(this, R.color.colorAccent)
            )
        )
        AutoJs.initInstance(this)
        if (Pref.isRunningVolumeControlEnabled()) {
            GlobalKeyObserver.init()
        }
        setupDrawableImageLoader()
        TimedTaskScheduler.init(this)
        initDynamicBroadcastReceivers()
    }


    @SuppressLint("CheckResult")
    private fun initDynamicBroadcastReceivers() {
        dynamicBroadcastReceivers = DynamicBroadcastReceivers(this)
        val localActions = ArrayList<String>()
        val actions = ArrayList<String>()
        TimedTaskManager.allIntentTasks
            .filter { task -> task.action != null }
            .doOnComplete {
                if (localActions.isNotEmpty()) {
                    dynamicBroadcastReceivers.register(localActions, true)
                }
                if (actions.isNotEmpty()) {
                    dynamicBroadcastReceivers.register(actions, false)
                }
                @Suppress("DEPRECATION")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                    Intent(
                        DynamicBroadcastReceivers.ACTION_STARTUP
                    )
                )
            }
            .subscribe({
                if (it.isLocal) {
                    it.action?.let { it1 -> localActions.add(it1) }
                } else {
                    it.action?.let { it1 -> actions.add(it1) }
                }
            }, { it.printStackTrace() })


    }

    private fun setupDrawableImageLoader() {
        Drawables.setDefaultImageLoader(object : ImageLoader {
            override fun loadInto(imageView: ImageView, uri: Uri) {
                Glide.with(imageView)
                    .load(uri)
                    .into(imageView)
            }

            override fun loadIntoBackground(view: View, uri: Uri) {
                Glide.with(view)
                    .load(uri)
                    .into(object : CustomViewTarget<View, Drawable>(view) {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            view.background = resource
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) =Unit
                        override fun onResourceCleared(placeholder: Drawable?)=Unit
                    })
            }

            override fun load(view: View, uri: Uri): Drawable {
                throw UnsupportedOperationException()
            }

            override fun load(
                view: View,
                uri: Uri,
                drawableCallback: ImageLoader.DrawableCallback
            ) {
                Glide.with(view)
                    .load(uri)
                    .into(object : CustomViewTarget<View, Drawable>(view){
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            drawableCallback.onLoaded(resource)
                        }
                        override fun onLoadFailed(errorDrawable: Drawable?) =Unit
                        override fun onResourceCleared(placeholder: Drawable?)=Unit
                    })
            }

            override fun load(view: View, uri: Uri, bitmapCallback: ImageLoader.BitmapCallback) {
                Glide.with(view)
                    .asBitmap()
                    .load(uri)
                    .into(object : CustomViewTarget<View, Bitmap>(view) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bitmapCallback.onLoaded(resource)
                        }
                        override fun onLoadFailed(errorDrawable: Drawable?) =Unit
                        override fun onResourceCleared(placeholder: Drawable?)=Unit
                    })
            }
        })
    }

    override val workManagerConfiguration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .build()

    companion object {

        private const val TAG = "App"
        private const val BUGLY_APP_ID = "19b3607b53"

        private lateinit var instance: WeakReference<App>

        val app: App
            get() = instance.get()!!
    }
}
