package com.blueoauld.server.common.configuration

import com.google.crypto.tink.apps.rewardedads.RewardedAdsVerifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdMobConfiguration {

    @Bean
    fun rewardedAdsVerifier(): RewardedAdsVerifier =
        RewardedAdsVerifier.Builder()
            .fetchVerifyingPublicKeysWith(
                RewardedAdsVerifier.KEYS_DOWNLOADER_INSTANCE_PROD
            )
            .build()
}