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
class ReadingsActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `should navigate to readings activity from menu`() {
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
    fun `should display add reading button`() {
        // Given
        navigateToReadingsActivity()

        // Then
        onView(withId(R.id.fabAddReading))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun `should open add reading dialog when fab clicked`() {
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
    fun `should display reading form fields`() {
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
    fun `should display address filter field`() {
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
    fun `should display meter selection field`() {
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
    fun `should display reading value field`() {
        // Given
        navigateToReadingsActivity()
        onView(withId(R.id.fabAddReading))
            .perform(click())

        // Then
        onView(withId(R.id.editTextReadingValue))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should display reading date field`() {
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
    fun `should validate required fields`() {
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
    fun `should close dialog when cancel clicked`() {
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