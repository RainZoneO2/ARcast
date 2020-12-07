package com.rain.arcast.ui.draw

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rain.arcast.R
import com.rain.arcast.ui.CanvasView

class DrawFragment : Fragment() {

    private lateinit var drawViewModel: DrawViewModel
    private lateinit var btnSave: Button
    private lateinit var canvas: CanvasView

    private val TAG = "Permissions"
    private val WRITE_REQUEST_CODE = 100

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

    override fun onStart() {
        super.onStart()

        btnSave.setOnClickListener {
            setupPerm()
            //canvas.clear()


        }
    }

    private fun setupPerm() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to store denied")
            /*
            * ActivityCompat.shouldShowRequestPermissionRationale(context, permission) should return true if the user has previously denied request, and false if not*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(
                    "Permission to access the external storage is " +
                            "required for this app to store drawings!"
                )

                builder.setPositiveButton("OK") { dialog, id ->
                    Log.i(TAG, "Clicked")
                    makeRequest()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                makeRequest() //????????????? why didnt this get executed lol
            }
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            WRITE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user.")
                } else {
                    canvas.saveToImg(requireContext())
                    Log.i(TAG, "Permission has been granted by user.")
                }
            }
        }


        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}