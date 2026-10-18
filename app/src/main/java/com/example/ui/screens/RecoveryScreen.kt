package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBgBase
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenContainer
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

enum class RecoveryStep {
    INPUT_EMAIL,
    INPUT_CODE,
    NEW_PASSWORD,
    SUCCESS
}

@Composable
fun RecoveryScreen(
    onSendRecoveryCode: (email: String) -> Result<String>,
    onVerifyRecoveryCode: (email: String, code: String) -> Boolean,
    onResetPassword: (email: String, newPassword: String) -> Result<Unit>,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(RecoveryStep.INPUT_EMAIL) }
    var emailInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var generatedCodePreview by remember { mutableStateOf<String?>(null) }

    // 6-digit OTP code boxes
    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }

    // New password states
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // 15-minute countdown
    var secondsLeft by remember { mutableIntStateOf(900) }
    LaunchedEffect(currentStep) {
        if (currentStep == RecoveryStep.INPUT_CODE) {
            secondsLeft = 900
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft -= 1
            }
        }
    }
    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, secs)

    BackHandler {
        if (currentStep == RecoveryStep.INPUT_CODE) {
            currentStep = RecoveryStep.INPUT_EMAIL
        } else if (currentStep == RecoveryStep.NEW_PASSWORD) {
            currentStep = RecoveryStep.INPUT_CODE
        } else {
            onBackToLogin()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation Sub-header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainerHigh)
                    .clickable {
                        if (currentStep == RecoveryStep.INPUT_CODE) {
                            currentStep = RecoveryStep.INPUT_EMAIL
                        } else if (currentStep == RecoveryStep.NEW_PASSWORD) {
                            currentStep = RecoveryStep.INPUT_CODE
                        } else {
                            onBackToLogin()
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("back_to_login_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentStep == RecoveryStep.INPUT_EMAIL) "Voltar ao Login" else "Voltar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CANAL SEGURO 256-BIT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Header Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceContainerHigh)
                .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        when (currentStep) {
                            RecoveryStep.SUCCESS -> NeonGreen.copy(alpha = 0.2f)
                            RecoveryStep.NEW_PASSWORD -> NeonViolet.copy(alpha = 0.2f)
                            else -> ElectricCyan.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (currentStep) {
                        RecoveryStep.SUCCESS -> Icons.Default.CheckCircle
                        RecoveryStep.NEW_PASSWORD -> Icons.Default.Key
                        RecoveryStep.INPUT_CODE -> Icons.Default.MarkEmailRead
                        RecoveryStep.INPUT_EMAIL -> Icons.Default.LockReset
                    },
                    contentDescription = null,
                    tint = when (currentStep) {
                        RecoveryStep.SUCCESS -> NeonGreen
                        RecoveryStep.NEW_PASSWORD -> NeonViolet
                        else -> ElectricCyan
                    },
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (currentStep) {
                    RecoveryStep.INPUT_EMAIL -> "Recuperar Conta RanKUs"
                    RecoveryStep.INPUT_CODE -> "Validação de Código"
                    RecoveryStep.NEW_PASSWORD -> "Criar Nova Senha"
                    RecoveryStep.SUCCESS -> "Senha Redefinida!"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (currentStep) {
                    RecoveryStep.INPUT_EMAIL -> "Digite seu e-mail cadastrado para receber o código aleatório de 6 dígitos e recuperar seu acesso."
                    RecoveryStep.INPUT_CODE -> "Insira abaixo o código de 6 dígitos enviado para $emailInput."
                    RecoveryStep.NEW_PASSWORD -> "Crie uma nova senha segura para vincular à sua conta gamer RanKUs."
                    RecoveryStep.SUCCESS -> "Sua nova senha foi salva e vinculada à sua conta com sucesso! Faça login para continuar competindo."
                },
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Step Content
        AnimatedContent(targetState = currentStep, label = "recovery_step_anim") { step ->
            Column(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    RecoveryStep.INPUT_EMAIL -> {
                        // Step 1: Input registered email
                        Text(
                            text = "Seu E-mail Cadastrado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            placeholder = {
                                Text("seu.email@dominio.com", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.5f))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = OutlineBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recovery_email_input")
                        )

                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = AlertRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                val result = onSendRecoveryCode(emailInput)
                                result.onSuccess { code ->
                                    generatedCodePreview = code
                                    // Open user's email client with code instructions
                                    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$emailInput")
                                        putExtra(Intent.EXTRA_SUBJECT, "RanKUs - Código de Recuperação de Senha")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Olá competidor RanKUs!\n\nSeu código de verificação para redefinir a senha é:\n\n$code\n\nCopie este código para o aplicativo para criar sua nova senha. Válido por 15 minutos."
                                        )
                                    }
                                    try {
                                        context.startActivity(Intent.createChooser(mailIntent, "Abrir e-mail"))
                                    } catch (_: Exception) {
                                        // Ignore if no email app installed
                                    }
                                    currentStep = RecoveryStep.INPUT_CODE
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "E-mail não encontrado."
                                }
                            },
                            enabled = emailInput.isNotBlank() && emailInput.contains("@"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyanContainer,
                                contentColor = ElectricCyanDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_recovery_code_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Receber Código no E-mail", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    RecoveryStep.INPUT_CODE -> {
                        // Code generated banner with copy option
                        generatedCodePreview?.let { code ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ElectricCyan.copy(alpha = 0.12f))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Código de 6 dígitos gerado:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = code,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ElectricCyan,
                                            letterSpacing = 4.sp
                                        )
                                        Text(
                                            text = "Também enviado para $emailInput",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("RanKUs Code", code))
                                            Toast.makeText(context, "Código copiado!", Toast.LENGTH_SHORT).show()
                                            // Auto-fill digits
                                            code.take(6).forEachIndexed { i, c ->
                                                otpDigits[i] = c.toString()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricCyan,
                                            contentColor = ElectricCyanDark
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copiar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // 6-digit input boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (index in 0 until 6) {
                                OutlinedTextField(
                                    value = otpDigits[index],
                                    onValueChange = { newVal ->
                                        if (newVal.length <= 1) {
                                            otpDigits[index] = newVal.filter { it.isDigit() }
                                            errorMessage = null
                                        } else if (newVal.length == 6) {
                                            // Handle paste
                                            newVal.forEachIndexed { i, c ->
                                                if (i < 6) otpDigits[i] = c.toString()
                                            }
                                        }
                                    },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        color = TextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceContainerHigh,
                                        unfocusedContainerColor = SurfaceContainerHigh,
                                        focusedBorderColor = NeonGreen,
                                        unfocusedBorderColor = OutlineBorder
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .testTag("otp_digit_$index")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Timer and Paste shortcut
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expira em: $formattedTime",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (secondsLeft < 120) AlertRed else TextSecondary
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerHigh)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                        val digits = clipText.filter { it.isDigit() }.take(6)
                                        if (digits.length == 6) {
                                            digits.forEachIndexed { i, c -> otpDigits[i] = c.toString() }
                                            Toast.makeText(context, "Código colado!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Copie primeiro o código de 6 dígitos.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Colar Código", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                            }
                        }

                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = AlertRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val fullCode = otpDigits.joinToString("")
                        Button(
                            onClick = {
                                errorMessage = null
                                val isValid = onVerifyRecoveryCode(emailInput, fullCode)
                                if (isValid) {
                                    currentStep = RecoveryStep.NEW_PASSWORD
                                } else {
                                    errorMessage = "Código incorreto ou expirado. Verifique os números digitados."
                                }
                            },
                            enabled = fullCode.length == 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreenContainer,
                                contentColor = NeonGreenDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("validate_code_btn")
                        ) {
                            Text(text = "Validar Código", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.East, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    RecoveryStep.NEW_PASSWORD -> {
                        // Step 3: Enter new password
                        Text(
                            text = "Nova Senha",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                errorMessage = null
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            placeholder = { Text("Mínimo 6 caracteres", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = OutlineBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_password_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Confirmar Nova Senha",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = {
                                confirmNewPassword = it
                                errorMessage = null
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            placeholder = { Text("Repita a nova senha", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = OutlineBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_new_password_input")
                        )

                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = AlertRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val canSave = newPassword.length >= 6 && confirmNewPassword.isNotBlank()
                        Button(
                            onClick = {
                                errorMessage = null
                                if (newPassword != confirmNewPassword) {
                                    errorMessage = "As senhas não coincidem. Digite novamente."
                                    return@Button
                                }
                                val result = onResetPassword(emailInput, newPassword)
                                result.onSuccess {
                                    currentStep = RecoveryStep.SUCCESS
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "Falha ao redefinir senha."
                                }
                            },
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreenContainer,
                                contentColor = NeonGreenDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_new_password_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Salvar Nova Senha", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    RecoveryStep.SUCCESS -> {
                        // Step 4: Success confirmation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NeonGreen.copy(alpha = 0.12f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Senha Atualizada!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sua credencial de acesso foi alterada com sucesso. Entre com seu e-mail e nova senha para competir.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onBackToLogin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreenContainer,
                                contentColor = NeonGreenDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("goto_login_after_recovery_btn")
                        ) {
                            Text(text = "Ir para o Login", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.East, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Proteção de Conta RanKUs: o suporte oficial nunca solicita sua senha. Mantenha seus registros e conquistas protegidos.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
