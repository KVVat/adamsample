package com.example.adamsample

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.malinskiy.adam.junit4.android.rule.Mode
import com.malinskiy.adam.junit4.android.rule.sandbox.SingleTargetAndroidDebugBridgeClient
import com.malinskiy.adam.junit4.rule.AdbRule
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


private const val BASIC_SAMPLE_PACKAGE = "com.example.android.testing.uiautomator.BasicSample"
private const val LONG_TIMEOUT = 5000L
private const val SHORT_TIMEOUT = 1000L
private const val PIN = 1234
private const val PASSWORD = "aaaa"
private const val STRING_TO_BE_TYPED = "UiAutomator"

/**
 *
 */
@RunWith(AndroidJUnit4::class)
class UIAutomatorTest {

  @get:Rule
  val adbRule = AdbRule(mode = Mode.ASSERT)
  lateinit var client:SingleTargetAndroidDebugBridgeClient;

  private lateinit var mDevice: UiDevice
  private var mContext: Context? = null

  @Before
  fun setUp() {
    val mDevice_ = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    mDevice = mDevice_!!;

    mContext = InstrumentationRegistry.getInstrumentation().context;
    mDevice.freezeRotation();
    sleepAndWakeUpDevice()
    client = adbRule.adb;
  }

  @After
  fun tearDown() {
    //mDevice.pressHome()
    mDevice.unfreezeRotation()
    //mDevice.waitForIdle()
  }


  @Test
  fun unlockScreen() {
    assert(isLockScreenEnbled())
    runBlocking {
      sleepAndWakeUpDevice()
      mDevice.waitForIdle()
      Thread.sleep(1000);
      swipeUp()
      client.execute(ShellCommandRequest("input text 0413"))
      Thread.sleep(1000);
      mDevice.pressEnter()
      Thread.sleep(1000);
    }
  }

  // @Test
  // fun navigateToScreenLock() {
  //   launchSettings(Settings.ACTION_SECURITY_SETTINGS);
  //   mDevice.wait(Until.findObject(By.text("Screen lock")), 5000).click();
  // }

  fun launchSettings(page:String){
    val intent = Intent(page)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    mContext!!.startActivity(intent)
    Thread.sleep(LONG_TIMEOUT * 2)
  }
  fun isLockScreenEnbled():Boolean{
    val km = mContext!!.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return km.isKeyguardSecure
  }
  fun sleepAndWakeUpDevice() {
    mDevice.sleep()
    Thread.sleep(1000)
    mDevice.wakeUp()
  }
  fun swipeUp(){
    mDevice.swipe(mDevice.getDisplayWidth() / 2, mDevice.getDisplayHeight(),
                  mDevice.getDisplayWidth() / 2, 0, 30);
    Thread.sleep(SHORT_TIMEOUT);
  }
}