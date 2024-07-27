package com.simplemobiletools.gallery.pro.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogRenameItemsBinding
import com.simplemobiletools.gallery.pro.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.extensions.getDoesFilePathExist
import com.simplemobiletools.gallery.pro.extensions.getFilenameExtension
import com.simplemobiletools.gallery.pro.extensions.getFilenameFromPath
import com.simplemobiletools.gallery.pro.extensions.getParentPath
import com.simplemobiletools.gallery.pro.extensions.isAValidFilename
import com.simplemobiletools.gallery.pro.extensions.isPathOnSD
import com.simplemobiletools.gallery.pro.extensions.renameFile
import com.simplemobiletools.gallery.pro.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.extensions.showKeyboard
import com.simplemobiletools.gallery.pro.extensions.toast

// used at renaming folders
@RequiresApi(Build.VERSION_CODES.O)
class RenameItemsDialog(
    val activity: BaseSimpleActivity,
    val paths: ArrayList<String>,
    val callback: () -> Unit
) {
    init {
        var ignoreClicks = false
        val view = DialogRenameItemsBinding.inflate(activity.layoutInflater, null, false)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.rename) { alertDialog ->
                    alertDialog.showKeyboard(view.renameItemsValue)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        val valueToAdd = view.renameItemsValue.text.toString()
                        val append =
                            view.renameItemsRadioGroup.checkedRadioButtonId == view.renameItemsRadioAppend.id

                        if (valueToAdd.isEmpty()) {
                            callback()
                            alertDialog.dismiss()
                            return@setOnClickListener
                        }

                        if (!valueToAdd.isAValidFilename()) {
                            activity.toast(R.string.invalid_name)
                            return@setOnClickListener
                        }

                        val validPaths = paths.filter { activity.getDoesFilePathExist(it) }
                        val sdFilePath = validPaths.firstOrNull { activity.isPathOnSD(it) }
                            ?: validPaths.firstOrNull()
                        if (sdFilePath == null) {
                            activity.toast(R.string.unknown_error_occurred)
                            alertDialog.dismiss()
                            return@setOnClickListener
                        }

                        activity.handleSAFDialog(sdFilePath) {
                            if (!it) {
                                return@handleSAFDialog
                            }

                            ignoreClicks = true
                            var pathsCnt = validPaths.size
                            for (path in validPaths) {
                                val fullName = path.getFilenameFromPath()
                                var dotAt = fullName.lastIndexOf(".")
                                if (dotAt == -1) {
                                    dotAt = fullName.length
                                }

                                val name = fullName.substring(0, dotAt)
                                val extension =
                                    if (fullName.contains(".")) ".${fullName.getFilenameExtension()}" else ""

                                val newName = if (append) {
                                    "$name$valueToAdd$extension"
                                } else {
                                    "$valueToAdd$fullName"
                                }

                                val newPath = "${path.getParentPath()}/$newName"

                                if (activity.getDoesFilePathExist(newPath)) {
                                    continue
                                }

                                activity.renameFile(path, newPath, true) { success, _ ->
                                    if (success) {
                                        pathsCnt--
                                        if (pathsCnt == 0) {
                                            callback()
                                            alertDialog.dismiss()
                                        }
                                    } else {
                                        ignoreClicks = false
                                        alertDialog.dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}