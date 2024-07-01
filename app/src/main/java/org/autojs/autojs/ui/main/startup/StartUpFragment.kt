package org.autojs.autojs.ui.main.startup

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.autojs.autoxjs.R

class StartUpFragment : Fragment() {
    private var isStarted = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

}