package com.example.chatapp.ui.Screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.Hilt.CAViewModel
import com.example.chatapp.Navigation.BottomNavigationItem
import com.example.chatapp.Navigation.BottomNavigationMenu
import com.example.chatapp.Navigation.DestinationScreen
import com.example.chatapp.util.CommonDivider
import com.example.chatapp.util.CommonProgressSpinner
import com.example.chatapp.util.CommonRow
import com.example.chatapp.util.TitleText
import com.example.chatapp.util.navigateTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusListScreen(navController: NavController, vm: CAViewModel) {
    val inProgress = vm.inProgressStatus.value
    if (inProgress)
        CommonProgressSpinner()
    else {
        val statuses = vm.status.value
        val userData = vm.userData.value

        val myStatuses = statuses.filter { it.user?.userId == userData?.userId }
        val otherStatuses = statuses.filter { it.user?.userId != userData?.userId }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                vm.uploadStatus(uri)
            }
        }

        Scaffold(
            floatingActionButton = {
                FAB {
                    //launcher gets content
                    launcher.launch("image/*")
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    TitleText(txt = "Status")
                    if (statuses.isEmpty())
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.White),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No statuses available")
                        }
                    else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            //Display statuses

                        }
                        if (myStatuses.isNotEmpty()) {
                            CommonRow(
                                imageUrl = myStatuses[0].user?.imageUrl,
                                name = myStatuses[0].user?.name
                            ) {
                                //captures out status
                                navigateTo(
                                    navController,
                                    DestinationScreen.SingleStatus.createRoute(myStatuses[0].user.userId)
                                )
                            }

                            CommonDivider()
                        }
                        //displays single instance for a single user
                        val uniqueUsers = otherStatuses.map { it.user }.toSet().toList()   //converts it to set then to list which fits well wih lazy column
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(uniqueUsers) { user ->     //items with list selection
                                CommonRow(
                                    imageUrl = user?.imageUrl,
                                    name = user?.name
                                ) {
                                    //captures other peoples status
                                    navigateTo(
                                        navController,
                                        DestinationScreen.SingleStatus.createRoute(user.userId)
                                    )
                                }
                            }
                        }
                    }
                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.STATUSLIST,
                        navController = navController
                    )
                }

            }
        )
    }
}

@Composable
fun FAB(onFabClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFabClick,       //onfabclick was passed above
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add status",
            tint = Color.White,
        )
    }
}
