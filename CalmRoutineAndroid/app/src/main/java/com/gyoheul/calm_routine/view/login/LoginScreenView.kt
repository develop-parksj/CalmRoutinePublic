package com.gyoheul.calm_routine.view.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.ToastModel
import com.gyoheul.calm_routine.view.DefaultTextButton
import com.gyoheul.calm_routine.view.LoadingScreenView

@Composable
fun LoginScreenView(navController: NavHostController, viewModel: LoginScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            DefaultTextButton(
                title = stringResource(R.string.login_login),
                onClick = {
                    viewModel.onLoginClick(
                        onSuccess = {
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onFailure = {
                            ToastModel.showToast(context, EnumClass.ToastType.FailLogin)
                        }
                    )
                }
            )
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}