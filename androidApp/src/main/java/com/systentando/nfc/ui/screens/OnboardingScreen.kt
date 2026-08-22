package com.systentando.nfc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systentando.nfc.R
import com.systentando.nfc.ui.theme.*
import com.systentando.nfc.ui.viewmodels.OnboardingViewModel

data class OnboardingPage(
    val image: Int,
    val title: String,
    val description: String,
    val actionLabel: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = listOf(
        OnboardingPage(
            image = R.drawable.onboarding_step1,
            title = "Descubra o poder do NFC",
            description = "Aproxime o celular de uma tag e descubra o que ela armazena.",
            actionLabel = "Próximo"
        ),
        OnboardingPage(
            image = R.drawable.onboarding_step2,
            title = "Grave suas Tags",
            description = "Personalize suas tags NFC com informações úteis, como contatos, links ou configurações de Wi-Fi.",
            actionLabel = "Próximo"
        ),
        OnboardingPage(
            image = R.drawable.onboarding_step3,
            title = "Segurança Total",
            description = "Proteção avançada para seus pagamentos e dados com tecnologia NFC. Mantenha tudo seguro e protegido.",
            actionLabel = "Começar"
        )
    )

    var currentPage by remember { mutableStateOf(0) }
    val page = pages[currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                viewModel.markOnboardingSeen()
                onSkip()
            }) {
                Text(
                    text = "Pular",
                    color = PrimaryTeal,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = page.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            color = OnSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(if (index == currentPage) 24.dp else 10.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (index == currentPage) PrimaryTeal else SurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (currentPage < pages.size - 1) {
                    currentPage++
                } else {
                    viewModel.markOnboardingSeen()
                    onFinish()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryTeal, SecondaryGreen)
                        ),
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.actionLabel,
                    color = OnSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
