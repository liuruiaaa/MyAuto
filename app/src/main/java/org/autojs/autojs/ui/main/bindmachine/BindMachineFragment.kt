package org.autojs.autojs.ui.main.bindmachine

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.autojs.autojs.model.mymodel.SelectItem
import org.autojs.autoxjs.R
import java.io.IOException
import java.util.LinkedList

class BindMachine : Fragment() {
    private val KEY_POPUP_ITEM_LAYOUT = "popup_item_layout"
    private lateinit var view:View;
    private var isStarted = false
    private var disposable: Disposable? = null
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
        view = inflater.inflate(R.layout.activity_fragment_bindmachine, container, false)
        addListItem()
        //addListItem2()
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(
            KEY_POPUP_ITEM_LAYOUT,
            popupItemLayout
        )
    }

    fun showToast(context: Context, message: String) {
        // 切换回主线程显示 Toast
        val mainHandler = android.os.Handler(context.mainLooper)
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private  fun addListItem2(){
        val listPopupWindowButton = view.findViewById<Button>(R.id.list_popup_window)
        val listPopupWindow: ListPopupWindow = initializeListPopupMenu(listPopupWindowButton)
        listPopupWindowButton.setOnClickListener { v: View? -> listPopupWindow.show() }

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
        listPopupWindow.show()
        // 返回设置好的列表弹出窗口对象
        return listPopupWindow
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose() // 在适当的生命周期结束时取消订阅
    }
    /**
     * 为绑定按钮添加 请求的数据
     */
    private fun addListItem() {
        val listPopupWindowButton = view.findViewById<Button>(R.id.list_popup_window)
        listPopupWindowButton.setOnClickListener {
            getOkHttpData { listStr ->
                if (listStr != null) {
                    val charSequenceList: List<CharSequence> = listStr.map { it as CharSequence }
                    // 创建一个 ListPopupWindow 对象，使用当前 Fragment 的上下文，指定样式为系统默认的 listPopupWindowStyle
                    val listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
                    // 在主线程上操作 UI
                    Observable.just(charSequenceList)
                        .observeOn(AndroidSchedulers.mainThread()) // 切换到主线程进行后续操作
                        .subscribe { charSequences -> // 订阅 Observable 发射的数据流
                            val listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
                            // 创建 ListPopupWindow 对象，使用当前 Fragment 的上下文，指定样式为系统默认的 listPopupWindowStyle
                            val adapter = ArrayAdapter<CharSequence>(
                                requireContext(),
                                popupItemLayout,  // 如果有自定义的布局，可以使用自定义的布局，否则使用系统提供的默认布局
                                charSequences.toTypedArray()  // 将 List<CharSequence> 转换为 Array<CharSequence>，作为适配器的数据源
                            )
                            listPopupWindow.setAdapter(adapter) // 设置 ListPopupWindow 的适配器
                            listPopupWindow.anchorView = listPopupWindowButton // 设置 ListPopupWindow 的锚点 View，即显示在哪个 View 下方
                            listPopupWindow.setOnItemClickListener { parent, view, position, id ->
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    adapter.getItem(position).toString(), // 获取点击项的数据并转换为字符串显示
                                    Snackbar.LENGTH_LONG
                                ).show()
                                listPopupWindow.dismiss() // 点击列表项后关闭 ListPopupWindow
                            }
                            listPopupWindow.show() // 显示 ListPopupWindow
                        }.also { disposable = it }
                } else {
                    // 处理获取数据失败的情况，例如显示错误信息等
                    showToast(requireContext(), "获取数据失败")
                }
            }
        }
    }
    
    private fun getOkHttpData(callback: (LinkedList<String>?) -> Unit) {
        val url = "https://www.baidu.com" // 替换为你的目标 URL
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 处理请求失败的情况
                e.printStackTrace()
                val errorMessage = "请求失败: " + e.message
                showToast(requireContext(), errorMessage)
                callback(null) // 请求失败时回调传递 null
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    val responseBody = response.body?.string()
                    // 使用 Gson 解析 JSON 字符串
//                    val gson = Gson()
//                    val itemType = object : TypeToken<List<SelectItem>>() {}.type
//                    val items: List<SelectItem> = gson.fromJson(responseBody, itemType)

                    val listStr = LinkedList<String>()
                    listStr.add("111111")
                    listStr.add("22222")
                    Log.i("BindMachine", "请求成功！$responseBody")
                    // showToast(requireContext(), "请求成功")
                    callback(listStr) // 请求成功时回调传递 selectItems
                } catch (e: IOException) {
                    e.printStackTrace()
                    val errorMessage = "请求失败: " + e.message
                    showToast(requireContext(), errorMessage)
                    callback(null) // 请求失败时回调传递 null
                }
            }
        })
    }

}