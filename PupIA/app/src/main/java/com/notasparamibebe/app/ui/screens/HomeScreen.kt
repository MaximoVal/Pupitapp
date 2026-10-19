package com.notasparamibebe.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notasparamibebe.app.theme.Blue050
import com.notasparamibebe.app.theme.Blue100
import com.notasparamibebe.app.theme.Blue500
import com.notasparamibebe.app.theme.RoseFavorite
import com.notasparamibebe.app.theme.SoftPink
import com.notasparamibebe.app.ui.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToList: (filterType: String) -> Unit,
    onDisconnected: () -> Unit
) {
    val context = LocalContext.current
    val todayNotes by viewModel.todayNotes.collectAsState()
    val myNotes by viewModel.myNotes.collectAsState()
    val favoriteNotes by viewModel.favoriteNotes.collectAsState()
    val secretCode by viewModel.secretCode.collectAsState()

    // Estados de actualización
    val updateInfo by viewModel.updateInfo.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val updateMessage by viewModel.updateMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDisconnectDialog by remember { mutableStateOf(false) }

    // Al abrir la pantalla de inicio, verificar silenciosamente si hay una nueva versión en GitHub
    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(silent = true)
    }

    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUpdateMessage()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Diálogo de confirmación de desconexión
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("¿Desconectar pareja?") },
            text = { Text("Si te desconectas, dejarás de recibir notas en vivo hasta que vuelvas a ingresar tu código secreto.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDisconnectDialog = false
                        viewModel.disconnect { onDisconnected() }
                    }
                ) {
                    Text("Desconectar", color = RoseFavorite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Nueva Versión Encontrada
    if (updateInfo != null) {
        val info = updateInfo!!
        AlertDialog(
            onDismissRequest = {
                if (downloadProgress == null) viewModel.dismissUpdateDialog()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Blue500
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡Nueva actualización! 🚀")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Versión disponible: ${info.newVersion} (actual: ${info.currentVersion})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (!info.releaseNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = info.releaseNotes,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    if (downloadProgress != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress!! },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Descargando: ${(downloadProgress!! * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                if (downloadProgress == null) {
                    TextButton(
                        onClick = {
                            info.downloadUrl?.let { url ->
                                viewModel.downloadAndInstallUpdate(context, url)
                            }
                        }
                    ) {
                        Text("Actualizar ahora", color = Blue500, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (downloadProgress == null) {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Más tarde")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Notas para mi bebe",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        secretCode?.let {
                            Text(
                                text = "Código: $it",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = Blue500
                        )
                    } else {
                        IconButton(onClick = { viewModel.checkForUpdates(silent = false) }) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Buscar actualizaciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { showDisconnectDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Desconectar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Crear nueva nota
            HomeMenuCard(
                title = "✏️ Crear nueva nota",
                subtitle = "Escribe algo especial para tu pareja",
                icon = Icons.Default.Edit,
                iconBackgroundColor = Blue100,
                iconColor = Blue500,
                badgeCount = null,
                onClick = onNavigateToCreate
            )

            // 2. Notas de hoy
            HomeMenuCard(
                title = "📅 Notas de hoy",
                subtitle = "Lo que te han enviado hoy",
                icon = Icons.Default.CalendarToday,
                iconBackgroundColor = Blue050,
                iconColor = Blue500,
                badgeCount = if (todayNotes.isNotEmpty()) todayNotes.size else null,
                onClick = { onNavigateToList("today") }
            )

            // 3. Mis notas
            HomeMenuCard(
                title = "💌 Mis notas",
                subtitle = "Las notas que has escrito",
                icon = Icons.Default.Mail,
                iconBackgroundColor = Blue050,
                iconColor = Blue500,
                badgeCount = if (myNotes.isNotEmpty()) myNotes.size else null,
                onClick = { onNavigateToList("mine") }
            )

            // 4. Tus favoritas
            HomeMenuCard(
                title = "❤️ Tus favoritas",
                subtitle = "Las notas que guardaste",
                icon = Icons.Default.Favorite,
                iconBackgroundColor = SoftPink,
                iconColor = RoseFavorite,
                badgeCount = if (favoriteNotes.isNotEmpty()) favoriteNotes.size else null,
                onClick = { onNavigateToList("favorites") }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconColor: Color,
    badgeCount: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (badgeCount != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
