package com.example.unimarketapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.unimarketapp.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavHostController, isSuperAdmin: Boolean, onLogout: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var pendingRequests by remember { mutableStateOf(listOf<Product>()) }

    LaunchedEffect(Unit) {
        db.collection("product_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    pendingRequests = value.toObjects(Product::class.java)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    if (isSuperAdmin) {
                        IconButton(onClick = { navController.navigate("manage_admins") }) {
                            Icon(Icons.Default.GroupAdd, "Manage Admins")
                        }
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFFBFBFB))) {
            if (pendingRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending requests", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(pendingRequests) { request ->
                        AdminRequestCard(request)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRequestCard(request: Product) {
    val db = FirebaseFirestore.getInstance()
    
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = request.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFF1F4F8)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(request.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("$${request.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Seller: ${request.sellerEmail}", fontSize = 12.sp, color = Color.Gray)
            Text("Category: ${request.category}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        db.collection("approved_products").document(request.id).set(request.copy(status = "approved"))
                        db.collection("product_requests").document(request.id).delete()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Approve") }
                
                Button(
                    onClick = {
                        db.collection("product_requests").document(request.id).delete()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Reject") }
            }
        }
    }
}
