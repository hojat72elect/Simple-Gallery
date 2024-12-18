package ca.hojat.smart.gallery.feature_about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.shared.ui.components.LinkifyTextComponent
import ca.hojat.smart.gallery.shared.ui.lists.SimpleLazyListScaffold
import ca.hojat.smart.gallery.shared.ui.settings.SettingsGroupTitle
import ca.hojat.smart.gallery.shared.ui.settings.SettingsHorizontalDivider
import ca.hojat.smart.gallery.shared.ui.settings.SettingsListItem
import ca.hojat.smart.gallery.shared.ui.settings.SettingsTitleTextComponent
import ca.hojat.smart.gallery.shared.ui.theme.SimpleTheme
import ca.hojat.smart.gallery.shared.data.domain.LanguageContributor
import ca.hojat.smart.gallery.shared.extensions.fromHtml
import kotlinx.collections.immutable.ImmutableList

private val startingPadding = Modifier.padding(start = 58.dp)

@Composable
internal fun ContributorsScreen(
    goBack: () -> Unit,
    showContributorsLabel: Boolean,
    contributors: ImmutableList<LanguageContributor>
) {
    SimpleLazyListScaffold(
        title = { scrolledColor ->
            Text(
                text = stringResource(id = R.string.contributors),
                modifier = Modifier
                    .padding(start = 28.dp)
                    .fillMaxWidth(),
                color = scrolledColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        goBack = goBack
    ) {
        item {
            SettingsGroupTitle {
                SettingsTitleTextComponent(
                    text = stringResource(id = R.string.development),
                    modifier = startingPadding
                )
            }
        }
        item {
            SettingsListItem(
                text = stringResource(id = R.string.contributors_developers),
                icon = R.drawable.ic_code_vector,
                tint = SimpleTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
        item {
            Spacer(modifier = Modifier.padding(vertical = SimpleTheme.dimens.padding.medium))
        }
        item {
            SettingsHorizontalDivider()
        }
        item {
            SettingsGroupTitle {
                SettingsTitleTextComponent(
                    text = stringResource(id = R.string.translation),
                    modifier = startingPadding
                )
            }
        }
        items(contributors, key = { it.contributorsId.plus(it.iconId).plus(it.labelId) }) {
            ContributorItem(
                languageContributor = it
            )
        }
        if (showContributorsLabel) {
            item {
                SettingsListItem(
                    icon = R.drawable.ic_heart_vector,
                    text = {
                        val source = stringResource(id = R.string.contributors_label)
                        LinkifyTextComponent {
                            source.fromHtml()
                        }
                    },
                    tint = SimpleTheme.colorScheme.onSurface
                )
            }
            item {
                Spacer(modifier = Modifier.padding(bottom = SimpleTheme.dimens.padding.medium))
            }
        }

    }
}

@Composable
private fun ContributorItem(
    modifier: Modifier = Modifier,
    languageContributor: LanguageContributor
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = languageContributor.labelId),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier)
            )
        },
        leadingContent = {
            val imageSize = Modifier
                .size(SimpleTheme.dimens.icon.medium)
                .padding(SimpleTheme.dimens.padding.medium)
            Image(
                modifier = imageSize,
                painter = painterResource(id = languageContributor.iconId),
                contentDescription = stringResource(id = languageContributor.contributorsId),
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        supportingContent = {
            Text(
                text = stringResource(id = languageContributor.contributorsId),
                modifier = Modifier
                    .fillMaxWidth(),
                color = SimpleTheme.colorScheme.onSurface
            )
        }
    )
}
