package com.rain.arcast.ui.draw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rain.arcast.R
import com.rain.arcast.ui.CanvasView

class DrawFragment : Fragment() {

    private lateinit var drawViewModel: DrawViewModel
    private lateinit var btnSave : Button
    private lateinit var canvas : CanvasView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        drawViewModel =
                ViewModelProvider(this).get(DrawViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_draw, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        drawViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        canvas = CanvasView(requireContext())
        btnSave = root.findViewById(R.id.btn_save)

        root.findViewById<ConstraintLayout>(R.id.draw_layout).addView(canvas)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnSave.setOnClickListener {
            canvas.clear()

            //canvas.saveToImg(requireContext())
        }

    }

    override fun onStart() {
        super.onStart()

        btnSave.setOnClickListener {
            //canvas.clear()
            //canvas.saveToImg(requireContext())

        }
    }
}