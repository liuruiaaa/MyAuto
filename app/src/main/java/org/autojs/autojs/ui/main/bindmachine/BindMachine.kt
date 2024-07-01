package org.autojs.autojs.ui.main.bindmachine

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.autojs.autoxjs.R

class BindMachine : Fragment() {
    private var isStarted = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_fragment_bindmachine, container, false)
        val button = view.findViewById<Button>(R.id.editText)
        //todo...

        return view
    }

}