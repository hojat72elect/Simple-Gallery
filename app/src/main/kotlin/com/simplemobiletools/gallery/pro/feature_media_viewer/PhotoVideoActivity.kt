package com.simplemobiletools.gallery.pro.feature_media_viewer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import com.simplemobiletools.gallery.pro.BuildConfig
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.feature_home.HomeActivity
import com.simplemobiletools.gallery.pro.databinding.FragmentHolderBinding
import com.simplemobiletools.gallery.pro.shared.ui.dialogs.PropertiesDialog
import com.simplemobiletools.gallery.pro.shared.extensions.actionBarHeight
import com.simplemobiletools.gallery.pro.shared.extensions.beGone
import com.simplemobiletools.gallery.pro.shared.extensions.beVisible
import com.simplemobiletools.gallery.pro.shared.extensions.beVisibleIf
import com.simplemobiletools.gallery.pro.shared.extensions.checkAppSideloading
import com.simplemobiletools.gallery.pro.shared.extensions.config
import com.simplemobiletools.gallery.pro.shared.extensions.getColoredDrawableWithColor
import com.simplemobiletools.gallery.pro.shared.extensions.getDoesFilePathExist
import com.simplemobiletools.gallery.pro.shared.extensions.getFilenameFromPath
import com.simplemobiletools.gallery.pro.shared.extensions.getFilenameFromUri
import com.simplemobiletools.gallery.pro.shared.extensions.getFinalUriFromPath
import com.simplemobiletools.gallery.pro.shared.extensions.getParentPath
import com.simplemobiletools.gallery.pro.shared.extensions.getRealPathFromURI
import com.simplemobiletools.gallery.pro.shared.extensions.getUriMimeType
import com.simplemobiletools.gallery.pro.shared.extensions.hideKeyboard
import com.simplemobiletools.gallery.pro.shared.extensions.hideSystemUI
import com.simplemobiletools.gallery.pro.shared.extensions.isExternalStorageManager
import com.simplemobiletools.gallery.pro.shared.extensions.isGif
import com.simplemobiletools.gallery.pro.shared.extensions.isGone
import com.simplemobiletools.gallery.pro.shared.extensions.isImageFast
import com.simplemobiletools.gallery.pro.shared.extensions.isPortrait
import com.simplemobiletools.gallery.pro.shared.extensions.isRawFast
import com.simplemobiletools.gallery.pro.shared.extensions.isSvg
import com.simplemobiletools.gallery.pro.shared.extensions.isVideoFast
import com.simplemobiletools.gallery.pro.shared.extensions.navigationBarHeight
import com.simplemobiletools.gallery.pro.shared.extensions.navigationBarOnSide
import com.simplemobiletools.gallery.pro.shared.extensions.navigationBarWidth
import com.simplemobiletools.gallery.pro.shared.extensions.openPath
import com.simplemobiletools.gallery.pro.shared.extensions.parseFileChannel
import com.simplemobiletools.gallery.pro.shared.extensions.portrait
import com.simplemobiletools.gallery.pro.shared.extensions.rescanPath
import com.simplemobiletools.gallery.pro.shared.extensions.rescanPaths
import com.simplemobiletools.gallery.pro.shared.extensions.setAs
import com.simplemobiletools.gallery.pro.shared.extensions.sharePath
import com.simplemobiletools.gallery.pro.shared.extensions.showFileOnMap
import com.simplemobiletools.gallery.pro.shared.extensions.showSystemUI
import com.simplemobiletools.gallery.pro.shared.extensions.statusBarHeight
import com.simplemobiletools.gallery.pro.shared.extensions.toHex
import com.simplemobiletools.gallery.pro.shared.extensions.toast
import com.simplemobiletools.gallery.pro.shared.extensions.viewBinding
import com.simplemobiletools.gallery.pro.shared.helpers.BOTTOM_ACTION_EDIT
import com.simplemobiletools.gallery.pro.shared.helpers.BOTTOM_ACTION_PROPERTIES
import com.simplemobiletools.gallery.pro.shared.helpers.BOTTOM_ACTION_SET_AS
import com.simplemobiletools.gallery.pro.shared.helpers.BOTTOM_ACTION_SHARE
import com.simplemobiletools.gallery.pro.shared.helpers.BOTTOM_ACTION_SHOW_ON_MAP
import com.simplemobiletools.gallery.pro.shared.helpers.IS_FROM_GALLERY
import com.simplemobiletools.gallery.pro.shared.helpers.IS_IN_RECYCLE_BIN
import com.simplemobiletools.gallery.pro.shared.helpers.IS_VIEW_INTENT
import com.simplemobiletools.gallery.pro.shared.helpers.MEDIUM
import com.simplemobiletools.gallery.pro.shared.helpers.NOMEDIA
import com.simplemobiletools.gallery.pro.shared.helpers.PATH
import com.simplemobiletools.gallery.pro.shared.helpers.REAL_FILE_PATH
import com.simplemobiletools.gallery.pro.shared.helpers.SHOW_FAVORITES
import com.simplemobiletools.gallery.pro.shared.helpers.SKIP_AUTHENTICATION
import com.simplemobiletools.gallery.pro.shared.helpers.TYPE_GIFS
import com.simplemobiletools.gallery.pro.shared.helpers.TYPE_IMAGES
import com.simplemobiletools.gallery.pro.shared.helpers.TYPE_PORTRAITS
import com.simplemobiletools.gallery.pro.shared.helpers.TYPE_RAWS
import com.simplemobiletools.gallery.pro.shared.helpers.TYPE_SVGS
import com.simplemobiletools.gallery.pro.shared.helpers.TYPE_VIDEOS
import com.simplemobiletools.gallery.pro.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.shared.helpers.getPermissionToRequest
import com.simplemobiletools.gallery.pro.shared.helpers.isRPlus
import com.simplemobiletools.gallery.pro.shared.data.domain.Medium
import com.simplemobiletools.gallery.pro.shared.activities.BaseActivity
import java.io.File
import java.io.FileInputStream

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
@UnstableApi
open class PhotoVideoActivity : BaseActivity(), ViewPagerFragment.FragmentListener {
    private var mMedium: Medium? = null
    private var mIsFullScreen = false
    private var mIsFromGallery = false
    private var mFragment: ViewPagerFragment? = null
    private var mUri: Uri? = null

    var mIsVideo = false

    private val binding by viewBinding(FragmentHolderBinding::inflate)

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public override fun onCreate(savedInstanceState: Bundle?) {
        showTransparentTop = true

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (checkAppSideloading()) {
            return
        }

        setupOptionsMenu()
        refreshMenuItems()
        handlePermission(getPermissionToRequest()) {
            if (it) {
                checkIntent(savedInstanceState)
            } else {
                toast(R.string.no_storage_permissions)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (config.bottomActions) {
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            setTranslucentNavigation()
        }

        if (config.blackBackground) {
            updateStatusbarColor(Color.BLACK)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initBottomActionsLayout()

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        (binding.fragmentViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        if (!portrait && navigationBarOnSide && navigationBarWidth > 0) {
            binding.fragmentViewerToolbar.setPadding(0, 0, navigationBarWidth, 0)
        } else {
            binding.fragmentViewerToolbar.setPadding(0, 0, 0, 0)
        }
    }

    fun refreshMenuItems() {
        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0

        binding.fragmentViewerToolbar.menu.apply {
            findItem(R.id.menu_set_as).isVisible =
                mMedium?.isImage() == true && visibleBottomActions and BOTTOM_ACTION_SET_AS == 0
            findItem(R.id.menu_edit).isVisible =
                mMedium?.isImage() == true && mUri?.scheme == "file" && visibleBottomActions and BOTTOM_ACTION_EDIT == 0
            findItem(R.id.menu_properties).isVisible =
                mUri?.scheme == "file" && visibleBottomActions and BOTTOM_ACTION_PROPERTIES == 0
            findItem(R.id.menu_share).isVisible = visibleBottomActions and BOTTOM_ACTION_SHARE == 0
            findItem(R.id.menu_show_on_map).isVisible =
                visibleBottomActions and BOTTOM_ACTION_SHOW_ON_MAP == 0
        }
    }

    private fun setupOptionsMenu() {
        (binding.fragmentViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        binding.fragmentViewerToolbar.apply {
            setTitleTextColor(Color.WHITE)
            overflowIcon = resources.getColoredDrawableWithColor(
                R.drawable.ic_three_dots_vector,
                Color.WHITE
            )
            navigationIcon = resources.getColoredDrawableWithColor(
                R.drawable.ic_arrow_left_vector,
                Color.WHITE
            )
        }

        updateMenuItemColors(binding.fragmentViewerToolbar.menu, forceWhiteIcons = true)
        binding.fragmentViewerToolbar.setOnMenuItemClickListener { menuItem ->
            if (mMedium == null || mUri == null) {
                return@setOnMenuItemClickListener true
            }

            when (menuItem.itemId) {
                R.id.menu_set_as -> setAs(mUri!!.toString())
                R.id.menu_open_with -> openPath(mUri!!.toString(), true)
                R.id.menu_share -> sharePath(mUri!!.toString())
                R.id.menu_edit -> toast("This feature is not implemented yet")
                R.id.menu_properties -> showProperties()
                R.id.menu_show_on_map -> showFileOnMap(mUri!!.toString())
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }

        binding.fragmentViewerToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun checkIntent(savedInstanceState: Bundle? = null) {
        if (intent.data == null && intent.action == Intent.ACTION_VIEW) {
            hideKeyboard()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        mUri = intent.data ?: return
        val uri = mUri.toString()
        if (uri.startsWith("content:/") && uri.contains("/storage/") && !intent.getBooleanExtra(
                IS_IN_RECYCLE_BIN,
                false
            )
        ) {
            val guessedPath = uri.substring(uri.indexOf("/storage/"))
            if (getDoesFilePathExist(guessedPath)) {
                val extras = intent.extras ?: Bundle()
                extras.apply {
                    putString(REAL_FILE_PATH, guessedPath)
                    intent.putExtras(this)
                }
            }
        }

        var filename = getFilenameFromUri(mUri!!)
        mIsFromGallery = intent.getBooleanExtra(IS_FROM_GALLERY, false)
        if (mIsFromGallery && filename.isVideoFast() && config.openVideosOnSeparateScreen) {
            launchVideoPlayer()
            return
        }

        if (intent.extras?.containsKey(REAL_FILE_PATH) == true) {
            val realPath = intent.extras!!.getString(REAL_FILE_PATH)
            if (realPath != null && getDoesFilePathExist(realPath)) {
                val isFileFolderHidden = (File(realPath).isHidden || File(
                    realPath.getParentPath(),
                    NOMEDIA
                ).exists() || realPath.contains("/."))
                val preventShowingHiddenFile =
                    (isRPlus() && !isExternalStorageManager()) && isFileFolderHidden
                if (!preventShowingHiddenFile) {
                    if (realPath.getFilenameFromPath().contains('.') || filename.contains('.')) {
                        if (isFileTypeVisible(realPath)) {
                            binding.bottomActions.root.beGone()
                            sendViewPagerIntent(realPath)
                            finish()
                            return
                        }
                    } else {
                        filename = realPath.getFilenameFromPath()
                    }
                }
            }
        }

        if (mUri!!.scheme == "file") {
            if (filename.contains('.')) {
                binding.bottomActions.root.beGone()
                rescanPaths(arrayListOf(mUri!!.path!!))
                sendViewPagerIntent(mUri!!.path!!)
                finish()
            }
            return
        } else {
            val realPath = applicationContext.getRealPathFromURI(mUri!!) ?: ""
            val isFileFolderHidden = (File(realPath).isHidden || File(
                realPath.getParentPath(),
                NOMEDIA
            ).exists() || realPath.contains("/."))
            val preventShowingHiddenFile =
                (isRPlus() && !isExternalStorageManager()) && isFileFolderHidden
            if (!preventShowingHiddenFile) {
                if (realPath != mUri.toString() && realPath.isNotEmpty() && mUri!!.authority != "mms" && filename.contains(
                        '.'
                    ) && getDoesFilePathExist(realPath)
                ) {
                    if (isFileTypeVisible(realPath)) {
                        binding.bottomActions.root.beGone()
                        rescanPaths(arrayListOf(mUri!!.path!!))
                        sendViewPagerIntent(realPath)
                        finish()
                        return
                    }
                }
            }
        }

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        if (!portrait && navigationBarOnSide && navigationBarWidth > 0) {
            binding.fragmentViewerToolbar.setPadding(0, 0, navigationBarWidth, 0)
        } else {
            binding.fragmentViewerToolbar.setPadding(0, 0, 0, 0)
        }

        checkNotchSupport()
        showSystemUI()
        val bundle = Bundle()
        val file = File(mUri.toString())
        val intentType = intent.type ?: ""
        val type = when {
            filename.isVideoFast() || intentType.startsWith("video/") -> TYPE_VIDEOS
            filename.isGif() || intentType.equals("image/gif", true) -> TYPE_GIFS
            filename.isRawFast() -> TYPE_RAWS
            filename.isSvg() -> TYPE_SVGS
            file.isPortrait() -> TYPE_PORTRAITS
            else -> TYPE_IMAGES
        }

        mIsVideo = type == TYPE_VIDEOS
        mMedium = Medium(
            null,
            filename,
            mUri.toString(),
            mUri!!.path!!.getParentPath(),
            0,
            0,
            file.length(),
            type,
            0,
            false,
            0L,
            0
        )
        binding.fragmentViewerToolbar.title =
            Html.fromHtml("<font color='${Color.WHITE.toHex()}'>${mMedium!!.name}</font>")
        bundle.putSerializable(MEDIUM, mMedium)

        if (savedInstanceState == null) {
            mFragment = if (mIsVideo) VideoFragment() else PhotoFragment()
            mFragment!!.listener = this
            mFragment!!.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_placeholder, mFragment!!).commit()
        }

        if (config.blackBackground) {
            binding.fragmentHolder.background = ColorDrawable(Color.BLACK)
        }

        if (config.maxBrightness) {
            val attributes = window.attributes
            attributes.screenBrightness = 1f
            window.attributes = attributes
        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val isFullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            mFragment?.fullscreenToggled(isFullscreen)
        }

        initBottomActions()
    }

    private fun launchVideoPlayer() {
        val newUri = getFinalUriFromPath(mUri.toString(), BuildConfig.APPLICATION_ID)
        if (newUri == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        var isPanorama = false
        val realPath = intent?.extras?.getString(REAL_FILE_PATH) ?: ""
        try {
            if (realPath.isNotEmpty()) {
                val fis = FileInputStream(File(realPath))
                parseFileChannel(realPath, fis.channel, 0, 0, 0) {
                    isPanorama = true
                }
            }
        } catch (ignored: Exception) {
        } catch (ignored: OutOfMemoryError) {
        }

        hideKeyboard()
        if (isPanorama) {
            Intent(applicationContext, PanoramaVideoActivity::class.java).apply {
                putExtra(PATH, realPath)
                startActivity(this)
            }
        } else {
            val mimeType = getUriMimeType(mUri.toString(), newUri)
            Intent(applicationContext, VideoPlayerActivity::class.java).apply {
                setDataAndType(newUri, mimeType)
                addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                if (intent.extras != null) {
                    putExtras(intent.extras!!)
                }

                startActivity(this)
            }
        }
        finish()
    }

    private fun sendViewPagerIntent(path: String) {
        ensureBackgroundThread {
            if (isPathPresentInMediaStore(path)) {
                openViewPager(path)
            } else {
                rescanPath(path) {
                    openViewPager(path)
                }
            }
        }
    }

    private fun openViewPager(path: String) {
        if (!intent.getBooleanExtra(IS_FROM_GALLERY, false)) {
            MediaActivity.mMedia.clear()
        }
        runOnUiThread {
            hideKeyboard()
            Intent(this, ViewPagerActivity::class.java).apply {
                putExtra(SKIP_AUTHENTICATION, intent.getBooleanExtra(SKIP_AUTHENTICATION, false))
                putExtra(SHOW_FAVORITES, intent.getBooleanExtra(SHOW_FAVORITES, false))
                putExtra(IS_VIEW_INTENT, true)
                putExtra(IS_FROM_GALLERY, mIsFromGallery)
                putExtra(PATH, path)
                startActivity(this)
            }
        }
    }

    private fun isPathPresentInMediaStore(path: String): Boolean {
        val uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(path)

        try {
            val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)
            cursor?.use {
                return cursor.moveToFirst()
            }
        } catch (_: Exception) {
        }

        return false
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun showProperties() {
        PropertiesDialog(this, mUri!!.path!!)
    }

    private fun isFileTypeVisible(path: String): Boolean {
        val filter = config.filterMedia
        return !(path.isImageFast() && filter and TYPE_IMAGES == 0 ||
                path.isVideoFast() && filter and TYPE_VIDEOS == 0 ||
                path.isGif() && filter and TYPE_GIFS == 0 ||
                path.isRawFast() && filter and TYPE_RAWS == 0 ||
                path.isSvg() && filter and TYPE_SVGS == 0 ||
                path.isPortrait() && filter and TYPE_PORTRAITS == 0)
    }

    private fun initBottomActions() {
        initBottomActionButtons()
        initBottomActionsLayout()
    }

    private fun initBottomActionsLayout() {
        binding.bottomActions.root.layoutParams.height =
            resources.getDimension(R.dimen.bottom_actions_height).toInt() + navigationBarHeight
        if (config.bottomActions) {
            binding.bottomActions.root.beVisible()
        } else {
            binding.bottomActions.root.beGone()
        }
    }

    private fun initBottomActionButtons() {
        arrayListOf(
            binding.bottomActions.bottomFavorite,
            binding.bottomActions.bottomDelete,
            binding.bottomActions.bottomRotate,
            binding.bottomActions.bottomProperties,
            binding.bottomActions.bottomChangeOrientation,
            binding.bottomActions.bottomSlideshow,
            binding.bottomActions.bottomShowOnMap,
            binding.bottomActions.bottomToggleFileVisibility,
            binding.bottomActions.bottomRename,
            binding.bottomActions.bottomCopy,
            binding.bottomActions.bottomMove,
            binding.bottomActions.bottomResize,
        ).forEach {
            it.beGone()
        }

        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0
        binding.bottomActions.bottomEdit.beVisibleIf(visibleBottomActions and BOTTOM_ACTION_EDIT != 0 && mMedium?.isImage() == true)
        binding.bottomActions.bottomEdit.setOnClickListener {
          toast("This feature is not implemented yet")
        }

        binding.bottomActions.bottomShare.beVisibleIf(visibleBottomActions and BOTTOM_ACTION_SHARE != 0)
        binding.bottomActions.bottomShare.setOnClickListener {
            if (mUri != null && binding.bottomActions.root.alpha == 1f) {
                sharePath(mUri!!.toString())
            }
        }

        binding.bottomActions.bottomSetAs.beVisibleIf(visibleBottomActions and BOTTOM_ACTION_SET_AS != 0 && mMedium?.isImage() == true)
        binding.bottomActions.bottomSetAs.setOnClickListener {
            setAs(mUri!!.toString())
        }

        binding.bottomActions.bottomShowOnMap.beVisibleIf(visibleBottomActions and BOTTOM_ACTION_SHOW_ON_MAP != 0)
        binding.bottomActions.bottomShowOnMap.setOnClickListener {
            showFileOnMap(mUri!!.toString())
        }
    }

    override fun fragmentClicked() {
        mIsFullScreen = !mIsFullScreen
        if (mIsFullScreen) {
            hideSystemUI()
        } else {
            showSystemUI()
        }

        val newAlpha = if (mIsFullScreen) 0f else 1f
        binding.topShadow.animate().alpha(newAlpha).start()
        if (!binding.bottomActions.root.isGone()) {
            binding.bottomActions.root.animate().alpha(newAlpha).start()
        }

        binding.fragmentViewerToolbar.animate().alpha(newAlpha).withStartAction {
            binding.fragmentViewerToolbar.beVisible()
        }.withEndAction {
            binding.fragmentViewerToolbar.beVisibleIf(newAlpha == 1f)
        }.start()
    }

    override fun videoEnded() = false

    override fun goToPrevItem() {}

    override fun goToNextItem() {}

    override fun launchViewVideoIntent(path: String) {}

    override fun isSlideShowActive() = false
}
