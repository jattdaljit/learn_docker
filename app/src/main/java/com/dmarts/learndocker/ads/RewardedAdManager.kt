package com.dmarts.learndocker.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        RewardedAd.load(
            context.applicationContext,
            AdIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    /** Shows the rewarded ad if loaded. [onRewarded] fires when the user earns the reward.
     *  [onDismissed] fires when the ad closes (with or without reward).
     *  Returns false if the ad is not ready yet. */
    fun show(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit): Boolean {
        val ad = rewardedAd ?: return false
        rewardedAd = null          // clear so we preload next one
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismissed()
                preload(activity)  // reload for next time
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                onDismissed()
                preload(activity)
            }
        }
        ad.show(activity) { onRewarded() }
        return true
    }

    fun isReady() = rewardedAd != null
}
