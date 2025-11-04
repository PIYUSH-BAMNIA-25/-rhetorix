package com.runanywhere.startup_hackathon20

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.database.UserEntity
import com.runanywhere.startup_hackathon20.viewmodel.AuthState
import com.runanywhere.startup_hackathon20.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: (UserEntity) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    val authState by viewModel.authState.collectAsState()

    // Handle auth success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess((authState as AuthState.Success).user)
        }
    }

    // Dark gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF0f0f1e)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Logo",
                    tint = Color(0xFF00d9ff),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Rhetorix",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title & Subtitle
            Text(
                text = if (isLoginMode) "Welcome Back!" else "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = if (isLoginMode) "Sign in to continue" else "Join the Debate!",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Show error message
            if (authState is AuthState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // Login or Signup Form
            if (isLoginMode) {
                LoginForm(
                    onLogin = { email, password ->
                        viewModel.login(email, password)
                    },
                    isLoading = authState is AuthState.Loading
                )
            } else {
                SignUpForm(
                    onSignUp = { name, email, password, dob ->
                        viewModel.signUp(name, email, password, dob)
                    },
                    isLoading = authState is AuthState.Loading
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle between Login/Signup
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoginMode) "Don't have an account?" else "Already have an account?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        viewModel.resetState()
                    }
                ) {
                    Text(
                        text = if (isLoginMode) "Sign Up" else "Log In",
                        color = Color(0xFF00d9ff),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    onLogin: (String, String) -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Enter your email", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00d9ff),
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Enter your password", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock,
                        contentDescription = "Toggle password",
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00d9ff),
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading
        )

        // Forgot Password Link
        TextButton(
            onClick = { /* TODO: Forgot password */ },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading
        ) {
            Text("Forgot Password?", color = Color(0xFF00d9ff), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign In Button
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    onLogin(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00d9ff),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Sign In", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SignUpForm(
    onSignUp: (String, String, String, String) -> Unit,
    isLoading: Boolean
) {
    var playerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Password strength
    val passwordStrength = remember(password) {
        when {
            password.length < 4 -> "Weak"
            password.length < 8 -> "Medium"
            else -> "Strong"
        }
    }
    
    val strengthColor = remember(password) {
        when {
            password.length < 4 -> Color.Red
            password.length < 8 -> Color.Yellow
            else -> Color.Green
        }
    }

    Column {
        // Player Name Field
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Enter your player name", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Player",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00d9ff),
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Enter your email", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color.Gray
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00d9ff),
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Create password", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock,
                        contentDescription = "Toggle password",
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00d9ff),
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading
        )

        // Password Strength Indicator
        if (password.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { 
                        when (passwordStrength) {
                            "Weak" -> 0.33f
                            "Medium" -> 0.66f
                            else -> 1f
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = strengthColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = passwordStrength,
                    color = strengthColor,
                    fontSize = 12.sp
                )
            }
        }

        // Date of Birth Field
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { dateOfBirth = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            placeholder = { Text("DD / MM / YYYY", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date of Birth",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00d9ff),
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading
        )

        // Sign Up Button
        Button(
            onClick = {
                if (playerName.isNotBlank() && email.isNotBlank() && 
                    password.isNotBlank() && dateOfBirth.isNotBlank()) {
                    onSignUp(playerName, email, password, dateOfBirth)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = playerName.isNotBlank() && email.isNotBlank() && 
                     password.isNotBlank() && dateOfBirth.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00d9ff),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class UserProfile(
    val name: String,
    val email: String,
    val dateOfBirth: String
)

// Previews
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(onAuthSuccess = {})
}

@Preview(showBackground = true)
@Composable
fun LoginFormPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF0f0f1e)
                    )
                )
            )
            .padding(24.dp)
    ) {
        LoginForm(onLogin = { _, _ -> }, isLoading = false)
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpFormPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF0f0f1e)
                    )
                )
            )
            .padding(24.dp)
    ) {
        SignUpForm(onSignUp = { _, _, _, _ -> }, isLoading = false)
    }
}