package com.firstexample.gestordetareas.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.firstexample.gestordetareas.domain.model.Tarea
import com.firstexample.gestordetareas.ui.viewmodel.TareaViewModel
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: TareaViewModel, onLogout: () -> Unit) {
    val tareas by viewModel.tareas.collectAsState()
    val perfil by viewModel.perfil.collectAsState()
    val completadasHoy by viewModel.tareasCompletadasHoy.collectAsState()

    val isAdmin = perfil?.rol == "admin"
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    val tareasMostrar = if (isAdmin) {
        tareas
    } else {
        tareas.filter { tarea -> !completadasHoy.contains(tarea.id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Mostramos el nombre o un texto por defecto mientras carga
                    val nombreMostrado = perfil?.nombre ?: "Cargando..."
                    Text("Hola, $nombreMostrado")
                },
                actions = {
                    // Botón para cerrar sesión
                    IconButton(onClick = { viewModel.cerrarSesion(onLogout) }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { mostrarDialogoCrear = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar Tarea")
                }
            }
        }
    ) { paddingValues ->
        if (tareasMostrar.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = if (isAdmin) "No hay tareas aún. ¡Crea una!" else "¡Felicidades! Terminaste tus tareas de hoy.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cambiamos 'tareas' por 'tareasMostrar' también aquí
                items(tareasMostrar) { tarea ->
                    TareaItem(
                        tarea = tarea,
                        isAdmin = isAdmin,
                        onEliminar = { viewModel.eliminarTarea(tarea.id) },
                        onCompletar = { viewModel.marcarTareaComoCompletada(tarea.id) } // AÑADIMOS ESTO
                    )
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        var tituloTarea by remember { mutableStateOf("") }
        var descripcionTarea by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogoCrear = false },
            title = { Text("Nueva Tarea Familiar") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tituloTarea,
                        onValueChange = { tituloTarea = it },
                        label = { Text("Título (ej. Lavar los trastes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcionTarea,
                        onValueChange = { descripcionTarea = it },
                        label = { Text("Descripción (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tituloTarea.isNotBlank()) {
                            viewModel.crearNuevaTarea(tituloTarea, descripcionTarea)
                            mostrarDialogoCrear = false // Cerramos el cuadro
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCrear = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TareaItem(tarea: Tarea, isAdmin: Boolean, onEliminar: () -> Unit, onCompletar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tarea.titulo, style = MaterialTheme.typography.titleMedium)
                tarea.descripcion?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                Text(text = "Fecha: ${tarea.fechaInicio}", style = MaterialTheme.typography.labelSmall)
            }

            // Si es Admin, mostramos basurero. Si es Usuario, mostramos Check
            if (isAdmin) {
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                IconButton(onClick = onCompletar) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Completar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}