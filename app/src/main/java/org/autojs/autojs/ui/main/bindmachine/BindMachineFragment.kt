package org.autojs.autojs.ui.main.bindmachine

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
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

    /**
     * 为绑定按钮添加 请求的数据
     */
    private fun addListItem() {
        val listPopupWindowButton = view.findViewById<Button>(R.id.list_popup_window)
        listPopupWindowButton.setOnClickListener {
            getOkHttpData { selectItems ->
                if (selectItems != null) {
                    // 创建 ListPopupWindow 并显示
                    // 创建一个 ListPopupWindow 对象，使用当前 Fragment 的上下文，指定样式为系统默认的 listPopupWindowStyle
                    val listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
                    val adapter = ArrayAdapter<SelectItem> (
                        requireContext(),
                        R.layout.cat_popup_item,
                       // popupItemLayout,
                        selectItems
                    )

                    listPopupWindow.setAdapter(adapter)
                    listPopupWindow.anchorView = listPopupWindowButton
                    listPopupWindow.setOnItemClickListener { parent, view, position, id ->
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            adapter.getItem(position)?.filename.toString(),
                            Snackbar.LENGTH_LONG
                        ).show()
                        listPopupWindow.dismiss()
                    }

                    listPopupWindow.show()
                } else {
                    // 处理获取数据失败的情况，例如显示错误信息等
                    showToast(requireContext(), "获取数据失败")
                }
            }
        }
    }



    private fun getOkHttpData(callback: (List<SelectItem>?) -> Unit) {
        val selectItems: MutableList<SelectItem> = mutableListOf() // 使用 mutableListOf 替代 LinkedList
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

                    var one = SelectItem("ccc","asdf")
                    var two = SelectItem("ccc2","asdf")
                    selectItems.add(one)
                    selectItems.add(two)


                    Log.i("BindMachine", "请求成功！$responseBody")
                    showToast(requireContext(), "请求成功")

                    callback(selectItems) // 请求成功时回调传递 selectItems
                } catch (e: IOException) {
                    e.printStackTrace()
                    val errorMessage = "请求失败: " + e.message
                    showToast(requireContext(), errorMessage)
                    callback(null) // 请求失败时回调传递 null
                }
            }
        })
    }

    private fun getOkHttpDatcca(callback: (List<SelectItem>?) -> Unit) {
        val selectItems: MutableList<SelectItem> = LinkedList()
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

                    // 使用 Gson 解析 JSON 字符串（示例中暂未使用 Gson 解析，你可以根据实际需要添加解析代码）

//                    val one = SelectItem()
//                    one.filename = "asdf"
//                    one.url = "ccc"
//
//                    var two = SelectItem()
//                    two.filename = "asdf"
//                    two.url = "ccc"
//                    selectItems.add(one)
//                    selectItems.add(two)

                    // selectItems.addAll(items)

                    Log.i("BindMachine", "请求成功！$responseBody")
                    showToast(requireContext(), "请求成功")

                    callback(selectItems) // 请求成功时回调传递 selectItems
                } catch (e: IOException) {
                    e.printStackTrace()
                    val errorMessage = "请求失败: " + e.message
                    showToast(requireContext(), errorMessage)
                    callback(null) // 请求失败时回调传递 null
                }
            }
        })
    }

//    private fun initializeListPopupMenu(
//        v: View,
//        listener: (ListPopupWindow) -> Unit // 定义回调接口，接收一个 ListPopupWindow 参数
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val selectItems = getOkHttpData()
//
//            // 切换回主线程处理 UI
//            withContext(Dispatchers.Main) {
//                val listPopupWindow = ListPopupWindow(
//                    requireContext(),
//                    null,
//                    R.attr.listPopupWindowStyle
//                )
//                val adapter = ArrayAdapter(
//                    requireContext(),
//                    popupItemLayout,
//                    selectItems
//                )
//
//                listPopupWindow.setAdapter(adapter)
//                listPopupWindow.anchorView = v
//                listPopupWindow.setOnItemClickListener { parent, view, position, id ->
//                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
//                        adapter.getItem(position)?.filename.toString(),
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                    listPopupWindow.dismiss()
//                }
//
//                // 调用回调接口，将初始化后的 ListPopupWindow 返回给调用方
//                listener.invoke(listPopupWindow)
//            }
//        }
//    }






//    private fun initializeListPopupMenu(v: View): ListPopupWindow {
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val selectItems = getOkHttpData()
//            // 创建一个 ListPopupWindow 对象，使用当前 Fragment 的上下文，指定样式为系统默认的 listPopupWindowStyle
//            val listPopupWindow =
//                ListPopupWindow(requireContext() , null, R.attr.listPopupWindowStyle)
//            // 创建一个 ArrayAdapter 对象，用于将字符串数组显示在列表弹出窗口中
//            val adapter: ArrayAdapter<SelectItem> =
//                ArrayAdapter<SelectItem>(
//                    requireContext(),
//                    popupItemLayout,  // 使用指定的布局资源来显示列表项，通常是一个布局文件
//                    selectItems
//                    // resources.getStringArray(R.array.cat_list_popup_window_content)
//                ) // 从资源中获取字符串数组
//            // 设置列表弹出窗口的适配器，用于填充和显示列表项
//            listPopupWindow.setAdapter(adapter)
//            // 设置列表弹出窗口的锚点视图，即显示在哪个视图的附近
//            listPopupWindow.anchorView = v
//            // 设置列表项的点击事件监听器
//            listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
//                // 当列表项被点击时，显示一个 Snackbar，显示被点击的列表项的内容
//                Snackbar.make(
//                    requireActivity().findViewById(android.R.id.content),  // 找到当前 Activity 的根视图
//                    adapter.getItem(position)?.filename.toString(),  // 获取点击的列表项内容并转换为字符串
//                    Snackbar.LENGTH_LONG
//                ) // Snackbar 显示的时长
//                    .show() // 显示 Snackbar
//                // 点击后关闭列表弹出窗口
//                listPopupWindow.dismiss()
//            }
//            // 返回设置好的列表弹出窗口对象
//            return listPopupWindow
//        }
//    }


//    var textInput = view.findViewById<TextInputLayout>(R.id.token_id)
//    textInput.editText?.setOnFocusChangeListener { v, hasFocus ->
//        if (!hasFocus) {
//            // 执行焦点失去时的操作
//            val url = "https://www.baidu.com" // 替换为你的目标 URL
//            val client = OkHttpClient()
//            val request = Request.Builder().url(url).build()
//            client.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    // 处理请求失败的情况
//                    e.printStackTrace()
//                    // 在 UI 线程显示 Toast 提示
//                    val errorMessage = "请求失败: " + e.message
//                    showToast(requireContext(), errorMessage)
//                }
//                override fun onResponse(call: Call, response: Response) {
//                    // 处理请求成功的情况，可以在这里处理返回的数据
//                    val responseBody = response.body?.string()
//                    Log.i("BindMachine", "请求成功！"+responseBody)
//                    showToast(requireContext(), "请求成功")
//                    // 根据需要处理返回的数据
//                }
//            })
//
//        }
//    }

}