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
    private var isStarted = false
    @LayoutRes val popupItemLayout=0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        if (savedInstanceState != null) {
//            popupItemLayout =
//                savedInstanceState.getInt("popup_item_layout")
//        }

        val view = inflater.inflate(R.layout.activity_fragment_bindmachine, container, false)
        val listPopupWindowButton = view.findViewById<Button>(R.id.list_popup_window)
       // val listPopupWindow: ListPopupWindow = initializeListPopupMenu(listPopupWindowButton)
        //listPopupWindowButton.setOnClickListener { v: View? -> listPopupWindow.show() }

        return view
    }

//    private fun initializeListPopupMenu(v: View) {
//        val listPopupWindow =
//            ListPopupWindow(getContext(), null, R.attr.listPopupWindowStyle)
//        val adapter: ArrayAdapter<CharSequence?> =
//            ArrayAdapter<Any?>(
//                getContext(),
//                popupItemLayout,
//                resources.getStringArray(R.array.cat_list_popup_window_content)
//            )
//        listPopupWindow.setAdapter(adapter)
//        listPopupWindow.anchorView = v
//        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
//            Snackbar.make(
//                getContext().findViewById(android.R.id.content),
//                adapter.getItem(position).toString(),
//                Snackbar.LENGTH_LONG
//            )
//                .show()
//            listPopupWindow.dismiss()
//        }
//        return listPopupWindow
//    }
}