package com.example.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * DeviceAdminReceiver that prevents users from uninstalling the app
 * while alarms are active or to enforce anti-snooze cheat protection.
 */
class AntiUninstallAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Anti-uninstall Device Admin privilege enabled.")
        Toast.makeText(
            context,
            "Anti-Uninstall protection is now active.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Anti-uninstall Device Admin privilege disabled.")
        Toast.makeText(
            context,
            "Anti-Uninstall protection deactivated.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // If an alarm mission is currently ringing, block with high severity message
        return if (AntiCheatProtectionManager.isAlarmMissionActive.value) {
            "⚠️ CRITICAL: Wake-Up Mission is currently active! You CANNOT deactivate Device Admin or uninstall the app until you conquer the alarm challenge."
        } else {
            "Disabling anti-uninstall protection will allow the alarm app to be removed or force-stopped during active wake-up alarms."
        }
    }

    companion object {
        private const val TAG = "AntiUninstallAdmin"
    }
}
