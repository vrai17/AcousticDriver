package com.vraidev.acousticdriver.ui.screens.drivers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vraidev.acousticdriver.core.air.AcousticIntermediateRepresentation
import com.vraidev.acousticdriver.core.air.CompatibilityScore
import com.vraidev.acousticdriver.core.air.ParameterEvaluation
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun TranslationReportDialog(
    driver: AcousticIntermediateRepresentation,
    score: CompatibilityScore,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "TRANSLATION REPORT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Text(
                    text = driver.metadata.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Source Device: ${driver.metadata.sourceDevice}",
                    fontSize = 12.sp,
                    color = ElectricIce
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Score Overview Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${score.percentage}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(text = "Compatibility", fontSize = 11.sp, color = TextSecondary)
                    }
                    Divider(
                        color = BorderSubtle,
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${score.nativeCount}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(text = "Native", fontSize = 11.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${score.approximatedCount}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                        Text(text = "Approx.", fontSize = 11.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${score.unsupportedCount}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                        Text(text = "Unsupp.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Parameter Breakdown",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(score.evaluations) { eval ->
                        ParameterEvaluationRow(eval)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BgDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Report", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ParameterEvaluationRow(eval: ParameterEvaluation) {
    val statusColor = when (eval.status) {
        com.vraidev.acousticdriver.core.air.ParameterClassification.EXACT,
        com.vraidev.acousticdriver.core.air.ParameterClassification.NATIVE -> SuccessGreen
        com.vraidev.acousticdriver.core.air.ParameterClassification.APPROXIMATED,
        com.vraidev.acousticdriver.core.air.ParameterClassification.PARTIAL -> AmberWarning
        else -> AccentRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = eval.parameterName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = eval.status.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Target: ${eval.targetEffect}",
                fontSize = 11.sp,
                color = ElectricIce
            )

            Text(
                text = eval.explanation,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
