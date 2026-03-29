package com.storybuilder.domain.repository

import com.storybuilder.domain.model.ApiCredentials
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.ProviderConfigurations
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing AI provider configurations
 */
interface ProviderConfigRepository {
    /**
     * Get all provider configurations
     */
    fun getProviderConfigurations(): Flow<ProviderConfigurations>
    
    /**
     * Get configurations synchronously
     */
    suspend fun getProviderConfigurationsSync(): ProviderConfigurations
    
    /**
     * Save API credentials for a provider
     */
    suspend fun saveApiCredentials(credentials: ApiCredentials)
    
    /**
     * Set the active provider
     */
    suspend fun setActiveProvider(provider: ApiProvider)
    
    /**
     * Get the active provider
     */
    fun getActiveProvider(): Flow<ApiProvider>
    
    /**
     * Get active provider synchronously
     */
    suspend fun getActiveProviderSync(): ApiProvider
    
    /**
     * Get the currently active credentials
     */
    suspend fun getActiveCredentials(): ApiCredentials?
    
    /**
     * Clear credentials for a provider
     */
    suspend fun clearProviderCredentials(provider: ApiProvider)
    
    /**
     * Check if any provider is configured
     */
    suspend fun hasAnyConfiguredProvider(): Boolean
}
