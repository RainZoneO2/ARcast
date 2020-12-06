package com.rain.arcast.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rain.arcast.R


private const val STROKE_WIDTH = 12f
class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context) {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    //val iv = findViewById<ImageView>(R.id.imageView2)

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
        //iv.setImageBitmap(extraBitmap)
        extraCanvas.drawColor(Color.WHITE)
    }

//    fun saveToImg(context: Context) {
//        //create directory if not exist
//       val dir = File(context.getExternalFilesDir(null) + "/tempfolder");
//        if (!dir.exists()) {
//            dir.mkdirs()
//        }
//
//        val output = File(dir, "tempfile.jpg");
//        val os: OutputStream?
//
//        try {
//            os = FileOutputStream(output)
//            extraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
//            os.flush()
//            os.close()
//
//            //this code will scan the image so that it will appear in your gallery when you open next time
//            MediaScannerConnection.scanFile(context, arrayOf(output.toString()), null,
//                    OnScanCompletedListener { path, uri -> Log.d("appname", "image is saved in gallery and gallery is refreshed.") }
//            )
//        } catch (e: Exception) {
//        }
//
//        dir.writeText("test")
//    }
}

/*For changing color of brush use paint.color, for resetting drawing surface use extracanvas.drawColor()*/