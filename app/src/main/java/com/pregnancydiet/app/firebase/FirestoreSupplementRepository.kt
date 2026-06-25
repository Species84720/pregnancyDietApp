package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.pregnancydiet.app.data.SupplementRepository
import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementLog
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime

class FirestoreSupplementRepository : SupplementRepository {
    override suspend fun loadSupplementsForDate(
        uid: String,
        date: LocalDate,
    ): Result<List<SupplementWithTodayStatus>> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val supplements = userRef.collection(SUPPLEMENTS_COLLECTION)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toSupplement() }
        val logsBySupplementId = userRef.collection(SUPPLEMENT_LOGS_COLLECTION)
            .whereEqualTo("date", date.toString())
            .get()
            .await()
            .documents
            .map { it.toSupplementLog() }
            .associateBy { it.supplementId }

        supplements.map { supplement ->
            SupplementWithTodayStatus(
                supplement = supplement,
                todayLog = logsBySupplementId[supplement.id],
            )
        }
    }

    override suspend fun saveSupplement(uid: String, supplement: Supplement): Result<Supplement> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val supplementsRef = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SUPPLEMENTS_COLLECTION)
        val supplementRef = supplement.id.takeIf { it.isNotBlank() }
            ?.let(supplementsRef::document)
            ?: supplementsRef.document()
        val supplementWithId = supplement.copy(id = supplementRef.id)
        supplementRef.set(supplementWithId.toFirestoreMap()).await()
        supplementWithId
    }

    override suspend fun deactivateSupplement(uid: String, supplementId: String): Result<Unit> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SUPPLEMENTS_COLLECTION)
            .document(supplementId)
            .update(
                mapOf(
                    "active" to false,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }

    override suspend fun markSupplementTaken(
        uid: String,
        supplementId: String,
        date: LocalDate,
        notes: String,
    ): Result<SupplementLog> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val safeDate = date.toString()
        val logRef = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SUPPLEMENT_LOGS_COLLECTION)
            .document("${supplementId}_$safeDate")
        val log = SupplementLog(
            id = logRef.id,
            supplementId = supplementId,
            date = safeDate,
            taken = true,
            takenAt = LocalDateTime.now().toString(),
            notes = notes.trim(),
        )
        logRef.set(log.toFirestoreMap()).await()
        log
    }
}

private const val USERS_COLLECTION = "users"
private const val SUPPLEMENTS_COLLECTION = "supplements"
private const val SUPPLEMENT_LOGS_COLLECTION = "supplementLogs"

private fun Supplement.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "dose" to dose,
    "frequency" to frequency,
    "timeOfDay" to timeOfDay,
    "prescribedBy" to prescribedBy,
    "instructions" to instructions,
    "startDate" to startDate,
    "endDate" to endDate,
    "active" to active,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp(),
)

private fun SupplementLog.toFirestoreMap(): Map<String, Any?> = mapOf(
    "supplementId" to supplementId,
    "date" to date,
    "taken" to taken,
    "takenAt" to takenAt,
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp(),
)

private fun DocumentSnapshot.toSupplement(): Supplement = Supplement(
    id = id,
    name = getString("name").orEmpty(),
    dose = getString("dose").orEmpty(),
    frequency = getString("frequency").orEmpty(),
    timeOfDay = getString("timeOfDay").orEmpty(),
    prescribedBy = getString("prescribedBy").orEmpty(),
    instructions = getString("instructions").orEmpty(),
    startDate = getString("startDate").orEmpty(),
    endDate = getString("endDate"),
    active = getBoolean("active") ?: true,
)

private fun DocumentSnapshot.toSupplementLog(): SupplementLog = SupplementLog(
    id = id,
    supplementId = getString("supplementId").orEmpty(),
    date = getString("date").orEmpty(),
    taken = getBoolean("taken") ?: false,
    takenAt = getString("takenAt"),
    notes = getString("notes").orEmpty(),
)
