package com.worldclock.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.not
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
    fun `should_navigate_to_meters_activity_from_menu`() {
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
    fun `should_display_add_meter_button`() {
        // Given
        navigateToMetersActivity()

        // Then
        onView(withId(R.id.fabAddMeter))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun `should_open_add_meter_dialog_when_fab_clicked`() {
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
    fun `should_display_meter_form_fields`() {
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
    fun `should_display_meter_type_radio_buttons`() {
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
    fun `should_display_dialog_buttons`() {
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
    fun `should_validate_required_fields`() {
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
    fun `should_close_dialog_when_cancel_clicked`() {
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