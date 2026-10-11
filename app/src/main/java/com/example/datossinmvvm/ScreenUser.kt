package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db = remember { crearDatabase(context) }
    val dao = remember { db.userDao() }
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var usersText by remember {
        mutableStateOf("Presiona Listar para consultar usuarios")
    }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de usuarios") },
                actions = {
                    TextButton(
                        onClick = {
                            if (firstName.isBlank() || lastName.isBlank()) {
                                message = "Ingresa nombre y apellido"
                            } else {
                                scope.launch {
                                    try {
                                        dao.insert(
                                            User(
                                                firstName = firstName.trim(),
                                                lastName = lastName.trim()
                                            )
                                        )
                                        firstName = ""
                                        lastName = ""
                                        message = "Usuario agregado correctamente"
                                    } catch (e: Exception) {
                                        Log.e("User", "Error al insertar", e)
                                        message = "Error al agregar usuario"
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Agregar")
                    }

                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    usersText = getUsers(dao)
                                    message = "Lista actualizada"
                                } catch (e: Exception) {
                                    Log.e("User", "Error al listar", e)
                                    message = "Error al listar usuarios"
                                }
                            }
                        }
                    ) {
                        Text("Listar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Registro de usuarios",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    scope.launch {
                        try {
                            dao.deleteLastUser()
                            usersText = getUsers(dao)
                            message = "Operación de eliminación completada"
                        } catch (e: Exception) {
                            Log.e("User", "Error al eliminar", e)
                            message = "Error al eliminar usuario"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar último usuario")
            }

            Text(text = message)

            Text(
                "Usuarios registrados",
                style = MaterialTheme.typography.titleLarge
            )

            Text(text = usersText)
        }
    }
}

fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): String {
    val users = dao.getAll()

    if (users.isEmpty()) {
        return "No hay usuarios registrados."
    }

    return users.joinToString("\n") { user ->
        "ID: ${user.uid} - ${user.firstName.orEmpty()} " +
            user.lastName.orEmpty()
    }
}
