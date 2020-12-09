package com.rain.arcast.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.rain.arcast.MainActivity
import com.rain.arcast.R

class RecyclerAdapter(val context: Context) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    var bitmapList = mutableListOf<Bitmap>()

    inner class ViewHolder(picView: View) : RecyclerView.ViewHolder(picView) {
        val imageView: ImageView = picView.findViewById(R.id.ivDrawing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.card_image, parent, false
        )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageBitmap(bitmapList[position])
        //holder.imageView
    }

    override fun getItemCount(): Int {
        return bitmapList.size
    }

    fun addBitmap(byteArray: ByteArray) {
        bitmapList.add(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
        notifyItemInserted(bitmapList.lastIndex)
    }

    fun deleteBitmap(position: Int) {
        (context as MainActivity).runOnUiThread {
            bitmapList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

}