package com.example.unimarketapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unimarketapp.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MyAdsScreen() {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var myAds by remember { mutableStateOf(listOf<Product>()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            db.collection("approved_products")
                .whereEqualTo("sellerId", currentUser.uid)
                .addSnapshotListener { approvedValue, error ->
                    if (error != null) {
                        loading = false
                        errorMessage = "Error fetching ads: ${error.localizedMessage}"
                        return@addSnapshotListener
                    }
                    
                    val approvedList = approvedValue?.toObjects(Product::class.java) ?: emptyList()
                    
                    db.collection("product_requests")
                        .whereEqualTo("sellerId", currentUser.uid)
                        .addSnapshotListener { requestsValue, reqError ->
                            loading = false
                            if (reqError != null) {
                                errorMessage = "Error fetching pending ads: ${reqError.localizedMessage}"
                                return@addSnapshotListener
                            }
                            val requestsList = requestsValue?.toObjects(Product::class.java) ?: emptyList()
                            myAds = (approvedList + requestsList).sortedByDescending { it.time }
                            errorMessage = null
                        }
                }
        } else {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFBFBFB))) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("My Ads", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = Color.Red, textAlign = TextAlign.Center)
                }
            }
        } else if (myAds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't posted any ads yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(myAds) { ad ->
                    MyAdItem(ad)
                }
            }
        }
    }
}

@Composable
fun MyAdItem(product: Product) {
    val db = FirebaseFirestore.getInstance()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F4F8)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${product.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = if (product.status == "approved") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = product.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.status == "approved") Color(0xFF2E7D32) else Color(0xFFEF6C00)
                    )
                }
            }
            IconButton(onClick = {
                val collection = if (product.status == "approved") "approved_products" else "product_requests"
                db.collection(collection).document(product.id).delete()
            }) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}
