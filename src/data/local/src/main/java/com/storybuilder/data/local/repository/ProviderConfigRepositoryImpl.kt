package com.storybuilder.data.local.repository

import com.storybuilder.data.local.preferences.SettingsDataStore
import com.storybuilder.domain.model.ApiCredentials
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.ProviderConfigurations
import com.storybuilder.domain.repository.ProviderConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderConfigRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ProviderConfigRepository {

    override fun getProviderConfigurations(): Flow<ProviderConfigurations> {
        return settingsDataStore.getProviderConfigurations()
    }

    override suspend fun getProviderConfigurationsSync(): ProviderConfigurations {
        return settingsDataStore.getProviderConfigurationsSync()
    }

    override suspend fun saveApiCredentials(credentials: ApiCredentials) {
        settingsDataStore.saveApiCredentials(credentials)
    }

    override suspend fun setActiveProvider(provider: ApiProvider) {
        settingsDataStore.setActiveProvider(provider)
    }

    override fun getActiveProvider(): Flow<ApiProvider> {
        return settingsDataStore.getActiveProvider()
    }

    override suspend fun getActiveProviderSync(): ApiProvider {
        return settingsDataStore.getActiveProviderSync()
    }

    override suspend fun getActiveCredentials(): ApiCredentials? {
        return getProviderConfigurationsSync().getActiveCredentials()
    }

    override suspend fun clearProviderCredentials(provider: ApiProvider) {
        settingsDataStore.clearProviderCredentials(provider)
    }

    override suspend fun hasAnyConfiguredProvider(): Boolean {
        return settingsDataStore.hasAnyConfiguredProvider()
    }
}
