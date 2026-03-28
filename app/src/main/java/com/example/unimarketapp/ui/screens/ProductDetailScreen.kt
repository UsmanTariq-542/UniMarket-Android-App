package com.example.unimarketapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var product by remember { mutableStateOf<Product?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(productId) {
        db.collection("approved_products").document(productId).get()
            .addOnSuccessListener { doc ->
                product = doc.toObject(Product::class.java)
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (product != null) {
            val p = product!!
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = p.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(300.dp).background(Color(0xFFF1F4F8)),
                    contentScale = ContentScale.Crop
                )
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = p.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$${p.price}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text(text = p.category, fontSize = 14.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Seller Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = p.sellerEmail, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = p.description, color = Color.DarkGray, lineHeight = 20.sp)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { navController.navigate("chat_detail/${p.sellerEmail}") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat with Seller", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
