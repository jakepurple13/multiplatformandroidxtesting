package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.*

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

    val switch = DataStore.switch

    val pager = Pager(
        PagingConfig(
            pageSize = PAGE_COUNT,
            enablePlaceholders = true
        ),
    ) { AvatarPagingSource(apiService, PAGE_COUNT) }
        .flow
        .cachedIn(viewModelScope)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ATLAScreen() {
    val vm: AvatarViewModel = remember { AvatarViewModel() }
    val lazyPagingItems = vm.pager.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val switch by vm.switch.flow.collectAsState(true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beer") },
                actions = { Text(animateIntAsState(lazyPagingItems.itemCount).value.toString()) }
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = lazyPagingItems::refresh
                ) { Icon(Icons.Default.Refresh, null) }

                IconToggleButton(
                    checked = switch,
                    onCheckedChange = { scope.launch { vm.switch.update(it) } }
                ) {
                    Icon(
                        if (switch) Icons.Default.TextIncrease else Icons.Default.TextDecrease,
                        null
                    )
                }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            items(
                count = lazyPagingItems.itemCount,
            ) {
                lazyPagingItems[it]
                    ?.let { character -> AvatarCard(character, switch) }
                    ?: run { AvatarPlaceholderCard() }
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarCard(item: Beer, showDescription: Boolean) {
    OutlinedCard {
        ListItem(
            headlineContent = { Text(item.name) },
            supportingContent = {
                AnimatedVisibility(
                    visible = showDescription,
                    enter = slideInVertically() + expandVertically(),
                    exit = slideOutVertically() + shrinkVertically()
                ) {
                    Text(item.description)
                }
            },
            leadingContent = {
                Surface(shape = CircleShape) {
                    KamelImage(
                        resource = asyncPainterResource(item.imageUrl.orEmpty()),
                        contentDescription = null,
                        modifier = Modifier.size(75.dp)
                    )
                }
            },
            modifier = Modifier.animateContentSize()
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
