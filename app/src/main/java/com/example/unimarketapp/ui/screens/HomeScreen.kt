package com.example.unimarketapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.unimarketapp.Category
import com.example.unimarketapp.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var approvedProducts by remember { mutableStateOf(listOf<Product>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("approved_products")
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                loading = false
                if (error != null) {
                    Log.e("HomeScreen", "Firestore error", error)
                    errorMessage = if (error.message?.contains("PERMISSION_DENIED") == true) {
                        "Firestore API is disabled or permissions are missing. Please check Firebase Console."
                    } else {
                        error.localizedMessage
                    }
                    return@addSnapshotListener
                }
                if (value != null) {
                    approvedProducts = value.toObjects(Product::class.java)
                    errorMessage = null
                }
            }
    }

    val categories = listOf(
        Category("All", Icons.Default.AllInclusive, Color(0xFFE8F5E9)),
        Category("Bikes", Icons.AutoMirrored.Filled.DirectionsBike, Color(0xFFE3F2FD)),
        Category("Cars", Icons.Default.DirectionsCar, Color(0xFFFFF3E0)),
        Category("Books", Icons.AutoMirrored.Filled.MenuBook, Color(0xFFF1F8E9)),
        Category("Laptops", Icons.Default.Laptop, Color(0xFFE0F2F1)),
        Category("Mobiles", Icons.Default.Smartphone, Color(0xFFF3E5F5))
    )

    val filteredProducts = approvedProducts.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (it.title.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_product") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Sell") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFFBFBFB))) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)) {
                    Text("UniMarket", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search products...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                }
            }

            LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name },
                        label = { Text(category.name) },
                        leadingIcon = { Icon(category.icon, null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No products available yet.", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(product) { navController.navigate("product_details/${product.id}") }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF1F4F8)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "$${product.price}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                Text(text = product.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                Text(text = product.category, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
