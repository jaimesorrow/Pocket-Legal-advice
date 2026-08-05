package com.pocketlegal.advice.presentation.legal.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketlegal.advice.data.model.LegalLaw

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalCategoryScreen(
    categoryId: String,
    onBack: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    viewModel: LegalCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) { viewModel.loadCategory(categoryId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryId) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToSearch(categoryId) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
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
                uiState.isLoading && uiState.laws.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null && uiState.laws.isEmpty() ->
                    Text(uiState.error ?: "", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                uiState.laws.isEmpty() ->
                    Text("No laws in this category yet.", modifier = Modifier.align(Alignment.Center))
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.laws) { law ->
                        LawItem(law = law)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun LawItem(law: LegalLaw) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(law.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(2.dp))
        Text(law.code, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(law.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
    }
}
