package com.gyoheul.calm_routine.view.settings.premiumUpgrade

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.view.DefaultTopAppBar
import com.gyoheul.calm_routine.view.LoadingScreenView

@Composable
fun PremiumUpgradeScreenView(navController: NavHostController, viewModel: PremiumUpgradeScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = stringResource(id = R.string.settings_premium_upgrade_title),
                onNavigationClick = {
                    navController.popBackStack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.settings_premium_upgrade_msg),
                style = LocalMainTypography.current.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            PremiumBenefitList() // 以下を参照

            Spacer(Modifier.height(32.dp))

            SubscriptionPlanCards(
                viewModel = viewModel,
                onDetailsClick = { offerIdToken ->
                    (context as? ComponentActivity)?.also {
                        viewModel.billingManager.launchBillingFlow(
                            it,
                            uiState.productDetails,
                            offerIdToken
                        )
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "You can cancel anytime from your Play Store account.",
                style = LocalMainTypography.current.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}

@Composable
fun PremiumBenefitList() {
    val benefits = listOf(
        R.string.settings_premium_upgrade_benefit1_title to R.string.settings_premium_upgrade_benefit1_desc,
        R.string.settings_premium_upgrade_benefit2_title to R.string.settings_premium_upgrade_benefit2_desc,
        R.string.settings_premium_upgrade_benefit3_title to R.string.settings_premium_upgrade_benefit3_desc,
        R.string.settings_premium_upgrade_benefit4_title to R.string.settings_premium_upgrade_benefit4_desc,
        R.string.settings_premium_upgrade_benefit5_title to R.string.settings_premium_upgrade_benefit5_desc,
        R.string.settings_premium_upgrade_benefit6_title to R.string.settings_premium_upgrade_benefit6_desc,
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        benefits.forEach { (titleRes, descRes) ->
            Column {
                Text(
                    text = stringResource(id = titleRes),
                    style = LocalMainTypography.current.titleMedium
                )
                Text(
                    text = stringResource(id = descRes),
                    style = LocalMainTypography.current.bodySmall
                )
            }
        }
    }
}

@Composable
fun SubscriptionPlanCards(
    viewModel: PremiumUpgradeScreenViewModel,
    onDetailsClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        uiState.cards.map { card ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = {
                    onDetailsClick(card.offerIdToken)
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LocalMainColorScheme.current.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(card.titleId),
                        style = LocalMainTypography.current.titleMedium,
                    )
                    if (!card.price.contains("\n")) {
                        Text(
                            text = card.price,
                            style = LocalMainTypography.current.bodyMedium
                        )
                    } else {
                        val texts = card.price.split("\n")
                        Text(
                            text = texts.first(),
                            style = LocalMainTypography.current.bodyMedium.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = texts.last(),
                            style = LocalMainTypography.current.bodyMedium
                        )
                    }
                }
            }
        }
    }
}