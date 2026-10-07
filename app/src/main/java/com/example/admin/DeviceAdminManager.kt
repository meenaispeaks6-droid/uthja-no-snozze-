package com.example.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Helper class to manage Device Administrator operations for anti-uninstall protection.
 */
class DeviceAdminManager(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager? =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

    val adminComponent = ComponentName(context, AntiUninstallAdminReceiver::class.java)

    /**
     * Checks if the app currently possesses active Device Administrator rights.
     */
    fun isAntiUninstallActive(): Boolean {
        return try {
            devicePolicyManager?.isAdminActive(adminComponent) == true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin status", e)
            false
        }
    }

    /**
     * Constructs an Intent to request Device Admin activation from the user.
     */
    fun createActivationIntent(
        explanation: String = "Prevents deleting or stopping the alarm app to bypass morning wake-up challenges."
    ): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)
        }
    }

    /**
     * Programmatically deactivates Device Administrator privileges.
     * Should be called when the user voluntarily turns off the setting in the app.
     */
    fun deactivateAdmin(): Boolean {
        return try {
            if (isAntiUninstallActive()) {
                devicePolicyManager?.removeActiveAdmin(adminComponent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove active admin", e)
            false
        }
    }

    companion object {
        private const val TAG = "DeviceAdminManager"
    }
}
