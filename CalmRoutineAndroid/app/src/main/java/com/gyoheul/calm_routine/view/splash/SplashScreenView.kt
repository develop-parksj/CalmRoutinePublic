package com.gyoheul.calm_routine.view.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.data.FirebaseModel
import com.gyoheul.calm_routine.view.AlertDialogView

@Composable
fun SplashScreenView(navController: NavHostController, viewModel: SplashScreenViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.initialize()

        val isLoggedIn: Boolean = FirebaseModel.getUser() != null
        if (isLoggedIn) {
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }

    val isDisplayDialog by viewModel.isDisplayDialog
    if (isDisplayDialog) {
        AlertDialogView(
            dialogType = viewModel.dialogType,
            onDismissRequest =  {
                viewModel.onDialogDismiss()
            }
        )
    }
}