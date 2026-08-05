package com.pocketlegal.advice.presentation.legal.directory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketlegal.advice.R
import com.pocketlegal.advice.data.model.LegalCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDirectoryScreen(
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: LegalDirectoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.legal_directory)) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_laws))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = viewModel::loadCategories) { Text("Retry") }
                }
                uiState.categories.isEmpty() -> Text(
                    stringResource(R.string.no_results),
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.categories) { category ->
                        CategoryItem(category = category, onClick = { onCategoryClick(category.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(category: LegalCategory, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(category.name, style = MaterialTheme.typography.titleLarge) },
        supportingContent = {
            Text(
                "${category.description} • ${category.lawCount} laws",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
