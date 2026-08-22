package com.systentando.nfc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.systentando.nfc.R
import com.systentando.nfc.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onboardingSeen: Boolean,
    hasPendingTag: Boolean,
    onGoToOnboarding: () -> Unit,
    onGoToHome: () -> Unit
) {
    LaunchedEffect(onboardingSeen) {
        if (onboardingSeen) {
            delay(1200)
            if (hasPendingTag) {
                onGoToHome()
            } else {
                onGoToHome()
            }
        } else {
            delay(1200)
            onGoToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        if (onboardingSeen) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_splash),
                    contentDescription = null,
                    modifier = Modifier.size(220.dp)
                )

                Text(
                    text = "NFC Toolkit",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "by Systentando",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceMuted
                )
            }
        } else {
            CircularProgressIndicator(color = PrimaryTeal)
        }
    }
}
