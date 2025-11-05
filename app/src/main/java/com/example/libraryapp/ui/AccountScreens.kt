package com.example.libraryapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.libraryapp.model.Account
import com.example.libraryapp.model.Gender
import com.example.libraryapp.model.Role
import com.example.libraryapp.viewmodel.AccountFormState
import com.example.libraryapp.viewmodel.AccountsViewModel

private const val ROUTE_HOME = "home"
private const val ROUTE_ADD = "add"
private const val ROUTE_DETAIL = "detail"

@Composable
fun LibraryAppRoot(viewModel: AccountsViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBack = navController.previousBackStackEntry != null

    val appBarTitle = when {
        currentRoute == null -> "Daftar Anggota"
        currentRoute.startsWith(ROUTE_DETAIL) -> "Profil Anggota"
        currentRoute == ROUTE_ADD -> "Tambah Anggota"
        else -> "Daftar Anggota"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(appBarTitle) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentRoute == ROUTE_HOME) {
                FloatingActionButton(onClick = { navController.navigate(ROUTE_ADD) }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah anggota")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_HOME) {
                val accounts by viewModel.accounts.collectAsState()
                AccountsListScreen(
                    accounts = accounts,
                    onAccountSelected = { account ->
                        navController.navigate("$ROUTE_DETAIL/${account.id}")
                    }
                )
            }
            composable(ROUTE_ADD) {
                val formState by viewModel.formState.collectAsState()
                AccountFormScreen(
                    formState = formState,
                    availableDomains = viewModel.availableDomains(),
                    onAvatarChange = viewModel::onAvatarChange,
                    onFirstNameChange = viewModel::onFirstNameChange,
                    onLastNameChange = viewModel::onLastNameChange,
                    onGenderChange = viewModel::onGenderChange,
                    onRoleChange = viewModel::onRoleChange,
                    onEmailLocalPartChange = viewModel::onEmailLocalPartChange,
                    onEmailDomainChange = viewModel::onEmailDomainChange,
                    onMemberNumberChange = viewModel::onMemberNumberChange,
                    onJoinDateChange = viewModel::onJoinDateChange,
                    onSubmit = {
                        if (viewModel.submitAccount()) {
                            navController.popBackStack()
                        }
                    }
                )
            }
            composable(
                route = "$ROUTE_DETAIL/{accountId}",
                arguments = listOf(navArgument("accountId") { type = NavType.StringType })
            ) { entry ->
                val accountId = entry.arguments?.getString("accountId")
                val account = remember(accountId) { accountId?.let(viewModel::getAccount) }
                if (account != null) {
                    AccountDetailScreen(account = account)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Data tidak ditemukan")
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountsListScreen(
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada anggota. Tambahkan data baru.")
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(accounts, key = { it.id }) { account ->
                AccountListItem(account = account, onClick = { onAccountSelected(account) })
            }
        }
    }
}

@Composable
private fun AccountListItem(account: Account, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarImage(
                imageUri = account.avatar,
                placeholder = account.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                size = 56.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.fullName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = account.role.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountFormScreen(
    formState: AccountFormState,
    availableDomains: List<String>,
    onAvatarChange: (String?) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onRoleChange: (Role) -> Unit,
    onEmailLocalPartChange: (String) -> Unit,
    onEmailDomainChange: (String) -> Unit,
    onMemberNumberChange: (String) -> Unit,
    onJoinDateChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val photoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        onAvatarChange(uri?.toString())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Foto Profil", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AvatarImage(
                imageUri = formState.avatarUri?.let(Uri::parse),
                placeholder = "?",
                size = 72.dp
            )
            TextButton(onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text("Pilih Gambar")
            }
        }
        formState.avatarError?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = formState.firstName,
            onValueChange = onFirstNameChange,
            label = { Text("Nama Depan") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.firstNameError != null
        )
        formState.firstNameError?.let { error -> ErrorText(error) }

        OutlinedTextField(
            value = formState.lastName,
            onValueChange = onLastNameChange,
            label = { Text("Nama Belakang") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.lastNameError != null
        )
        formState.lastNameError?.let { error -> ErrorText(error) }

        Text(text = "Jenis Kelamin", style = MaterialTheme.typography.titleSmall)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Gender.entries.forEach { gender ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onGenderChange(gender) }
                ) {
                    RadioButton(selected = formState.gender == gender, onClick = { onGenderChange(gender) })
                    Text(text = gender.displayName)
                }
            }
        }
        formState.genderError?.let { error -> ErrorText(error) }

        var roleExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = formState.role?.displayName ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Jabatan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                isError = formState.roleError != null
            )
            ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                Role.entries.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.displayName) },
                        onClick = {
                            onRoleChange(role)
                            roleExpanded = false
                        }
                    )
                }
            }
        }
        formState.roleError?.let { error -> ErrorText(error) }

        Text(text = "Email", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = formState.emailLocalPart,
            onValueChange = onEmailLocalPartChange,
            label = { Text("Alamat Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.emailLocalPartError != null
        )
        formState.emailLocalPartError?.let { error -> ErrorText(error) }

        var domainExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = domainExpanded, onExpandedChange = { domainExpanded = it }) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = formState.emailDomain,
                onValueChange = {},
                readOnly = true,
                label = { Text("Domain Email") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = domainExpanded) }
            )
            ExposedDropdownMenu(expanded = domainExpanded, onDismissRequest = { domainExpanded = false }) {
                availableDomains.forEach { domain ->
                    DropdownMenuItem(
                        text = { Text(domain) },
                        onClick = {
                            onEmailDomainChange(domain)
                            domainExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = formState.memberNumber,
            onValueChange = onMemberNumberChange,
            label = { Text("Nomor Anggota") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.memberNumberError != null
        )
        formState.memberNumberError?.let { error -> ErrorText(error) }

        OutlinedTextField(
            value = formState.joinDate,
            onValueChange = onJoinDateChange,
            label = { Text("Tanggal Bergabung") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.joinDateError != null,
            supportingText = { Text("Contoh: 12 Januari 2024") }
        )
        formState.joinDateError?.let { error -> ErrorText(error) }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
            Text("Simpan")
        }
    }
}

@Composable
private fun AccountDetailScreen(account: Account, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarImage(
            imageUri = account.avatar,
            placeholder = account.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            size = 120.dp
        )
        Text(text = account.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = account.role.displayName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        InfoRow(label = "Jenis Kelamin", value = account.gender.displayName)
        InfoRow(label = "Email", value = account.email)
        InfoRow(label = "Nomor Anggota", value = account.memberNumber)
        InfoRow(label = "Tanggal Bergabung", value = account.joinDate)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun AvatarImage(imageUri: Uri?, placeholder: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

