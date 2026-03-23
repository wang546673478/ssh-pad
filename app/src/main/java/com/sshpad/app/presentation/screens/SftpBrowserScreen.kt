package com.sshpad.app.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sshpad.app.ssh.SftpClientWrapper
import com.sshpad.app.ssh.SftpEntry

/**
 * SFTP File Browser Screen
 * Week 8: SFTP File Transfer Support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SftpBrowserScreen(
    sftpClient: SftpClientWrapper,
    currentPath: String = "/",
    onNavigateBack: () -> Unit,
    onFileSelected: (SftpEntry) -> Unit,
    onUploadRequested: () -> Unit,
    onDownloadRequested: (SftpEntry) -> Unit
) {
    var directoryContents by remember { mutableStateOf<List<SftpEntry>>(emptyList()) }
    var currentDirectoryPath by remember { mutableStateOf(currentPath) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSortOptions by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(SortBy.NAME) }
    var sortAscending by remember { mutableStateOf(true) }

    LaunchedEffect(currentDirectoryPath) {
        isLoading = true
        error = null
        try {
            val entries = sftpClient.listDirectory(currentDirectoryPath)
            directoryContents = sortEntries(entries, sortBy, sortAscending)
        } catch (e: Exception) {
            error = e.message ?: "Failed to load directory"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SFTP Browser",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentDirectoryPath != "/") {
                            val parentPath = currentDirectoryPath
                                .trimEnd('/')
                                .substringBeforeLast('/')
                                .ifEmpty { "/" }
                            currentDirectoryPath = parentPath
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUploadRequested) {
                        Icon(
                            Icons.Filled.Upload,
                            contentDescription = "Upload"
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortOptions = true }) {
                            Icon(
                                Icons.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortOptions,
                            onDismissRequest = { showSortOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by Name") },
                                onClick = {
                                    sortBy = SortBy.NAME
                                    sortAscending = !sortAscending
                                    directoryContents = sortEntries(directoryContents, sortBy, sortAscending)
                                    showSortOptions = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Size") },
                                onClick = {
                                    sortBy = SortBy.SIZE
                                    sortAscending = !sortAscending
                                    directoryContents = sortEntries(directoryContents, sortBy, sortAscending)
                                    showSortOptions = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Date") },
                                onClick = {
                                    sortBy = SortBy.DATE
                                    sortAscending = !sortAscending
                                    directoryContents = sortEntries(directoryContents, sortBy, sortAscending)
                                    showSortOptions = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                currentDirectoryPath = currentDirectoryPath
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                else -> {
                    Column {
                        // Path bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentDirectoryPath,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        // File list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Parent directory entry
                            if (currentDirectoryPath != "/") {
                                item {
                                    FileListItem(
                                        entry = SftpEntry(
                                            name = "..",
                                            path = "..",
                                            isDirectory = true,
                                            size = 0,
                                            lastModified = 0
                                        ),
                                        onClick = {
                                            val parentPath = currentDirectoryPath
                                                .trimEnd('/')
                                                .substringBeforeLast('/')
                                                .ifEmpty { "/" }
                                            currentDirectoryPath = parentPath
                                        }
                                    )
                                }
                            }
                            
                            items(directoryContents) { entry ->
                                FileListItem(
                                    entry = entry,
                                    onClick = {
                                        if (entry.isDirectory) {
                                            currentDirectoryPath = entry.path
                                        } else {
                                            onFileSelected(entry)
                                        }
                                    },
                                    onLongClick = {
                                        if (!entry.isDirectory) {
                                            onDownloadRequested(entry)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileListItem(
    entry: SftpEntry,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (entry.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                contentDescription = null,
                tint = if (entry.isDirectory) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!entry.isDirectory) {
                    Text(
                        text = entry.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private enum class SortBy {
    NAME, SIZE, DATE
}

private fun sortEntries(
    entries: List<SftpEntry>,
    sortBy: SortBy,
    ascending: Boolean
): List<SftpEntry> {
    val directories = entries.filter { it.isDirectory && it.name != ".." }
    val files = entries.filterNot { it.isDirectory }
    
    val sortedDirectories = when (sortBy) {
        SortBy.NAME -> directories.sortedBy { it.name.lowercase() }
        SortBy.SIZE -> directories.sortedBy { it.size }
        SortBy.DATE -> directories.sortedBy { it.lastModified }
    }
    
    val sortedFiles = when (sortBy) {
        SortBy.NAME -> files.sortedBy { it.name.lowercase() }
        SortBy.SIZE -> files.sortedBy { it.size }
        SortBy.DATE -> files.sortedBy { it.lastModified }
    }
    
    val combined = if (ascending) {
        sortedDirectories + sortedFiles
    } else {
        sortedDirectories.reversed() + sortedFiles.reversed()
    }
    
    return combined
}
