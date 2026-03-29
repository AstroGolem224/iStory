package com.storybuilder.data.ai

import com.storybuilder.data.ai.di.ApiKeyProvider
import com.storybuilder.data.ai.repository.StoryAIRepositoryImpl
import com.storybuilder.domain.model.Story
import com.storybuilder.domain.model.StoryBeat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StoryAIRepositoryIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: StoryAIRepositoryImpl
    private lateinit var aiClient: AIClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        ApiKeyProvider.setApiKey("test-api-key")

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GeminiApiService::class.java)
        aiClient = AIClient(apiService)
        
        val promptFactory = PromptFactory()
        val responseParser = ResponseParser()
        
        repository = StoryAIRepositoryImpl(
            aiClient = aiClient,
            promptFactory = promptFactory,
            responseParser = responseParser
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `generateOpeningBeat should return valid StoryBeat with 3 options`() = runBlocking {
        // Given
        val story = Story(
            id = "test-story-001",
            title = "Test Story",
            genreId = "mystery",
            characterId = "char-001",
            darknessLevel = 5
        )

        val mockResponse = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": "{\"narratorText\": \"The ancient door creaks open, revealing a dark corridor. Dust motes dance in the faint light from your lantern. Something moves in the shadows ahead.\", \"suggestedOptions\": [\"Search for hidden traps carefully\", \"Enter boldly with weapon drawn\", \"Listen for sounds from within\"], \"storyComplete\": false}"
                                }
                            ],
                            "role": "model"
                        },
                        "finishReason": "STOP",
                        "index": 0
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.generateOpeningBeat(
            story = story,
            characterName = "Detective Test",
            characterDescription = "A test character",
            genreName = "Mystery"
        )

        // Then
        assertTrue(result.isSuccess)
        val beat = result.getOrNull()
        assertNotNull(beat)
        assertEquals(story.id, beat?.storyId)
        assertEquals(0, beat?.sequenceOrder)
        assertTrue(beat?.narratorText?.isNotBlank() == true)
        assertEquals(3, beat?.suggestedOptions?.size)
        assertEquals("Search for hidden traps carefully", beat?.suggestedOptions?.get(0))
        assertEquals("Enter boldly with weapon drawn", beat?.suggestedOptions?.get(1))
        assertEquals("Listen for sounds from within", beat?.suggestedOptions?.get(2))
    }

    @Test
    fun `generateNextBeat should use previous context`() = runBlocking {
        // Given
        val story = Story(
            id = "test-story-002",
            title = "Test Story 2",
            genreId = "fantasy",
            characterId = "char-002",
            darknessLevel = 7
        )

        val previousBeat = StoryBeat(
            id = "beat-001",
            storyId = story.id,
            sequenceOrder = 0,
            narratorText = "You stand at the cave entrance.",
            suggestedOptions = listOf("Enter", "Leave", "Wait"),
            selectedOptionIndex = 0
        )

        val mockResponse = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": "{\"narratorText\": \"You step into the cave. The air grows colder with each step. Your torch flickers, casting dancing shadows on the wet stone walls.\", \"suggestedOptions\": [\"Light a new torch\", \"Continue forward cautiously\", \"Call out to check for echoes\"], \"storyComplete\": false}"
                                }
                            ],
                            "role": "model"
                        },
                        "finishReason": "STOP",
                        "index": 0
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )

        // When
        val result = repository.generateNextBeat(
            story = story,
            characterName = "Warrior Test",
            characterDescription = "A brave warrior",
            genreName = "Fantasy",
            previousBeats = listOf(previousBeat)
        )

        // Then
        assertTrue(result.isSuccess)
        val beat = result.getOrNull()
        assertNotNull(beat)
        assertEquals(1, beat?.sequenceOrder)
        assertEquals(3, beat?.suggestedOptions?.size)
    }

    @Test
    fun `repository should expose isGenerating flow`() = runBlocking {
        // Verify isGenerating flow exists and has initial value
        val initialValue = repository.isGenerating.value
        assertEquals(false, initialValue)
    }
}
