/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package code.name.monkey.retromusic.preferences

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.fragments.NowPlayingScreen
import code.name.monkey.retromusic.util.NavigationUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.ViewUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide

class NowPlayingScreenPreference : ATEDialogPreference {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    private val mLayoutRes = R.layout.preference_dialog_now_playing_screen

    override fun getDialogLayoutResource(): Int {
        return mLayoutRes;
    }

    init {
        icon?.setColorFilter(ThemeStore.textColorSecondary(context), PorterDuff.Mode.SRC_IN)
    }
}

class NowPlayingScreenPreferenceDialog : PreferenceDialogFragmentCompat(), ViewPager.OnPageChangeListener {

    private var viewPagerPosition: Int = 0

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        this.viewPagerPosition = position
    }

    override fun onDialogClosed(positiveResult: Boolean) {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.preference_dialog_now_playing_screen, null)
        val viewPager = view.findViewById<ViewPager>(R.id.now_playing_screen_view_pager)
                ?: throw  IllegalStateException("Dialog view must contain a ViewPager with id 'now_playing_screen_view_pager'")
        viewPager.adapter = NowPlayingScreenAdapter(activity!!)
        viewPager.addOnPageChangeListener(this)
        viewPager.pageMargin = ViewUtil.convertDpToPixel(32f, resources).toInt()
        viewPager.currentItem = PreferenceUtil.getInstance(requireContext()).nowPlayingScreen.ordinal


        return MaterialDialog(requireContext()).show {
            title(R.string.pref_title_now_playing_screen_appearance)
            positiveButton(R.string.set) {
                val nowPlayingScreen = NowPlayingScreen.values()[viewPagerPosition]
                if (isNowPlayingThemes(nowPlayingScreen)) {
                    val result = getString(nowPlayingScreen.titleRes) + " theme is Pro version feature."
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                    NavigationUtil.goToProVersion(activity!!)
                } else {
                    PreferenceUtil.getInstance(requireContext()).nowPlayingScreen = nowPlayingScreen
                }
            }
            cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
            negativeButton(android.R.string.cancel)
            customView(view = view, scrollable = false, noVerticalPadding = false)
        }
    }

    private fun isNowPlayingThemes(nowPlayingScreen: NowPlayingScreen): Boolean {
        if (nowPlayingScreen == NowPlayingScreen.BLUR_CARD) {
            PreferenceUtil.getInstance(requireContext()).resetCarouselEffect()
            PreferenceUtil.getInstance(requireContext()).resetCircularAlbumArt()
        }

        return (nowPlayingScreen == NowPlayingScreen.FULL ||
                nowPlayingScreen == NowPlayingScreen.CARD ||
                nowPlayingScreen == NowPlayingScreen.PLAIN ||
                nowPlayingScreen == NowPlayingScreen.BLUR ||
                nowPlayingScreen == NowPlayingScreen.COLOR ||
                nowPlayingScreen == NowPlayingScreen.SIMPLE ||
                nowPlayingScreen == NowPlayingScreen.BLUR_CARD ||
                nowPlayingScreen == NowPlayingScreen.CIRCLE ||
                nowPlayingScreen == NowPlayingScreen.ADAPTIVE)
                && !App.isProVersion()
    }

    companion object {
        fun newInstance(key: String): NowPlayingScreenPreferenceDialog {
            val bundle = Bundle()
            bundle.putString(ARG_KEY, key)
            val fragment = NowPlayingScreenPreferenceDialog()
            fragment.arguments = bundle
            return fragment
        }
    }
}

private class NowPlayingScreenAdapter internal constructor(private val context: Context) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val nowPlayingScreen = NowPlayingScreen.values()[position]

        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.preference_now_playing_screen_item, collection, false) as ViewGroup
        collection.addView(layout)

        val image = layout.findViewById<ImageView>(R.id.image)
        val title = layout.findViewById<TextView>(R.id.title)
        Glide.with(context).load(nowPlayingScreen.drawableResId).into(image)
        title.setText(nowPlayingScreen.titleRes)

        return layout
    }

    override fun destroyItem(collection: ViewGroup,
                             position: Int,
                             view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return NowPlayingScreen.values().size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(NowPlayingScreen.values()[position].titleRes)
    }
}