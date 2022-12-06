package com.example.adamsample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

class BootReceiver: BroadcastReceiver() {
  override fun onReceive(appContext: Context?, intent: Intent?) {
    Log.d("DirectBoot","received!"+intent?.action.toString())
    val intentName = intent?.action.toString();
    if(intentName == "android.intent.action.LOCKED_BOOT_COMPLETED"){
      //Check Device Context Encrypted Storage (DES)

      //Check we can read the file in des and can not read in ces

    } else if(intentName == "android.intent.action.BOOT_COMPLETED"){
      //Check Credential Encrypted Storage (CES)
    } else if(intentName == "android.intent.action.MY_TEST_DES"){
      Log.d("BootReceiver", "Let's create a file into the DES.")
      val desContext: Context = appContext!!.createDeviceProtectedStorageContext();// Access appDataFilename that lives in device encrypted storage
      val isLoremIpsum: InputStream = appContext.resources.openRawResource(
        appContext.resources.getIdentifier("loremipsum",
                                           "raw", appContext.packageName));

      createTestFileViaCertainContext("des_",desContext,isLoremIpsum)
      createTestFileViaCertainContext("ces_",appContext,isLoremIpsum)

      Log.d("test",desContext?.filesDir!!.absolutePath)

    }
  }
  fun createTestFileViaCertainContext(prefix:String,targetContext: Context?,input:InputStream,){

    val content = input.bufferedReader().use(BufferedReader::readText)

    try {
      val outputStream: FileOutputStream? = targetContext!!.openFileOutput(prefix+"test.txt",MODE_PRIVATE)
      outputStream?.apply {
        write(content.toByteArray(Charset.forName("UTF-8")))
        flush()
        close()
      }
    } catch (ex: IOException) {
      throw RuntimeException("IOException")
    }
  }
}