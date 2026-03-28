package com.example.unimarketapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAdminsScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var adminEmail by remember { mutableStateOf("") }
    var adminList by remember { mutableStateOf(listOf<String>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("admins").addSnapshotListener { value, _ ->
            adminList = value?.documents?.map { it.id } ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Admins") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Add New Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Admin Email") },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (adminEmail.isNotBlank()) {
                            db.collection("admins").document(adminEmail).set(mapOf("addedAt" to System.currentTimeMillis()))
                                .addOnSuccessListener { 
                                    adminEmail = ""
                                    Toast.makeText(context, "Admin added", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add") }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Current Admins", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(adminList) { email ->
                    if (email != "usmanntariq1100@gmail.com") {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(email, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    db.collection("admins").document(email).delete()
                                }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            }
                        }
                    }
                }
            }
        }
    }
}
