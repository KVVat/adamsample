package com.example.adamsample

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.malinskiy.adam.junit4.android.rule.Mode
import com.malinskiy.adam.junit4.rule.AdbRule
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class EncryptedFileTest {

  @get:Rule
  val rule = ActivityScenarioRule(MainActivity::class.java)
  @get:Rule
  val adbRule = AdbRule(mode = Mode.ASSERT)

  //lateinit var client:AndroidDebugBridgeClient;
  private lateinit var norm_enc_data: SharedPreferences
  //private lateinit var norm_editor: SharedPreferences.Editor

  val PREF_NAME = "EncryptedSharedPref"
  lateinit var appContext:Context;
  lateinit var masterKeyAlias:String;

  @Before
  fun setup()
  {
    appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    //
    norm_enc_data = EncryptedSharedPreferences
      .create(
        PREF_NAME,
        masterKeyAlias,
        appContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
      )

    val fdelete: File = File(appContext.getFilesDir(), "my_sensitive_loremipsum.txt")
    if (fdelete.exists()) {fdelete.delete()}
  }

  fun userEncryptedKey(){
    try {
      val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
      keyStore.load(null)
      val keyGenerator: KeyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

      // Set the alias of the entry in Android KeyStore where the key will appear
      // and the constrains (purposes) in the constructor of the Builder
      keyGenerator.init(KeyGenParameterSpec.Builder(KEY_NAME,
                                                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                          .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                          .setUserAuthenticationRequired(true) // Require that the user has unlocked in the last 30 seconds
                          .setUserAuthenticationValidityDurationSeconds(30)
                          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                          .build())
      keyGenerator.generateKey()
    } catch (e: NoSuchAlgorithmException) {
      throw java.lang.RuntimeException("Failed to create a symmetric key", e)
    } catch (e: NoSuchProviderException) {
      throw java.lang.RuntimeException("Failed to create a symmetric key", e)
    } catch (e: InvalidAlgorithmParameterException) {
      throw java.lang.RuntimeException("Failed to create a symmetric key", e)
    } catch (e: KeyStoreException) {
      throw java.lang.RuntimeException("Failed to create a symmetric key", e)
    } catch (e: CertificateException) {
      throw java.lang.RuntimeException("Failed to create a symmetric key", e)
    } catch (e: IOException) {
      throw java.lang.RuntimeException("Failed to create a symmetric key", e)
    }
  }

  @Test
  fun testEncryptedSharedPreference(){
    checkEncryptedSharedPreference(norm_enc_data)
  }

  fun checkEncryptedSharedPreference(data:SharedPreferences)
  {
    val sampleString = "The quick brown fox jumps over the lazy dog";

    var editor: SharedPreferences.Editor = data.edit();

    editor.putInt("IntTest", 65535);
    editor.putBoolean("BooleanTest", true);
    editor.putString("StringTest", sampleString);
    editor.apply()
    //check availability

    val intSaved = data.getInt("IntTest", 1)
    assertEquals(65535,intSaved)
    val boolSaved = data.getBoolean("BooleanTest", false)
    assertEquals(true,boolSaved)
    val strSaved = data.getString("StringTest", "")
    assertEquals(sampleString,strSaved)
    //
    loadSharedPrefs(PREF_NAME);
  }

  @Test
  fun testEncryptedFile() {
    val fileToWrite = "my_sensitive_loremipsum.txt"

    val isLoremIpsum: InputStream = appContext.resources.openRawResource(
      appContext.resources.getIdentifier("loremipsum",
                                         "raw", appContext.packageName));
    val content = isLoremIpsum.bufferedReader().use(BufferedReader::readText)

    val fTarget: File = File(appContext.getFilesDir(), fileToWrite)

    val encryptedFile = EncryptedFile.Builder(
      fTarget,
      appContext,
      masterKeyAlias,
      EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()

    //Write loaded file with EncryptedFile class
    try {
      val outputStream: FileOutputStream? = encryptedFile.openFileOutput()
      outputStream?.apply {
        write(content.toByteArray(Charset.forName("UTF-8")))
        flush()
        close()
      }

    } catch (ex: IOException) {
      throw RuntimeException("IOException")
    }
    //Check Availability
    assert(fTarget.exists())
    val original:String;
    encryptedFile.openFileInput().use { fileInputStream ->
      try {
        val sb = StringBuilder()
        val br = BufferedReader(InputStreamReader(fileInputStream) as Reader?)
        br.readLine()
          .forEach {
            sb.append(it)
          }
        br.close()
        original = sb.toString()
        Log.d("fileContents", original)

      } catch (ex: Exception) {
        // Error occurred opening raw file for reading.
        throw RuntimeException("IOException")
      } finally {
        fileInputStream.close()
      }
    }
    //Check the file is encrypted (Read the file with BufferedReader)
    val fTargetStream = FileInputStream(fTarget);
    try {
      val sb = StringBuilder()
      val br = BufferedReader(InputStreamReader(fTargetStream) as Reader?)
      br.readLine()
        .forEach {
          sb.append(it)
        }
      br.close()
      val encrypted = sb.toString();
      Log.d("fileContents",encrypted)

      assertNotEquals(original,encrypted)
    } catch (ex: Exception) {
      // Error occurred opening raw file for reading.
      throw RuntimeException("IOException")
    } finally {
      fTargetStream.close();
    }
  }

  fun loadSharedPrefs(vararg prefs: String?) {
    // Logging messages left in to view Shared Preferences. I filter out all logs except for ERROR; hence why I am printing error messages.
    for (pref_name in prefs) {
      val preference: SharedPreferences = appContext.getSharedPreferences(pref_name, MODE_PRIVATE)
      for (key in preference.all.keys) {
        Log.i(javaClass.name,String.format("Shared Preference : %s - %s", pref_name, key)+
              preference.getString(key, "error")!!)
      }
    }
  }
}