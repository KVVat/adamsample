package com.example.adamsample


import assertk.assertThat
import assertk.assertions.startsWith
import com.example.adamsample.rule.AdbDeviceRule
import com.malinskiy.adam.request.pkg.InstallRemotePackageRequest
import com.malinskiy.adam.request.pkg.UninstallRemotePackageRequest
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import com.malinskiy.adam.request.shell.v1.ShellCommandResult
import com.malinskiy.adam.request.sync.v1.PushFileRequest
import java.io.File
import java.nio.file.Paths
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UniqueIDUnitTest {

  @Rule
  @JvmField
  val adb = AdbDeviceRule()
  val client = adb.adb

  @Before
  fun setup() {
    runBlocking {
      client.execute(UninstallRemotePackageRequest("com.example.adamsample"), adb.deviceSerial)
      client.execute(ShellCommandRequest("rm /data/local/tmp/appupdate-v2-debug.apk"),
                     adb.deviceSerial)
    }
  }

  @After
  fun teardown() {
    runBlocking {
      client.execute(UninstallRemotePackageRequest("com.example.adamsample"), adb.deviceSerial)
      client.execute(ShellCommandRequest("rm /data/local/tmp/appupdate-v2-debug.apk"),
                     adb.deviceSerial)
    }
  }

  @Test
  fun adamShellCommandSample() {
    runBlocking {
      val response = client.execute(ShellCommandRequest("echo hello"), adb.deviceSerial)
      println(response.output);
    }
  }

  fun preparerInstall(apkFile: File, reinstall: Boolean = false): ShellCommandResult {
    var stdio: ShellCommandResult
    runBlocking {
      val fileName = apkFile.name
      val channel = client.execute(PushFileRequest(apkFile, "/data/local/tmp/$fileName"),
                                   GlobalScope,
                                   serial = adb.deviceSerial);
      while (!channel.isClosedForReceive) {
        val progress: Double? =
          channel.tryReceive().onClosed {
          }.getOrNull()
      }
      stdio = client.execute(InstallRemotePackageRequest(
        "/data/local/tmp/$fileName", reinstall), serial = adb.deviceSerial)
    }
    return stdio;
  }

  //@TestInformation(SFR="FDP_ACF_EXT.1/AppUpadate")
  @Test
  fun accessControlExt1_appUpdate_TestNormal() {
    //A test for FDP_ACF_EXT.1/AppUpdate
    //UserDataProtectionTest.accessControlExt1_appUpdate_TestNormal

    runBlocking {
      //
      val file_apk_v1_debug: File =
        File(Paths.get("src", "test", "resources", "appupdate-v1-debug.apk").toUri());
      val file_apk_v2_debug: File =
        File(Paths.get("src", "test", "resources", "appupdate-v2-debug.apk").toUri());
      val file_apk_v2_signed: File =
        File(Paths.get("src", "test", "resources", "appupdate-v2-signed.apk").toUri());

      var res = preparerInstall(file_apk_v1_debug, false);
      assertThat(res.output).startsWith("Success")

      res = preparerInstall(file_apk_v2_debug);
      assertThat(res.output).startsWith("Success")

      //degrade
      res = preparerInstall(file_apk_v1_debug, false);
      assertThat(res.output).startsWith("Failure")

      //unistall the test file before next test
      client.execute(UninstallRemotePackageRequest("com.example.adamsapmle"), adb.deviceSerial)
    }
  }
}