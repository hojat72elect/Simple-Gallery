package com.simplemobiletools.gallery.pro.new_architecture.feature_about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.components.LinkifyTextComponent
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.lists.SimpleLazyListScaffold
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.settings.SettingsHorizontalDivider
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.SimpleTheme
import com.simplemobiletools.gallery.pro.models.FAQItem
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.fromHtml
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FAQScreen(
    goBack: () -> Unit,
    faqItems: ImmutableList<FAQItem>,
) {
    SimpleLazyListScaffold(
        title = stringResource(id = R.string.frequently_asked_questions),
        goBack = goBack,
        contentPadding = PaddingValues(bottom = SimpleTheme.dimens.padding.medium)
    ) {
        itemsIndexed(faqItems) { index, faqItem ->
            Column(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = {
                        val text =
                            if (faqItem.title is Int) stringResource(faqItem.title) else faqItem.title as String
                        Text(
                            text = text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            color = SimpleTheme.colorScheme.primary,
                            lineHeight = 16.sp,
                        )
                    },
                    supportingContent = {
                        if (faqItem.text is Int) {
                            val text = stringResource(id = faqItem.text).fromHtml()
                            LinkifyTextComponent(
                                text = { text },
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = faqItem.text as String,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.padding(bottom = SimpleTheme.dimens.padding.medium))
                if (index != faqItems.lastIndex) {
                    SettingsHorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = SimpleTheme.dimens.padding.small)
                    )
                }
            }
        }
    }
}

