package com.example.chatapp.ui.Screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.Hilt.CAViewModel
import com.example.chatapp.Navigation.BottomNavigationItem
import com.example.chatapp.Navigation.BottomNavigationMenu
import com.example.chatapp.Navigation.DestinationScreen
import com.example.chatapp.util.CommonDivider
import com.example.chatapp.util.CommonImage
import com.example.chatapp.util.CommonProgressSpinner
import com.example.chatapp.util.navigateTo

@Composable
fun ProfileScreen(navController: NavController, vm: CAViewModel) {
    val inProgress = vm.inProgress.value
    if (inProgress)
        CommonProgressSpinner()
    else {
        val userData = vm.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var number by rememberSaveable { mutableStateOf(userData?.number ?: "") }
        var status by rememberSaveable { mutableStateOf(userData?.status ?: "") }

        val scrollState = rememberScrollState()
        val focus = LocalFocusManager.current

        Column {
            ProfileContent(
                modifier = Modifier.weight(1f).verticalScroll(scrollState).padding(8.dp).background(
                    Color.White),
                vm = vm,
                name = name, //passes name as name
                number = number,
                status = status,
                onNameChange = { name = it },
                onNumberChange = { number = it },
                onStatusChange = { status = it },
                onSave = {
                    focus.clearFocus(true)
                    //call vm to update user profile
                    vm.updateProfileData(name, number, status)
                },
                onBack = {
                    focus.clearFocus(true)

                    navigateTo(navController, DestinationScreen.ChatList.route)
                },
                onLogout = {
                    vm.onLogout()
                    navigateTo(navController, DestinationScreen.Login.route)
                }
            )

            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: CAViewModel,
    name: String,
    number: String,
    status: String,
    //below labda help react to modification of above strings(name, status, number)
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSave: () -> Unit, //labda on clicking save button
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val imageUrl = vm.userData?.value?.imageUrl

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", modifier = Modifier.clickable { onBack.invoke() })
            Text(text = "Save", modifier = Modifier.clickable { onSave.invoke() })
        }

        CommonDivider()

        ProfileImage(imageUrl = imageUrl, vm = vm)

        CommonDivider()

        //Field which facilitates namechange
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    containerColor = Color.Transparent
                )
            )
        }

        //Field where we can change the number
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Number", modifier = Modifier.width(100.dp))
            TextField(
                value = number,
                onValueChange = onNumberChange,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    containerColor = Color.Transparent
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Status", modifier = Modifier.width(100.dp))
            TextField(
                value = status,
                onValueChange = onStatusChange,
                modifier = Modifier
                    .height(150.dp),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    containerColor = Color.Transparent
                ),
                singleLine = false
            )
        }

        CommonDivider()

        //Log out
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Logout", modifier = Modifier.clickable { onLogout.invoke() })
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?, vm: CAViewModel) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->    //nullable uri
        uri?.let {
            //call vm to upload image
            vm.uploadProfileImage(uri)
        }
    }
    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                launcher.launch("image/*") //any type of image
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(shape = CircleShape,
                modifier = Modifier.padding(8.dp).size(100.dp))
            {
                CommonImage(data = imageUrl)
            }
            Text(text = "Change profile picture")
        }

        val isLoading = vm.inProgress.value
        if (isLoading)
            CommonProgressSpinner()
    }
}
