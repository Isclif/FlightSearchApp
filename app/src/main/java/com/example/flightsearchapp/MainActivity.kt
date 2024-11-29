package com.example.flightsearchapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.flightsearchapp.data.FlightDao
import com.example.flightsearchapp.data.Favorite
import com.example.flightsearchapp.data.Airport
import com.example.flightsearchapp.preferences.PreferencesManager
import com.example.flightsearchapp.ui.theme.FlightSearchAppTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
//import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.flightsearchapp.data.FavoriteTrip
import com.example.flightsearchapp.data.FavoriteWithAirports
import com.example.flightsearchapp.data.FlightDatabase
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlightSearchAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Initialisation de l'instance de FlightDao via Room
                    // val db = Room.databaseBuilder(applicationContext, FlightDatabase::class.java, "flight_search_db").build()
                    // applicationContext.deleteDatabase("flight_search_db")
                    val db = FlightDatabase.getDatabase(applicationContext)
                    val flightDao = db.flightDao() // Obtention de l'instance de FlightDao via Room
                    val preferencesManager = PreferencesManager(LocalContext.current) // Utilisation de LocalContext pour le contexte

                    SearchScreen(flightDao = flightDao, preferencesManager = preferencesManager)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    flightDao: FlightDao,
    preferencesManager: PreferencesManager
) {
    // Etat mutable pour la recherche
    val query = remember { mutableStateOf("") }

    // Utilisation d'une liste d'éléments Airport et Favorite
    val suggestions = remember { mutableStateListOf<Airport>() }
//    val favorites = remember { mutableStateListOf<Favorite>() }
    val focusRequester = remember { FocusRequester() }

    val favorites = remember { mutableStateListOf<FavoriteWithAirports>() }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val favoritesFlow = preferencesManager.getFavoriteTrips().collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    // Utilisation de LaunchedEffect pour observer le changement de la query
    LaunchedEffect(query.value) {
        if (query.value.isNotEmpty()) {
            suggestions.clear()
            favorites.clear()
            suggestions.addAll(flightDao.searchAirports("%${query.value}%")) // Requête pour rechercher des aéroports
        }
    }

    // Vérification de la liste des favoris à l'ouverture de l'application
    LaunchedEffect(Unit) {
        // Si le dataStore est vide, on met le focus sur le TextField
        if (favoritesFlow.value.isNotEmpty()) {
            focusRequester.freeFocus()
        } else {
            focusRequester.requestFocus()
        }
    }

    fun listFligths(code : String) {
        coroutineScope.launch {
            favorites.addAll(flightDao.getAirportsForFavoriteCode(iataCode = code))
            query.value = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flight Search", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF205FA6))
            )
        }
    ) { padding ->

        Column (
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(5.dp)
        ){
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                enabled = true,
                shape = RoundedCornerShape(100),
                value = query.value, // Définir la valeur du texte
                onValueChange = { query.value = it }, // Définir la logique pour modifier la valeur
                placeholder = { Text("Rechercher un aéroport...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") // Icône de recherche à gauche
                },
//                trailingIcon = {
//                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Microphone") // Icône du microphone à droite
//                },
                modifier = Modifier
                    .fillMaxWidth() // Pour que le champ occupe toute la largeur disponible
                    .padding(horizontal = 16.dp) // Ajouter un peu de marge autour du champ
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFD5E3FF), // Couleur de fond du champ
                    focusedIndicatorColor = Color.Transparent, // Retirer la ligne de focus
                    unfocusedIndicatorColor = Color.Transparent, // Retirer la ligne de focus quand non sélectionné
                    focusedLabelColor = Color.Gray, // Couleur du label quand sélectionné
                    unfocusedLabelColor = Color.Gray // Couleur du label quand non sélectionné
                )
            )

            if (!favorites.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Flights From ${favorites.firstOrNull()?.departure_code}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                                  favorites.clear()
                        },
                        modifier = Modifier
                            .size(40.dp) // Taille de l'icône
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Favories",
                            tint = Color(0xFFF0288D1)
                        )
                    }
                }
            }
            if (favoritesFlow.value.isNotEmpty() && favorites.isEmpty() && query.value.isEmpty() ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Favorites routes",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val isSup = preferencesManager.clearFavoriteTrips()

                                snackbarHostState.showSnackbar(
                                    if (isSup) "Favoris suprimés avec succès !" else "Pas de favoris !."
                                )
                            }
                        },
                        modifier = Modifier
                            .size(40.dp) // Taille de l'icône
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }

            if (query.value.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Affichage du snackbar
            SnackbarHost(
                hostState = snackbarHostState,
//                modifier = Modifier)
            )

            LazyColumn (
                modifier = Modifier
                    .fillMaxSize()
            ){
                if (query.value.isNotEmpty()) {
                    // Utilisation de items avec suggestions de type Airport
                    items(suggestions) { airport ->
//                      Text("${airport.iata_code} - ${airport.name}")  // Utilisation des noms corrects de propriété
                        Box(modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp))
                        {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${airport.iata_code}  ") // Texte en gras
                                    }
                                    append("${airport.name}") // Texte normal
                                },
                                style = TextStyle(fontSize = 12.sp),
                                modifier = Modifier.clickable {
                                    listFligths(code = airport.iata_code)
                                    focusRequester.freeFocus()
                                }
                            )
                        }

                    }
                } else if (!favorites.isEmpty()) {
                    // Si aucune recherche n'est effectuée, afficher les aéroports favoris
                    items(favorites) { favorite ->
                        Card(
                            shape = RoundedCornerShape(topEnd = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E2EC)),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp) // Hauteur totale de la carte
                            ) {
                                // Contenu principal
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    // Ligne de départ
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "DEPART",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Gray
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = buildAnnotatedString {
                                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append("${favorite.departure_code}  ") // Texte en gras
                                                    }
                                                    append("${favorite.departure_name}") // Texte normal
                                                },
                                                style = TextStyle(fontSize = 12.sp) // Style global pour la phrase
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Ligne d'arrivée
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "ARRIVE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.Gray
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = buildAnnotatedString {
                                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append("${favorite.destination_code}  ") // Texte en gras
                                                    }
                                                    append("${favorite.destination_name}") // Texte normal
                                                },
                                                style = TextStyle(fontSize = 12.sp) // Style global pour la phrase
                                            )
                                        }
                                    }
                                }

                                // color button star #944B00
                                // Icône en position absolue
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val isAdded = preferencesManager.saveFavoriteTrip(
                                                context = context,
                                                FavoriteTrip(
                                                    departure_code = favorite.departure_code,
                                                    departure_name = favorite.departure_name,
                                                    destination_code = favorite.destination_code,
                                                    destination_name = favorite.destination_name
                                                )
                                            )

                                            snackbarHostState.showSnackbar(
                                                if (isAdded) "Ajouté aux favoris avec succès !" else "Ce vol est déjà présent dans les favoris."
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp) // Taille de l'icône
                                        .align(Alignment.CenterEnd) // Alignement centré verticalement à gauche
                                        .absoluteOffset(x = -20.dp) // Décalage à gauche
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = Color(0xFF74777F)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(favoritesFlow.value) { favoriteRoute ->

                        Card(
                            shape = RoundedCornerShape(topEnd = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E2EC)),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp) // Hauteur totale de la carte
                            ) {
                                // Contenu principal
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    // Ligne de départ
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "DEPART",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Gray
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = buildAnnotatedString {
                                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append("${favoriteRoute.departure_code}  ") // Texte en gras
                                                    }
                                                    append("${favoriteRoute.departure_name}") // Texte normal
                                                },
                                                style = TextStyle(fontSize = 12.sp) // Style global pour la phrase
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Ligne d'arrivée
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "ARRIVE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.Gray
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = buildAnnotatedString {
                                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append("${favoriteRoute.destination_code}  ") // Texte en gras
                                                    }
                                                    append("${favoriteRoute.destination_name}") // Texte normal
                                                },
                                                style = TextStyle(fontSize = 12.sp) // Style global pour la phrase
                                            )
                                        }
                                    }
                                }

                                // Icône en position absolue
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .size(40.dp) // Taille de l'icône
                                        .align(Alignment.CenterEnd) // Alignement centré verticalement à gauche
                                        .absoluteOffset(x = -20.dp) // Décalage à gauche
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = Color(0xFF944B00)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() {
    FlightSearchAppTheme {
        val db = FlightDatabase.getDatabase(LocalContext.current)
        // val db = Room.databaseBuilder(LocalContext.current, FlightDatabase::class.java, "flightsearch_db").build()
        val flightDao = db.flightDao() // Remplacer par l'instance réelle de FlightDao
        val preferencesManager = PreferencesManager(LocalContext.current) // Utilisation de LocalContext pour PreferencesManager
        SearchScreen(flightDao = flightDao, preferencesManager = preferencesManager)
    }
}
