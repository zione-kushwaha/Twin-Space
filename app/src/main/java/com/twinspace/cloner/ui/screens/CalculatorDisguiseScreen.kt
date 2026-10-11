package com.twinspace.cloner.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twinspace.cloner.ui.theme.AppClonerDarkTeal
import com.twinspace.cloner.ui.theme.AppClonerLightMint
import com.twinspace.cloner.ui.theme.AppClonerTeal
import com.twinspace.cloner.ui.theme.TextDarkPrimary
import com.twinspace.cloner.ui.theme.TextDarkSecondary
import java.text.DecimalFormat

// Ultra-clean Light Mode Calculator Theme Colors
private val CalcBg = Color(0xFFF8FAFC)
private val CalcKeypadBg = Color(0xFFFFFFFF)
private val CalcDisplayBg = Color(0xFFF8FAFC)

private val NumberKeyBg = Color(0xFFF1F5F9)
private val NumberKeyText = Color(0xFF0F172A)

private val FunctionKeyBg = Color(0xFFEEF2FF)
private val FunctionKeyText = Color(0xFF4F46E5)

private val OperatorKeyBg = Color(0xFFE0E7FF)
private val OperatorKeyText = Color(0xFF4338CA)

private val ClearKeyBg = Color(0xFFFEE2E2)
private val ClearKeyText = Color(0xFFDC2626)

private val EqualsKeyBg = Color(0xFF4F46E5)
private val EqualsKeyText = Color.White

@Composable
fun CalculatorDisguiseScreen(
    onUnlock: (String) -> Boolean,
    modifier: Modifier = Modifier,
    defaultPinHint: String = "1234"
) {
    val context = LocalContext.current
    var displayText by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    var showSecretHintDialog by remember { mutableStateOf(false) }
    var justCalculated by remember { mutableStateOf(false) }

    fun handleInput(token: String) {
        when (token) {
            "C" -> {
                displayText = "0"
                expressionHistory = ""
                justCalculated = false
            }
            "DEL" -> {
                if (displayText.length > 1 && displayText != "Error") {
                    displayText = displayText.dropLast(1)
                } else {
                    displayText = "0"
                }
                justCalculated = false
            }
            "±" -> {
                if (displayText != "0" && displayText != "Error") {
                    displayText = if (displayText.startsWith("-")) {
                        displayText.substring(1)
                    } else {
                        "-$displayText"
                    }
                }
            }
            "%" -> {
                try {
                    val num = displayText.toDoubleOrNull()
                    if (num != null) {
                        val pct = num / 100.0
                        displayText = formatNumber(pct)
                    }
                } catch (_: Exception) {
                    displayText = "Error"
                }
            }
            "=" -> {
                // First check if current raw display text matches secret PIN
                val rawDigits = displayText.filter { it.isDigit() }
                val unlocked = onUnlock(displayText) || (rawDigits.isNotEmpty() && onUnlock(rawDigits))
                if (!unlocked) {
                    // Perform real, believable arithmetic calculation
                    try {
                        val fullExpr = if (expressionHistory.isNotEmpty() && !justCalculated) {
                            "$expressionHistory$displayText"
                        } else {
                            displayText
                        }
                        val result = evaluateMathExpression(fullExpr)
                        expressionHistory = "$fullExpr = "
                        displayText = result
                        justCalculated = true
                    } catch (_: Exception) {
                        displayText = "Error"
                        expressionHistory = ""
                    }
                }
            }
            "+", "−", "×", "÷" -> {
                if (displayText != "Error") {
                    expressionHistory = "$displayText $token "
                    displayText = "0"
                    justCalculated = false
                }
            }
            "." -> {
                if (justCalculated) {
                    displayText = "0."
                    expressionHistory = ""
                    justCalculated = false
                } else if (!displayText.contains(".")) {
                    displayText += "."
                }
            }
            else -> { // Digit
                if (displayText == "0" || displayText == "Error" || justCalculated) {
                    displayText = token
                    if (justCalculated) expressionHistory = ""
                    justCalculated = false
                } else {
                    if (displayText.length < 15) {
                        displayText += token
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CalcBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Realistic Top Bar (Deg/Rad indicator & Discreet Vault Info button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF22252E))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DEG",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calculator",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Discreet info/hint button for users to recall default PIN without breaking disguise
            IconButton(
                onClick = { showSecretHintDialog = true },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Calculator Info",
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Display Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CalcDisplayBg)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // Expression History line
            if (expressionHistory.isNotEmpty()) {
                Text(
                    text = expressionHistory,
                    color = Color(0xFF94A3B8),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Main Display Number
            val textLength = displayText.length
            val fontSize = when {
                textLength > 12 -> 34.sp
                textLength > 9 -> 42.sp
                textLength > 6 -> 52.sp
                else -> 60.sp
            }

            Text(
                text = displayText,
                color = NumberKeyText,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.End,
                maxLines = 1,
                lineHeight = fontSize
            )
        }

        // Keypad Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(CalcKeypadBg)
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Symmetrical 5x4 Grid
                val keyRows = listOf(
                    listOf("C", "±", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "−"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "DEL", "=")
                )

                for (row in keyRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (key in row) {
                            val isAction = key in listOf("÷", "×", "−", "+")
                            val isEquals = key == "="
                            val isClear = key == "C"
                            val isFunction = key in listOf("±", "%", "DEL")

                            val bg = when {
                                isEquals -> EqualsKeyBg
                                isClear -> ClearKeyBg
                                isAction -> OperatorKeyBg
                                isFunction -> FunctionKeyBg
                                else -> NumberKeyBg
                            }

                            val textColor = when {
                                isEquals -> EqualsKeyText
                                isClear -> ClearKeyText
                                isAction -> OperatorKeyText
                                isFunction -> FunctionKeyText
                                else -> NumberKeyText
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bg)
                                    .clickable { handleInput(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = textColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        color = textColor,
                                        fontSize = if (isAction || isEquals) 26.sp else 23.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Discreet Vault Help Dialog
    if (showSecretHintDialog) {
        AlertDialog(
            onDismissRequest = { showSecretHintDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(AppClonerLightMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = AppClonerDarkTeal,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Secret Vault Access",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "To unlock your hidden TwinSpace vault:",
                        color = TextDarkSecondary,
                        fontSize = 13.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppClonerLightMint)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1. Type PIN: $defaultPinHint\n2. Press '=' (Equals)",
                            color = AppClonerDarkTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                    Text(
                        text = "Default PIN is '$defaultPinHint'. You can change this in Settings.",
                        color = TextDarkSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecretHintDialog = false }) {
                    Text("Got It", color = AppClonerTeal, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Complete arithmetic evaluator (+, -, *, /) with operator precedence and percent
private fun evaluateMathExpression(expr: String): String {
    val clean = expr.trim()
        .replace("×", "*")
        .replace("÷", "/")
        .replace("−", "-")
        .replace(" ", "")

    if (clean.isEmpty()) return "0"

    try {
        val tokens = tokenize(clean)
        val result = parseAdditionSubtraction(tokens)
        return formatNumber(result)
    } catch (_: Exception) {
        return "Error"
    }
}

private fun formatNumber(value: Double): String {
    return if (value.isNaN() || value.isInfinite()) {
        "Error"
    } else if (value % 1.0 == 0.0 && Math.abs(value) < 1e12) {
        value.toLong().toString()
    } else {
        val df = DecimalFormat("#.########")
        df.format(value)
    }
}

private fun tokenize(s: String): List<String> {
    val list = mutableListOf<String>()
    var i = 0
    while (i < s.length) {
        val c = s[i]
        if (c in "+*/") {
            list.add(c.toString())
            i++
        } else if (c == '-') {
            // check if negative number or subtraction
            if (i == 0 || list.lastOrNull() in listOf("+", "-", "*", "/")) {
                // Negative number sign
                var num = "-"
                i++
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                    num += s[i]
                    i++
                }
                list.add(num)
            } else {
                list.add("-")
                i++
            }
        } else if (c.isDigit() || c == '.') {
            var num = ""
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                num += s[i]
                i++
            }
            list.add(num)
        } else {
            i++
        }
    }
    return list
}

private fun parseAdditionSubtraction(tokens: List<String>): Double {
    if (tokens.isEmpty()) return 0.0

    // First evaluate all multiplications and divisions into a reduced list
    val reducedTokens = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        if (token == "*" || token == "/") {
            val left = reducedTokens.removeAt(reducedTokens.size - 1).toDouble()
            val right = tokens[i + 1].toDouble()
            val res = if (token == "*") left * right else left / right
            reducedTokens.add(res.toString())
            i += 2
        } else {
            reducedTokens.add(token)
            i++
        }
    }

    // Now evaluate addition and subtraction from left to right
    var result = reducedTokens[0].toDouble()
    var j = 1
    while (j < reducedTokens.size) {
        val op = reducedTokens[j]
        val right = reducedTokens[j + 1].toDouble()
        if (op == "+") {
            result += right
        } else if (op == "-") {
            result -= right
        }
        j += 2
    }
    return result
}
