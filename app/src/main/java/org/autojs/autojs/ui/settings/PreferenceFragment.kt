package org.autojs.autojs.ui.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.stardust.pio.PFiles
import de.psdev.licensesdialog.LicensesDialog
import org.autojs.autojs.external.open.RunIntentActivity
import org.autojs.autojs.ui.util.launchActivity
import org.autojs.autojs.ui.widget.CommonMarkdownView
import org.autojs.autoxjs.R

class PreferenceFragment : PreferenceFragmentCompat() {
    private val ACTION_MAP = mutableMapOf<String, (activity: Activity) -> Unit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ACTION_MAP.apply {
            //.put(getString(R.string.text_theme_color), () -> selectThemeColor(getActivity()))
            // .put(getString(R.string.text_check_for_updates), () -> new UpdateCheckDialog(getActivity()).show())
            // .put(getString(R.string.text_issue_report), () -> startActivity(new Intent(getActivity(), IssueReporterActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
            //这个是在映射自定义的行为 牛逼！
            put(getString(R.string.text_about_me_and_repo)) {
                it.launchActivity<AboutActivity> {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            put(getString(R.string.text_licenses)) { showLicenseDialog(it) }
            put(getString(R.string.text_licenses_other)) { showLicenseDialog2(it) }
        }

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is ScriptDirPathPreference) {
            ScriptDirPathPreferenceFragmentCompat.newInstance(preference.getKey())?.let {
                it.setTargetFragment(this, 1234)
                it.show(
                    this.parentFragmentManager,
                    "androidx.preference.PreferenceFragment.DIALOG1"
                )
                return
            }
        }
        super.onDisplayPreferenceDialog(preference)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        // 从 ACTION_MAP 中取出与点击的 preference 标题相对应的动作。
        val action = ACTION_MAP[preference.title.toString()]
        // 获取当前的活动（Activity）实例。
        val activity = requireActivity()
        // 检查点击的偏好设置是否是“运行脚本”的偏好设置项。
        if (preference.title == getString(R.string.text_intent_run_script)) {
            // 根据偏好设置的开关状态设置相应组件（Component）的启用状态。
            val state = if ((preference as SwitchPreference).isChecked) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            // 使用PackageManager设置组件的启用/禁用状态，这里不让应用被杀死。
            activity.packageManager.setComponentEnabledSetting(
                ComponentName(activity, RunIntentActivity::class.java),
                state,
                PackageManager.DONT_KILL_APP
            )
            // 返回true，表示事件已处理。
            return true
        }
        // 如果ACTION_MAP中找到了对应的动作，则执行该动作。
        return if (action != null) {
            action(activity)
            // 返回true，表示事件已被处理。
            true
        } else {
            // 如果没有找到，就调用父类的onPreferenceTreeClick方法。
            super.onPreferenceTreeClick(preference)
        }
    }

    companion object {
        private fun showLicenseDialog(context: Context) {
            LicensesDialog.Builder(context)
                .setNotices(R.raw.licenses)
                .setIncludeOwnLicense(true)
                .build()
                .show()
        }

        private fun showLicenseDialog2(context: Context) {
            CommonMarkdownView.DialogBuilder(context)
                .padding(36, 0, 36, 0)
                .markdown(PFiles.read(context.resources.openRawResource(R.raw.licenses_other)))
                .title(R.string.text_licenses_other)
                .positiveText(R.string.ok)
                .canceledOnTouchOutside(false)
                .show()
        }
    }
}