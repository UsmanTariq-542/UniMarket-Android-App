package com.example.unimarketapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var regCode by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Join the student marketplace",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("University Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = regCode,
            onValueChange = { regCode = it },
            label = { Text("Registration Code") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter secret code") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || regCode.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (regCode != "UNI2026") {
                    Toast.makeText(context, "Invalid registration code!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true
                scope.launch {
                    try {
                        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                        val uid = result.user?.uid ?: ""
                        
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email.trim(),
                            "role" to "student",
                            "verified" to true
                        )

                        // Try to save to Firestore with a timeout so we don't hang forever
                        try {
                            withTimeout(5000) {
                                db.collection("users").document(uid).set(userData).await()
                            }
                        } catch (e: Exception) {
                            Log.e("Signup", "Firestore save failed or timed out", e)
                            // We proceed anyway because the Auth account was created
                        }

                        loading = false
                        Toast.makeText(context, "Welcome to UniMarket!", Toast.LENGTH_SHORT).show()
                        onSignupSuccess()
                    } catch (e: Exception) {
                        loading = false
                        Log.e("SignupError", "Registration failed", e)
                        val errorMessage = when (e) {
                            is FirebaseAuthException -> {
                                when (e.errorCode) {
                                    "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
                                    "ERROR_INVALID_EMAIL" -> "Invalid email format."
                                    "ERROR_WEAK_PASSWORD" -> "Password is too weak."
                                    else -> e.localizedMessage
                                }
                            }
                            else -> e.localizedMessage
                        }
                        Toast.makeText(context, errorMessage ?: "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !loading
        ) {
            if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLoginClick) {
            Text("Already have an account? Login")
        }
    }
}
