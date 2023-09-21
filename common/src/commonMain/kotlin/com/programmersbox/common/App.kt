package com.programmersbox.common

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.paging.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job

@Composable
internal fun App() {
    Surface {
        ATLAScreen()
    }
}

private const val PAGE_COUNT = 20

class AvatarViewModel {
    private val viewModelScope = CoroutineScope(Job() + Dispatchers.IO)
    private val apiService = AvatarApiService()

    @OptIn(ExperimentalPagingApi::class)
    val pager = Pager(
        PagingConfig(
            pageSize = PAGE_COUNT,
            enablePlaceholders = true
        ),
        //remoteMediator = AvatarRemoteMediator(PAGE_COUNT, apiService)
    ) { AvatarPagingSource(apiService, PAGE_COUNT) }
        .flow
        .cachedIn(viewModelScope)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ATLAScreen() {
    val vm: AvatarViewModel = remember { AvatarViewModel() }
    val lazyPagingItems = vm.pager.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avatar: The Last Airbender") },
                actions = { Text(animateIntAsState(lazyPagingItems.itemCount).value.toString()) }
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = lazyPagingItems::refresh
                ) { Icon(Icons.Default.Refresh, null) }
            }
        }
    ) { p ->
        LazyColumn(
            contentPadding = p,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                item {
                    Text(
                        text = "Waiting for items to load from the backend",
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            items(
                count = lazyPagingItems.itemCount,
            ) {
                lazyPagingItems[it]
                    ?.let { character -> AvatarCard(character) }
                    ?: run { AvatarPlaceholderCard() }
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarCard(item: Beer) {
    Card {
        ListItem(
            headlineContent = { Text(item.name) },
            supportingContent = { Text(item.description) },
            leadingContent = {
                Surface(shape = CircleShape) {
                    KamelImage(
                        resource = asyncPainterResource(item.imageUrl),
                        contentDescription = null,
                        modifier = Modifier.size(75.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun AvatarPlaceholderCard() {
    Card {
        ListItem(
            headlineContent = {
                Text(
                    "",
                )
            },
            supportingContent = {
                Text(
                    "",
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .border(1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape)
                        .size(50.dp)
                        .clip(CircleShape)
                )
            }
        )
    }
}
