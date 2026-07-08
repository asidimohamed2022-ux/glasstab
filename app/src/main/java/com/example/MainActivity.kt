package com.aistudio.glasstap.gltapg

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : ComponentActivity() {

    private var rewardedAd: RewardedAd? = null
    private var adStatus by mutableStateOf("Initializing Ads...")
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = {
                    if (rewardedAd != null) {
                        rewardedAd?.show(this@MainActivity) { rewardItem ->
                            val rewardAmount = rewardItem.amount
                            val rewardType = rewardItem.type
                            Log.d(TAG, "User earned the reward: $rewardAmount $rewardType")
                            adStatus = "Reward Earned!"
                            loadRewardedAd() // Load the next ad
                        }
                    } else {
                        Log.d(TAG, "The rewarded ad wasn't ready yet.")
                    }
                }, enabled = rewardedAd != null) {
                    Text(text = if (rewardedAd != null) "Watch Ad for Reward" else adStatus)
                }
            }
        }

        // 1. Initialize the Mobile Ads SDK
        MobileAds.initialize(this) { initializationStatus ->
            Log.d(TAG, "Initialization complete: $initializationStatus")
            runOnUiThread {
                adStatus = "Loading Ad..."
                loadRewardedAd()
            }
        }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        // Using Google's official public test rewarded ad unit ID
        val testAdUnitId = "ca-app-pub-3940256099942544/5224354917"

        RewardedAd.load(this, testAdUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                rewardedAd = null
                adStatus = "Ad Failed to Load: ${adError.message}"
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                adStatus = "Ad Ready"
            }
        })
    }
}
