package com.example.chatapp.Navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.Hilt.CAViewModel
import com.example.chatapp.ui.Screen.ChatListScreen
import com.example.chatapp.ui.Screen.LoginScreen
import com.example.chatapp.ui.Screen.ProfileScreen
import com.example.chatapp.ui.Screen.SignupScreen
import com.example.chatapp.ui.Screen.SingleChatScreen
import com.example.chatapp.ui.Screen.SingleStatusScreen
import com.example.chatapp.ui.Screen.StatusListScreen
import com.example.chatapp.util.NotificationMessage

@Composable
fun ChatAppNavigation() {
    val navController = rememberNavController()
    val vm = hiltViewModel<CAViewModel>()
    
    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route){
        composable(DestinationScreen.SignUp.route){
           SignupScreen(navController, vm)
        }
        composable(DestinationScreen.Login.route){
            LoginScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Profile.route){
            ProfileScreen(navController = navController, vm=vm)
        }

        composable(DestinationScreen.StatusList.route){
            StatusListScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.SingleStatus.route){
            val userId = it.arguments?.getString("userId")
            userId?.let{
                SingleStatusScreen(navController = navController, vm = vm, userId = userId)
            }
        }
        composable(DestinationScreen.ChatList.route){
            ChatListScreen(navController = navController, vm=vm)
        }
        composable(DestinationScreen.SingleChat.route){
            val chatId = it.arguments?.getString("chatId")
            chatId?.let{
                SingleChatScreen(navController = navController, vm = vm, chatId = it)
            }
        }
    }

}