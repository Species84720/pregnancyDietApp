package com.pregnancydiet.app.ai

interface AiRepository {
    suspend fun generateSummary(request: AiSummaryRequest): AiSummaryResult
}

class DefaultAiRepository(
    private val service: AiService = SecureBackendAiServiceContract(),
    private val parser: AiResponseParser = AiResponseParser(),
) : AiRepository {
    override suspend fun generateSummary(request: AiSummaryRequest): AiSummaryResult {
        val rawResult = service.generateSummary(request)
        return rawResult.fold(
            onSuccess = { rawResponse -> parser.parse(rawResponse, request) },
            onFailure = { failure ->
                AiPromptGuardrails.fallbackFor(
                    request = request,
                    reason = failure.message ?: "AI backend request failed.",
                )
            },
        )
    }
}
