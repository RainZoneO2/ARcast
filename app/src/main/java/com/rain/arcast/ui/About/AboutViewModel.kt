package com.rain.arcast.ui.About

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SlideshowViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This app was created by Alghaith Ahmad - S8912J as a final semester project! " +
                "If you enjoyed it, please leave feedback! (Just kidding there isn't any feedback functionality lol)"
    }
    val text: LiveData<String> = _text
}