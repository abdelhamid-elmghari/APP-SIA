package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookingRecord
import com.example.model.BookingStatus
import com.example.model.PaymentStatus
import com.example.state.AppUiState
import com.example.state.TimizarViewModel
import com.example.ui.theme.*

@Composable
fun BookingManagementScreen(
    uiState: AppUiState,
    viewModel: TimizarViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .testTag("bookings_screen")
            .fillMaxSize()
            .background(WarmCanvas)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightIndigo)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isPartnerMode) "Partner Dashboard" else "My Bookings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (uiState.isPartnerMode) "Review tourist requests" else "Real-time reservation status",
                            fontSize = 12.sp,
                            color = Sand
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.switchUserRole(!uiState.isPartnerMode) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (uiState.isPartnerMode) Terracotta else OasisGreenContainer,
                            contentColor = if (uiState.isPartnerMode) Color.White else OnOasisGreenContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("toggle_partner_mode_btn")
                    ) {
                        Text(if (uiState.isPartnerMode) "Tourist View" else "Partner Portal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uiState.isPartnerMode) {
            val partnerBookings = uiState.bookings.filter { it.providerId == uiState.partnerId }
            item {
                Text(
                    text = "Incoming Requests (${partnerBookings.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TwilightIndigo
                    )
                )
            }

            items(partnerBookings) { bk ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("partner_booking_${bk.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bk.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (bk.status == BookingStatus.CONFIRMED) OasisGreenContainer else Sand.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = bk.status.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bk.status == BookingStatus.CONFIRMED) OnOasisGreenContainer else TextDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tourist: ${bk.userId} • Date: ${bk.date} • ${bk.guestsCount} Persons", fontSize = 11.sp, color = MutedText)

                        if (bk.status == BookingStatus.PENDING) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.updateBookingStatusByProvider(bk.id, BookingStatus.CONFIRMED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = OasisGreen),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f).testTag("partner_accept_btn_${bk.id}")
                                ) {
                                    Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.updateBookingStatusByProvider(bk.id, BookingStatus.REJECTED) },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f).testTag("partner_reject_btn_${bk.id}")
                                ) {
                                    Text("Decline", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Upcoming Journeys & Reservations",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TwilightIndigo
                    )
                )
            }

            items(uiState.bookings) { bk ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("booking_card_${bk.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bk.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TwilightIndigo)
                                Text("Provider: ${bk.providerName} • ${bk.date}", fontSize = 11.sp, color = MutedText)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (bk.status) {
                                    BookingStatus.CONFIRMED -> OasisGreenContainer
                                    BookingStatus.PENDING -> Sand.copy(alpha = 0.5f)
                                    else -> BorderSoft
                                }
                            ) {
                                Text(
                                    text = bk.status.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (bk.status) {
                                        BookingStatus.CONFIRMED -> OnOasisGreenContainer
                                        else -> TextDark
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${bk.totalAmountMad} MAD (${bk.guestsCount} guests)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Terracotta)

                            if (bk.status == BookingStatus.CONFIRMED && bk.paymentStatus != PaymentStatus.PAID) {
                                Button(
                                    onClick = { viewModel.processPaymentSimulation(bk.id, true) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OasisGreen),
                                    modifier = Modifier.testTag("pay_sandbox_btn_${bk.id}")
                                ) {
                                    Text("Pay Sandbox", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (bk.paymentStatus == PaymentStatus.PAID) {
                                Text("Paid (Verified)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OasisGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
