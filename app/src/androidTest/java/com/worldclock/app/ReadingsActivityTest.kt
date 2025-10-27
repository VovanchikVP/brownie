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
class ReadingsActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `should_navigate_to_readings_activity_from_menu`() {
        // When
        onView(withId(R.id.menuButton))
            .perform(click())
        onView(withText("Показания"))
            .perform(click())

        // Then
        onView(withId(R.id.recyclerViewReadings))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_add_reading_button`() {
        // Given
        navigateToReadingsActivity()

        // Then
        onView(withId(R.id.fabAddReading))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun `should_open_add_reading_dialog_when_fab_clicked`() {
        // Given
        navigateToReadingsActivity()

        // When
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // Then
        onView(withText("Добавить показание"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_reading_form_fields`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // Then
        onView(withId(R.id.editTextAddressFilter))
            .check(matches(isDisplayed()))
        onView(withId(R.id.autoCompleteMeter))
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextReadingValue))
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextReadingDate))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_address_filter_field`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // Then
        onView(withId(R.id.editTextAddressFilter))
            .check(matches(isDisplayed()))
        onView(withText("Фильтр по адресу"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_meter_selection_field`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // Then
        onView(withId(R.id.autoCompleteMeter))
            .check(matches(isDisplayed()))
        onView(withText("Прибор учета"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_reading_value_field`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // Then
        onView(withId(R.id.editTextReadingValue))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_reading_date_field`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // Then
        onView(withId(R.id.editTextReadingDate))
            .check(matches(isDisplayed()))
        onView(withText("Дата показания"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_validate_required_fields`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // When
        onView(withId(R.id.buttonSave))
            .perform(click())

        // Then
        onView(withId(R.id.autoCompleteMeter))
            .check(matches(hasErrorText("Выберите прибор учета")))
        onView(withId(R.id.editTextReadingValue))
            .check(matches(hasErrorText("Введите значение показания")))
        onView(withId(R.id.editTextReadingDate))
            .check(matches(hasErrorText("Выберите дату и время")))
    }

    @Test
    fun `should_close_dialog_when_cancel_clicked`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // When
        onView(withId(R.id.buttonCancel))
            .perform(click())

        // Then
        onView(withText("Добавить показание"))
            .check(matches(not(isDisplayed())))
    }

    private fun navigateToReadingsActivity() {
        onView(withId(R.id.menuButton))
            .perform(click())
        onView(withText("Показания"))
            .perform(click())
    }
}