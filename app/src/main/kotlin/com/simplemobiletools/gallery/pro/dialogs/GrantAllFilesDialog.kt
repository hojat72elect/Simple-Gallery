package com.simplemobiletools.gallery.pro.dialogs

import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.extensions.applyColorFilter
import com.simplemobiletools.gallery.pro.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.databinding.DialogGrantAllFilesBinding
import com.simplemobiletools.gallery.pro.extensions.launchGrantAllFilesIntent

class GrantAllFilesDialog(val activity: BaseSimpleActivity) {
    init {
        val binding = DialogGrantAllFilesBinding.inflate(activity.layoutInflater)
        binding.grantAllFilesImage.applyColorFilter(activity.getProperTextColor())

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok) { _, _ -> activity.launchGrantAllFilesIntent() }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { }
            }
    }
}
