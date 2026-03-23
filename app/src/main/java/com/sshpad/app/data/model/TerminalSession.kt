package com.sshpad.app.data.model

/**
 * Terminal session state
 */
data class TerminalSession(
    val connectionId: String,
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Terminal",
    val connectionName: String = "",
    val connectionHost: String = "",
    val width: Int = 80,
    val height: Int = 24,
    val fontSize: Float = 14f,
    val theme: TerminalTheme = TerminalTheme.DARK,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Terminal color themes
     */
    enum class TerminalTheme {
        DARK,
        LIGHT,
        SOLARIZED_DARK,
        SOLARIZED_LIGHT,
        MONOKAI
    }
}

/**
 * Tab state for multi-tab session management
 * Week 8: Multi-tab Support
 */
data class TabSession(
    val tabId: String = java.util.UUID.randomUUID().toString(),
    val terminalSession: TerminalSession,
    val isConnected: Boolean = true,
    val hasUnreadOutput: Boolean = false
)

/**
 * Tab manager state for managing multiple terminal tabs
 */
data class TabManagerState(
    val tabs: List<TabSession> = emptyList(),
    val activeTabId: String? = null,
    val showTabStrip: Boolean = true
) {
    val activeTab: TabSession? get() = tabs.find { it.tabId == activeTabId }
    val activeTabIndex: Int get() = tabs.indexOfFirst { it.tabId == activeTabId }
    
    fun addTab(tab: TabSession): TabManagerState {
        return copy(
            tabs = tabs + tab,
            activeTabId = tab.tabId
        )
    }
    
    fun removeTab(tabId: String): TabManagerState {
        val newTabs = tabs.filterNot { it.tabId == tabId }
        return copy(
            tabs = newTabs,
            activeTabId = if (activeTabId == tabId) newTabs.lastOrNull()?.tabId else activeTabId
        )
    }
    
    fun activateTab(tabId: String): TabManagerState {
        return copy(
            activeTabId = tabId,
            tabs = tabs.map { 
                if (it.tabId == tabId) it.copy(hasUnreadOutput = false) else it 
            }
        )
    }
    
    fun nextTab(): TabManagerState {
        if (tabs.isEmpty()) return this
        val currentIndex = activeTabIndex
        val nextIndex = (currentIndex + 1) % tabs.size
        return activateTab(tabs[nextIndex].tabId)
    }
    
    fun previousTab(): TabManagerState {
        if (tabs.isEmpty()) return this
        val currentIndex = if (activeTabIndex < 0) 0 else activeTabIndex
        val prevIndex = (currentIndex - 1 + tabs.size) % tabs.size
        return activateTab(tabs[prevIndex].tabId)
    }
}

/**
 * Terminal buffer data
 */
data class TerminalBuffer(
    val lines: List<TerminalLine> = emptyList(),
    val cursorRow: Int = 0,
    val cursorCol: Int = 0,
    val scrollbackSize: Int = 1000
)

/**
 * Single terminal line with styling
 */
data class TerminalLine(
    val text: String = "",
    val styles: List<LineStyle> = emptyList()
) {
    data class LineStyle(
        val startIndex: Int,
        val endIndex: Int,
        val foregroundColor: Int? = null,
        val backgroundColor: Int? = null,
        val isBold: Boolean = false,
        val isUnderline: Boolean = false,
        val isItalic: Boolean = false
    )
}
