package com.sshpad.app.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sshpad.app.presentation.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Terminal Screen Compose UI Tests
 * Week 7: UI Test Coverage
 */
@RunWith(AndroidJUnit4::class)
class TerminalScreenComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun terminalScreen_displaysConnectionName() {
        composeTestRule.onNodeWithText("SSH Pad").assertIsDisplayed()
    }

    @Test
    fun terminalScreen_displaysDisconnectButton() {
        composeTestRule.onNodeWithContentDescription("Disconnect").assertIsDisplayed()
    }

    @Test
    fun terminalScreen_displaysMoreMenu() {
        composeTestRule.onNodeWithContentDescription("More").assertIsDisplayed()
    }

    @Test
    fun terminalScreen_menuOpensDropdown() {
        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Zoom In").assertIsDisplayed()
    }
}
