package org.autojs.autojs.ui.main.bindmachine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import org.autojs.autoxjs.R

class BindMachine : Fragment() {
    private val KEY_POPUP_ITEM_LAYOUT = "popup_item_layout"
    private var isStarted = false
    @LayoutRes
    private var popupItemLayout = R.layout.cat_popup_item
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (savedInstanceState != null) {
            popupItemLayout = savedInstanceState.getInt(KEY_POPUP_ITEM_LAYOUT)
        }
        val view = inflater.inflate(R.layout.activity_fragment_bindmachine, container, false)
        val listPopupWindowButton = view.findViewById<Button>(R.id.list_popup_window)
        val listPopupWindow: ListPopupWindow = initializeListPopupMenu(listPopupWindowButton)
        listPopupWindowButton.setOnClickListener { v: View? -> listPopupWindow.show() }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(
            KEY_POPUP_ITEM_LAYOUT,
            popupItemLayout
        )
    }


    private fun initializeListPopupMenu(v: View): ListPopupWindow {
        // 创建一个 ListPopupWindow 对象，使用当前 Fragment 的上下文，指定样式为系统默认的 listPopupWindowStyle
        val listPopupWindow =
            ListPopupWindow(requireContext() , null, R.attr.listPopupWindowStyle)
        // 创建一个 ArrayAdapter 对象，用于将字符串数组显示在列表弹出窗口中
        val adapter: ArrayAdapter<CharSequence> =
            ArrayAdapter<CharSequence>(
                requireContext(),
                popupItemLayout,  // 使用指定的布局资源来显示列表项，通常是一个布局文件
                resources.getStringArray(R.array.cat_list_popup_window_content)
            ) // 从资源中获取字符串数组
        // 设置列表弹出窗口的适配器，用于填充和显示列表项
        listPopupWindow.setAdapter(adapter)
        // 设置列表弹出窗口的锚点视图，即显示在哪个视图的附近
        listPopupWindow.anchorView = v
        // 设置列表项的点击事件监听器
        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            // 当列表项被点击时，显示一个 Snackbar，显示被点击的列表项的内容
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),  // 找到当前 Activity 的根视图
                adapter.getItem(position).toString(),  // 获取点击的列表项内容并转换为字符串
                Snackbar.LENGTH_LONG
            ) // Snackbar 显示的时长
                .show() // 显示 Snackbar
            // 点击后关闭列表弹出窗口
            listPopupWindow.dismiss()
        }
        // 返回设置好的列表弹出窗口对象
        return listPopupWindow
    }
}