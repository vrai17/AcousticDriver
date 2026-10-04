package com.vraidev.acousticdriver.ui.screens.splash

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.R
import com.vraidev.acousticdriver.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("Initializing Audio Engine…") }
    var progress by remember { mutableStateOf(0.1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    LaunchedEffect(Unit) {
        delay(400)
        statusText = "Scanning Audio HAL & DSP Capabilities…"
        progress = 0.45f
        delay(500)
        statusText = "Translating Heritage Acoustic Drivers…"
        progress = 0.85f
        delay(500)
        statusText = "Vrai-Dev Engine Ready"
        progress = 1.0f
        delay(350)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .clickable { onSplashFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // High-res Dev Logo
            Image(
                painter = painterResource(id = R.drawable.dev_logo),
                contentDescription = "Vrai-Dev Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
                    .scale(glowScale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name & Subtitle
            Text(
                text = "ACOUSTIC DRIVER",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricIce,
                letterSpacing = 2.sp
            )

            Text(
                text = "Modern Heritage Audio Tuning Platform",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp),
                color = CyberCyan,
                trackColor = SurfaceVariantDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusText,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Bottom Developer Credit & Website link
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Engineered by Vrai-Dev",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "vraidev.com",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vraidev.com"))
                    context.startActivity(intent)
                }
            )
        }
    }
}
