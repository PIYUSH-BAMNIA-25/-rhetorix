package com.runanywhere.startup_hackathon20

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.AuthState
import com.runanywhere.startup_hackathon20.AuthViewModel
import com.runanywhere.startup_hackathon20.UserData
import kotlin.math.cos
import kotlin.math.sin
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.activity.compose.BackHandler

// Elegant Dark Theme Color Palette with Golden Accents
private val DeepBlack = Color(0xFF0D0D12)
private val RichBlack = Color(0xFF16161D)
private val DarkSlate = Color(0xFF1E1E28)
private val GoldPrimary = Color(0xFFD4AF37)
private val GoldLight = Color(0xFFF4E6B8)
private val GoldDark = Color(0xFFB8963C)
private val AmberAccent = Color(0xFFFFB84D)
private val CopperShine = Color(0xFFE8985E)
private val SilverGray = Color(0xFF9BA4B5)
private val PearlWhite = Color(0xFFF5F5F7)
private val SoftWhite = Color(0xFFE8E8ED)
private val ErrorRose = Color(0xFFFF6B6B)
private val SuccessGreen = Color(0xFF51CF66)

enum class AuthPage {
    MAIN, LOGIN, SIGNUP
}

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    if (password.isEmpty()) return

    val strength = when {
        password.length < 6 -> PasswordStrength.WEAK
        password.length < 10 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }

    val (color, label, bars) = when (strength) {
        PasswordStrength.WEAK -> Triple(ErrorRose, "Weak password", 1)
        PasswordStrength.MEDIUM -> Triple(AmberAccent, "Medium password", 2)
        PasswordStrength.STRONG -> Triple(SuccessGreen, "Strong password", 3)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Strength bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index < bars) color else SilverGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LoadingOverlay(loading: Boolean) {
    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = GoldPrimary
            )
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: (UserData) -> Unit,
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

    // Handle back button - go from Login/SignUp to MAIN
    BackHandler(enabled = currentPage != AuthPage.MAIN) {
        viewModel.resetState()
        currentPage = AuthPage.MAIN
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(800)),
        exit = fadeOut(animationSpec = tween(800))
    ) {
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
                onSignUp = { username, firstName, lastName, email, password, dob ->
                    viewModel.signUp(username, firstName, lastName, email, password, dob)
                },
                authState = authState
            )
        }
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
            .background(DeepBlack)
    ) {
        // Animated luxury background with floating particles
        AnimatedLuxuryBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(0.382f))
            
            // Logo Section - Golden Ratio: 1.618
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.618f)
            ) {
                // Animated Logo with golden glow
                AnimatedLogo()
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // App Name with shimmer effect
                ShimmerText(
                    text = "RETRORIX",
                    fontSize = 52.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tagline
                Text(
                    text = "Master the Art of Debate",
                    fontSize = 16.sp,
                    color = SilverGray,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Light
                )
            }
            
            // Buttons Section - Golden Ratio: 1
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Login Button with golden gradient
                GoldenButton(
                    text = "LOG IN",
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Sign Up Button - outlined style
                OutlinedGoldenButton(
                    text = "SIGN UP",
                    onClick = onNavigateToSignUp,
                    modifier = Modifier.fillMaxWidth()
                )
                
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
    
    val isLoading = authState is AuthState.Loading
    val isEmailValid = remember(email) {
        email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        AnimatedLuxuryBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = DarkSlate.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Profile Circle Avatar with elegant animation
            AnimatedProfileAvatar()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Welcome Text
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PearlWhite,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Sign in to continue your journey",
                fontSize = 15.sp,
                color = SilverGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Error Message
            AnimatedVisibility(
                visible = authState is AuthState.Error,
                enter = slideInVertically(animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = slideOutVertically(animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                ErrorCard(message = (authState as? AuthState.Error)?.message ?: "")
            }
            
            // Email Field
            ElegantTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                enabled = !isLoading,
                isError = !isEmailValid,
                errorMessage = if (!isEmailValid) "Invalid email format" else null,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Password Field
            ElegantTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = {
                    passwordVisible = !passwordVisible
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                enabled = !isLoading,
                isError = password.length < 6,
                errorMessage = if (password.length < 6) "Password must be at least 6 characters" else null,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (email.isNotBlank() && password.isNotBlank() && isEmailValid) {
                            onLogin(email, password)
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Sign In Button
            GoldenButton(
                text = if (isLoading) "SIGNING IN..." else "SIGN IN",
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank() && isEmailValid) {
                        onLogin(email, password)
                    }
                },
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading && isEmailValid,
                loading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = SilverGray,
                    fontSize = 15.sp
                )
                Text(
                    text = "Sign Up",
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToSignUp() }
                )
            }
        }
    }
}

// ==================== SIGN UP SCREEN ====================
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onSignUp: (String, String, String, String, String, String) -> Unit,
    authState: AuthState
) {
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isLoading = authState is AuthState.Loading
    val isSignUpSuccess = authState is AuthState.SignUpSuccess
    val passwordsMatch = password == confirmPassword || confirmPassword.isEmpty()
    val isEmailValid = remember(email) {
        email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid = password.length >= 6 || password.isEmpty()
    val allFieldsFilled = username.isNotBlank() && firstName.isNotBlank() &&
            lastName.isNotBlank() && email.isNotBlank() &&
            dateOfBirth.isNotBlank() && password.isNotBlank() &&
            confirmPassword.isNotBlank()

    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    // Auto-navigate to login after successful signup (after 2 seconds)
    LaunchedEffect(isSignUpSuccess) {
        if (isSignUpSuccess) {
            kotlinx.coroutines.delay(2000)
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        AnimatedLuxuryBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = DarkSlate.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Profile Avatar
            AnimatedProfileAvatar(size = 100.dp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Welcome Text
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PearlWhite,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Join the debate arena today",
                fontSize = 15.sp,
                color = SilverGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
            
            Spacer(modifier = Modifier.height(36.dp))

            // Success Message
            AnimatedVisibility(
                visible = isSignUpSuccess,
                enter = slideInVertically(animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = slideOutVertically(animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                SuccessCard(
                    message = (authState as? AuthState.SignUpSuccess)?.message ?: "Success!",
                    onNavigateToLogin = onNavigateToLogin
                )
            }

            // Error Message
            AnimatedVisibility(
                visible = authState is AuthState.Error,
                enter = slideInVertically(animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = slideOutVertically(animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(
                        300
                    )
                )
            ) {
                ErrorCard(message = (authState as? AuthState.Error)?.message ?: "")
            }
            
            // Username Field
            ElegantTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = "Username",
                leadingIcon = Icons.Default.Person,
                enabled = !isLoading && !isSignUpSuccess,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // First Name & Last Name in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElegantTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = "First Name",
                    leadingIcon = Icons.Default.Person,
                    enabled = !isLoading && !isSignUpSuccess,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                ElegantTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = "Last Name",
                    leadingIcon = Icons.Default.Person,
                    enabled = !isLoading && !isSignUpSuccess,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email Field
            ElegantTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                enabled = !isLoading && !isSignUpSuccess,
                isError = !isEmailValid,
                errorMessage = if (!isEmailValid) "Invalid email format" else null,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth Field with Date Picker
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading && !isSignUpSuccess) { showDatePicker = true },
                placeholder = {
                Text(
                        "Date of Birth",
                        color = SilverGray.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date of Birth",
                        tint = GoldPrimary
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        enabled = !isLoading && !isSignUpSuccess
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick Date",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PearlWhite,
                    unfocusedTextColor = SoftWhite,
                    disabledTextColor = SoftWhite,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = SilverGray.copy(alpha = 0.3f),
                    focusedContainerColor = DarkSlate.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkSlate.copy(alpha = 0.3f),
                    disabledContainerColor = DarkSlate.copy(alpha = 0.3f),
                    cursorColor = GoldPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = false,
                readOnly = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            ElegantTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = {
                    passwordVisible = !passwordVisible
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                enabled = !isLoading && !isSignUpSuccess,
                isError = !isPasswordValid,
                errorMessage = if (!isPasswordValid) "Password must be at least 6 characters" else null,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            PasswordStrengthIndicator(password = password)

            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Password Field
            ElegantTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePasswordVisibility = {
                    confirmPasswordVisible = !confirmPasswordVisible
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                enabled = !isLoading && !isSignUpSuccess,
                isError = !passwordsMatch,
                errorMessage = if (!passwordsMatch) "Passwords do not match" else null,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (allFieldsFilled && passwordsMatch && isEmailValid && isPasswordValid) {
                            onSignUp(username, firstName, lastName, email, password, dateOfBirth)
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // Sign Up Button
            if (!isSignUpSuccess) {
                GoldenButton(
                    text = if (isLoading) "CREATING ACCOUNT..." else "CREATE ACCOUNT",
                    onClick = {
                        if (allFieldsFilled && passwordsMatch && isEmailValid && isPasswordValid) {
                            onSignUp(username, firstName, lastName, email, password, dateOfBirth)
                        }
                    },
                    enabled = allFieldsFilled && passwordsMatch && !isLoading && isEmailValid && isPasswordValid,
                    loading = isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Link
            if (!isSignUpSuccess) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = SilverGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Log In",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToLogin() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { selectedDate ->
                dateOfBirth = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    LoadingOverlay(loading = isLoading)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = dateFormat.format(java.util.Date(selectedMillis))
                        onDateSelected(formattedDate)
                    }
                }
            ) {
                Text("OK", color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SilverGray)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = DarkSlate
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = DarkSlate,
                titleContentColor = PearlWhite,
                headlineContentColor = PearlWhite,
                weekdayContentColor = SilverGray,
                subheadContentColor = GoldPrimary,
                yearContentColor = SoftWhite,
                currentYearContentColor = GoldPrimary,
                selectedYearContentColor = DeepBlack,
                selectedYearContainerColor = GoldPrimary,
                dayContentColor = SoftWhite,
                selectedDayContentColor = DeepBlack,
                selectedDayContainerColor = GoldPrimary,
                todayContentColor = GoldPrimary,
                todayDateBorderColor = GoldPrimary
            )
        )
    }
}

@Composable
fun SuccessCard(message: String, onNavigateToLogin: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuccessGreen.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = message,
                    color = SuccessGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SuccessGreen),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SuccessGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "GO TO LOGIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ==================== REUSABLE COMPONENTS ====================

@Composable
fun AnimatedLogo(size: Int = 140) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating golden ring
        Canvas(modifier = Modifier
            .size((size + 20).dp)
            .rotate(rotation)
        ) {
            val radius = this.size.width / 2
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GoldPrimary,
                        GoldLight,
                        AmberAccent,
                        GoldPrimary
                    )
                ),
                radius = radius,
                style = Stroke(width = 3.dp.toPx())
            )
        }
        
        // Inner glow
        Canvas(modifier = Modifier.size((size + 40).dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldPrimary.copy(alpha = 0.4f),
                        GoldPrimary.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                radius = this.size.width / 2
            )
        }
        
        // R Letter
        Text(
            text = "R",
            fontSize = (size * 0.6f).sp,
            fontWeight = FontWeight.ExtraBold,
            color = GoldPrimary,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
fun ShimmerText(text: String, fontSize: androidx.compose.ui.unit.TextUnit) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 4.sp,
        style = MaterialTheme.typography.displayMedium.copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    PearlWhite,
                    GoldLight,
                    PearlWhite,
                    GoldLight,
                    PearlWhite
                ),
                start = Offset(offset * 1000, 0f),
                end = Offset((offset + 1) * 1000, 0f)
            )
        )
    )
}

@Composable
fun AnimatedProfileAvatar(size: androidx.compose.ui.unit.Dp = 120.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avatarRotation"
    )
    
    Box(
        modifier = Modifier.size(size + 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating gradient border
        Canvas(
            modifier = Modifier
                .size(size + 20.dp)
                .rotate(rotation)
        ) {
            val radius = this.size.width / 2
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GoldPrimary,
                        AmberAccent,
                        CopperShine,
                        GoldLight,
                        GoldPrimary
                    )
                ),
                radius = radius,
                style = Stroke(width = 3.dp.toPx())
            )
        }
        
        // Avatar Circle
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.2f),
                            DarkSlate.copy(alpha = 0.8f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = GoldPrimary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
fun GoldenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current

    Button(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.height(62.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = DeepBlack,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = SilverGray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(31.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled && !loading) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                GoldDark,
                                GoldPrimary,
                                GoldLight,
                                GoldPrimary,
                                GoldDark
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                SilverGray.copy(alpha = 0.3f),
                                SilverGray.copy(alpha = 0.3f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(31.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = DeepBlack,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (enabled) DeepBlack else SilverGray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun OutlinedGoldenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(62.dp),
        enabled = enabled,
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(GoldDark, GoldPrimary, GoldLight)
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DarkSlate.copy(alpha = 0.3f),
            contentColor = GoldPrimary,
            disabledContainerColor = DarkSlate.copy(alpha = 0.1f),
            disabledContentColor = SilverGray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(31.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun ElegantTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    placeholder,
                    color = SilverGray.copy(alpha = 0.7f),
                    fontSize = 15.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = placeholder,
                    tint = if (isError) ErrorRose else GoldPrimary
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Clear else Icons.Default.Lock,
                            contentDescription = "Toggle password",
                            tint = SilverGray
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PearlWhite,
                unfocusedTextColor = SoftWhite,
                disabledTextColor = SilverGray.copy(alpha = 0.5f),
                focusedBorderColor = if (isError) ErrorRose else GoldPrimary,
                unfocusedBorderColor = if (isError) ErrorRose.copy(alpha = 0.5f) else SilverGray.copy(alpha = 0.3f),
                focusedContainerColor = DarkSlate.copy(alpha = 0.5f),
                unfocusedContainerColor = DarkSlate.copy(alpha = 0.3f),
                disabledContainerColor = DarkSlate.copy(alpha = 0.2f),
                cursorColor = GoldPrimary,
                errorBorderColor = ErrorRose,
                errorCursorColor = ErrorRose
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = enabled,
            isError = isError
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ErrorRose,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ErrorRose.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ErrorRose.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = ErrorRose,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = ErrorRose,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AnimatedLuxuryBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Create multiple floating orbs
    val orbs = remember {
        List(6) { index ->
            Triple(
                (0..100).random() / 100f, // x position
                (0..100).random() / 100f, // y position
                (30..80).random() // size
            )
        }
    }
    
    val animatedValues = orbs.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 15000 + index * 3000,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "orb$index"
        )
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    DeepBlack,
                    RichBlack,
                    DeepBlack
                )
            )
        )
        
        // Floating orbs with motion
        orbs.forEachIndexed { index, (xPos, yPos, orbSize) ->
            val angle = animatedValues[index].value
            val radiusX = size.width * 0.15f
            val radiusY = size.height * 0.1f
            
            val centerX = size.width * xPos
            val centerY = size.height * yPos
            
            val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * radiusX
            val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * radiusY
            
            // Orb glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        when (index % 3) {
                            0 -> GoldPrimary.copy(alpha = 0.15f)
                            1 -> AmberAccent.copy(alpha = 0.12f)
                            else -> CopperShine.copy(alpha = 0.1f)
                        },
                        Color.Transparent
                    )
                ),
                radius = orbSize.toFloat(),
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
        onSignUp = { _, _, _, _, _, _ -> },
        authState = AuthState.Idle
    )
}