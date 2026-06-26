package com.pregnancydiet.app.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAccessModeRepositoryTest {
    @Test
    fun `default access mode is free hourly`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()

        assertEquals(AiAccessMode.FREE_HOURLY, repository.getAccessMode())
    }

    @Test
    fun `user mode requires credential`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()

        repository.setAccessMode(AiAccessMode.USER_ACCOUNT)

        assertEquals(AiAccessMode.FREE_HOURLY, repository.getAccessMode())
    }

    @Test
    fun `disconnect clears credential and switches to free hourly`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()
        assertEquals(SaveCredentialResult.Success, repository.saveUserAccountCredential("pk_user_safe"))

        repository.clearUserAccountCredential()

        assertEquals(AiAccessMode.FREE_HOURLY, repository.getAccessMode())
        assertEquals(null, repository.getUserAccountCredentialOrNull())
    }

    @Test
    fun `blank credential fails`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()

        assertEquals(SaveCredentialResult.Blank, repository.saveUserAccountCredential("   "))
    }

    @Test
    fun `server secret credential is blocked`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()

        assertEquals(SaveCredentialResult.UnsafeSecretKey, repository.saveUserAccountCredential("sk_server_secret"))
    }

    @Test
    fun `obviously malformed credential fails`() = runBlocking {
        val repository = InMemoryAiAccessModeRepository()

        assertEquals(SaveCredentialResult.InvalidFormat, repository.saveUserAccountCredential("bad key with spaces"))
    }

    @Test
    fun `credential validator blocks unsafe server secrets`() {
        assertTrue(AiCredentialValidator.isUnsafeServerSecret("sk_server_secret"))
    }
}
