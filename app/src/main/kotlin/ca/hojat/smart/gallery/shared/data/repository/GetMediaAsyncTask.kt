package ca.hojat.smart.gallery.shared.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import ca.hojat.smart.gallery.shared.helpers.FAVORITES
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_DATE_MODIFIED
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_DATE_TAKEN
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_SIZE
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getFavoritePaths
import ca.hojat.smart.gallery.shared.helpers.GROUP_BY_DATE_TAKEN_DAILY
import ca.hojat.smart.gallery.shared.helpers.GROUP_BY_DATE_TAKEN_MONTHLY
import ca.hojat.smart.gallery.shared.helpers.GROUP_BY_LAST_MODIFIED_DAILY
import ca.hojat.smart.gallery.shared.helpers.GROUP_BY_LAST_MODIFIED_MONTHLY
import ca.hojat.smart.gallery.shared.helpers.MediaFetcher
import ca.hojat.smart.gallery.shared.helpers.RECYCLE_BIN
import ca.hojat.smart.gallery.shared.helpers.SHOW_ALL
import ca.hojat.smart.gallery.shared.data.domain.Medium
import ca.hojat.smart.gallery.shared.data.domain.ThumbnailItem

/**
 * This class fetches media files from the device storage in the background and then returns the results to the UI thread.
 * TODO: Should be migrated to a UseCase with Coroutines.
 */
@SuppressLint("StaticFieldLeak")
class GetMediaAsyncTask(
    val context: Context,
    private val mPath: String,
    val isPickImage: Boolean = false,
    val isPickVideo: Boolean = false,
    val showAll: Boolean,
    val callback: (media: ArrayList<ThumbnailItem>) -> Unit
) :
    AsyncTask<Void, Void, ArrayList<ThumbnailItem>>() {
    private val mediaFetcher = MediaFetcher(context)

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void): ArrayList<ThumbnailItem> {
        val pathToUse = if (showAll) SHOW_ALL else mPath
        val folderGrouping = context.config.getFolderGrouping(pathToUse)
        val folderSorting = context.config.getFolderSorting(pathToUse)
        val getProperDateTaken = folderSorting and SORT_BY_DATE_TAKEN != 0 ||
                folderGrouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
                folderGrouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

        val getProperLastModified = folderSorting and SORT_BY_DATE_MODIFIED != 0 ||
                folderGrouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
                folderGrouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

        val getProperFileSize = folderSorting and SORT_BY_SIZE != 0
        val favoritePaths = context.getFavoritePaths()
        val getVideoDurations = context.config.showThumbnailVideoDuration
        val lastModifieds =
            if (getProperLastModified) mediaFetcher.getLastModifieds() else HashMap()
        val dateTakens = if (getProperDateTaken) mediaFetcher.getDateTakens() else HashMap()

        val media = if (showAll) {
            val foldersToScan = mediaFetcher.getFoldersToScan().filter {
                it != RECYCLE_BIN && it != FAVORITES
            }
            val media = ArrayList<Medium>()
            foldersToScan.forEach {
                val newMedia = mediaFetcher.getFilesFrom(
                    it,
                    isPickImage,
                    isPickVideo,
                    getProperDateTaken,
                    getProperLastModified,
                    getProperFileSize,
                    favoritePaths,
                    getVideoDurations,
                    lastModifieds,
                    dateTakens.clone() as HashMap<String, Long>,
                    null
                )
                media.addAll(newMedia)
            }

            mediaFetcher.sortMedia(media, context.config.getFolderSorting(SHOW_ALL))
            media
        } else {
            mediaFetcher.getFilesFrom(
                mPath,
                isPickImage,
                isPickVideo,
                getProperDateTaken,
                getProperLastModified,
                getProperFileSize,
                favoritePaths,
                getVideoDurations,
                lastModifieds,
                dateTakens,
                null
            )
        }

        return mediaFetcher.groupMedia(media, pathToUse)
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(media: ArrayList<ThumbnailItem>) {
        super.onPostExecute(media)
        callback(media)
    }

    fun stopFetching() {
        mediaFetcher.shouldStop = true
        cancel(true)
    }
}
