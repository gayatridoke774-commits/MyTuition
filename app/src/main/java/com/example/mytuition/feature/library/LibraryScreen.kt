package com.example.mytuition.feature.library

import kotlinx.coroutines.launch

import kotlinx.coroutines.CancellationException

import com.example.mytuition.core.di.AppContainer

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.platform.LocalContext

import android.widget.Toast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytuition.core.designsystem.MyTuitionColors
import com.example.mytuition.core.designsystem.MyTuitionSpacing
import com.example.mytuition.core.designsystem.MyTuitionTypography
import com.example.mytuition.core.designsystem.PastelBackground
import com.example.mytuition.core.domain.model.Resource
import com.example.mytuition.core.domain.model.ResourceType
import java.util.Locale

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.provideFactory()
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val selectedBatch by viewModel.selectedBatch.collectAsStateWithLifecycle()
    val allResources by viewModel.allResources.collectAsStateWithLifecycle()

    val subjectOptions = allResources
        .mapNotNull { it.subject?.takeIf(String::isNotBlank) }
        .distinct()
        .sorted()

    val batchOptions = allResources
        .mapNotNull { it.batch?.takeIf(String::isNotBlank) }
        .distinct()
        .sorted()

    val subjectMenuExpanded = remember { mutableStateOf(false) }
    val batchMenuExpanded = remember { mutableStateOf(false) }

    val hasActiveFilters = searchQuery.isNotBlank() ||
        selectedType != null ||
        selectedSubject != null ||
        selectedBatch != null

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharingResourceId = remember { mutableStateOf<String?>(null) }
    val pocketBaseApi = AppContainer.pocketBaseApi

    val isFromCache = when (val current = state) {
        is LibraryUiState.Success -> current.isFromCache
        is LibraryUiState.Empty -> current.isFromCache
        else -> false
    }

    PastelBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MyTuitionSpacing.lg,
                        vertical = MyTuitionSpacing.md
                    )
            ) {
                Text(
                    text = "Library",
                    style = MyTuitionTypography.HeadlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MyTuitionColors.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your learning resources in one place",
                    style = MyTuitionTypography.BodyMedium.copy(
                        fontSize = 15.sp,
                        color = MyTuitionColors.TextSecondary
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = MyTuitionSpacing.lg,
                    end = MyTuitionSpacing.lg,
                    bottom = 110.dp
                ),
                verticalArrangement = Arrangement.spacedBy(MyTuitionSpacing.md)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search resources...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { viewModel.selectType(null) },
                            label = { Text("All") }
                        )

                        ResourceType.values().forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { viewModel.selectType(type) },
                                label = {
                                    Text(type.name.lowercase().replaceFirstChar {
                                        it.uppercase()
                                    })
                                }
                            )
                        }
                    }
                }

                if (subjectOptions.isNotEmpty() || batchOptions.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (subjectOptions.isNotEmpty()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            subjectMenuExpanded.value = true
                                        }
                                    ) {
                                        Text(
                                            selectedSubject ?: "Subject",
                                            maxLines = 1
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = subjectMenuExpanded.value,
                                        onDismissRequest = {
                                            subjectMenuExpanded.value = false
                                        }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All subjects") },
                                            onClick = {
                                                viewModel.selectSubject(null)
                                                subjectMenuExpanded.value = false
                                            }
                                        )

                                        subjectOptions.forEach { subject ->
                                            DropdownMenuItem(
                                                text = { Text(subject) },
                                                onClick = {
                                                    viewModel.selectSubject(subject)
                                                    subjectMenuExpanded.value = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (batchOptions.isNotEmpty()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            batchMenuExpanded.value = true
                                        }
                                    ) {
                                        Text(
                                            selectedBatch ?: "Batch",
                                            maxLines = 1
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = batchMenuExpanded.value,
                                        onDismissRequest = {
                                            batchMenuExpanded.value = false
                                        }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All batches") },
                                            onClick = {
                                                viewModel.selectBatch(null)
                                                batchMenuExpanded.value = false
                                            }
                                        )

                                        batchOptions.forEach { batch ->
                                            DropdownMenuItem(
                                                text = { Text(batch) },
                                                onClick = {
                                                    viewModel.selectBatch(batch)
                                                    batchMenuExpanded.value = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (hasActiveFilters) {
                    item {
                        TextButton(
                            onClick = { viewModel.clearFilters() }
                        ) {
                            Text("Clear filters")
                        }
                    }
                }

                if (isFromCache) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Offline mode: showing previously saved resources.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                when (val current = state) {
                    is LibraryUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MyTuitionColors.PrimaryPurple
                                )
                            }
                        }
                    }

                    is LibraryUiState.Error -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Couldn't load resources",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = current.message,
                                    color = MyTuitionColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.refresh() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    is LibraryUiState.Empty -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No resources found",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = current.message,
                                    color = MyTuitionColors.TextSecondary
                                )
                                if (hasActiveFilters) {
                                    TextButton(
                                        onClick = { viewModel.clearFilters() }
                                    ) {
                                        Text("Clear filters")
                                    }
                                }
                                TextButton(
                                    onClick = { viewModel.refresh() }
                                ) {
                                    Text("Refresh")
                                }
                            }
                        }
                    }

                    is LibraryUiState.Success -> {
                        item {
                            Text(
                                text = "${current.resources.size} resource(s)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MyTuitionColors.TextSecondary
                            )
                        }

                        items(
                            items = current.resources,
                            key = { it.id }
                        ) { resource ->
                            ResourceCard(
                                resource = resource,
                                onOpen = {
                                    resource.url?.let { url ->
                                        runCatching {
                                            uriHandler.openUri(url)
                                        }.onFailure { exception ->
                                            Toast.makeText(
                                                context,
                                                exception.message ?: "Unable to open resource.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                onShare = {
                                    if (sharingResourceId.value == null) {
                                        sharingResourceId.value = resource.id
                                        scope.launch {
                                            try {
                                                LibraryFileSharer.share(
                                                    context,
                                                    pocketBaseApi,
                                                    resource
                                                )
                                            } catch (exception: CancellationException) {
                                                throw exception
                                            } catch (exception: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    exception.message ?: "Unable to share resource.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } finally {
                                                sharingResourceId.value = null
                                            }
                                        }
                                    }
                                },
                                isSharing = sharingResourceId.value == resource.id,
                                isShareEnabled = sharingResourceId.value == null
                            )
                        }

                        item {
                            TextButton(
                                onClick = { viewModel.refresh() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Refresh resources")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceCard(
    resource: Resource,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    isSharing: Boolean,
    isShareEnabled: Boolean
) {
    val hasUrl = !resource.url.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MyTuitionColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resource.type.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MyTuitionColors.PrimaryPurple
                )

                resource.sizeBytes?.let { size ->
                    Text(
                        text = formatFileSize(size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MyTuitionColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (hasUrl) "Resource available" else "File link unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = MyTuitionColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpen,
                    enabled = hasUrl
                ) {
                    Text("Open")
                }

                TextButton(
                    onClick = onShare,
                    enabled = hasUrl && isShareEnabled && !isSharing
                ) {
                    Text(if (isSharing) "Sharing…" else "Share")
                }
            }
        }
    }
}
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024L * 1024L ->
            String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024L ->
            String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
