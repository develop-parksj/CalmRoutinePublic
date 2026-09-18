package com.gyoheul.calm_routine.view

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.ui.theme.Blue
import com.gyoheul.calm_routine.ui.theme.ComponentStyles
import com.gyoheul.calm_routine.ui.theme.LoadingColor
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.ui.theme.Red
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopAppBar(
    title: String,
    navigationIcon: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    navigationContentDescription: String = "Back",
    onNavigationClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = LocalMainColorScheme.current.onPrimary,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = LocalMainColorScheme.current.primary,
            titleContentColor = LocalMainColorScheme.current.onPrimary,
        ),
        navigationIcon = {
            IconButton(
                onClick = onNavigationClick
            ) {
                Icon(
                    navigationIcon,
                    contentDescription = navigationContentDescription,
                    tint = LocalMainColorScheme.current.onPrimary
                )
            }
        },
        actions = actions
    )
}

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun DefaultTextButton(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    textColor: Color = LocalMainColorScheme.current.onSecondary,
    buttonColor: Color = LocalMainColorScheme.current.secondary,
    style: Style = ComponentStyles.secondaryButton(buttonColor, textColor),
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.styleable(style = style),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Let style handle background
            contentColor = Color.Unspecified // Let style handle content color if possible
        )
    ) {
        Text(
            text = title,
            style = textStyle
        )
    }
}

@Composable
fun DefaultButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonColor: Color? = null,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor ?: LocalMainColorScheme.current.secondary
        ),
        content = content
    )
}

@Composable
fun NativeBottomAdView(nativeBottomAd: NativeAd?) {
    val context = LocalContext.current
    nativeBottomAd?.let { ad ->
        AndroidView(
            factory = { context ->
                LayoutInflater.from(context).inflate(R.layout.small_native_ad, null) as NativeAdView
            },
            update = { adView: NativeAdView ->
                // Headline (必須)
                adView.headlineView = adView.findViewById(R.id.primary)
                (adView.headlineView as TextView).text = ad.headline

                // Icon
                adView.iconView = adView.findViewById(R.id.icon)
                if (ad.icon != null) {
                    (adView.iconView as ImageView).setImageDrawable(ad.icon?.drawable)
                    adView.iconView?.visibility = View.VISIBLE
                } else {
                    adView.iconView?.visibility = View.GONE
                }

                // Call to Action
                adView.callToActionView = adView.findViewById(R.id.cta)
                if (ad.callToAction != null) {
                    (adView.callToActionView as Button).text = ad.callToAction
                    adView.callToActionView?.visibility = View.VISIBLE
                } else {
                    adView.callToActionView?.visibility = View.GONE
                }

                // Star Rating
                adView.starRatingView = adView.findViewById(R.id.rating_bar)
                if (ad.starRating != null) {
                    (adView.starRatingView as RatingBar).rating = ad.starRating!!.toFloat()
                    adView.starRatingView?.visibility = View.VISIBLE
                    val stars = (adView.starRatingView as RatingBar).progressDrawable
                    stars.setTint(ContextCompat.getColor(context, R.color.gnt_ad_green))
                } else {
                    adView.starRatingView?.visibility = View.GONE
                }

                // Advertiser, Store, Price, etc.がないため、secondaryに優先順位を適用
                adView.advertiserView = adView.findViewById(R.id.secondary)
                val secondaryView = adView.advertiserView as TextView
                if (ad.advertiser != null) {
                    secondaryView.text = ad.advertiser
                    secondaryView.visibility = View.VISIBLE
                } else if (ad.store != null) {
                    secondaryView.text = ad.store
                    secondaryView.visibility = View.VISIBLE
                } else {
                    secondaryView.visibility = View.GONE
                }

                // 最後に必須
                adView.setNativeAd(ad)
            }
        )
    }
}

@Composable
fun LoadingScreenView(
    title: String? = null
) {
    var dotCount by remember { mutableIntStateOf(0) }

    // ドットアニメーション (0~3 繰り返し)
    LaunchedEffect(Unit) {
        while (true) {
            delay(500.milliseconds) // ドットが変わる間隔 (ms)
            dotCount = (dotCount + 1) % 4 // 0,1,2,3 繰り返し
        }
    }

    val dots = ".".repeat(dotCount)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoadingColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = LocalMainColorScheme.current.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title?.let {
                    "$it$dots"
                } ?: "${stringResource(R.string.common_loading)}$dots",
                textAlign = TextAlign.Center,
                style = LocalMainTypography.current.bodyLarge,
                color = LocalMainColorScheme.current.background,
            )
        }
    }
}

@Composable
fun AlertDialogView(
    dialogType: EnumClass.DialogType,
    confirmButtonClick: (() -> Unit)? = null,
    dismissButtonClick: (() -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    AlertDialogView(
        title = dialogType.titleId
            ?.takeIf { stringResource(it).isNotBlank() }
            ?.let { stringResource(it) },
        text = {
            Text(stringResource(dialogType.textId))
        },
        confirmButtonText = stringResource(dialogType.confirmButtonTextId),
        confirmButtonClick = confirmButtonClick,
        confirmButtonColor = dialogType.confirmButtonColor(),
        dismissButtonText = dialogType.dismissButtonTextId
            ?.takeIf { stringResource(it).isNotBlank() }
            ?.let { stringResource(it) },
        dismissButtonClick = dismissButtonClick,
        dismissButtonColor = dialogType.dismissButtonColor(),
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun AlertDialogView(
    title: String? = null,
    text: @Composable (() -> Unit)?,
    confirmButtonText: String,
    confirmButtonClick: (() -> Unit)? = null,
    confirmButtonColor: Color = Blue,
    dismissButtonText: String? = null,
    dismissButtonClick: (() -> Unit)? = null,
    dismissButtonColor: Color = Red,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title?.takeIf { it.isNotBlank() }?.let {
            {
                Text(it)
            }
        },
        text = text,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                confirmButtonClick?.invoke()
            }) {
                Text(
                    text = confirmButtonText,
                    color = confirmButtonColor
                )
            }
        },
        dismissButton = dismissButtonText?.takeIf { it.isNotBlank() }?.let {
            {
                TextButton(onClick = {
                    onDismissRequest()
                    dismissButtonClick?.invoke()
                }) {
                    Text(
                        text = it,
                        color = dismissButtonColor
                    )
                }
            }
        }
    )
}

@Composable
fun ShadowBox(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Vertical,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.wrapContentSize()
    ) {
        content()

        Box(
            modifier = when (orientation) {
                Orientation.Vertical -> Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f) // 適宜調整
                            )
                        )
                    )
                Orientation.Horizontal -> Modifier
                    .fillMaxHeight()
                    .width(32.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f) // 適宜調整
                            )
                        )
                    )
            }
        )
    }
}