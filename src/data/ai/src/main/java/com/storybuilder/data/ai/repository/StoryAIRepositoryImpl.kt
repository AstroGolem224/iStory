package com.storybuilder.data.ai.repository

import com.storybuilder.data.ai.AIClient
import com.storybuilder.data.ai.PromptFactory
import com.storybuilder.data.ai.ResponseParser
import com.storybuilder.data.ai.client.AIClientFactory
import com.storybuilder.data.ai.client.AIClientError
import com.storybuilder.data.ai.model.Candidate
import com.storybuilder.data.ai.model.ContentResponse
import com.storybuilder.data.ai.model.GeminiResponse
import com.storybuilder.data.ai.model.PartResponse
import com.storybuilder.domain.model.ApiProvider
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import com.storybuilder.domain.repository.ProviderConfigRepository
import com.storybuilder.domain.repository.StoryAIRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryAIRepositoryImpl @Inject constructor(
    private val promptFactory: PromptFactory,
    private val responseParser: ResponseParser,
    private val clientFactory: AIClientFactory,
    private val providerConfigRepository: ProviderConfigRepository
) : StoryAIRepository {

    private val _isGenerating = MutableStateFlow(false)
    override val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _activeProvider = MutableStateFlow(ApiProvider.GOOGLE)
    
    init {
        // Load active provider from settings asynchronously, but initial GOOGLE default is safe
        CoroutineScope(Dispatchers.IO).launch {
            providerConfigRepository.getActiveProvider().collect { provider ->
                _activeProvider.value = provider
            }
        }
    }

    override suspend fun generateOpeningBeat(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String
    ): Result<StoryBeat> {
        _isGenerating.value = true
        return try {
            val prompt = promptFactory.buildOpeningBeatPrompt(
                story = story,
                characterName = characterName,
                characterDescription = characterDescription,
                genreName = genreName,
                genreToneGuidelines = genreToneGuidelines
            )

            generateWithActiveProvider(prompt, story.id, 0)
        } finally {
            _isGenerating.value = false
        }
    }

    override suspend fun generateNextBeat(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>
    ): Result<StoryBeat> {
        _isGenerating.value = true
        return try {
            val prompt = promptFactory.buildOptionsModePrompt(
                story = story,
                characterName = characterName,
                characterDescription = characterDescription,
                genreName = genreName,
                genreToneGuidelines = genreToneGuidelines,
                previousBeats = previousBeats
            )

            val nextSequenceOrder = previousBeats.maxOfOrNull { it.sequenceOrder }?.plus(1) ?: 0
            generateWithActiveProvider(prompt, story.id, nextSequenceOrder)
        } finally {
            _isGenerating.value = false
        }
    }

    override suspend fun generateFreeTextBeat(
        story: Story,
        characterName: String,
        characterDescription: String,
        genreName: String,
        genreToneGuidelines: String,
        previousBeats: List<StoryBeat>,
        userInput: String
    ): Result<StoryBeat> {
        _isGenerating.value = true
        return try {
            val prompt = promptFactory.buildFreeTextModePrompt(
                story = story,
                characterName = characterName,
                characterDescription = characterDescription,
                genreName = genreName,
                genreToneGuidelines = genreToneGuidelines,
                previousBeats = previousBeats,
                userInput = userInput
            )

            val nextSequenceOrder = previousBeats.maxOfOrNull { it.sequenceOrder }?.plus(1) ?: 0
            generateWithActiveProvider(prompt, story.id, nextSequenceOrder)
        } finally {
            _isGenerating.value = false
        }
    }

    private suspend fun generateWithActiveProvider(
        prompt: String,
        storyId: String,
        sequenceOrder: Int
    ): Result<StoryBeat> {
        val provider = _activeProvider.value
        val credentials = providerConfigRepository.getActiveCredentials()
            ?: return Result.failure(AIClientError.ProviderError("No active provider configured"))

        val client = clientFactory.getClient(provider)
        
        // Default temperature
        val temperature = 0.8f

        return client.generateResponse(
            prompt = prompt,
            credentials = credentials,
            temperature = temperature
        ).fold(
            onSuccess = { unifiedResponse ->
                // Map unified response to Gemini-like format for parsing
                val geminiResponse = GeminiResponse(
                    candidates = listOf(
                        Candidate(
                            content = ContentResponse(
                                parts = listOf(
                                    PartResponse(text = unifiedResponse.content)
                                ),
                                role = "model"
                            ),
                            finishReason = "STOP",
                            index = 0
                        )
                    )
                )
                
                responseParser.parseStoryBeatResponse(
                    geminiResponse = geminiResponse,
                    storyId = storyId,
                    sequenceOrder = sequenceOrder
                )
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override suspend fun setActiveProvider(provider: ApiProvider) {
        _activeProvider.value = provider
        providerConfigRepository.setActiveProvider(provider)
    }

    override fun getActiveProvider(): ApiProvider = _activeProvider.value
    
    override fun getActiveProviderFlow(): StateFlow<ApiProvider> = _activeProvider.asStateFlow()

    override suspend fun testConnection(provider: ApiProvider): Result<Boolean> {
        val configs = providerConfigRepository.getProviderConfigurationsSync()
        val credentials = configs.providers[provider]
            ?: return Result.failure(AIClientError.ProviderError("Provider not configured"))

        if (credentials.apiKey.isBlank()) {
            return Result.failure(AIClientError.InvalidApiKey(provider.name))
        }

        val client = clientFactory.getClient(provider)
        return client.testConnection(credentials)
    }
}
