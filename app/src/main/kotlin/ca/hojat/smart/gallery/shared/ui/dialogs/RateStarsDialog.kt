package ca.hojat.smart.gallery.shared.ui.dialogs

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.shared.ui.dialogs.alert_dialog.AlertDialogState
import ca.hojat.smart.gallery.shared.ui.dialogs.alert_dialog.DialogSurface
import ca.hojat.smart.gallery.shared.ui.dialogs.alert_dialog.dialogTextColor
import ca.hojat.smart.gallery.shared.ui.theme.Shapes
import ca.hojat.smart.gallery.shared.ui.theme.SimpleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RateStarsAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    onRating: (stars: Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = alertDialogState::hide,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        modifier = modifier
    ) {
        var currentRating by remember { mutableIntStateOf(0) }
        val coroutineScope = rememberCoroutineScope()
        DialogSurface {
            Column {
                Text(
                    text = stringResource(id = R.string.rate_our_app),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = SimpleTheme.dimens.padding.extraLarge,
                            bottom = SimpleTheme.dimens.padding.large
                        ),
                    textAlign = TextAlign.Center,
                    color = dialogTextColor,
                    fontSize = 16.sp
                )
                StarRating(
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(SimpleTheme.dimens.padding.extraLarge),
                    currentRating = currentRating,
                    onRatingChanged = { stars ->
                        currentRating = stars
                        coroutineScope.launch {
                            onRating(stars)
                            delay(500L)
                            alertDialogState.hide()
                        }
                    }
                )
                TextButton(
                    onClick = alertDialogState::hide,
                    modifier = Modifier
                        .align(End)
                        .padding(
                            end = SimpleTheme.dimens.padding.extraLarge,
                            bottom = SimpleTheme.dimens.padding.medium
                        )
                ) {
                    Text(text = stringResource(id = R.string.later))
                }
            }
        }
    }
}

@Composable
private fun StarRating(
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    currentRating: Int,
    onRatingChanged: (Int) -> Unit,
    starsColor: Color = SimpleTheme.colorScheme.primary,
) {
    val animatedRating by animateIntAsState(
        targetValue = currentRating,
        label = "animatedRating",
        animationSpec = tween()
    )
    Row(modifier) {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= animatedRating) Icons.Filled.Star
                else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = starsColor,
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = Shapes.large)
                    .clickable { onRatingChanged(i) }
                    .padding(4.dp)
            )
        }
    }
}


