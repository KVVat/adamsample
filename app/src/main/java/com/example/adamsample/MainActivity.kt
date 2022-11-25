package com.example.adamsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.adamsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var binding:ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater);
    setContentView(binding.root)
  }

  override fun onStart() {
    super.onStart()

    val signature:String =BuildConfig.APPLICATION_ID+"\n"+
      "Version:"+BuildConfig.VERSION_NAME+"/"+BuildConfig.VERSION_CODE+"\n"+BuildConfig.BUILD_TYPE

    binding.mainText.text = signature;
  }

  companion object {
    // Used to load the 'gtest' library on application startup.
    init {
      System.loadLibrary("native-lib")
    }
  }
}