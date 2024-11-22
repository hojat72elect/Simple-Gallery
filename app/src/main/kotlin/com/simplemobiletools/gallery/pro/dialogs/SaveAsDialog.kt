package com.simplemobiletools.gallery.pro.dialogs

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogSaveAsBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getDoesFilePathExist
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFileUrisFromFileDirItems
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFilenameFromPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getParentPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getPicturesDirectoryPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hideKeyboard
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.humanizePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isAValidFilename
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isExternalStorageManager
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isInDownloadDir
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isRestrictedWithSAFSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.showKeyboard
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.toFileDirItem
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.toast
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.value
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isRPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.activities.BaseActivity
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n")
class SaveAsDialog(
    val activity: BaseActivity,
    val path: String,
    private val appendFilename: Boolean,
    private val cancelCallback: (() -> Unit)? = null,
    val callback: (savePath: String) -> Unit
) {
    init {
        var realPath = path.getParentPath()
        if (activity.isRestrictedWithSAFSdk30(realPath) && !activity.isInDownloadDir(realPath)) {
            realPath = activity.getPicturesDirectoryPath(realPath)
        }

        val binding = DialogSaveAsBinding.inflate(activity.layoutInflater).apply {
            folderValue.setText("${activity.humanizePath(realPath).trimEnd('/')}/")

            val fullName = path.getFilenameFromPath()
            val dotAt = fullName.lastIndexOf(".")
            var name = fullName

            if (dotAt > 0) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                extensionValue.setText(extension)
            }

            if (appendFilename) {
                name += "_1"
            }

            filenameValue.setText(name)
            folderValue.setOnClickListener {
                activity.hideKeyboard(folderValue)
                FilePickerDialog(
                    activity = activity,
                    currPath = realPath,
                    pickFile = false,
                    showHidden = false,
                    showFAB = true,
                    canAddShowHiddenButton = true
                ) {
                    folderValue.setText(activity.humanizePath(it))
                    realPath = it
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel) { _, _ -> cancelCallback?.invoke() }
            .setOnCancelListener { cancelCallback?.invoke() }
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.save_as
                ) { alertDialog ->
                    alertDialog.showKeyboard(binding.filenameValue)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = binding.filenameValue.value
                        val extension = binding.extensionValue.value

                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        if (extension.isEmpty()) {
                            activity.toast(R.string.extension_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val newFilename = "$filename.$extension"
                        val newPath = "${realPath.trimEnd('/')}/$newFilename"
                        if (!newFilename.isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        if (activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(
                                activity.getString(R.string.file_already_exists_overwrite),
                                newFilename
                            )
                            ConfirmationDialog(activity, title) {
                                if ((isRPlus() && !isExternalStorageManager())) {
                                    val fileDirItem =
                                        arrayListOf(File(newPath).toFileDirItem(activity))
                                    val fileUris = activity.getFileUrisFromFileDirItems(fileDirItem)
                                    activity.updateSDK30Uris(fileUris) { success ->
                                        if (success) {
                                            selectPath(alertDialog, newPath)
                                        }
                                    }
                                } else {
                                    selectPath(alertDialog, newPath)
                                }
                            }
                        } else {
                            selectPath(alertDialog, newPath)
                        }
                    }
                }
            }
    }

    private fun selectPath(alertDialog: AlertDialog, newPath: String) {
        activity.handleSAFDialogSdk30(newPath) {
            if (!it) {
                return@handleSAFDialogSdk30
            }
            callback(newPath)
            alertDialog.dismiss()
        }
    }
}
