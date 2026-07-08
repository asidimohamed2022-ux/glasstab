package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.GameViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : ComponentActivity() {
  
  private val TAG = "MainActivity"
  private var rewardedAd: RewardedAd? = null

  private val gameViewModel: GameViewModel by viewModels {
    GameViewModelFactory((application as GlassTapApplication).repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Mobile Ads SDK
    MobileAds.initialize(this) { status ->
      Log.d(TAG, "Mobile Ads SDK Initialized.")
      loadRewardedAd()
    }

    // Set Ad Callback trigger on ViewModel
    gameViewModel.onShowAdRequested = {
      rewardedAd?.let { ad ->
        ad.show(this) { rewardItem ->
          Log.d(TAG, "Rewarded callback triggered. Earned reward: ${rewardItem.amount}")
          gameViewModel.onAdRewardCompleted()
        }
      } ?: run {
        Log.e(TAG, "Ad was not loaded when show requested.")
        gameViewModel.isAdReady = false
        loadRewardedAd()
      }
    }

    setContent {
      MyApplicationTheme {
        GameScreen(viewModel = gameViewModel)
      }
    }
  }

  private fun loadRewardedAd() {
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
      this,
      "ca-app-pub-3940256099942544/5224354917",
      adRequest,
      object : RewardedAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.e(TAG, "Ad failed to load: ${adError.message}")
          rewardedAd = null
          gameViewModel.isAdReady = false
        }

        override fun onAdLoaded(ad: RewardedAd) {
          Log.d(TAG, "Ad loaded successfully.")
          rewardedAd = ad
          gameViewModel.isAdReady = true

          ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
              Log.d(TAG, "Ad dismissed by user.")
              rewardedAd = null
              gameViewModel.isAdReady = false
              loadRewardedAd() // Pre-load next ad
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
              Log.e(TAG, "Ad failed to show: ${adError.message}")
              rewardedAd = null
              gameViewModel.isAdReady = false
              loadRewardedAd() // Pre-load next ad
            }
          }
        }
      }
    )
  }
}

