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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List // NUEVO ÍCONO PARA EL HISTORIAL
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: TareaViewModel, onLogout: () -> Unit) {
    val tareas by viewModel.tareas.collectAsState()
    val perfil by viewModel.perfil.collectAsState()
    val tareasOcultas by viewModel.tareasOcultas.collectAsState()
    //val completadasHoy by viewModel.tareasCompletadasHoy.collectAsState()

    val historial by viewModel.historial.collectAsState()
    val isAdmin = perfil?.rol == "admin"
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var tareaEnEdicion by remember { mutableStateOf<Tarea?>(null) }
    var mostrarDialogoHistorial by remember { mutableStateOf(false) }

    val tareasMostrar = if (isAdmin) {
        tareas
    } else {
        //tareas.filter { tarea -> !completadasHoy.contains(tarea.id) }
        tareas.filter { tarea -> !tareasOcultas.contains(tarea.id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val nombreMostrado = perfil?.nombre ?: "Cargando..."
                    Text("Hola, $nombreMostrado")
                },
                actions = {
                    // Si es admin, mostramos el botón del Historial
                    if (isAdmin) {
                        IconButton(onClick = {
                            viewModel.cargarHistorialCompleto() // Descargamos los datos frescos
                            mostrarDialogoHistorial = true // Mostramos la ventana
                        }) {
                            Icon(Icons.Filled.List, contentDescription = "Ver Historial")
                        }
                    }
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
                items(tareasMostrar) { tarea ->
                    TareaItem(
                        tarea = tarea,
                        isAdmin = isAdmin,
                        onEliminar = { viewModel.eliminarTarea(tarea.id) },
                        onEditar = {
                            // Al tocar editar, guardamos la tarea y abrimos el diálogo
                            tareaEnEdicion = tarea
                            mostrarDialogoCrear = true
                        },
                        onCompletar = { viewModel.marcarTareaComoCompletada(tarea.id) }
                    )
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        // Inicializamos los campos con los datos de la tarea si estamos editando, o vacíos si es nueva
        var tituloTarea by remember { mutableStateOf(tareaEnEdicion?.titulo ?: "") }
        var descripcionTarea by remember { mutableStateOf(tareaEnEdicion?.descripcion ?: "") }
        var frecuencia by remember { mutableStateOf(tareaEnEdicion?.tipoFrecuencia ?: "una_vez") }

        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCrear = false
                tareaEnEdicion = null // Limpiamos al cerrar
            },
            title = { Text(if (tareaEnEdicion == null) "Nueva Tarea" else "Editar Tarea") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tituloTarea,
                        onValueChange = { tituloTarea = it },
                        label = { Text("Título") },
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
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Frecuencia:", style = MaterialTheme.typography.labelLarge)
                    // Opciones de frecuencia
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = frecuencia == "una_vez", onClick = { frecuencia = "una_vez" })
                            Text("Solo una vez")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = frecuencia == "diaria", onClick = { frecuencia = "diaria" })
                            Text("Todos los días (Diaria)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = frecuencia == "semanal", onClick = { frecuencia = "semanal" })
                            Text("Cada semana (Semanal)")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tituloTarea.isNotBlank()) {
                            if (tareaEnEdicion == null) {
                                viewModel.crearNuevaTarea(tituloTarea, descripcionTarea, frecuencia)
                            } else {
                                viewModel.actualizarTareaExistente(tareaEnEdicion!!, tituloTarea, descripcionTarea, frecuencia)
                            }
                            mostrarDialogoCrear = false
                            tareaEnEdicion = null
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoCrear = false
                    tareaEnEdicion = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (mostrarDialogoHistorial) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoHistorial = false },
            title = { Text("Historial de Actividades") },
            text = {
                if (historial.isEmpty()) {
                    Text("Nadie ha completado tareas aún.")
                } else {
                    LazyColumn {
                        items(historial) { detalle ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(text = "✅ ${detalle.nombreUsuario}", style = MaterialTheme.typography.titleSmall)
                                Text(text = "Completó: ${detalle.tituloTarea}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "El: ${detalle.fecha}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                Divider(modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { mostrarDialogoHistorial = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun TareaItem(tarea: Tarea, isAdmin: Boolean, onEliminar: () -> Unit, onEditar: () -> Unit, onCompletar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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

                // Mostramos la frecuencia de forma amigable
                val textoFrecuencia = when (tarea.tipoFrecuencia) {
                    "diaria" -> "Rutina Diaria"
                    "semanal" -> "Rutina Semanal"
                    else -> "Solo una vez"
                }
                Text(text = "Frecuencia: $textoFrecuencia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }

            // Si es Admin, mostramos Editar y Basurero
            if (isAdmin) {
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                IconButton(onClick = onCompletar) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Completar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}