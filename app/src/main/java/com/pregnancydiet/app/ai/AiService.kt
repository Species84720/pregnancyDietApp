package com.pregnancydiet.app.ai

interface AiService {
    val backendFunctionName: String

    suspend fun generateSummary(request: AiSummaryRequest): Result<String>
}

class SecureBackendAiServiceContract(
    override val backendFunctionName: String = "generatePregnancyAiSummary",
) : AiService {
    override suspend fun generateSummary(request: AiSummaryRequest): Result<String> = Result.failure(
        IllegalStateException(
            "Secure backend AI function '$backendFunctionName' is not configured in this Android build. " +
                "Android must send structured context to a backend and must not call Pollinations.ai directly.",
        ),
    )
}
