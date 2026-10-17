package com.twinspace.cloner

import com.twinspace.cloner.core.AppItem
import com.twinspace.cloner.domain.repository.ShortcutRepository
import com.twinspace.cloner.domain.usecase.PinShortcutUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutUseCaseTest {

    private class FakeShortcutRepository(
        var supported: Boolean = true
    ) : ShortcutRepository {
        val pinnedShortcuts = mutableListOf<String>()

        override fun isShortcutPinningSupported(): Boolean = supported

        override fun pinAppShortcut(app: AppItem, customName: String?, cloneIndex: Int): Boolean {
            if (!supported) return false
            pinnedShortcuts.add("${app.packageName}_$cloneIndex")
            return true
        }
    }

    @Test
    fun testPinShortcutWhenSupported() {
        val fakeRepo = FakeShortcutRepository(supported = true)
        val useCase = PinShortcutUseCase(fakeRepo)

        val app = AppItem(packageName = "com.whatsapp", appName = "WhatsApp", cloneIndex = 2)
        val result = useCase(app = app, customName = "WhatsApp Work", cloneIndex = 2)

        assertTrue(result)
        assertTrue(fakeRepo.pinnedShortcuts.contains("com.whatsapp_2"))
    }

    @Test
    fun testPinShortcutWhenNotSupported() {
        val fakeRepo = FakeShortcutRepository(supported = false)
        val useCase = PinShortcutUseCase(fakeRepo)

        val app = AppItem(packageName = "com.whatsapp", appName = "WhatsApp", cloneIndex = 1)
        val result = useCase(app = app)

        assertFalse(result)
        assertTrue(fakeRepo.pinnedShortcuts.isEmpty())
    }
}
