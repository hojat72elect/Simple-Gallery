package com.simplemobiletools.gallery.pro.shared.ui.dialogs

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogCreateNewFolderBinding
import com.simplemobiletools.gallery.pro.shared.activities.BaseActivity
import com.simplemobiletools.gallery.pro.shared.extensions.createAndroidSAFDirectory
import com.simplemobiletools.gallery.pro.shared.extensions.createSAFDirectorySdk30
import com.simplemobiletools.gallery.pro.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.shared.extensions.getDocumentFile
import com.simplemobiletools.gallery.pro.shared.extensions.getFilenameFromPath
import com.simplemobiletools.gallery.pro.shared.extensions.getParentPath
import com.simplemobiletools.gallery.pro.shared.extensions.humanizePath
import com.simplemobiletools.gallery.pro.shared.extensions.isAStorageRootFolder
import com.simplemobiletools.gallery.pro.shared.extensions.isAValidFilename
import com.simplemobiletools.gallery.pro.shared.extensions.isAccessibleWithSAFSdk30
import com.simplemobiletools.gallery.pro.shared.extensions.isRestrictedSAFOnlyRoot
import com.simplemobiletools.gallery.pro.shared.extensions.needsStupidWritePermissions
import com.simplemobiletools.gallery.pro.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.shared.extensions.showErrorToast
import com.simplemobiletools.gallery.pro.shared.extensions.showKeyboard
import com.simplemobiletools.gallery.pro.shared.extensions.toast
import com.simplemobiletools.gallery.pro.shared.extensions.value
import com.simplemobiletools.gallery.pro.shared.helpers.isRPlus
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
class CreateNewFolderDialog(
    val activity: BaseActivity,
    val path: String,
    val callback: (path: String) -> Unit
) {
    init {
        val view = DialogCreateNewFolderBinding.inflate(activity.layoutInflater, null, false)
        view.folderPath.setText("${activity.humanizePath(path).trimEnd('/')}/")

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    view.root,
                    this,
                    R.string.create_new_folder
                ) { alertDialog ->
                    alertDialog.showKeyboard(view.folderName)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(View.OnClickListener {
                            val name = view.folderName.value
                            when {
                                name.isEmpty() -> activity.toast(R.string.empty_name)
                                name.isAValidFilename() -> {
                                    val file = File(path, name)
                                    if (file.exists()) {
                                        activity.toast(R.string.name_taken)
                                        return@OnClickListener
                                    }

                                    createFolder("$path/$name", alertDialog)
                                }

                                else -> activity.toast(R.string.invalid_name)
                            }
                        })
                }
            }
    }


    private fun createFolder(path: String, alertDialog: AlertDialog) {
        try {
            when {
                activity.isRestrictedSAFOnlyRoot(path) && activity.createAndroidSAFDirectory(path) -> sendSuccess(
                    alertDialog,
                    path
                )

                activity.isAccessibleWithSAFSdk30(path) -> activity.handleSAFDialogSdk30(path) {
                    if (it && activity.createSAFDirectorySdk30(path)) {
                        sendSuccess(alertDialog, path)
                    }
                }

                activity.needsStupidWritePermissions(path) -> activity.handleSAFDialog(path) {
                    if (it) {
                        try {
                            val documentFile = activity.getDocumentFile(path.getParentPath())
                            val newDir = documentFile?.createDirectory(path.getFilenameFromPath())
                                ?: activity.getDocumentFile(path)
                            if (newDir != null) {
                                sendSuccess(alertDialog, path)
                            } else {
                                activity.toast(R.string.unknown_error_occurred)
                            }
                        } catch (e: SecurityException) {
                            activity.showErrorToast(e)
                        }
                    }
                }

                File(path).mkdirs() -> sendSuccess(alertDialog, path)
                isRPlus() && activity.isAStorageRootFolder(path.getParentPath()) -> activity.handleSAFCreateDocumentDialogSdk30(
                    path
                ) {
                    if (it) {
                        sendSuccess(alertDialog, path)
                    }
                }

                else -> activity.toast(
                    activity.getString(
                        R.string.could_not_create_folder,
                        path.getFilenameFromPath()
                    )
                )
            }
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, path: String) {
        callback(path.trimEnd('/'))
        alertDialog.dismiss()
    }
}