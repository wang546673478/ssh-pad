package com.sshpad.app.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sshpad.app.presentation.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Connection List Screen Compose UI Tests
 * Week 7: UI Test Coverage
 */
@RunWith(AndroidJUnit4::class)
class ConnectionListScreenComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun connectionListScreen_displaysTitle() {
        composeTestRule.onNodeWithText("SSH Connections").assertIsDisplayed()
    }

    @Test
    fun connectionListScreen_displaysAddButton() {
        composeTestRule.onNodeWithContentDescription("Add Connection").assertIsDisplayed()
    }

    @Test
    fun connectionListScreen_displaysEmptyState() {
        composeTestRule.onNodeWithText("No connections yet").assertIsDisplayed()
    }
}
