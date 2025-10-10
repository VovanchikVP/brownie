package com.worldclock.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MetersActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `should navigate to meters activity from menu`() {
        // When
        onView(withId(R.id.menuButton))
            .perform(click())
        onView(withText("Приборы учета"))
            .perform(click())

        // Then
        onView(withId(R.id.recyclerViewMeters))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should display add meter button`() {
        // Given
        navigateToMetersActivity()

        // Then
        onView(withId(R.id.fabAddMeter))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun `should open add meter dialog when fab clicked`() {
        // Given
        navigateToMetersActivity()

        // When
        onView(withId(R.id.fabAddMeter))
            .perform(click())

        // Then
        onView(withText("Добавить прибор учета"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should display meter form fields`() {
        // Given
        navigateToMetersActivity()
        onView(withId(R.id.fabAddMeter))
            .perform(click())

        // Then
        onView(withId(R.id.editTextMeterNumber))
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextMeterAddress))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should display meter type radio buttons`() {
        // Given
        navigateToMetersActivity()
        onView(withId(R.id.fabAddMeter))
            .perform(click())

        // Then
        onView(withId(R.id.radioElectricity))
            .check(matches(isDisplayed()))
        onView(withId(R.id.radioGas))
            .check(matches(isDisplayed()))
        onView(withId(R.id.radioHotWater))
            .check(matches(isDisplayed()))
        onView(withId(R.id.radioColdWater))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should display dialog buttons`() {
        // Given
        navigateToMetersActivity()
        onView(withId(R.id.fabAddMeter))
            .perform(click())

        // Then
        onView(withId(R.id.buttonCancel))
            .check(matches(isDisplayed()))
        onView(withId(R.id.buttonSave))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should validate required fields`() {
        // Given
        navigateToMetersActivity()
        onView(withId(R.id.fabAddMeter))
            .perform(click())

        // When
        onView(withId(R.id.buttonSave))
            .perform(click())

        // Then
        onView(withId(R.id.editTextMeterNumber))
            .check(matches(hasErrorText("Введите номер прибора")))
        onView(withId(R.id.editTextMeterAddress))
            .check(matches(hasErrorText("Введите адрес установки")))
    }

    @Test
    fun `should close dialog when cancel clicked`() {
        // Given
        navigateToMetersActivity()
        onView(withId(R.id.fabAddMeter))
            .perform(click())

        // When
        onView(withId(R.id.buttonCancel))
            .perform(click())

        // Then
        onView(withText("Добавить прибор учета"))
            .check(matches(not(isDisplayed())))
    }

    private fun navigateToMetersActivity() {
        onView(withId(R.id.menuButton))
            .perform(click())
        onView(withText("Приборы учета"))
            .perform(click())
    }
}