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
class TariffsActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `should_navigate_to_tariffs_activity_from_menu`() {
        // When
        onView(withId(R.id.menuButton))
            .perform(click())
        onView(withText("Тарифы"))
            .perform(click())

        // Then
        onView(withId(R.id.recyclerViewTariffs))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_add_tariff_button`() {
        // Given
        navigateToTariffsActivity()

        // Then
        onView(withId(R.id.fabAddTariff))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun `should_open_add_tariff_dialog_when_fab_clicked`() {
        // Given
        navigateToTariffsActivity()

        // When
        onView(withId(R.id.fabAddTariff))
            .perform(click())

        // Then
        onView(withText("Добавить тариф"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_tariff_form_fields`() {
        // Given
        navigateToTariffsActivity()
        onView(withId(R.id.fabAddTariff))
            .perform(click())

        // Then
        onView(withId(R.id.autoCompleteMeter))
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextTariffRate))
            .check(matches(isDisplayed()))
        onView(withId(R.id.editTextStartDate))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_display_end_date_checkbox`() {
        // Given
        navigateToTariffsActivity()
        onView(withId(R.id.fabAddTariff))
            .perform(click())

        // Then
        onView(withId(R.id.checkboxEndDate))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_show_end_date_field_when_checkbox_checked`() {
        // Given
        navigateToTariffsActivity()
        onView(withId(R.id.fabAddTariff))
            .perform(click())

        // When
        onView(withId(R.id.checkboxEndDate))
            .perform(click())

        // Then
        onView(withId(R.id.editTextEndDate))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `should_hide_end_date_field_when_checkbox_unchecked`() {
        // Given
        navigateToTariffsActivity()
        onView(withId(R.id.fabAddTariff))
            .perform(click())
        onView(withId(R.id.checkboxEndDate))
            .perform(click())

        // When
        onView(withId(R.id.checkboxEndDate))
            .perform(click())

        // Then
        onView(withId(R.id.editTextEndDate))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun `should_validate_required_fields`() {
        // Given
        navigateToTariffsActivity()
        onView(withId(R.id.fabAddTariff))
            .perform(click())

        // When
        onView(withId(R.id.buttonSave))
            .perform(click())

        // Then
        onView(withId(R.id.autoCompleteMeter))
            .check(matches(hasErrorText("Выберите прибор учета")))
        onView(withId(R.id.editTextTariffRate))
            .check(matches(hasErrorText("Введите тариф")))
        onView(withId(R.id.editTextStartDate))
            .check(matches(hasErrorText("Выберите дату начала")))
    }

    @Test
    fun `should_close_dialog_when_cancel_clicked`() {
        // Given
        navigateToTariffsActivity()
        onView(withId(R.id.fabAddTariff))
            .perform(click())

        // When
        onView(withId(R.id.buttonCancel))
            .perform(click())

        // Then
        onView(withText("Добавить тариф"))
            .check(matches(not(isDisplayed())))
    }

    private fun navigateToTariffsActivity() {
        onView(withId(R.id.menuButton))
            .perform(click())
        onView(withText("Тарифы"))
            .perform(click())
    }
}