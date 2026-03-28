package com.example.unimarketapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unimarketapp.ui.screens.*
import com.example.unimarketapp.ui.theme.UniMarketAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

data class Product(
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val sellerType: String = "Student Seller",
    val description: String = "",
    val imageUrl: String = "",
    val sellerId: String = "",
    val sellerEmail: String = "",
    val status: String = "pending",
    val time: Long = System.currentTimeMillis()
)

data class Category(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniMarketAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UniMarketApp()
                }
            }
        }
    }
}

@Composable
fun UniMarketApp() {
    var showSplash by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    // Listen for Auth changes globally
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (showSplash) {
        SplashScreen(onContinue = { showSplash = false })
    } else {
        if (currentUser == null) {
            AuthNavHost(onAuthSuccess = { 
                // State is handled by AuthStateListener
            })
        } else {
            MainScreen(onLogout = { 
                auth.signOut()
            })
        }
    }
}

@Composable
fun AuthNavHost(onAuthSuccess: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { 
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onSignupClick = { navController.navigate("signup") }
            ) 
        }
        composable("signup") { 
            SignupScreen(
                onSignupSuccess = onAuthSuccess,
                onLoginClick = { navController.popBackStack() }
            ) 
        }
    }
}

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var isAdmin by remember { mutableStateOf(false) }
    var isSuperAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            isSuperAdmin = currentUser.email == "usmanntariq1100@gmail.com"
            try {
                val adminDoc = db.collection("admins").document(currentUser.email!!).get().await()
                isAdmin = isSuperAdmin || adminDoc.exists()
            } catch (e: Exception) {
                Log.e("UniMarket", "Error checking admin status", e)
                isAdmin = isSuperAdmin
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("home", "chats", "add_product", "my_ads", "account", "admin_dashboard")) {
                NavigationBar(containerColor = Color.White, tonalElevation = 12.dp) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        icon = { Icon(if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home, null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "chats",
                        onClick = { navController.navigate("chats") },
                        icon = { Icon(if (currentRoute == "chats") Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat, null) },
                        label = { Text("Chats") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "add_product",
                        onClick = { navController.navigate("add_product") },
                        icon = { Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(35.dp), tint = MaterialTheme.colorScheme.primary) },
                        label = { Text("Sell") }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "my_ads",
                        onClick = { navController.navigate("my_ads") },
                        icon = { Icon(if (currentRoute == "my_ads") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null) },
                        label = { Text("My Ads") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "account" || currentRoute == "admin_dashboard",
                        onClick = { 
                            if (isAdmin) navController.navigate("admin_dashboard")
                            else navController.navigate("account")
                        },
                        icon = { Icon(if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person, null) },
                        label = { Text(if (isAdmin) "Admin" else "Account") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(navController) }
            composable("chats") { ChatListScreen(navController) }
            composable("account") { ProfileScreen(onLogout = onLogout) }
            composable("my_ads") { MyAdsScreen() }
            composable("admin_dashboard") { AdminDashboardScreen(navController, isSuperAdmin, onLogout) }
            composable("manage_admins") { ManageAdminsScreen(navController) }
            composable("add_product") { AddProductScreen(navController) }
            composable(
                route = "chat_detail/{otherUserEmail}",
                arguments = listOf(navArgument("otherUserEmail") { type = NavType.StringType })
            ) { backStackEntry ->
                val otherEmail = backStackEntry.arguments?.getString("otherUserEmail") ?: ""
                ChatDetailScreen(otherEmail, onBack = { navController.popBackStack() })
            }
            composable(
                route = "product_details/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(productId, navController)
            }
        }
    }
}

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.White
            )
            Text(
                "UniMarket",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "The Student Marketplace",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
