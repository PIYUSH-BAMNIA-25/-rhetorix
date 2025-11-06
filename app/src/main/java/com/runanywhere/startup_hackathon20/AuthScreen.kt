package com.runanywhere.startup_hackathon20

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.runanywhere.startup_hackathon20.AuthState
import com.runanywhere.startup_hackathon20.AuthViewModel
import kotlin.math.cos
import kotlin.math.sin

// Color Palette - Dark theme with attractive colors
private val CyanPrimary = Color(0xFF00D9FF)
private val CyanLight = Color(0xFF5FEDFF)
private val PurpleAccent = Color(0xFF9D4EDD)
private val DarkBackground = Color(0xFF0A0A0F)
private val DarkSurface = Color(0xFF1A1A2E)
private val DarkCard = Color(0xFF16213E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFFB0B0B0)
private val ErrorRed = Color(0xFFFF6B6B)

enum class AuthPage {
    MAIN, LOGIN, SIGNUP
}

@Composable
fun AuthScreen(
    onAuthSuccess: (UserEntity) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var currentPage by remember { mutableStateOf(AuthPage.MAIN) }
    val authState by viewModel.authState.collectAsState()

    // Handle auth success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess((authState as AuthState.Success).user)
        }
    }

    when (currentPage) {
        AuthPage.MAIN -> MainAuthScreen(
            onNavigateToLogin = { currentPage = AuthPage.LOGIN },
            onNavigateToSignUp = { currentPage = AuthPage.SIGNUP }
        )
        AuthPage.LOGIN -> LoginScreen(
            onNavigateToSignUp = { 
                viewModel.resetState()
                currentPage = AuthPage.SIGNUP 
            },
            onNavigateBack = { currentPage = AuthPage.MAIN },
            onLogin = { email, password ->
                viewModel.login(email, password)
            },
            authState = authState
        )
        AuthPage.SIGNUP -> SignUpScreen(
            onNavigateToLogin = { 
                viewModel.resetState()
                currentPage = AuthPage.LOGIN 
            },
            onNavigateBack = { currentPage = AuthPage.MAIN },
            onSignUp = { name, email, password, dob ->
                viewModel.signUp(name, email, password, dob)
            },
            authState = authState
        )
    }
}

// ==================== MAIN AUTH SCREEN ====================
@Composable
fun MainAuthScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        DarkBackground
                    )
                )
            )
    ) {
        // Animated background particles
        AnimatedBackgroundParticles()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Section - Golden Ratio: 1.618
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.618f)
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                
                // R Logo with glow effect
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    Canvas(modifier = Modifier.size(130.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CyanPrimary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width / 2
                        )
                    }
                    
                    Text(
                        text = "R",
                        fontSize = 88.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanPrimary,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // App Name
                Text(
                    text = "Retrorix",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tagline
                Text(
                    text = "Master the Art of Debate",
                    fontSize = 16.sp,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.weight(0.5f))
            }
            
            // Buttons Section - Golden Ratio: 1
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Login Button
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(31.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Text(
                        text = "Log IN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Sign Up Button
                OutlinedButton(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    border = BorderStroke(2.dp, CyanPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkCard.copy(alpha = 0.5f),
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(31.dp)
                ) {
                    Text(
                        text = "Sign UP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ==================== LOGIN SCREEN ====================
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    authState: AuthState
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    
    val isLoading = authState is AuthState.Loading
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        DarkBackground
                    )
                )
            )
    ) {
        AnimatedBackgroundParticles()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header - Logo and Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "R",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Retrorix",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            
            // Profile Circle Avatar (Instagram style)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Gradient border ring
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(CyanPrimary, PurpleAccent, CyanLight)
                        ),
                        radius = size.width / 2,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CyanPrimary.copy(alpha = 0.3f),
                                    PurpleAccent.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = TextWhite,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Welcome Text
            Text(
                text = "Welcome Back, Player!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Error Message
            if (authState is AuthState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorRed.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = ErrorRed,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
            
            // Email/Username Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                placeholder = { 
                    Text(
                        "Username or Email", 
                        color = TextGray,
                        fontSize = 16.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = if (emailFocused) CyanPrimary else TextGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                    focusedContainerColor = DarkCard.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkCard.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                placeholder = { 
                    Text(
                        "Password", 
                        color = TextGray,
                        fontSize = 16.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = if (passwordFocused) CyanPrimary else TextGray
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock,
                            contentDescription = "Toggle password",
                            tint = TextGray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                    focusedContainerColor = DarkCard.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkCard.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Sign In Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        onLogin(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = DarkBackground,
                    disabledContainerColor = CyanPrimary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(29.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DarkBackground,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Don't have account
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = TextGray,
                    fontSize = 15.sp
                )
                TextButton(
                    onClick = onNavigateToSignUp,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Sign Up",
                        color = CyanPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== SIGN UP SCREEN ====================
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onSignUp: (String, String, String, String) -> Unit,
    authState: AuthState
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val isLoading = authState is AuthState.Loading
    val passwordsMatch = password == confirmPassword
    val allFieldsFilled = username.isNotBlank() && email.isNotBlank() && 
                         dateOfBirth.isNotBlank() && password.isNotBlank() && 
                         confirmPassword.isNotBlank()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        DarkBackground
                    )
                )
            )
    ) {
        AnimatedBackgroundParticles()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header - Logo and Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "R",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Retrorix",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            
            // Profile Circle Avatar (Instagram style)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Gradient border ring
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(CyanPrimary, PurpleAccent, CyanLight)
                        ),
                        radius = size.width / 2,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CyanPrimary.copy(alpha = 0.3f),
                                    PurpleAccent.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = TextWhite,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Welcome Text
            Text(
                text = "Start Your Journey Today!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Error Message
            if (authState is AuthState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorRed.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = ErrorRed,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
            
            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { 
                    Text(
                        "Username", 
                        color = TextGray,
                        fontSize = 15.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username",
                        tint = TextGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                    focusedContainerColor = DarkCard.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkCard.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp),
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
                placeholder = { 
                    Text(
                        "Email", 
                        color = TextGray,
                        fontSize = 15.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = TextGray
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                    focusedContainerColor = DarkCard.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkCard.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Date of Birth Field
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { 
                    Text(
                        "Date of Birth (DOB)", 
                        color = TextGray,
                        fontSize = 15.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "DOB",
                        tint = TextGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                    focusedContainerColor = DarkCard.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkCard.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { 
                    Text(
                        "Password", 
                        color = TextGray,
                        fontSize = 15.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = TextGray
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock,
                            contentDescription = "Toggle password",
                            tint = TextGray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.3f),
                    focusedContainerColor = DarkCard.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkCard.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { 
                    Text(
                        "Confirm Password", 
                        color = TextGray,
                        fontSize = 15.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password",
                        tint = TextGray
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Info else Icons.Default.Lock,
                            contentDescription = "Toggle password",
                            tint = TextGray
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = if (passwordsMatch || confirmPassword.isEmpty()) CyanPrimary else ErrorRed,
                    unfocusedBorderColor = if (passwordsMatch || confirmPassword.isEmpty()) TextGray.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.5f),
                    focusedContainerColor = DarkCard.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkCard.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Password match indicator
            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Sign Up Button
            Button(
                onClick = {
                    if (allFieldsFilled && passwordsMatch) {
                        onSignUp(username, email, password, dateOfBirth)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = allFieldsFilled && passwordsMatch && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = DarkBackground,
                    disabledContainerColor = CyanPrimary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(29.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DarkBackground,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Already have account
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextGray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Log In",
                        color = CyanPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== ANIMATED BACKGROUND ====================
@Composable
fun AnimatedBackgroundParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val animatedOffsets = remember {
        List(8) { 
            Pair(
                (0..360).random().toFloat(),
                (50..150).random().toFloat()
            )
        }
    }

    val animatedValues = List(8) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 20000 + index * 2000,
                    easing = LinearEasing
                )
            ),
            label = "particle$index"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        animatedOffsets.forEachIndexed { index, (angle, radius) ->
            val time = animatedValues[index].value + angle
            
            val x = size.width / 2 + cos(Math.toRadians(time.toDouble())).toFloat() * radius
            val y = size.height / 2 + sin(Math.toRadians(time.toDouble())).toFloat() * radius
            
            drawCircle(
                color = if (index % 2 == 0) PurpleAccent.copy(alpha = 0.3f) else CyanPrimary.copy(alpha = 0.2f),
                radius = (8 + index * 2).toFloat(),
                center = Offset(x, y)
            )
        }
    }
}

// ==================== PREVIEWS ====================
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAuthScreenPreview() {
    MainAuthScreen(
        onNavigateToLogin = {},
        onNavigateToSignUp = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToSignUp = {},
        onNavigateBack = {},
        onLogin = { _, _ -> },
        authState = AuthState.Idle
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onNavigateToLogin = {},
        onNavigateBack = {},
        onSignUp = { _, _, _, _ -> },
        authState = AuthState.Idle
    )
}