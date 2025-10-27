package com.worldclock.app.tutorial

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class TutorialManagerTest {

    @Mock
    private lateinit var mockActivity: Activity

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var tutorialManager: TutorialManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Настраиваем моки
        `when`(mockActivity.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        doNothing().`when`(mockEditor).apply()
    }

    @Test
    fun `isTutorialCompleted should return false by default`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("tutorial_completed", false)).thenReturn(false)
        tutorialManager = TutorialManager(mockActivity)

        // When
        val result = tutorialManager.isTutorialCompleted()

        // Then
        assert(!result)
    }

    @Test
    fun `isTutorialCompleted should return true when tutorial is completed`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("tutorial_completed", false)).thenReturn(true)
        tutorialManager = TutorialManager(mockActivity)

        // When
        val result = tutorialManager.isTutorialCompleted()

        // Then
        assert(result)
    }

    @Test
    fun `completeTutorial should set tutorial as completed`() {
        // Given
        tutorialManager = TutorialManager(mockActivity)

        // When
        tutorialManager.completeTutorial()

        // Then
        verify(mockEditor).putBoolean("tutorial_completed", true)
        verify(mockEditor).putInt("tutorial_step", 0)
        verify(mockEditor).apply()
    }

    @Test
    fun `skipTutorial should complete tutorial`() {
        // Given
        tutorialManager = TutorialManager(mockActivity)

        // When
        tutorialManager.skipTutorial()

        // Then
        verify(mockEditor).putBoolean("tutorial_completed", true)
        verify(mockEditor).putInt("tutorial_step", 0)
        verify(mockEditor).apply()
    }

    @Test
    fun `tutorial steps should have correct constants`() {
        // Given & When & Then
        assert(TutorialManager.STEP_WELCOME == 0)
        assert(TutorialManager.STEP_ADD_METER == 1)
        assert(TutorialManager.STEP_ADD_TARIFF == 2)
        assert(TutorialManager.STEP_ADD_READINGS == 3)
        assert(TutorialManager.STEP_VIEW_MAIN == 4)
        assert(TutorialManager.STEP_CLICK_ADDRESS == 5)
        assert(TutorialManager.STEP_CLICK_METER == 6)
        assert(TutorialManager.STEP_ADD_READING_FROM_COSTS == 7)
        assert(TutorialManager.STEP_COMPLETE == 8)
    }
}
