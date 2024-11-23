package com.simplemobiletools.gallery.pro.shared.ui.dialogs

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.shared.activities.BaseActivity

@SuppressLint("InflateParams")
class AllFilesPermissionDialog(
    val activity: BaseActivity,
    message: String = "",
    val callback: (result: Boolean) -> Unit,
    val neutralPressed: () -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(
            R.layout.dialog_message,
            null
        )
        view.findViewById<TextView>(R.id.message).text = message

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.all_files) { _, _ -> positivePressed() }
            .setNeutralButton(R.string.media_only) { _, _ -> neutralPressed() }
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun positivePressed() {
        dialog?.dismiss()
        callback(true)
    }
}
