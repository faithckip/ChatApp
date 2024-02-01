package com.example.chatapp.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.chatapp.Hilt.CAViewModel
import com.example.chatapp.Navigation.DestinationScreen


//checks a user has signed in and moved to another screen
@Composable
fun CheckSignedIn(vm: CAViewModel, navController: NavController) {
    val alreadySignedIn = remember { mutableStateOf( false) }
    val signedIn = vm.signedIn.value
    if (signedIn && !alreadySignedIn.value){
        alreadySignedIn.value = true
        navController.navigate(DestinationScreen.Profile.route){
            popUpTo( 0)
        }
    }

}