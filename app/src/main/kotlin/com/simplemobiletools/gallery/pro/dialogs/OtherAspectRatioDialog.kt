package com.simplemobiletools.gallery.pro.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogOtherAspectRatioBinding
import com.simplemobiletools.gallery.pro.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.BaseActivity

class OtherAspectRatioDialog(
    val activity: BaseActivity,
    private val lastOtherAspectRatio: Pair<Float, Float>?,
    val callback: (aspectRatio: Pair<Float, Float>) -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val binding = DialogOtherAspectRatioBinding.inflate(activity.layoutInflater).apply {
            otherAspectRatio21.setOnClickListener { ratioPicked(Pair(2f, 1f)) }
            otherAspectRatio32.setOnClickListener { ratioPicked(Pair(3f, 2f)) }
            otherAspectRatio43.setOnClickListener { ratioPicked(Pair(4f, 3f)) }
            otherAspectRatio53.setOnClickListener { ratioPicked(Pair(5f, 3f)) }
            otherAspectRatio169.setOnClickListener { ratioPicked(Pair(16f, 9f)) }
            otherAspectRatio199.setOnClickListener { ratioPicked(Pair(19f, 9f)) }
            otherAspectRatioCustom.setOnClickListener { customRatioPicked() }

            otherAspectRatio12.setOnClickListener { ratioPicked(Pair(1f, 2f)) }
            otherAspectRatio23.setOnClickListener { ratioPicked(Pair(2f, 3f)) }
            otherAspectRatio34.setOnClickListener { ratioPicked(Pair(3f, 4f)) }
            otherAspectRatio35.setOnClickListener { ratioPicked(Pair(3f, 5f)) }
            otherAspectRatio916.setOnClickListener { ratioPicked(Pair(9f, 16f)) }
            otherAspectRatio919.setOnClickListener { ratioPicked(Pair(9f, 19f)) }

            val radio1SelectedItemId = when (lastOtherAspectRatio) {
                Pair(2f, 1f) -> otherAspectRatio21.id
                Pair(3f, 2f) -> otherAspectRatio32.id
                Pair(4f, 3f) -> otherAspectRatio43.id
                Pair(5f, 3f) -> otherAspectRatio53.id
                Pair(16f, 9f) -> otherAspectRatio169.id
                Pair(19f, 9f) -> otherAspectRatio199.id
                else -> 0
            }
            otherAspectRatioDialogRadio1.check(radio1SelectedItemId)

            val radio2SelectedItemId = when (lastOtherAspectRatio) {
                Pair(1f, 2f) -> otherAspectRatio12.id
                Pair(2f, 3f) -> otherAspectRatio23.id
                Pair(3f, 4f) -> otherAspectRatio34.id
                Pair(3f, 5f) -> otherAspectRatio35.id
                Pair(9f, 16f) -> otherAspectRatio916.id
                Pair(9f, 19f) -> otherAspectRatio919.id
                else -> 0
            }
            otherAspectRatioDialogRadio2.check(radio2SelectedItemId)

            if (radio1SelectedItemId == 0 && radio2SelectedItemId == 0) {
                otherAspectRatioDialogRadio1.check(otherAspectRatioCustom.id)
            }
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun customRatioPicked() {
        CustomAspectRatioDialog(activity, lastOtherAspectRatio) {
            callback(it)
            dialog?.dismiss()
        }
    }

    private fun ratioPicked(pair: Pair<Float, Float>) {
        callback(pair)
        dialog?.dismiss()
    }
}
