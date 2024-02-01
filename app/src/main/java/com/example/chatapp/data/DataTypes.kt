package com.example.chatapp.data


data class UserData(
    val userId: String? = "",  //nullable string "?
    val name: String? ="",
    val number: String? ="",
    val imageUrl: String? ="",
    val status: String? ="",
    val statusImg: String? =" ",
    val contacts: List<String>? = listOf()
){
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "number" to number,
        "imageUrl" to imageUrl,
        "status" to status,
        "statusImg" to statusImg,
        "contacts" to contacts
    )
}

//will store and retrieve data from the firestore
data class ChatData(
    val chatId: String? ="",
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()
)

data class ChatUser(
    val userId: String? = "",
    val name: String? = " ",
    val imageUrl: String? = " ",
    val number: String? = " "
)

data class Message(
    val sentBy: String? = "",
    val message: String? ="",
    val timestamp: String? = ""
)

data class Status(
    val user: ChatUser = ChatUser(),
    val imageUrl: String? = "",
    val message: Long? = null,
    val timestamp: Long? = null
)