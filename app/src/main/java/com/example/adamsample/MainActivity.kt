package com.example.adamsample

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.adamsample.databinding.ActivityMainBinding
import com.example.adamsample.utils.ReflectionUtils
import com.example.adamsample.utils.UniqueIDUtils
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

  private lateinit var binding:ActivityMainBinding
  private lateinit var receiver : BootReceiver // For Checking DirectBoot
  private lateinit var data: SharedPreferences
  private lateinit var editor: SharedPreferences.Editor
  private val TAG:String = "MainActivity"
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater);
    setContentView(binding.root)

    receiver = BootReceiver()
    IntentFilter(Intent.ACTION_BOOT_COMPLETED).also {
      registerReceiver(receiver,it)
    }
    IntentFilter(Intent.ACTION_LOCKED_BOOT_COMPLETED).also {
      registerReceiver(receiver,it)
    }
    IntentFilter("android.intent.action.MY_TEST_DES").also {
      registerReceiver(receiver,it)
    }

  }

  override fun onStart() {
    super.onStart()

    val signature:String =BuildConfig.APPLICATION_ID+"\n"+
      "Version:"+BuildConfig.VERSION_NAME+"/"+BuildConfig.VERSION_CODE+"\n"+BuildConfig.BUILD_TYPE

    binding.mainText.text = signature;

    //Context.createCredentialProtectedStorageContext()
    //applicationContext.createCredentialProtectedStorageContext()
    val isLoremIpsum: InputStream = applicationContext.resources.openRawResource(
      applicationContext.resources.getIdentifier("loremipsum",
                                         "raw", applicationContext.packageName));

    val ret:List<String> = ReflectionUtils.checkDeclaredMethod(applicationContext,"");
    Log.d("test", ret.toString());
    val ce_con:Context = ReflectionUtils.invokeReflectionCall(applicationContext.javaClass,
                                         "createCredentialProtectedStorageContext",
                                         applicationContext,null) as Context;


    Log.d("test", ce_con.filesDir.absolutePath);
    createTestFileViaCertainContext("ccces_",ce_con,isLoremIpsum)
    //Save something good to the Device Encrypted Storage

    //Save something good to the Credential Encrypted Storage

    //

    //Write Unique ID
    Log.d(TAG,"UUID:"+
          getPrefValueOrWrite("UUID",UniqueIDUtils.generateUuid()));
    Log.d(TAG,"AndroidId:"+
          getPrefValueOrWrite("AndroidID",UniqueIDUtils.getAndroidId(applicationContext)));
    Log.d(TAG,"Widevine:"+
          getPrefValueOrWrite("WideVine",UniqueIDUtils.getWidevineId()));
    GlobalScope.launch {
      Log.d(TAG,"GoogleAdId:"+
            getPrefValueOrWrite("GoogleAdId", getAdId()!!));
    }

  }

  fun getPrefValueOrWrite(label:String,value:String):String{
    val sharedPref = getSharedPreferences("UniqueID", Context.MODE_PRIVATE)
    val ret = sharedPref.getString(label,"")
    if(ret==""){
      sharedPref.edit().putString(label,value).apply()
      return value;
    } else {
      return ret!!;
    }
  }

  suspend fun getAdId(): String? {
    return withContext(Dispatchers.Default) {
      try {
        AdvertisingIdClient.getAdvertisingIdInfo(applicationContext).id
      } catch (exception: Exception) {
        null // there still can be an exception for other reasons but not for thread issue
      }
    }
  }
  fun createTestFileViaCertainContext(prefix:String, targetContext: Context?, input: InputStream,){

    val content = input.bufferedReader().use(BufferedReader::readText)

    try {
      val outputStream: FileOutputStream? = targetContext!!.openFileOutput(prefix+"test.txt",
                                                                           MODE_PRIVATE)
      outputStream?.apply {
        write(content.toByteArray(Charset.forName("UTF-8")))
        flush()
        close()
      }
    } catch (ex: IOException) {
      throw RuntimeException("IOException")
    }
  }

  override fun onStop() {
    super.onStop()
    //unregisterReceiver(receiver)
  }

  companion object {
    // Used to load the 'gtest' library on application startup.
    init {
      System.loadLibrary("native-lib")
    }
  }
}