package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.ComptageScreen
import com.example.ui.screens.EntriesScreen
import com.example.ui.screens.ExitsScreen
import com.example.ui.screens.LocationsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReceiptOverlay
import com.example.ui.screens.StockScreen
import com.example.ui.screens.UsersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.AppViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsState()
                val uiEvents = viewModel.uiEvents
                val activeReceipt by viewModel.activeReceipt.collectAsState()

                // Reactive notification toasts
                LaunchedEffect(key1 = uiEvents) {
                    uiEvents.collectLatest { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                if (currentUser == null) {
                    // Portal screen container
                    LoginScreen(viewModel = viewModel)
                } else {
                    // Full stock manager workspace
                    MainWorkspace(
                        viewModel = viewModel,
                        activeReceipt = activeReceipt,
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }
}

data class TabItem(
    val id: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainWorkspace(
    viewModel: AppViewModel,
    activeReceipt: com.example.viewmodel.ReceiptDocument?,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val role = currentUser?.role ?: "consultateur"

    var currentTab by remember { mutableStateOf("stock") }

    // Map role-based visible bottom bar items
    val tabItems = remember(role) {
        val list = mutableListOf<TabItem>()
        // 'stock' is visible universally
        list.add(TabItem("stock", "Stock", Icons.Default.Inventory))

        // 'contacts' is visible for User and Admin
        if (role == "admin" || role == "user") {
            list.add(TabItem("contacts", "Contacts", Icons.Default.Business))
        }

        // 'entries' and 'exits' are visible for User and Admin
        if (role == "admin" || role == "user") {
            list.add(TabItem("entries", "Entrées", Icons.Default.Input))
            list.add(TabItem("exits", "Sorties", Icons.Default.LocalShipping))
        }

        // 'locations' visible universally
        list.add(TabItem("locations", "Placements", Icons.Default.QrCode))

        // 'comptage' visible universally
        list.add(TabItem("comptage", "Comptage", Icons.Default.Assessment))

        // 'users' is only visible if Admin
        if (role == "admin") {
            list.add(TabItem("users", "Opérateurs", Icons.Default.Group))
        }

        list
    }

    // Double-check tab coherence upon switching roles
    LaunchedEffect(key1 = role) {
        if (tabItems.none { it.id == currentTab }) {
            currentTab = "stock"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Stylized workspace header with Marlin branding and Logout button
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ttf_logo),
                            contentDescription = "TTF Marlin",
                            modifier = Modifier.size(width = 66.dp, height = 30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TTF STOCK",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentUser?.fullName ?: "",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                            .testTag("logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Deconnexion du portail",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Adaptive custom Bottom Nav
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Keep safe from Android system gesture bar pill!
                    .testTag("bottom_nav_bar")
            ) {
                tabItems.forEach { item ->
                    val isSelected = currentTab == item.id
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = item.id },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Workspace tab host transition wrapper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "stock" -> StockScreen(viewModel = viewModel)
                "contacts" -> ContactsScreen(viewModel = viewModel)
                "entries" -> EntriesScreen(viewModel = viewModel)
                "exits" -> ExitsScreen(viewModel = viewModel)
                "locations" -> LocationsScreen(viewModel = viewModel)
                "comptage" -> ComptageScreen(viewModel = viewModel)
                "users" -> UsersScreen(viewModel = viewModel)
            }
        }
    }

    // Printable Document Overlay trigger
    activeReceipt?.let { doc ->
        ReceiptOverlay(viewModel = viewModel, receipt = doc)
    }
}
