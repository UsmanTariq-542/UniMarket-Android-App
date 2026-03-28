package com.example.unimarketapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Message(
    val sender: String = "",
    val receiver: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var chatUsers by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            db.collection("chats")
                .whereEqualTo("sender", currentUser.email)
                .addSnapshotListener { value, _ ->
                    val users = value?.toObjects(Message::class.java)?.map { it.receiver }?.toSet() ?: emptySet()
                    chatUsers = chatUsers + users
                }
            db.collection("chats")
                .whereEqualTo("receiver", currentUser.email)
                .addSnapshotListener { value, _ ->
                    val users = value?.toObjects(Message::class.java)?.map { it.sender }?.toSet() ?: emptySet()
                    chatUsers = chatUsers + users
                }
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Messages") }) }
    ) { padding ->
        if (chatUsers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No messages yet", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(chatUsers.toList()) { email ->
                    ListItem(
                        headlineContent = { Text(email, fontWeight = FontWeight.Bold) },
                        leadingContent = {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Text(email.take(1).uppercase())
                            }
                        },
                        modifier = Modifier.clickable { navController.navigate("chat_detail/$email") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(otherEmail: String, onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var newMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            db.collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { value, _ ->
                    val allMsg = value?.toObjects(Message::class.java) ?: emptyList()
                    messages = allMsg.filter { 
                        (it.sender == currentUser.email && it.receiver == otherEmail) ||
                        (it.sender == otherEmail && it.receiver == currentUser.email)
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherEmail, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth().navigationBarsPadding().imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newMessage.isNotBlank() && currentUser != null) {
                                val msg = Message(currentUser.email!!, otherEmail, newMessage)
                                db.collection("chats").add(msg)
                                newMessage = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                    ) { Icon(Icons.AutoMirrored.Filled.Send, null) }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.sender == currentUser?.email
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart) {
                    Surface(
                        color = if (isMe) MaterialTheme.colorScheme.primary else Color(0xFFE9E9EB),
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        )
                    ) {
                        Text(
                            text = msg.content,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (isMe) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}
