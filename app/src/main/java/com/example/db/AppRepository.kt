package com.example.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class AppRepository(private val appDao: AppDao) {

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    // ----------------------------------------------------------------
    // GLOBAL SYNC FUNCTION (App Call this on Start or Refresh)
    // ----------------------------------------------------------------
    suspend fun syncAllFromCloud() = withContext(Dispatchers.IO) {
        try {
            // 1. Sync Users
            fetchListFromCloud<User>("users")?.let { cloudUsers ->
                cloudUsers.forEach { appDao.insertUser(it) }
            }
            // 2. Sync Articles
            fetchListFromCloud<Article>("articles")?.let { cloudArticles ->
                cloudArticles.forEach { appDao.insertArticle(it) }
            }
            // 3. Sync Contacts
            fetchListFromCloud<Contact>("contacts")?.let { cloudContacts ->
                cloudContacts.forEach { appDao.insertContact(it) }
            }
            // 4. Sync Stock Entries
            fetchListFromCloud<StockEntry>("stock_entries")?.let { cloudEntries ->
                cloudEntries.forEach { appDao.insertStockEntry(it) }
            }
            // 5. Sync Stock Exits
            fetchListFromCloud<StockExit>("stock_exits")?.let { cloudExits ->
                cloudExits.forEach { appDao.insertStockExit(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inline fun <reified T> fetchListFromCloud(tableName: String): List<T>? {
        return try {
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/$tableName?select=*")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonString = response.body?.string() ?: return null
                val type = Types.newParameterizedType(List::class.java, T::class.java)
                moshi.adapter<List<T>>(type).fromJson(jsonString)
            } else {
                response.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ----------------------------------------------------------------
    // USERS (Cloud + Local Sync)
    // ----------------------------------------------------------------
    val allUsersFlow: Flow<List<User>> = appDao.getAllUsersFlow()
    
    suspend fun getUserByUsername(username: String): User? = appDao.getUserByUsername(username)
    suspend fun getUserById(id: Int): User? = appDao.getUserById(id)

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        try {
            // نرسلو البيانات بلا الـ ID باش Supabase يتكلف بالترتيب وما يوقعش تعارض
            val jsonPayload = "{\"username\":\"${user.username}\",\"full_name\":\"${user.fullName}\",\"role\":\"${user.role}\",\"password_hash\":\"${user.passwordHash}\"}"
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/users")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.insertUser(user)
    }

    suspend fun deleteUserById(userId: Int) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/users?id=eq.$userId")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.deleteUserById(userId)
    }

    suspend fun updateUser(id: Int, username: String, fullName: String, role: String) = withContext(Dispatchers.IO) {
        val existing = appDao.getUserById(id)
        val hash = existing?.passwordHash ?: "admin123"
        try {
            val jsonPayload = "{\"username\":\"$username\",\"full_name\":\"$fullName\",\"role\":\"$role\",\"password_hash\":\"$hash\"}"
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/users?id=eq.$id")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .patch(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.updateUser(id, hash, fullName, role)
    }

    suspend fun updateUserWithPassword(id: Int, passwordHash: String, fullName: String, role: String) = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = "{\"username\":\"\",\"full_name\":\"$fullName\",\"role\":\"$role\",\"password_hash\":\"$passwordHash\"}"
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/users?id=eq.$id")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .patch(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.updateUser(id, passwordHash, fullName, role)
    }

    // ----------------------------------------------------------------
    // ARTICLES (Cloud + Local Sync)
    // ----------------------------------------------------------------
    val allArticlesFlow: Flow<List<Article>> = appDao.getAllArticlesFlow()
    
    suspend fun getArticleByReference(ref: String): Article? = appDao.getArticleByReference(ref)
    suspend fun getArticleById(id: Int): Article? = appDao.getArticleById(id)

    suspend fun insertArticle(article: Article): Long = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = "{\"reference\":\"${article.reference}\",\"name\":\"${article.name}\",\"description\":\"${article.description}\",\"quantity\":${article.quantity}}"
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/articles")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.insertArticle(article)
    }

    suspend fun deleteArticleById(articleId: Int) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/articles?id=eq.$articleId")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.deleteArticleById(articleId)
    }

    suspend fun updateArticle(id: Int, reference: String, name: String, description: String) = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = "{\"reference\":\"$reference\",\"name\":\"$name\",\"description\":\"$description\"}"
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/articles?id=eq.$id")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .patch(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.updateArticle(id, reference, name, description)
    }

    // ----------------------------------------------------------------
    // CONTACTS (Cloud + Local Sync)
    // ----------------------------------------------------------------
    val allContactsFlow: Flow<List<Contact>> = appDao.getAllContactsFlow()

    suspend fun insertContact(contact: Contact): Long = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = "{\"name\":\"${contact.name}\",\"type\":\"${contact.type}\",\"phone\":\"${contact.phone}\",\"email\":\"${contact.email}\",\"address\":\"${contact.address}\"}"
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/contacts")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .post(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.insertContact(contact)
    }

    suspend fun deleteContactById(contactId: Int) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/contacts?id=eq.$contactId")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.deleteContactById(contactId)
    }

    suspend fun updateContact(id: Int, name: String, type: String, phone: String, email: String, address: String) = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = "{\"name\":\"$name\",\"type\":\"$type\",\"phone\":\"$phone\",\"email\":\"$email\",\"address\":\"$address\"}"
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/rest/v1/contacts?id=eq.$id")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .patch(body)
                .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) { e.printStackTrace() }
        appDao.updateContact(id, name, type, phone, email, address)
    }

    // ----------------------------------------------------------------
    // FLOWS & TRANSACTIONS
    // ----------------------------------------------------------------
    val allStockEntriesFlow: Flow<List<StockEntry>> = appDao.getAllStockEntriesFlow()
    val allStockExitsFlow: Flow<List<StockExit>> = appDao.getAllStockExitsFlow()
    val allSlotAssignmentsFlow: Flow<List<SlotAssignment>> = appDao.getAllSlotAssignmentsFlow()

    suspend fun getSlotAssignmentByCode(code: String): SlotAssignment? = appDao.getSlotAssignmentByCode(code)

    suspend fun assignSlot(code: String, articleId: Int?, supplierName: String, palletNumber: String = "") {
        val slot = SlotAssignment(
            locationCode = code,
            articleId = articleId,
            supplierName = supplierName,
            palletNumber = palletNumber
        )
        appDao.insertSlotAssignment(slot)
    }

    suspend fun removeSlotAssignment(code: String) {
        appDao.deleteSlotAssignmentByCode(code)
    }

    suspend fun executeStockEntry(
        articleId: Int,
        supplierName: String,
        destination: String,
        quantity: Int,
        palletNumber: String,
        quality: String,
        productionDate: String,
        emballageDate: String
    ) {
        val entry = StockEntry(
            articleId = articleId,
            supplierName = supplierName,
            destination = destination,
            quantity = quantity,
            palletNumber = palletNumber,
            quality = quality,
            productionDate = productionDate,
            emballageDate = emballageDate
        )
        
        // إرسال الـ Entry لـ Supabase
        withContext(Dispatchers.IO) {
            try {
                val json = moshi.adapter(StockEntry::class.java).toJson(entry)
                val body = json.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("${SupabaseConfig.URL}/rest/v1/stock_entries")
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                    .post(body)
                    .build()
                client.newCall(request).execute().close()
            } catch (e: Exception) { e.printStackTrace() }
        }

        appDao.insertStockEntry(entry)
        appDao.updateArticleQuantity(articleId, quantity)

        val article = appDao.getArticleById(articleId)
        if (article != null) {
            updateArticle(article.id, article.reference, article.name, article.description)
        }

        if (destination.isNotBlank()) {
            assignSlot(destination, articleId, supplierName, palletNumber)
        }
    }

    suspend fun executeStockExit(
        articleId: Int,
        clientName: String,
        matricule: String,
        supplierName: String,
        sourceLocation: String,
        quantity: Int,
        palletNumber: String,
        quality: String,
        productionDate: String,
        emballageDate: String
    ) {
        val exit = StockExit(
            articleId = articleId,
            clientName = clientName,
            matricule = matricule,
            supplierName = supplierName,
            sourceLocation = sourceLocation,
            quantity = quantity,
            palletNumber = palletNumber,
            quality = quality,
            productionDate = productionDate,
            emballageDate = emballageDate
        )

        // إرسال الـ Exit لـ Supabase
        withContext(Dispatchers.IO) {
            try {
                val json = moshi.adapter(StockExit::class.java).toJson(exit)
                val body = json.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("${SupabaseConfig.URL}/rest/v1/stock_exits")
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                    .post(body)
                    .build()
                client.newCall(request).execute().close()
            } catch (e: Exception) { e.printStackTrace() }
        }

        appDao.insertStockExit(exit)
        appDao.updateArticleQuantity(articleId, -quantity)

        val article = appDao.getArticleById(articleId)
        if (article != null) {
            updateArticle(article.id, article.reference, article.name, article.description)
        }

        if (sourceLocation.isNotBlank()) {
            removeSlotAssignment(sourceLocation)
        }
    }
}
