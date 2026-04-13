package com.storybuilder.data.ai.di

import com.storybuilder.data.ai.GeminiApiService
import com.storybuilder.data.ai.client.AIClientFactory
import com.storybuilder.data.ai.client.UnifiedAIClient
import com.storybuilder.data.ai.client.anthropic.AnthropicClient
import com.storybuilder.data.ai.client.anthropic.AnthropicService
import com.storybuilder.data.ai.client.gemini.GeminiUnifiedClient
import com.storybuilder.data.ai.client.nim.NimClient
import com.storybuilder.data.ai.client.nim.NimService
import com.storybuilder.data.ai.client.openai.OpenAIClient
import com.storybuilder.data.ai.client.openai.OpenAIService
import com.storybuilder.data.ai.client.openrouter.OpenRouterClient
import com.storybuilder.data.ai.client.openrouter.OpenRouterService
import com.storybuilder.data.ai.interceptor.NetworkConnectionInterceptor
import com.storybuilder.data.ai.repository.StoryAIRepositoryImpl
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.repository.StoryAIRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {

    companion object {
        // Base URLs
        private const val GOOGLE_BASE_URL = "https://generativelanguage.googleapis.com/"
        private const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
        private const val ANTHROPIC_BASE_URL = "https://api.anthropic.com/v1/"
        private const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"
        private const val NIM_BASE_URL = "https://integrate.api.nvidia.com/v1/"

        // ========== Google/Gemini ==========
        
        @Provides
        @Singleton
        @Named("BaseOkHttpClient")
        fun provideBaseOkHttpClient(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ): OkHttpClient {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            return OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
        }

        @Provides
        @Singleton
        fun provideGeminiOkHttpClient(
            @Named("BaseOkHttpClient") baseClient: OkHttpClient
        ): OkHttpClient {
            val apiKeyInterceptor = Interceptor { chain ->
                val original = chain.request()
                val url = original.url.newBuilder()
                    .addQueryParameter("key", ApiKeyProvider.getApiKey())
                    .build()
                val request = original.newBuilder()
                    .url(url)
                    .build()
                chain.proceed(request)
            }

            return baseClient.newBuilder()
                .addInterceptor(apiKeyInterceptor)
                .build()
        }

        @Provides
        @Singleton
        fun provideGeminiRetrofit(okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(GOOGLE_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideGeminiApiService(retrofit: Retrofit): GeminiApiService {
            return retrofit.create(GeminiApiService::class.java)
        }

        // ========== OpenAI ==========
        
        @Provides
        @Singleton
        @Named("OpenAIRetrofit")
        fun provideOpenAIRetrofit(
            @Named("BaseOkHttpClient") baseClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(OPENAI_BASE_URL)
                .client(baseClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideOpenAIService(@Named("OpenAIRetrofit") retrofit: Retrofit): OpenAIService {
            return retrofit.create(OpenAIService::class.java)
        }

        // ========== Anthropic ==========
        
        @Provides
        @Singleton
        @Named("AnthropicRetrofit")
        fun provideAnthropicRetrofit(
            @Named("BaseOkHttpClient") baseClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(ANTHROPIC_BASE_URL)
                .client(baseClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideAnthropicService(@Named("AnthropicRetrofit") retrofit: Retrofit): AnthropicService {
            return retrofit.create(AnthropicService::class.java)
        }

        // ========== OpenRouter ==========
        
        @Provides
        @Singleton
        @Named("OpenRouterRetrofit")
        fun provideOpenRouterRetrofit(
            @Named("BaseOkHttpClient") baseClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(OPENROUTER_BASE_URL)
                .client(baseClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideOpenRouterService(@Named("OpenRouterRetrofit") retrofit: Retrofit): OpenRouterService {
            return retrofit.create(OpenRouterService::class.java)
        }

        // ========== NVIDIA NIM ==========
        
        @Provides
        @Singleton
        @Named("NimRetrofit")
        fun provideNimRetrofit(
            @Named("BaseOkHttpClient") baseClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(NIM_BASE_URL)
                .client(baseClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideNimService(@Named("NimRetrofit") retrofit: Retrofit): NimService {
            return retrofit.create(NimService::class.java)
        }

        // ========== AI Client Factory ==========
        
        @Provides
        @Singleton
        fun provideAIClientFactory(
            openAIClient: OpenAIClient,
            anthropicClient: AnthropicClient,
            geminiClient: GeminiUnifiedClient,
            openRouterClient: OpenRouterClient,
            nimClient: NimClient
        ): AIClientFactory {
            return AIClientFactory(
                mapOf(
                    ApiProvider.OPENAI to openAIClient,
                    ApiProvider.ANTHROPIC to anthropicClient,
                    ApiProvider.GOOGLE to geminiClient,
                    ApiProvider.OPENROUTER to openRouterClient,
                    ApiProvider.NIM to nimClient
                )
            )
        }
    }

    @Binds
    abstract fun bindStoryAIRepository(
        impl: StoryAIRepositoryImpl
    ): StoryAIRepository
}

/**
 * Singleton object to store the API key dynamically (legacy support)
 */
object ApiKeyProvider {
    @Volatile
    private var apiKey: String = ""

    fun setApiKey(key: String) {
        apiKey = key
    }

    fun getApiKey(): String = apiKey
}
