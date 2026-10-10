package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.state.ActiveScreen
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.theme.*

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val iconRes: Int,
    val badge: String
)

@Composable
fun WelcomeOnboardingScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Journeys Rooted Here",
                subtitle = "DISCOVER DRÂA-TAFILALET",
                description = "Traverse ancient caravan ksour, UNESCO Ait Ben Haddou, sheer Todra limestone gorges, and golden Erg Chebbi dunes.",
                iconRes = R.drawable.art_ait_ben_haddou,
                badge = "Heritage & Nature"
            ),
            OnboardingSlide(
                title = "Direct Community Impact",
                subtitle = "AUTHENTIC BERBER CONNECTIONS",
                description = "Book directly with licensed local guides, family kasbahs, and women's weaving and pottery cooperatives.",
                iconRes = R.drawable.art_moroccan_hospitality,
                badge = "100% Fair Tourism"
            ),
            OnboardingSlide(
                title = "Smart Desert Travel",
                subtitle = "TELEMETRY & PASSPORT",
                description = "Real-time Tizi n'Tichka pass weather, offline desert caches, and cryptographic digital passport stamps for every kasbah visited.",
                iconRes = R.drawable.art_merzouga_dunes,
                badge = "Smart Navigation"
            )
        )
    }

    val currentSlide = slides[currentSlideIndex]

    Column(
        modifier = modifier
            .testTag("onboarding_screen")
            .fillMaxSize()
            .background(WarmCanvas)
    ) {
        // Visual Artwork Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
        ) {
            Image(
                painter = painterResource(id = currentSlide.iconRes),
                contentDescription = currentSlide.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient to blend into WarmCanvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                WarmCanvas
                            ),
                            startY = 0f,
                            endY = 700f
                        )
                    )
            )

            // Top Brand Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TwilightIndigo.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Terracotta)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SIA",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.HOME) },
                    modifier = Modifier.testTag("onboarding_skip_btn")
                ) {
                    Text("Skip", color = TwilightIndigo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Onboarding Content & Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Sand.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = currentSlide.badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Terracotta,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentSlide.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TwilightIndigo,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentSlide.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { index ->
                    val isSelected = index == currentSlideIndex
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 28.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Terracotta else BorderColor)
                            .clickable { currentSlideIndex = index }
                    )
                }
            }

            // CTA Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (currentSlideIndex < slides.size - 1) {
                            currentSlideIndex++
                        } else {
                            viewModel.navigateTo(ActiveScreen.HOME)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_primary_cta"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentSlideIndex < slides.size - 1) "Continue" else "Start Exploring",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(ActiveScreen.AUTH) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_login_btn"),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Sign In or Register", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TwilightIndigo)
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf("guest.traveler@timizar.ma") }
    var passwordInput by remember { mutableStateOf("••••••••") }

    Column(
        modifier = modifier
            .testTag("auth_screen")
            .fillMaxSize()
            .background(WarmCanvas)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Moroccan Brand Emblem
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Terracotta),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Terrain, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to Timizar Go",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TwilightIndigo,
                fontSize = 24.sp
            )
        )
        Text(
            text = "Your gateway to Drâa-Tafilalet heritage",
            fontSize = 13.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Terracotta) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Terracotta) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.navigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_btn"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                ) {
                    Text("Continue Journey", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.navigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_guest_btn"),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Continue as Guest Explorer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TwilightIndigo)
                }
            }
        }
    }
}
