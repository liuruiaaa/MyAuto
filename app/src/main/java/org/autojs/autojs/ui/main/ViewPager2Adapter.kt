package org.autojs.autojs.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.autojs.autojs.ui.main.bindmachine.BindMachineFragment
import org.autojs.autojs.ui.main.scripts.ScriptListFragment
import org.autojs.autojs.ui.main.startup.StartUpFragment
import org.autojs.autojs.ui.main.web.EditorAppManager

class ViewPager2Adapter(
    fragmentActivity: FragmentActivity,
    private val startUpFragment: StartUpFragment,
    private val bindMachineFragment: BindMachineFragment,
    private val scriptListFragment: ScriptListFragment
) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> {
                startUpFragment
            }
            1 -> {
                bindMachineFragment
            }
            else -> {
                scriptListFragment
            }
        }
        return fragment
    }

    override fun getItemCount(): Int {
        return 3
    }
}