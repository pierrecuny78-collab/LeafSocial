package org.leafvibe.leafsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Oasis = Color(0xFF206B4D)
private val Walnut = Color(0xFF6B4935)
private val Cream = Color(0xFFF8F5EB)
private val Sage = Color(0xFFA8B99D)
private val Deep = Color(0xFF173E31)

data class Space(val name: String, val emoji: String, val subtitle: String)
private val spaces = listOf(
    Space("LeafCommunity", "💬", "Publications, communautés et échanges"),
    Space("LeafLearn", "🎓", "Cours, fiches, quiz et progression"),
    Space("LeafIdeas", "💡", "Idées, tableaux et cartes mentales"),
    Space("LeafNews", "🌍", "Actualités et veille personnalisée"),
    Space("LeafMarket", "🛍️", "Comparaison, listes et annonces"),
    Space("LeafLibrary", "📚", "Documents, PDF et collections"),
    Space("LeafWellness", "🌱", "Habitudes, objectifs et bien-être")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("leafsocial", MODE_PRIVATE)
        setContent {
            var signedIn by remember { mutableStateOf(prefs.getBoolean("signed_in", false)) }
            var user by remember { mutableStateOf(prefs.getString("user", "") ?: "") }
            LeafTheme {
                if (!signedIn) {
                    LoginScreen(
                        onLogin = { name ->
                            user = name
                            prefs.edit().putBoolean("signed_in", true).putString("user", name).apply()
                            signedIn = true
                        }
                    )
                } else {
                    MainScreen(user = user, onLogout = {
                        prefs.edit().clear().apply()
                        signedIn = false
                        user = ""
                    })
                }
            }
        }
    }
}

@Composable
private fun LeafTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Oasis, onPrimary = Color.White, secondary = Walnut,
            background = Cream, surface = Color.White, onSurface = Deep
        ),
        content = content
    )
}

@Composable
private fun LoginScreen(onLogin: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxSize(), color = Cream) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🍃", fontSize = 64.sp)
            Text("LeafSocial", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Oasis)
            Text("Un espace pour apprendre, partager et créer.", color = Walnut)
            Spacer(Modifier.height(30.dp))
            OutlinedTextField(displayName, { displayName = it }, label = { Text("Nom affiché") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Adresse e-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("Mot de passe") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onLogin(displayName.trim().ifBlank { email.substringBefore("@").ifBlank { "Membre Leaf" } }) },
                enabled = email.contains("@") && password.length >= 4,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Entrer dans LeafSocial") }
            Spacer(Modifier.height(12.dp))
            Text("Prototype 0.1 : compte local de démonstration. L'authentification serveur sera intégrée avant une diffusion publique.", fontSize = 12.sp, color = Walnut)
        }
    }
}

@Composable
private fun MainScreen(user: String, onLogout: () -> Unit) {
    var selected by remember { mutableStateOf("Accueil") }
    var selectedSpace by remember { mutableStateOf<Space?>(null) }
    var composer by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val prefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("leafsocial", 0)
    var posts by remember { mutableStateOf(prefs.getStringSet("posts", emptySet())?.toList()?.reversed() ?: emptyList()) }
    val tabs = listOf("Accueil", "Explorer", "Créer", "Profil")
    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBarLeaf(title = selectedSpace?.name ?: "LeafSocial", onBack = { selectedSpace = null }, showBack = selectedSpace != null)
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab && selectedSpace == null,
                        onClick = { selected = tab; selectedSpace = null; if (tab == "Créer") composer = true },
                        icon = {
                            Icon(when(tab) {
                                "Accueil" -> Icons.Default.Home
                                "Explorer" -> Icons.Default.Explore
                                "Créer" -> Icons.Default.AddCircle
                                else -> Icons.Default.Person
                            }, contentDescription = tab)
                        },
                        label = { Text(tab) }
                    )
                }
            }
        }
    ) { padding ->
        if (selectedSpace != null) {
            SpaceScreen(selectedSpace!!, Modifier.padding(padding))
        } else when (selected) {
            "Accueil" -> HomeScreen(user, posts, Modifier.padding(padding), onOpen = { selectedSpace = it }, onCompose = { composer = true })
            "Explorer" -> ExplorerScreen(Modifier.padding(padding), onOpen = { selectedSpace = it })
            "Profil" -> ProfileScreen(user, posts.size, Modifier.padding(padding), onLogout)
            else -> HomeScreen(user, posts, Modifier.padding(padding), onOpen = { selectedSpace = it }, onCompose = { composer = true })
        }
    }
    if (composer) {
        AlertDialog(
            onDismissRequest = { composer = false },
            title = { Text("Nouvelle publication") },
            text = {
                OutlinedTextField(draft, { draft = it }, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text("Qu'avez-vous envie de partager ?") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (draft.isNotBlank()) {
                        posts = listOf(draft.trim()) + posts
                        prefs.edit().putStringSet("posts", posts.toSet()).apply()
                    }
                    draft = ""; composer = false
                }) { Text("Publier") }
            },
            dismissButton = { TextButton(onClick = { composer = false }) { Text("Annuler") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarLeaf(title: String, onBack: () -> Unit, showBack: Boolean) {
    TopAppBar(
        title = { Text(if (showBack) title else "🍃 $title", fontWeight = FontWeight.Bold, color = Oasis) },
        navigationIcon = { if (showBack) IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Retour") } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
    )
}

@Composable
private fun HomeScreen(user: String, posts: List<String>, modifier: Modifier, onOpen: (Space) -> Unit, onCompose: () -> Unit) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Column(Modifier.fillMaxWidth().background(Deep, RoundedCornerShape(22.dp)).padding(20.dp)) {
                Text("Bonjour $user 👋", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Votre espace pour découvrir et progresser.", color = Color(0xFFE6EBDD))
                Spacer(Modifier.height(12.dp))
                Button(onClick = onCompose, colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Deep)) { Icon(Icons.Default.Add, null); Text("  Créer une publication") }
            }
        }
        item { Text("Vos espaces", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Deep) }
        items(spaces) { space -> SpaceCard(space, onClick = { onOpen(space) }) }
        item { Text("Fil d'actualité local", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Deep) }
        if (posts.isEmpty()) item { Text("Aucune publication pour le moment. Partagez votre première idée !", color = Walnut) }
        items(posts) { post ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Text("🍃  $user", color = Oasis, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(post, color = Deep)
                }
            }
        }
    }
}

@Composable
private fun ExplorerScreen(modifier: Modifier, onOpen: (Space) -> Unit) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Explorer LeafSocial", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Deep) }
        item { Text("Retrouvez les sept univers réunis dans une seule application.", color = Walnut) }
        items(spaces) { SpaceCard(it) { onOpen(it) } }
    }
}

@Composable
private fun SpaceCard(space: Space, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(Cream, CircleShape), contentAlignment = Alignment.Center) { Text(space.emoji, fontSize = 27.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(space.name, color = Oasis, fontWeight = FontWeight.Bold)
                Text(space.subtitle, color = Walnut, fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Sage)
        }
    }
}

@Composable
private fun SpaceScreen(space: Space, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(space.emoji, fontSize = 54.sp)
        Text(space.name, fontSize = 26.sp, color = Oasis, fontWeight = FontWeight.Bold)
        Text(space.subtitle, color = Walnut)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp)) {
                Text("Espace prêt à évoluer", fontWeight = FontWeight.Bold, color = Deep)
                Text("Cette première version pose la navigation et l'identité de ce module. Les fonctions spécialisées seront ajoutées par étapes.", color = Walnut)
            }
        }
    }
}

@Composable
private fun ProfileScreen(user: String, count: Int, modifier: Modifier, onLogout: () -> Unit) {
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Mon profil", fontSize = 26.sp, color = Oasis, fontWeight = FontWeight.Bold)
        Text("👤  $user", fontSize = 20.sp, color = Deep)
        Text("$count publication(s) locale(s)", color = Walnut)
        Text("Compte de démonstration sur cet appareil.", color = Walnut)
        OutlinedButton(onClick = onLogout) { Icon(Icons.Default.Logout, null); Text("  Se déconnecter") }
    }
}
