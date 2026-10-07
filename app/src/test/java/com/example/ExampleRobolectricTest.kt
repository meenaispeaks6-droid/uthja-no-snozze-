package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.AlarmScheduler
import com.example.data.local.Converters
import com.example.data.model.DismissType
import com.example.data.model.WakeMode
import com.example.sleep.SleepTrackingData
import com.example.data.model.AlarmEntity
import com.example.data.model.SleepSessionEntity
import com.example.data.supabase.toSupabaseDto
import com.example.data.supabase.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun test_supabase_alarm_dto_conversion() {
    val alarm = AlarmEntity(
      id = 42L,
      title = "Supabase Test Alarm",
      hour = 7,
      minute = 30,
      repeatDays = "DAILY",
      isEnabled = true,
      wakeMode = WakeMode.BEAST,
      dismissType = DismissType.SHAKE
    )
    val dto = alarm.toSupabaseDto(userId = "user_123")
    assertEquals("Supabase Test Alarm", dto.title)
    assertEquals(7, dto.hour)
    assertEquals("BEAST", dto.wakeMode)
    assertEquals("SHAKE", dto.dismissType)
    assertEquals("user_123", dto.userId)

    val convertedEntity = dto.toEntity()
    assertEquals(alarm.title, convertedEntity.title)
    assertEquals(alarm.wakeMode, convertedEntity.wakeMode)
    assertEquals(alarm.dismissType, convertedEntity.dismissType)
  }

  @Test
  fun read_appName_from_context() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LUNE", appName)
  }

  @Test
  fun test_type_converters() {
    val converters = Converters()
    assertEquals("BEAST", converters.fromWakeMode(WakeMode.BEAST))
    assertEquals(WakeMode.BEAST, converters.toWakeMode("BEAST"))
    assertEquals(DismissType.SHAKE, converters.toDismissType("SHAKE"))
  }

  @Test
  fun test_alarm_scheduler_future_time() {
    val now = Calendar.getInstance()
    val nextTrigger = AlarmScheduler.calculateNextTriggerTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), "DAILY")
    assertTrue(nextTrigger >= now.timeInMillis)

    val remainingFormatted = AlarmScheduler.formatRemainingTime(System.currentTimeMillis() + 3_600_000L)
    assertTrue(remainingFormatted.contains("Rings in"))
  }

  @Test
  fun test_sleep_tracking_data_metrics() {
    // 7.5 hours = 450 minutes = 27,000 seconds
    val data = SleepTrackingData(
        isTracking = true,
        startedAt = System.currentTimeMillis() - 27_000_000L,
        elapsedSeconds = 27_000L,
        movementCount = 14,
        restlessSpikeCount = 4
    )

    assertEquals(450, data.durationMinutes)
    val score = data.calculateSleepScore(goalMinutes = 480)
    assertTrue("Sleep score should be high for 7.5h calm rest: $score", score >= 80)
    val rating = data.calculateMovementRating()
    assertTrue("Movement rating should be peaceful or calm: $rating", rating.contains("Peaceful") || rating.contains("Calm"))
    val deep = data.calculateDeepSleepMinutes()
    val light = data.calculateLightSleepMinutes()
    val restless = data.calculateRestlessMinutes()
    assertTrue("Total breakdown should be positive", deep > 0 && light > 0 && restless > 0)
    assertEquals(450, deep + light + restless)
  }
}
