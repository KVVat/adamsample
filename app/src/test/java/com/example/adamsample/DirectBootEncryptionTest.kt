package com.example.adamsample

import com.example.adamsample.rule.AdbDeviceRule
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DirectBootEncryptionTest {
  @Rule
  @JvmField
  val adb = AdbDeviceRule()
  val client = adb.adb
  @Before
  fun setup() {
    runBlocking {
    }
  }

  @Test
  fun testDeviceEncryptedStorage()
  {
    runBlocking {
      //send intent to save a file in the DES
      //$ adb shell am start -a andorid.intent.action.MY_TEST_DES -p com.example.adamsample
      client.execute(ShellCommandRequest(
        "am broadcast -a android.intent.action.MY_TEST_DES"),
                     adb.deviceSerial)
      //reboot
      //check adb logcat line to take the test result in the apk module
    }
  }
}