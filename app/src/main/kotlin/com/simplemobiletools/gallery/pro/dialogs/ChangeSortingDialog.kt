package com.simplemobiletools.gallery.pro.dialogs

import android.content.DialogInterface
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogChangeSortingBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.beGoneIf
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.beVisibleIf
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isVisible
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SHOW_ALL
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_BY_DATE_MODIFIED
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_BY_DATE_TAKEN
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_BY_NAME
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_BY_PATH
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_BY_RANDOM
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_BY_SIZE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_DESCENDING
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SORT_USE_NUMERIC_VALUE
import com.simplemobiletools.gallery.pro.new_architecture.shared.activities.BaseActivity

class ChangeSortingDialog(
    val activity: BaseActivity,
    private val isDirectorySorting: Boolean,
    private val showFolderCheckbox: Boolean,
    val path: String = "",
    val callback: () -> Unit
) :
    DialogInterface.OnClickListener {
    private var currSorting = 0
    private var config = activity.config
    private var pathToUse = if (!isDirectorySorting && path.isEmpty()) SHOW_ALL else path
    private val binding: DialogChangeSortingBinding

    init {
        currSorting =
            if (isDirectorySorting) config.directorySorting else config.getFolderSorting(pathToUse)
        binding = DialogChangeSortingBinding.inflate(activity.layoutInflater).apply {
            sortingDialogOrderDivider.beVisibleIf(showFolderCheckbox || (currSorting and SORT_BY_NAME != 0 || currSorting and SORT_BY_PATH != 0))

            sortingDialogNumericSorting.beVisibleIf(showFolderCheckbox && (currSorting and SORT_BY_NAME != 0 || currSorting and SORT_BY_PATH != 0))
            sortingDialogNumericSorting.isChecked = currSorting and SORT_USE_NUMERIC_VALUE != 0

            sortingDialogUseForThisFolder.beVisibleIf(showFolderCheckbox)
            sortingDialogUseForThisFolder.isChecked = config.hasCustomSorting(pathToUse)
            sortingDialogBottomNote.beVisibleIf(!isDirectorySorting)
            sortingDialogRadioCustom.beVisibleIf(isDirectorySorting)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, this)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.sort_by
                )
            }

        setupSortRadio()
        setupOrderRadio()
    }

    private fun setupSortRadio() {
        val sortingRadio = binding.sortingDialogRadioSorting
        sortingRadio.setOnCheckedChangeListener { _, checkedId ->
            val isSortingByNameOrPath =
                checkedId == binding.sortingDialogRadioName.id || checkedId == binding.sortingDialogRadioPath.id
            binding.sortingDialogNumericSorting.beVisibleIf(isSortingByNameOrPath)
            binding.sortingDialogOrderDivider.beVisibleIf(binding.sortingDialogNumericSorting.isVisible() || binding.sortingDialogUseForThisFolder.isVisible())

            val hideSortOrder =
                checkedId == binding.sortingDialogRadioCustom.id || checkedId == binding.sortingDialogRadioRandom.id
            binding.sortingDialogRadioOrder.beGoneIf(hideSortOrder)
            binding.sortingDialogSortingDivider.beGoneIf(hideSortOrder)
        }

        val sortBtn = when {
            currSorting and SORT_BY_PATH != 0 -> binding.sortingDialogRadioPath
            currSorting and SORT_BY_SIZE != 0 -> binding.sortingDialogRadioSize
            currSorting and SORT_BY_DATE_MODIFIED != 0 -> binding.sortingDialogRadioLastModified
            currSorting and SORT_BY_DATE_TAKEN != 0 -> binding.sortingDialogRadioDateTaken
            currSorting and SORT_BY_RANDOM != 0 -> binding.sortingDialogRadioRandom
            currSorting and SORT_BY_CUSTOM != 0 -> binding.sortingDialogRadioCustom
            else -> binding.sortingDialogRadioName
        }
        sortBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        var orderBtn = binding.sortingDialogRadioAscending

        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = binding.sortingDialogRadioDescending
        }
        orderBtn.isChecked = true
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val sortingRadio = binding.sortingDialogRadioSorting
        var sorting = when (sortingRadio.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_name -> SORT_BY_NAME
            R.id.sorting_dialog_radio_path -> SORT_BY_PATH
            R.id.sorting_dialog_radio_size -> SORT_BY_SIZE
            R.id.sorting_dialog_radio_last_modified -> SORT_BY_DATE_MODIFIED
            R.id.sorting_dialog_radio_random -> SORT_BY_RANDOM
            R.id.sorting_dialog_radio_custom -> SORT_BY_CUSTOM
            else -> SORT_BY_DATE_TAKEN
        }

        if (binding.sortingDialogRadioOrder.checkedRadioButtonId == R.id.sorting_dialog_radio_descending) {
            sorting = sorting or SORT_DESCENDING
        }

        if (binding.sortingDialogNumericSorting.isChecked) {
            sorting = sorting or SORT_USE_NUMERIC_VALUE
        }

        if (isDirectorySorting) {
            config.directorySorting = sorting
        } else {
            if (binding.sortingDialogUseForThisFolder.isChecked) {
                config.saveCustomSorting(pathToUse, sorting)
            } else {
                config.removeCustomSorting(pathToUse)
                config.sorting = sorting
            }
        }

        if (currSorting != sorting) {
            callback()
        }
    }
}
