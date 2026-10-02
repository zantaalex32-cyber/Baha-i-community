package com.example.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CoreActivity
import com.example.data.model.EventSchedule
import com.example.data.model.Neighborhood
import com.example.ui.viewmodel.ClusterViewModel
import com.example.ui.viewmodel.DashboardTab
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ClusterViewModel,
    onSignOut: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val clustersState by viewModel.clustersState.collectAsStateWithLifecycle()
    val neighborhoodsState by viewModel.neighborhoodsState.collectAsStateWithLifecycle()
    val activitiesState by viewModel.activitiesState.collectAsStateWithLifecycle()
    val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<EventSchedule?>(null) }

    var showAddActivityDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<CoreActivity?>(null) }

    var showAddNeighborhoodDialog by remember { mutableStateOf(false) }
    var neighborhoodToEdit by remember { mutableStateOf<Neighborhood?>(null) }

    var showProfileDialog by remember { mutableStateOf(false) }

    val neighborhoodsList = (neighborhoodsState as? UiState.Success)?.data ?: emptyList()
    val userProfile = (userProfileState as? UiState.Success)?.data

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ClusterHub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Bahá'í Community & Neighborhood Coordination",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showProfileDialog = true },
                        modifier = Modifier.testTag("profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile & Role",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.OVERVIEW,
                    onClick = { viewModel.selectTab(DashboardTab.OVERVIEW) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                    label = { Text("Overview") },
                    modifier = Modifier.testTag("tab_overview")
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.SCHEDULE,
                    onClick = { viewModel.selectTab(DashboardTab.SCHEDULE) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Events & Feasts") },
                    label = { Text("Events") },
                    modifier = Modifier.testTag("tab_schedule")
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.ACTIVITIES,
                    onClick = { viewModel.selectTab(DashboardTab.ACTIVITIES) },
                    icon = { Icon(Icons.Default.School, contentDescription = "Core Activities") },
                    label = { Text("Activities") },
                    modifier = Modifier.testTag("tab_activities")
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.NEIGHBORHOODS,
                    onClick = { viewModel.selectTab(DashboardTab.NEIGHBORHOODS) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Neighborhoods") },
                    label = { Text("Sectors") },
                    modifier = Modifier.testTag("tab_neighborhoods")
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.ENCRYPTION,
                    onClick = { viewModel.selectTab(DashboardTab.ENCRYPTION) },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Security & Crypto") },
                    label = { Text("Security") },
                    modifier = Modifier.testTag("tab_security")
                )
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                DashboardTab.OVERVIEW, DashboardTab.SCHEDULE -> {
                    FloatingActionButton(
                        onClick = {
                            eventToEdit = null
                            showAddEventDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_event")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Schedule Event")
                    }
                }
                DashboardTab.ACTIVITIES -> {
                    FloatingActionButton(
                        onClick = {
                            activityToEdit = null
                            showAddActivityDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_activity")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Core Activity")
                    }
                }
                DashboardTab.NEIGHBORHOODS -> {
                    FloatingActionButton(
                        onClick = {
                            neighborhoodToEdit = null
                            showAddNeighborhoodDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_neighborhood")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Register Neighborhood")
                    }
                }
                else -> {}
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Secondary tab row to switch to Cluster & Goals
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                DashboardTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    DashboardTab.OVERVIEW -> {
                        OverviewPane(
                            clusterState = clustersState,
                            neighborhoodsState = neighborhoodsState,
                            activitiesState = activitiesState,
                            eventsState = eventsState,
                            onNavigateToTab = { viewModel.selectTab(it) },
                            onReseedSampleData = { viewModel.reseedSampleData() }
                        )
                    }
                    DashboardTab.SCHEDULE -> {
                        EventSchedulePane(
                            eventsState = eventsState,
                            onEditEvent = {
                                eventToEdit = it
                                showAddEventDialog = true
                            },
                            onDeleteEvent = { viewModel.deleteEvent(it) }
                        )
                    }
                    DashboardTab.ACTIVITIES -> {
                        CoreActivitiesPane(
                            activitiesState = activitiesState,
                            neighborhoodsState = neighborhoodsState,
                            onEditActivity = {
                                activityToEdit = it
                                showAddActivityDialog = true
                            },
                            onDeleteActivity = { viewModel.deleteCoreActivity(it) }
                        )
                    }
                    DashboardTab.NEIGHBORHOODS -> {
                        NeighborhoodsPane(
                            neighborhoodsState = neighborhoodsState,
                            activitiesState = activitiesState,
                            onEditNeighborhood = {
                                neighborhoodToEdit = it
                                showAddNeighborhoodDialog = true
                            },
                            onDeleteNeighborhood = { viewModel.deleteNeighborhood(it) }
                        )
                    }
                    DashboardTab.CLUSTER_INFO -> {
                        ClusterInfoPane(
                            clusterState = clustersState,
                            onSaveCluster = { viewModel.saveCluster(it) }
                        )
                    }
                    DashboardTab.ENCRYPTION -> {
                        CryptoSecurityPane()
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddEventDialog) {
        AddEditEventDialog(
            initialEvent = eventToEdit,
            neighborhoods = neighborhoodsList,
            onDismiss = { showAddEventDialog = false },
            onSave = { event, encrypt ->
                viewModel.saveEvent(event, encrypt)
                showAddEventDialog = false
            }
        )
    }

    if (showAddActivityDialog) {
        AddEditActivityDialog(
            initialActivity = activityToEdit,
            neighborhoods = neighborhoodsList,
            onDismiss = { showAddActivityDialog = false },
            onSave = { activity ->
                viewModel.saveCoreActivity(activity)
                showAddActivityDialog = false
            }
        )
    }

    if (showAddNeighborhoodDialog) {
        AddEditNeighborhoodDialog(
            initialNeighborhood = neighborhoodToEdit,
            onDismiss = { showAddNeighborhoodDialog = false },
            onSave = { neighborhood ->
                viewModel.saveNeighborhood(neighborhood)
                showAddNeighborhoodDialog = false
            }
        )
    }

    if (showProfileDialog) {
        EditUserProfileDialog(
            initialProfile = userProfile,
            onDismiss = { showProfileDialog = false },
            onSave = { profile, encrypt ->
                viewModel.saveUserProfile(profile, encrypt)
                showProfileDialog = false
            }
        )
    }
}
