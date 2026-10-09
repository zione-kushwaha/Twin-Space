package com.twinspace.cloner.core

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class CloneRecord(
    val id: String, // e.g. "com.whatsapp_1", "com.whatsapp_2"
    val packageName: String,
    val cloneIndex: Int, // 1 for 1st clone (badge 2), 2 for 2nd clone (badge 3), etc.
    val customName: String = "",
    val badgeText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("twinspace_prefs", Context.MODE_PRIVATE)

    var isDisguiseModeEnabled: Boolean
        get() = prefs.getBoolean("disguise_mode", false)
        set(value) = prefs.edit().putBoolean("disguise_mode", value).apply()

    var secretPin: String
        get() = prefs.getString("secret_pin", "1234") ?: "1234"
        set(value) = prefs.edit().putString("secret_pin", value).apply()

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean("app_lock_enabled", false)
        set(value) = prefs.edit().putBoolean("app_lock_enabled", value).apply()

    // Package-level tracking
    fun getClonedPackages(): Set<String> {
        val raw = prefs.getStringSet("cloned_packages", null) ?: return emptySet()
        return HashSet(raw)
    }

    fun addClonedPackage(pkg: String) {
        val current = HashSet(getClonedPackages())
        current.add(pkg)
        prefs.edit().putStringSet("cloned_packages", current).commit()
    }

    fun removeClonedPackage(pkg: String) {
        val current = HashSet(getClonedPackages())
        current.remove(pkg)
        prefs.edit().putStringSet("cloned_packages", current).commit()
    }

    // Multi-instance clone records
    fun getCloneRecords(): List<CloneRecord> {
        val jsonStr = prefs.getString("cloned_instances_json", null) ?: return emptyList()
        val list = mutableListOf<CloneRecord>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CloneRecord(
                        id = obj.optString("id"),
                        packageName = obj.optString("packageName"),
                        cloneIndex = obj.optInt("cloneIndex", 1),
                        customName = obj.optString("customName", ""),
                        badgeText = obj.optString("badgeText", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}

        // Deduplicate only if identical record ID was saved multiple times
        val seen = mutableSetOf<String>()
        val deduplicated = mutableListOf<CloneRecord>()
        for (r in list) {
            val key = if (r.id.isNotBlank()) r.id else "${r.packageName}_${r.cloneIndex}"
            if (seen.add(key)) {
                deduplicated.add(r)
            }
        }

        if (deduplicated.size != list.size) {
            saveCloneRecords(deduplicated)
        }

        return deduplicated
    }

    fun saveCloneRecords(records: List<CloneRecord>) {
        val arr = JSONArray()
        for (r in records) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("packageName", r.packageName)
                put("cloneIndex", r.cloneIndex)
                put("customName", r.customName)
                put("badgeText", r.badgeText)
                put("timestamp", r.timestamp)
            }
            arr.put(obj)
        }
        prefs.edit().putString("cloned_instances_json", arr.toString()).commit()
    }

    fun addCloneRecord(record: CloneRecord) {
        val current = getCloneRecords().toMutableList()
        current.removeAll { it.id == record.id }
        current.add(record)
        saveCloneRecords(current)
        addClonedPackage(record.packageName)
    }

    fun removeCloneRecord(id: String) {
        val current = getCloneRecords().toMutableList()
        val target = current.find { it.id == id }
        current.removeAll { it.id == id }
        saveCloneRecords(current)

        if (target != null) {
            val remainingForPkg = current.count { it.packageName == target.packageName }
            if (remainingForPkg == 0) {
                removeClonedPackage(target.packageName)
            }
        }
    }

    fun removeAllClones() {
        prefs.edit()
            .remove("cloned_instances_json")
            .remove("cloned_packages")
            .putBoolean("has_initialized_defaults", true)
            .commit()
    }

    fun getCloneCount(pkg: String): Int {
        val inRecords = getCloneRecords().count { it.packageName == pkg }
        if (inRecords > 0) return inRecords
        return if (getClonedPackages().contains(pkg)) 1 else 0
    }

    fun isAppCloned(pkg: String): Boolean {
        return getCloneCount(pkg) > 0 || getClonedPackages().contains(pkg)
    }

    var isStealthNotificationEnabled: Boolean
        get() = prefs.getBoolean("stealth_notifications", false)
        set(value) = prefs.edit().putBoolean("stealth_notifications", value).apply()

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean("has_completed_onboarding", true)
        set(value) = prefs.edit().putBoolean("has_completed_onboarding", value).apply()

    var hasInitializedDefaults: Boolean
        get() = prefs.getBoolean("has_initialized_defaults", false)
        set(value) = prefs.edit().putBoolean("has_initialized_defaults", value).apply()

    var hasCleanedDefaultSeeds: Boolean
        get() = prefs.getBoolean("has_cleaned_default_seeds", false)
        set(value) = prefs.edit().putBoolean("has_cleaned_default_seeds", value).apply()
}
