package com.rain.arcast.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rain.arcast.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID.randomUUID


private const val STROKE_WIDTH = 12f
class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context) {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    private var path = Path()

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private var currentX = 0f
    private var currentY = 0f

    //private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private val touchTolerance = 0.4f

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.white, null)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized) extraBitmap.recycle() //What is if(::..)? How does it work?

        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)

        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)

            currentX = motionTouchEventX
            currentY = motionTouchEventY

            extraCanvas.drawPath(path, paint)
        }
        invalidate()
    }

    private fun touchUp() {
        path.reset()
    }

    fun clear() {
        extraCanvas.drawColor(Color.WHITE)
    }

    fun saveToImg(context: Context) {

        val file: File?

        try {
            file = File(
                    context.getExternalFilesDir(null)
                            .toString() + File.separator + "image-" + randomUUID() + ".png"
            )
            file.createNewFile()

            val bos = ByteArrayOutputStream()
            extraBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            val bitmapData = bos.toByteArray()

            //Upload happens here
            upload(context, bitmapData, file.name)

            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            Toast.makeText(context, "SUCCESSFUL!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun upload(context: Context, byteData: ByteArray, fileName: String) {
        val imageRef: StorageReference =
            FirebaseStorage.getInstance().reference.child("images" + File.separator + fileName)
        //TODO: Update metadata with Firebase.getInstance().getUID()
        imageRef.putBytes(byteData)
            .addOnSuccessListener { task ->
                Toast.makeText(context, "Uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { task ->
                Toast.makeText(context, "Failed! Error: " + task.message, Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { task ->
                val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
                Log.i("UPLOAD", progress.toString())
            }
    }
}

/*For changing color of brush use paint.color, for resetting drawing surface use extracanvas.drawColor()*/