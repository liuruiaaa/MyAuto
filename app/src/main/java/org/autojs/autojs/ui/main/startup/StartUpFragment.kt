package org.autojs.autojs.ui.main.startup

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity.MODE_PRIVATE
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.autojs.autojs.model.script.ScriptFile
import org.autojs.autoxjs.R
import java.io.IOException
import org.autojs.autojs.model.script.Scripts.run
import java.io.File
import java.io.FileOutputStream

class StartUpFragment : Fragment() {
    private var sharedPreferences: SharedPreferences? = null
    private var isStarted = false
    private  var file:File? = null
    private var disposable: Disposable? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferences = requireContext().getSharedPreferences("autoFileJs", MODE_PRIVATE)
        val view = inflater.inflate(R.layout.activity_fragment_startup, container, false)
        val button = view.findViewById<Button>(R.id.btn_action)
        // 定义防止连续点击的时间间隔
        val debounceTime = 1000L
        var lastClickTime = 0L
        // 找到布局中的按钮，并为其设置点击事件监听器
        button.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if(currentTime - lastClickTime >= debounceTime) {
                lastClickTime = currentTime
                if(!isStarted){
                    button.text = "启动中。。。"
                    // 调用 getOkHttpData 方法，并处理返回的数据
                    getOkHttpData { result ->
                        if(result.equals("启动成功")){
                            getOkHttpJs { results ->
                                run(ScriptFile(file!!.path)) //执行js文件
                                Observable.just(results)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ result ->
                                        if (result == "文件下载成功") {
                                            button.setBackgroundResource(R.drawable.circle_button_bg_blue)
                                            button.text = "已启动"
                                            isStarted = true
                                        } else {
                                            button.setBackgroundResource(R.drawable.circle_button_bg_blue)
                                            button.text = "启动失败"
                                            isStarted = true
                                        }
                                    }, { error ->
                                        error.printStackTrace()
                                    }).also { disposable = it }
                            }
                        }
                    }
                    // todo...
                    // 按钮点击后改变按钮的背景颜色和文字内容
                    button.setBackgroundResource(R.drawable.circle_button_bg_blue)  // 改变背景颜色为绿色
                    button.text = "已启动"
                    isStarted = true
                }else{
                    button.setBackgroundResource(R.drawable.circle_button_bg_width)  // 改变背景颜色为白色
                    button.text = "点击开始"
                    isStarted = false
                }
                // 按钮点击时的操作
                Toast.makeText(activity, "按钮被点击了！", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose() // 在适当的生命周期结束时取消订阅
    }

    //这个是去拉去js，然后开始去执行js文件
    private fun getOkHttpJs(callback:  (String?) -> Unit){
        //val url = "https://leleoss.oss-cn-shenzhen.aliyuncs.com/js/xianyu_listen%20.js" // 替换为你的目标 URL
        val url = "https://leleoss.oss-cn-shenzhen.aliyuncs.com/js/test.js" // 替换为你的目标 URL
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
                    // 读取文本内容到字符串
                    //val responseBody = response.body?.string()
                    val inputStream = response.body?.byteStream()
                    // 将响应内容写入文件
                    // 创建目标文件夹
                    val directory = File(requireContext().getExternalFilesDir(null), "sample/")
                    directory.mkdirs()
                    // 创建目标文件
                    file = File(directory, "xianyu_listen.js")
                   // file = File(requireContext().getExternalFilesDir(null), "sample/定时器/xianyu_listen.js")
                    val outputStream = FileOutputStream(file)

                    //val outputStream = FileOutputStream("/data/user/0/com.rui_2014.myapp/files/sample/定时器/xianyu_listen.js") // 替换为你保存文件的路径

                    inputStream?.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output) // 将输入流内容复制到输出流（文件）
                        }
                    }


                    // 处理你的文本内容，这里假设你要打印出来
                   // Log.i("js的内容是：", responseBody ?: "Empty response body")

                    Log.i("Download", "文件下载成功！")
                    callback("文件下载成功")
                } catch (e: IOException) {
                    e.printStackTrace()
                    val errorMessage = "文件下载失败: " + e.message
                    showToast(requireContext(), errorMessage)
                    callback(null) // 下载失败时回调传递 null
                }
            }
        })

    }

    //这个是去官网拉去js，然后做映射的逻辑处理
    private fun getOkHttpData(callback:  (String?) -> Unit) {
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

                    val listStr = "启动成功"
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

    fun showToast(context: Context, message: String) {
        // 切换回主线程显示 Toast
        val mainHandler = android.os.Handler(context.mainLooper)
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}