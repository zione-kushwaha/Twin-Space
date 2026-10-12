package com.twinspace.cloner.ui.screens

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ServiceWorkerController
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.twinspace.cloner.core.WebTwinService
import com.twinspace.cloner.ui.theme.AppClonerDarkTeal
import com.twinspace.cloner.ui.theme.AppClonerDeepTeal
import com.twinspace.cloner.ui.theme.AppClonerLightMint
import com.twinspace.cloner.ui.theme.AppClonerPageBg
import com.twinspace.cloner.ui.theme.AppClonerTeal
import com.twinspace.cloner.ui.theme.TextDarkPrimary
import com.twinspace.cloner.ui.theme.TextDarkSecondary

val defaultWebTwins = listOf(
    WebTwinService("whatsapp", "WhatsApp Web", "https://web.whatsapp.com", 0xFF25D366, "Second WhatsApp account (Desktop scan mode)"),
    WebTwinService("telegram", "Telegram Web", "https://web.telegram.org/k/", 0xFF229ED9, "Second Telegram login (Fast Web K)"),
    WebTwinService("instagram", "Instagram", "https://www.instagram.com", 0xFFE1306C, "Dual Instagram feed & stories"),
    WebTwinService("facebook", "Facebook", "https://m.facebook.com", 0xFF1877F2, "Second Facebook profile & Messenger"),
    WebTwinService("twitter", "X / Twitter", "https://x.com", 0xFF1DA1F2, "Second X / Twitter account"),
    WebTwinService("tiktok", "TikTok", "https://www.tiktok.com", 0xFFEE1D52, "Separate TikTok creator login"),
    WebTwinService("discord", "Discord", "https://discord.com/login", 0xFF5865F2, "Alt Discord server user"),
    WebTwinService("reddit", "Reddit", "https://www.reddit.com", 0xFFFF4500, "Alt Reddit account")
)

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebTwinScreen(
    modifier: Modifier = Modifier,
    onActiveChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var activeService by remember { mutableStateOf<WebTwinService?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val customServices = remember { mutableStateListOf<WebTwinService>() }

    var currentUrl by remember { mutableStateOf("") }
    var urlBarText by remember { mutableStateOf("") }
    var isEditingUrl by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // File chooser callback for attachments (camera, photos, files)
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            fileUploadCallback?.onReceiveValue(arrayOf(uri))
        } else {
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }

    // Hardware/gesture back button handling
    BackHandler(enabled = activeService != null) {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            activeService = null
            onActiveChanged(false)
        }
    }

    if (activeService != null) {
        val service = activeService!!

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Subtle top loading progress indicator (like real mobile browsers, disappears when loaded)
            if (loadProgress in 0.01f..0.99f) {
                LinearProgressIndicator(
                    progress = { loadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .align(Alignment.TopCenter),
                    color = com.twinspace.cloner.ui.theme.TwinPrimary,
                    trackColor = Color.Transparent,
                )
            }

            // Error Overlay if Web Fails
            if (loadError != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Unable to load page",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDarkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = loadError ?: "Please check internet connection",
                        fontSize = 13.sp,
                        color = TextDarkSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                loadError = null
                                webViewRef?.reload()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppClonerTeal)
                        ) {
                            Text("Retry")
                        }
                        OutlinedButton(
                            onClick = {
                                isDesktopMode = !isDesktopMode
                                webViewRef?.let {
                                    applyDesktopSettings(it.settings, isDesktopMode)
                                    loadError = null
                                    it.reload()
                                }
                            }
                        ) {
                            Text(if (isDesktopMode) "Try Mobile View" else "Try Desktop View")
                        }
                    }
                }
            } else {
                // Native Android WebView with Perfect Viewport & Screen Sizing
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            setBackgroundColor(android.graphics.Color.WHITE)

                            // Comprehensive Web & Viewport Settings for Screen Adaptation
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                allowFileAccess = true
                                allowContentAccess = true
                                mediaPlaybackRequiresUserGesture = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                javaScriptCanOpenWindowsAutomatically = true

                                // Zoom & Viewport settings for 720x1612 & high-DPI displays
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                                textZoom = 100
                                defaultTextEncodingName = "UTF-8"
                            }

                            // WhatsApp Web requires desktop mode by default for QR scanner
                            val initialDesktop = service.id == "whatsapp"
                            isDesktopMode = initialDesktop
                            applyDesktopSettings(settings, initialDesktop)

                            // Enable service workers for PWAs & web apps
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                try {
                                    ServiceWorkerController.getInstance().serviceWorkerWebSettings.apply {
                                        allowContentAccess = true
                                        allowFileAccess = true
                                        blockNetworkLoads = false
                                    }
                                } catch (_: Exception) {}
                            }

                            // Cookies Support
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            // Download Listener
                            setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                                try {
                                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                                        setMimeType(mimetype)
                                        addRequestHeader("User-Agent", userAgent)
                                        setDescription("Downloading file from Web Twin...")
                                        setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS,
                                            URLUtil.guessFileName(url, contentDisposition, mimetype)
                                        )
                                    }
                                    val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    dm.enqueue(request)
                                    Toast.makeText(ctx, "Download started...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Download error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress / 100f
                                }

                                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                    android.util.Log.d("WebTwin", "${consoleMessage?.message()} [${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()}]")
                                    return true
                                }

                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    try {
                                        request?.grant(request.resources)
                                    } catch (_: Exception) {}
                                }

                                override fun onGeolocationPermissionsShowPrompt(
                                    origin: String?,
                                    callback: GeolocationPermissions.Callback?
                                ) {
                                    callback?.invoke(origin, true, false)
                                }

                                override fun onShowFileChooser(
                                    webView: WebView?,
                                    filePathCallback: ValueCallback<Array<Uri>>?,
                                    fileChooserParams: FileChooserParams?
                                ): Boolean {
                                    fileUploadCallback = filePathCallback
                                    val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "*/*"
                                    }
                                    filePickerLauncher.launch(intent)
                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return if (url.startsWith("http://") || url.startsWith("https://")) {
                                        false
                                    } else {
                                        try {
                                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                            true
                                        } catch (_: Exception) {
                                            true
                                        }
                                    }
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    loadProgress = 0.15f
                                    currentUrl = url ?: ""
                                    canGoBack = canGoBack()
                                    canGoForward = canGoForward()
                                    loadError = null
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    loadProgress = 1f
                                    currentUrl = url ?: ""
                                    canGoBack = canGoBack()
                                    canGoForward = canGoForward()
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        loadError = error?.description?.toString() ?: "Network error"
                                    }
                                }
                            }

                            loadUrl(service.url)
                            webViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        // Grid View of Web Twins (Pure Light Mode)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(com.twinspace.cloner.ui.theme.TwinBg)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Web Twins (Multi-Account)",
                        color = com.twinspace.cloner.ui.theme.TwinTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Isolated web sandboxes for dual accounts without extra apps",
                        color = com.twinspace.cloner.ui.theme.TwinTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Add Custom Web Twin Button
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .background(com.twinspace.cloner.ui.theme.TwinPrimarySoft, CircleShape)
                        .border(1.dp, com.twinspace.cloner.ui.theme.TwinBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Web Twin",
                        tint = com.twinspace.cloner.ui.theme.TwinPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val allServices = defaultWebTwins + customServices

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allServices, key = { it.id }) { service ->
                    WebTwinCard(
                        service = service,
                        onClick = {
                            activeService = service
                            onActiveChanged(true)
                        }
                    )
                }

                item {
                    // Add Custom Card
                    Card(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = com.twinspace.cloner.ui.theme.TwinSurfaceAlt),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.twinspace.cloner.ui.theme.TwinBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = com.twinspace.cloner.ui.theme.TwinPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Custom Web Twin",
                                color = com.twinspace.cloner.ui.theme.TwinTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Any website or web app",
                                color = com.twinspace.cloner.ui.theme.TwinTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Custom Web Twin Dialog
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("https://") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add Custom Web Twin", fontWeight = FontWeight.Bold, color = TextDarkPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the website name and URL to create an isolated multi-account web twin:",
                        color = TextDarkSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Service Name (e.g. ChatGPT, Slack)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppClonerTeal,
                            focusedLabelColor = AppClonerTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("URL") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppClonerTeal,
                            focusedLabelColor = AppClonerTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newUrl.isNotBlank()) {
                            val formattedUrl = sanitizeUrlOrSearch(newUrl)
                            customServices.add(
                                WebTwinService(
                                    id = "custom_${System.currentTimeMillis()}",
                                    name = newName.trim(),
                                    url = formattedUrl.trim(),
                                    colorHex = 0xFF4EAA99,
                                    description = "Custom isolated sandbox"
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppClonerTeal)
                ) {
                    Text("Add Twin", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextDarkSecondary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

private fun sanitizeUrlOrSearch(input: String): String {
    val trimmed = input.trim()
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else if (trimmed.contains(".") && !trimmed.contains(" ")) {
        "https://$trimmed"
    } else {
        "https://www.google.com/search?q=${Uri.encode(trimmed)}"
    }
}

private fun applyDesktopSettings(settings: WebSettings, isDesktop: Boolean) {
    settings.useWideViewPort = true
    settings.loadWithOverviewMode = true
    if (isDesktop) {
        settings.userAgentString = DESKTOP_USER_AGENT
    } else {
        settings.userAgentString = null // Default mobile chrome
    }
}

@Composable
fun WebTwinCard(
    service: WebTwinService,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = com.twinspace.cloner.ui.theme.TwinSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.twinspace.cloner.ui.theme.TwinBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(service.colorHex).copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = service.name,
                    tint = Color(service.colorHex),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = service.name,
                color = com.twinspace.cloner.ui.theme.TwinTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = service.description,
                color = com.twinspace.cloner.ui.theme.TwinTextSecondary,
                fontSize = 11.sp,
                maxLines = 2
            )
        }
    }
}
