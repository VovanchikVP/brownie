package com.worldclock.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class SimpleAndroidTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.worldclock.app", appContext.packageName)
    }

    @Test
    fun basicMathTest() {
        // Simple test to verify Android test environment works
        assertEquals(4, 2 + 2)
        assertTrue(5 > 3)
        assertFalse(1 > 2)
    }
}