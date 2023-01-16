package com.example.adamsample

import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.malinskiy.adam.junit4.android.rule.Mode
import com.malinskiy.adam.junit4.rule.AdbRule
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 */
@RunWith(AndroidJUnit4::class)
class NIAPSECEncryptionTest {
  @get:Rule
  val rule = ActivityScenarioRule(MainActivity::class.java)

  @get:Rule
  val adbRule = AdbRule(mode = Mode.ASSERT)

  @Before
  fun setup()
  {

  }

  //To Connect host to device in google network, we should see this instruction
  //https://g3doc.corp.google.com/company/teams/android/wfh/adb/remote_device_proxy.md?cl=head
  //ssh -R 5037:127.0.0.1:5037 hostname.locale.corp.google.com

  @Test
  fun testEncryptedFile() {
    runBlocking {
      val result = adbRule.adb.execute(ShellCommandRequest("echo \"hello world\""))
      assert(result.exitCode == 0)
      assert(result.output.startsWith("hello world"))
      Log.d("TAG", result.output)
    }
  }
}