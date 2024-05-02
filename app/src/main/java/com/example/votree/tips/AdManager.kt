package com.example.votree.tips

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

object AdManager {
    fun loadBannerAd(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}