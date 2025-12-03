package com.eventify.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String, String, String) -> Unit, // token, role, name, email, id
    onSwitchToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .shadow(10.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connexion",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2196F3)
                )
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFFF9800),
                        focusedLabelColor = Color(0xFFFF9800)
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle mot de passe")
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF2196F3),
                        focusedLabelColor = Color(0xFF2196F3)
                    )
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        loading = true
                        error = null
                        CoroutineScope(Dispatchers.IO).launch {
                            val (token, role, name, userEmail, id) = loginRequest(email, password) // <-- Quint
                            loading = false
                            if (token != null && role != null && name != null && userEmail != null && id != null) {
                                onLoginSuccess(token, role, name, userEmail, id)
                            } else {
                                error = id ?: "Erreur inconnue"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Se connecter", color = Color.White)
                }
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onSwitchToSignup) {
                    Text("Pas encore de compte ? Inscris-toi", color = Color(0xFFFF9800))
                }
                AnimatedVisibility(visible = error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
                }
                if (loading) {
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            }
        }
    }
}

// Renvoie Token JWT, Rôle, Nom, Email, ID (ou message d'erreur dans le 5e élément)
suspend fun loginRequest(email: String, password: String): Quint<String?, String?, String?, String?, String?> {
    return try {
        val url = URL("http://10.0.2.2:3001/api/auth/login")
        val json = """{"email":"$email","password":"$password"}"""
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            outputStream.write(json.toByteArray())
            val code = responseCode
            if (code in 200..299) {
                val resp = inputStream.bufferedReader().readText()
                val jsonResp = JSONObject(resp)
                val token = jsonResp.optString("token")
                val userJson = jsonResp.optJSONObject("user") // <-- récupère l'objet user
                val role = userJson?.optString("role")
                val name = userJson?.optString("name")
                val userEmail = userJson?.optString("email")
                val id = userJson?.optString("_id") // <-- récupère l'id
                Quint(token, role, name, userEmail, id)
            } else {
                val errorBody = errorStream?.bufferedReader()?.readText() ?: inputStream.bufferedReader().readText()
                val msg = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(errorBody)?.groupValues?.getOrNull(1) ?: errorBody
                Quint(null, null, null, null, msg)
            }
        }
    } catch (ex: Exception) {
        Quint(null, null, null, null, ex.localizedMessage)
    }
}

// Structure pour retourner 5 valeurs
data class Quint<A,B,C,D,E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
