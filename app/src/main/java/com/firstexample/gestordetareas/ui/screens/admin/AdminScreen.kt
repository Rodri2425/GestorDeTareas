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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: TareaViewModel, onLogout: () -> Unit) {
    // Escuchamos la lista de tareas. Si cambia, la pantalla se redibuja sola.
    val tareas by viewModel.tareas.collectAsState()
    val perfil by viewModel.perfil.collectAsState() // ESCUCHAMOS EL PERFIL
    val isAdmin = perfil?.rol == "admin"

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
            // Solo dibujamos el botón de agregar si es Admin
            if (isAdmin) {
                FloatingActionButton(onClick = { viewModel.agregarTareaPrueba() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar Tarea Prueba")
                }
            }
        }
    ) { paddingValues ->
        if (tareas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay tareas. ¡Presiona el botón + para agregar una!")
            }
        } else {
            // LazyColumn es el equivalente moderno del RecyclerView, ideal para listas
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tareas) { tarea ->
                    TareaItem(
                        tarea = tarea,
                        isAdmin = isAdmin, // Le pasamos el dato a la tarjeta
                        onEliminar = { viewModel.eliminarTarea(tarea.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TareaItem(tarea: Tarea, isAdmin: Boolean, onEliminar: () -> Unit) {
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

            // Solo dibujamos el basurero si es Admin
            if (isAdmin) {
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}