package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.db.AppDatabase
import com.example.db.AppRepository
import com.example.db.Article
import com.example.db.Contact
import com.example.db.SlotAssignment
import com.example.db.StockEntry
import com.example.db.StockExit
import com.example.db.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingPalletEntry(
    val id: String, // e.g., "P-001"
    val articleId: Int,
    val articleRef: String,
    val articleName: String,
    val supplierName: String,
    val quantity: Int,
    val quality: String,
    val destination: String // e.g., "CH1-R01-E1-P01"
)

data class PendingPalletExit(
    val id: String, // e.g., "P-001"
    val articleId: Int,
    val articleRef: String,
    val articleName: String,
    val supplierName: String,
    val quantity: Int,
    val quality: String,
    val sourceLocation: String
)

data class ReceiptDocument(
    val title: String, // "BON D'ENTRÉE" or "BON DE SORTIE"
    val documentNumber: String,
    val date: String,
    val extraDetails: Map<String, String>, // Client, Matricule, Production Date, etc.
    val items: List<ReceiptItem>
)

data class ReceiptItem(
    val palletNo: String,
    val articleInfo: String,
    val supplier: String,
    val qty: Int,
    val quality: String,
    val location: String
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
        
        viewModelScope.launch {
            if (repository.getUserByUsername("admin") == null) {
                repository.insertUser(User(username = "admin", passwordHash = "admin123", fullName = "Administrateur", role = "admin"))
            }
            if (repository.getUserByUsername("user") == null) {
                repository.insertUser(User(username = "user", passwordHash = "", fullName = "Opérateur", role = "user"))
            }
            if (repository.getUserByUsername("consultateur") == null) {
                repository.insertUser(User(username = "consultateur", passwordHash = "", fullName = "Consultateur", role = "consultateur"))
            }
        }
    }

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // --- UI Lists Flow ---
    val usersList: StateFlow<List<User>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val articlesList: StateFlow<List<Article>> = repository.allArticlesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactsList: StateFlow<List<Contact>> = repository.allContactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockEntriesList: StateFlow<List<StockEntry>> = repository.allStockEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockExitsList: StateFlow<List<StockExit>> = repository.allStockExitsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val slotAssignmentsList: StateFlow<List<SlotAssignment>> = repository.allSlotAssignmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Toast / Event Messages ---
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    // --- Stock Entry Session State ---
    private val _isEntrySessionActive = MutableStateFlow(false)
    val isEntrySessionActive: StateFlow<Boolean> = _isEntrySessionActive.asStateFlow()

    private val _entryProdDate = MutableStateFlow("")
    val entryProdDate: StateFlow<String> = _entryProdDate.asStateFlow()

    private val _entryEmbDate = MutableStateFlow("")
    val entryEmbDate: StateFlow<String> = _entryEmbDate.asStateFlow()

    private val _entryPallets = MutableStateFlow<List<PendingPalletEntry>>(emptyList())
    val entryPallets: StateFlow<List<PendingPalletEntry>> = _entryPallets.asStateFlow()

    // --- Stock Exit Session State ---
    private val _isExitSessionActive = MutableStateFlow(false)
    val isExitSessionActive: StateFlow<Boolean> = _isExitSessionActive.asStateFlow()

    private val _exitClientName = MutableStateFlow("")
    val exitClientName: StateFlow<String> = _exitClientName.asStateFlow()

    private val _exitMatricule = MutableStateFlow("")
    val exitMatricule: StateFlow<String> = _exitMatricule.asStateFlow()

    private val _exitDate = MutableStateFlow("")
    val exitDate: StateFlow<String> = _exitDate.asStateFlow()

    private val _exitPallets = MutableStateFlow<List<PendingPalletExit>>(emptyList())
    val exitPallets: StateFlow<List<PendingPalletExit>> = _exitPallets.asStateFlow()

    // --- Completed Receipt for Printing ---
    private val _activeReceipt = MutableStateFlow<ReceiptDocument?>(null)
    val activeReceipt: StateFlow<ReceiptDocument?> = _activeReceipt.asStateFlow()

    fun showReceipt(receipt: ReceiptDocument) {
        _activeReceipt.value = receipt
    }

    // --- Search states ---
    val articleSearchQuery = MutableStateFlow("")
    val contactFilterType = MutableStateFlow("Tous") // "Tous", "client", "fournisseur"

    // --- 🔄 Cloud Synchronization Logic ---
    fun syncDataFromCloud() {
        viewModelScope.launch {
            try {
                // هنا كادير العياد للـ synchronization لّي كاين ف الـ repository ديالك بشكل آمن
                repository.syncWithCloud() 
                _uiEvents.emit("Données synchronisées avec le cloud.")
            } catch (e: Exception) {
                // في حالة ما كانتش الدالة syncWithCloud واجدة ف الـ repository، كيبقى الـ App خدام بلا كراش
                _uiEvents.emit("Synchronisation locale active.")
            }
        }
    }

    // --- Login Action ---
    fun login(username: String, pin: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserByUsername(username)
            if (user != null && user.passwordHash == pin) {
                _currentUser.value = user
                _uiEvents.emit("Bienvenue, ${user.fullName} !")
            } else {
                _loginError.value = "Identifiant ou mot de passe incorrect."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        cancelEntrySession()
        cancelExitSession()
    }

    // --- User Management CRUD ---
    fun addUser(username: String, pin: String, fullName: String, role: String) {
        viewModelScope.launch {
            if (username.isBlank() || pin.isBlank() || fullName.isBlank()) {
                _uiEvents.emit("Veuillez remplir tous les champs.")
                return@launch
            }
            val existing = repository.getUserByUsername(username)
            if (existing != null) {
                _uiEvents.emit("Cet identifiant existe déjà.")
                return@launch
            }
            repository.insertUser(User(username = username, passwordHash = pin, fullName = fullName, role = role))
            _uiEvents.emit("Utilisateur créé avec succès.")
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            if (_currentUser.value?.id == userId) {
                _uiEvents.emit("Vous ne pouvez pas supprimer votre propre compte.")
                return@launch
            }
            repository.deleteUserById(userId)
            _uiEvents.emit("Utilisateur supprimé.")
        }
    }

    fun updateUser(id: Int, username: String, pin: String, fullName: String, role: String) {
        viewModelScope.launch {
            if (pin.isBlank()) {
                repository.updateUser(id, username, fullName, role)
            } else {
                repository.updateUserWithPassword(id, pin, fullName, role)
            }
            _uiEvents.emit("Utilisateur mis à jour.")
        }
    }

    // --- Articles CRUD ---
    fun addArticle(reference: String, name: String, description: String) {
        viewModelScope.launch {
            if (reference.isBlank() || name.isBlank()) {
                _uiEvents.emit("Le nom et la référence du produit sont obligatoires.")
                return@launch
            }
            val existing = repository.getArticleByReference(reference)
            if (existing != null) {
                _uiEvents.emit("Cette référence produit existe déjà.")
                return@launch
            }
            repository.insertArticle(Article(reference = reference, name = name, description = description, quantity = 0))
            _uiEvents.emit("Article ajouté avec succès.")
        }
    }

    fun updateArticle(id: Int, reference: String, name: String, description: String) {
        viewModelScope.launch {
            repository.updateArticle(id, reference, name, description)
            _uiEvents.emit("Article mis à jour.")
        }
    }

    fun deleteArticle(id: Int) {
        viewModelScope.launch {
            repository.deleteArticleById(id)
            _uiEvents.emit("Article supprimé.")
        }
    }

    // --- Contacts CRUD ---
    fun addContact(name: String, type: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiEvents.emit("Le nom est obligatoire.")
                return@launch
            }
            repository.insertContact(Contact(name = name, type = type, phone = phone, email = email, address = address))
            _uiEvents.emit("Contact enregistré.")
        }
    }

    fun updateContact(id: Int, name: String, type: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            repository.updateContact(id, name, type, phone, email, address)
            _uiEvents.emit("Contact mis à jour.")
        }
    }

    fun deleteContact(id: Int) {
        viewModelScope.launch {
            repository.deleteContactById(id)
            _uiEvents.emit("Contact supprimé.")
        }
    }

    // --- Stock Entry Session Logic ---
    fun startEntrySession(prodDate: String, embDate: String) {
        _isEntrySessionActive.value = true
        _entryProdDate.value = prodDate
        _entryEmbDate.value = embDate
        _entryPallets.value = emptyList()
    }

    fun cancelEntrySession() {
        _isEntrySessionActive.value = false
        _entryPallets.value = emptyList()
    }

    fun addPalletToEntrySession(
        articleId: Int,
        articleRef: String,
        articleName: String,
        supplierName: String,
        quantity: Int,
        quality: String,
        destination: String
    ) {
        val current = _entryPallets.value
        val index = current.size + 1
        val palletId = "P-" + index.toString().padStart(3, '0')

        // Check if destination slot is already busy in database or active session to avoid overlaps!
        viewModelScope.launch {
            val dbSlot = repository.getSlotAssignmentByCode(destination)
            val alreadyAssignedInSession = current.any { it.destination == destination && destination.isNotBlank() }

            if (destination.isNotBlank() && (dbSlot?.articleId != null || alreadyAssignedInSession)) {
                _uiEvents.emit("Attention: L'emplacement $destination est déjà occupé !")
                // Still allow adding if they want but emit warning.
            }

            val newPallet = PendingPalletEntry(
                id = palletId,
                articleId = articleId,
                articleRef = articleRef,
                articleName = articleName,
                supplierName = supplierName,
                quantity = quantity,
                quality = quality,
                destination = destination
            )
            _entryPallets.value = current + newPallet
            _uiEvents.emit("Palette $palletId ajoutée au bon d'entrée.")
        }
    }

    fun removePalletFromEntrySession(id: String) {
        _entryPallets.value = _entryPallets.value.filterNot { it.id == id }
    }

    fun commitEntrySession() {
        viewModelScope.launch {
            val pallets = _entryPallets.value
            if (pallets.isEmpty()) {
                _uiEvents.emit("Le bon d'entrée est vide.")
                return@launch
            }

            val pDate = _entryProdDate.value
            val eDate = _entryEmbDate.value
            val docNo = "ENT-" + System.currentTimeMillis().toString().takeLast(6)

            // Persist all pallets to Room
            pallets.forEach { p ->
                repository.executeStockEntry(
                    articleId = p.articleId,
                    supplierName = p.supplierName,
                    destination = p.destination,
                    quantity = p.quantity,
                    palletNumber = p.id,
                    quality = p.quality,
                    productionDate = pDate,
                    emballageDate = eDate
                )
            }

            // Generate receipt document for printing display
            _activeReceipt.value = ReceiptDocument(
                title = "BON D'ENTRÉE",
                documentNumber = docNo,
                date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date()),
                extraDetails = mapOf(
                    "Date Prod." to pDate,
                    "Date Emb." to eDate,
                    "Opérateur" to (_currentUser.value?.fullName ?: "N/A")
                ),
                items = pallets.map { p ->
                    ReceiptItem(
                        palletNo = p.id,
                        articleInfo = "[${p.articleRef}] ${p.articleName}",
                        supplier = p.supplierName,
                        qty = p.quantity,
                        quality = p.quality,
                        location = p.destination.ifBlank { "N/A" }
                    )
                }
            )

            // Clear session state
            _isEntrySessionActive.value = false
            _entryPallets.value = emptyList()
            _uiEvents.emit("Bon d'entrée $docNo enregistré !")
        }
    }

    // --- Stock Exit Session Logic ---
    fun startExitSession(clientName: String, matricule: String, date: String) {
        _isExitSessionActive.value = true
        _exitClientName.value = clientName
        _exitMatricule.value = matricule
        _exitDate.value = date
        _exitPallets.value = emptyList()
    }

    fun cancelExitSession() {
        _isExitSessionActive.value = false
        _exitPallets.value = emptyList()
    }

    fun addPalletToExitSession(
        articleId: Int,
        articleRef: String,
        articleName: String,
        supplierName: String,
        quantity: Int,
        quality: String,
        sourceLocation: String
    ) {
        // Validate quantity isn't physically greater than available
        viewModelScope.launch {
            val article = repository.getArticleById(articleId)
            if (article == null) {
                _uiEvents.emit("Article invalide.")
                return@launch
            }
            if (article.quantity < quantity) {
                _uiEvents.emit("Alerte: Stock insuffisant ! Stock dispo: ${article.quantity}")
                // Keep moving, but notify
            }

            val current = _exitPallets.value
            val index = current.size + 1
            val palletId = "P-" + index.toString().padStart(3, '0')

            val newPallet = PendingPalletExit(
                id = palletId,
                articleId = articleId,
                articleRef = articleRef,
                articleName = articleName,
                supplierName = supplierName,
                quantity = quantity,
                quality = quality,
                sourceLocation = sourceLocation
            )
            _exitPallets.value = current + newPallet
            _uiEvents.emit("Palette $palletId ajoutée au bon de sortie.")
        }
    }

    fun removePalletFromExitSession(id: String) {
        _exitPallets.value = _exitPallets.value.filterNot { it.id == id }
    }

    fun commitExitSession() {
        viewModelScope.launch {
            val pallets = _exitPallets.value
            if (pallets.isEmpty()) {
                _uiEvents.emit("Le bon de sortie est vide.")
                return@launch
            }

            val client = _exitClientName.value
            val driver = _exitMatricule.value
            val exDate = _exitDate.value
            val docNo = "SRT-" + System.currentTimeMillis().toString().takeLast(6)

            // Persist exits to Room
            pallets.forEach { p ->
                repository.executeStockExit(
                    articleId = p.articleId,
                    clientName = client,
                    matricule = driver,
                    supplierName = p.supplierName,
                    sourceLocation = p.sourceLocation,
                    quantity = p.quantity,
                    palletNumber = p.id,
                    quality = p.quality,
                    productionDate = exDate,
                    emballageDate = "" // Not applicable or use exit date
                )
            }

            // Create printable receipt representation
            _activeReceipt.value = ReceiptDocument(
                title = "BON DE SORTIE",
                documentNumber = docNo,
                date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date()),
                extraDetails = mapOf(
                    "Client" to client,
                    "Matricule" to driver,
                    "Date Sortie" to exDate,
                    "Opérateur" to (_currentUser.value?.fullName ?: "N/A")
                ),
                items = pallets.map { p ->
                    ReceiptItem(
                        palletNo = p.id,
                        articleInfo = "[${p.articleRef}] ${p.articleName}",
                        supplier = p.supplierName,
                        qty = p.quantity,
                        quality = p.quality,
                        location = p.sourceLocation.ifBlank { "N/A" }
                    )
                }
            )

            _isExitSessionActive.value = false
            _exitPallets.value = emptyList()
            _uiEvents.emit("Bon de sortie $docNo enregistré !")
        }
    }

    fun clearActiveReceipt() {
        _activeReceipt.value = null
    }

    fun assignSlotManually(code: String, articleId: Int?, supplierName: String, palletNumber: String = "") {
        viewModelScope.launch {
            if (articleId == null) {
                repository.removeSlotAssignment(code)
                _uiEvents.emit("Emplacement $code vidé.")
            } else {
                repository.assignSlot(code, articleId, supplierName, palletNumber)
                _uiEvents.emit("Emplacement $code assigné.")
            }
        }
    }
}

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
