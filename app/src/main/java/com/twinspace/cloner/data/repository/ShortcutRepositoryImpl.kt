package com.twinspace.cloner.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.twinspace.cloner.MainActivity
import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.domain.repository.ShortcutRepository

class ShortcutRepositoryImpl(
    private val context: Context
) : ShortcutRepository {

    override fun isShortcutPinningSupported(): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context)
    }

    override fun pinAppShortcut(app: AppItem, customName: String?, cloneIndex: Int): Boolean {
        if (!isShortcutPinningSupported()) return false

        val label = customName?.takeIf { it.isNotBlank() } ?: "${app.appName} #$cloneIndex"
        val shortcutId = "twinspace_shortcut_${app.packageName}_$cloneIndex"

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("KEY_LAUNCH_PACKAGE", app.packageName)
            putExtra("KEY_LAUNCH_CLONE_INDEX", cloneIndex)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val iconBitmap = createBadgedIcon(app, cloneIndex)
        val iconCompat = if (iconBitmap != null) {
            IconCompat.createWithBitmap(iconBitmap)
        } else {
            IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(iconCompat)
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun createBadgedIcon(app: AppItem, cloneIndex: Int): Bitmap? {
        val drawable = app.icon ?: return null
        val size = 192
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)

        // Draw stylish badge in bottom-right corner
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 38f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val badgeRadius = 34f
        val cx = size - badgeRadius - 8f
        val cy = size - badgeRadius - 8f

        // Badge border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(cx, cy, badgeRadius, borderPaint)
        canvas.drawCircle(cx, cy, badgeRadius, badgePaint)

        val textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText("$cloneIndex", cx, textY, textPaint)

        return bitmap
    }
}
