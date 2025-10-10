package com.worldclock.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `app name should be displayed`() {
        // Then
        onView(withId(R.id.titleText))
            .check(matches(isDisplayed()))
            .check(matches(withText("Домовой")))
    }

    @Test
    fun `menu button should be displayed and clickable`() {
        // Then
        onView(withId(R.id.menuButton))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun `menu button should open menu dialog`() {
        // When
        onView(withId(R.id.menuButton))
            .perform(click())

        // Then
        onView(withText("Выберите раздел"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `menu should contain all sections`() {
        // When
        onView(withId(R.id.menuButton))
            .perform(click())

        // Then
        onView(withText("Приборы учета"))
            .check(matches(isDisplayed()))
        onView(withText("Тарифы"))
            .check(matches(isDisplayed()))
        onView(withText("Показания"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `costs recycler view should be displayed`() {
        // Then
        onView(withId(R.id.recyclerViewCosts))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `empty state should be displayed when no data`() {
        // Then
        onView(withId(R.id.emptyStateText))
            .check(matches(isDisplayed()))
    }
}