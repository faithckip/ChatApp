package com.example.chatapp.Hilt

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.chatapp.data.COLLECTION_CHAT
import com.example.chatapp.data.COLLECTION_MESSAGES
import com.example.chatapp.data.COLLECTION_STATUS
import com.example.chatapp.data.COLLECTION_USER
import com.example.chatapp.data.ChatData
import com.example.chatapp.data.ChatUser
import com.example.chatapp.data.Event
import com.example.chatapp.data.Message
import com.example.chatapp.data.Status
import com.example.chatapp.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel  //everything hilt injects in the model will be in the viewmodel
class CAViewModel @Inject constructor(

    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage

): ViewModel() {
    //spinner flag showing inProgress
    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>( null) //? makes it nullable
    val userData = mutableStateOf<UserData?>(null)
    val signedIn = mutableStateOf(false)

    val chats = mutableStateOf<List<ChatData>>(listOf())          //lists the chat of a  user
    val inProgressChats = mutableStateOf(false)             //similar to inprogress only that it relates to chats

    val inProgressChatMessages = mutableStateOf( false)     //spinner for receiving the chat messages
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    var currentChatMessagesListener : ListenerRegistration? = null  //listener registration that is nullable by default.clears current chat messages

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    //retrieves current user from firebase
    init {
        //auth.signOut() //shortcut way to signs out the user automatically from the profile (short cut)
        val  currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    fun onLogin(email: String, password: String){
        if (email.isEmpty() or password.isEmpty()){
            handleException(customMessage = "Please fill in all fields ")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let{
                        getUserData(it)
                    }
                }
                else
                    handleException(task.exception, "Login failed")
            }
            .addOnFailureListener {
                handleException(it, "Login failed")
            }
    }


    //signin functionality
    fun onSignUp(name: String, number: String, email: String, password: String){
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()){
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection(COLLECTION_USER).whereEqualTo("number", number)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty)
                    //provide auth
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){ //checks if task is successful
                                signedIn.value = true
                                //create user profile
                                createOrUpdateProfile(name= name, number= number)
                            }
                            else
                                handleException(task.exception, "Signup failed")
                            inProgress.value = false //lets user know functionality has completed
                        }
                    else
                        handleException(customMessage = "number already exists")
            }
            .addOnFailureListener {
                handleException(it)
            }
    }
    //for Test purposes
   // init { handleException(customMessage = "Test") }
// to retrieve userData information
    private fun getUserData(uid: String){
        inProgress.value = true
        db.collection(COLLECTION_USER).document(uid)
            .addSnapshotListener { value, error ->
                if (error != null)
                    handleException(error, "Cannot retrieve user data")
                if (value != null){
                    val user = value.toObject<UserData>()
                    userData.value = user
                    inProgress.value = false
                    populateChats()
                }
            }
    }
    //handles logout
    fun onLogout(){
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event( "Logged out") // informs user they are logged out
        chats.value = listOf()    //resets chats value when we logout

    }

    //error handling
    private fun handleException(exception: Exception? = null, customMessage: String = " "){
        Log.e("ChatAppClone", "Chat App exception", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message =if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
        inProgress.value = false
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null,

    ){
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name, //gets existing name if no name has been provided
            number = number ?: userData.value?.number,
            imageUrl = imageUrl ?: userData.value?.imageUrl
        )
        uid?.let { uid ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(uid)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        //update user
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener{
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Cannot update user")
                            }
                    } else{
                        //create user
                        db.collection(COLLECTION_USER).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid) //retrieves the user
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot retrieve user")
                }

        }

    }
    fun updateProfileData(name: String, number: String, status: String){
        createOrUpdateProfile(name= name, number = number, /*status = status*/ )
    }
    //upload & display Image
    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit){
        inProgress.value = true

        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("image/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun uploadProfileImage(uri: Uri){
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    //add chat functionality
    fun onAddChat(number:String){
        if (number.isNullOrEmpty() or !number.isDigitsOnly())    //check if number is empty or contains digits only
            handleException(customMessage = "Number must contain only digits")
        else
        {
            //queries the database if the number/chat already exists else create a new one
            db.collection(COLLECTION_CHAT)
                .where(
                    //check if chats is user1 or 2 exists
                    Filter.or(
                        //check if chat user1 exists
                        Filter.and(
                            Filter.equalTo("user1.number", number),
                            Filter.equalTo("user2.number", userData.value?.number)    //owner is user2(my number)
                        ),
                        Filter.and(
                            Filter.equalTo("user1.number", userData.value?.number ),  //owner is user1
                            Filter.equalTo("user2.number", number)
                        )
                     )
                )
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty){
                        db.collection(COLLECTION_USER).whereEqualTo("number", number)
                            .get()
                            .addOnSuccessListener {
                                if (it.isEmpty)     //if its empty then we can add our chat
                                    handleException(customMessage = "Cannot retrieve user with number $number")
                                else{
                                    val chatPartner = it.toObjects<UserData>()[0]
                                    val id = db.collection(COLLECTION_CHAT).document().id
                                    //val uuid = UUID.randomUUID().toString()
                                    val chat = ChatData(
                                        id,
                                        ChatUser(    //captures all variables in ChatUserin the DataTypes files
                                            userData.value?.userId,
                                            userData.value?.name,
                                            userData.value?.imageUrl,
                                            userData.value?.number,
                                        ),
                                        ChatUser(
                                            chatPartner.userId,
                                            chatPartner.name,
                                            chatPartner.imageUrl,
                                            chatPartner.number
                                        )
                                    )
                                    //puts the above chat objects in the chat database
                                    db.collection(COLLECTION_CHAT).document(id).set(chat)
                                }
                            }
                            //handles failure listener
                            .addOnFailureListener {
                                handleException(it)
                            }
                    }
                    else{
                        handleException(customMessage = "Chat already exists")
                    }
                }
                .addOnFailureListener { handleException(it) }
        }
    }

    //function to Retrieve chat
    private fun populateChats(){
        inProgressChats.value = true
        //interrogates the database to check if the user exists
        db.collection(COLLECTION_CHAT).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId ),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null)
                    handleException(error)
                //if user exists, we map it
                if (value != null)
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                inProgressChats.value = false
            }
    }

    //function to send message
    fun onSendReply(chatId: String, message:String){
        val time = Calendar.getInstance().time.toString()    //gives actual time for sent message
        val msg = Message(userData.value?.userId, message, time)

        //retrieves particular chat
        db.collection(COLLECTION_CHAT)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .document()
            .set(msg)
    }

    fun populateChat(chatId: String){
        inProgressChatMessages.value = true                 //Spinner that displays loading in the background
        currentChatMessagesListener = db.collection(COLLECTION_CHAT)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .addSnapshotListener { value, error ->               //continuously listens for any updates to this collection
                if (error != null)
                    handleException(error)
                if (value != null)
                    chatMessages.value = value.documents
                        .mapNotNull { it.toObject<Message>() }
                        .sortedBy { it.timestamp }
                inProgressChatMessages.value = false
            }
    }
    fun depopulateChat(){
        currentChatMessagesListener = null   //cancels the above chat message on exit

        chatMessages.value = listOf()
    }

    //function to retrieve and populate chats
    private fun populateStatuses(){
        inProgressStatus.value = true
        val milliTimeDelta = 24*60*60*1000       //limit status retrieval to those posted last 24 hours (1000 milliseconds)
        val cutoff = System.currentTimeMillis() - milliTimeDelta   //cutoff takes the status within the timelimit 24 hrs
        db.collection(COLLECTION_CHAT)
            .where(
                Filter.or(                              //queries chat collection
                    Filter.equalTo("user1.userId", userData.value?.userId),
                    Filter.equalTo("user2.userId", userData.value?.userId)
                )
            )
            .addSnapshotListener { value, error ->
                if (error != null)
                    handleException(error)
                if (value != null){
                    val currentConnections = arrayListOf(userData.value?.userId)    //extracts relevant users. List of all connections
                    val chats = value.toObjects<ChatData>()
                    chats.forEach { chat ->
                        if (chat.user1.userId == userData.value?.userId)
                            currentConnections.add(chat.user2.userId)
                        else
                            currentConnections.add(chat.user1.userId)
                    }
                    //retrieves chat of users retrieved from the above arraylist
                    db.collection(COLLECTION_STATUS)
                        .whereGreaterThan("timestamp", cutoff)      //filters for the time within timestamp & greater than cutoff
                        .whereIn("user.userId", currentConnections)
                        .addSnapshotListener { value, error ->
                            if (error != null)
                                handleException(error)
                            if (value != null)
                                status.value = value.toObjects()
                            inProgressStatus.value = false
                        }
                }
            }
    }

    //create new status which will be stored in the firebase database
    private fun createStatus(imageUrl: String){

        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.name,
                userData.value?.imageUrl,
                userData.value?.number,
            ),
            imageUrl,
            System.currentTimeMillis()   //coz timestamp in datatype is type long
        )
        //Adds the created status to database
        db.collection(COLLECTION_STATUS).document().set(newStatus)
    }
    //function uploads the status
    fun uploadStatus(imageUrl: Uri){
        uploadImage(imageUrl){
            createStatus(imageUrl = it.toString())
        }
    }
}

//after the injection we need to run "clean project" so that it cleans the previoud build before hilt