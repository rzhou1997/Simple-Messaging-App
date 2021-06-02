package com.example.messengerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.util.*

class ChatActivity : AppCompatActivity() {
    val adapterMessages = GroupAdapter<GroupieViewHolder>()
    var toOtherUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        //now let's use parcel to get into into our chat page so let's initialize this first.
        toOtherUser = intent.getParcelableExtra(NewConversationActivity.NAMEKEYVALUE)

        //now we can get the constant and put the support bar as the name of the user we picked
        supportActionBar?.title=toOtherUser?.name

        findViewById<RecyclerView>(R.id.recycler_view_message_display).adapter = adapterMessages
        messageListener()
        findViewById<Button>(R.id.sendText).setOnClickListener {
            sendMessages()
        }
    }

    private fun messageListener(){

        val fromTextId = FirebaseAuth.getInstance().uid
        val toTextId = toOtherUser?.uid
        val referenceDatabase = FirebaseDatabase.getInstance().getReference("/Message-separate-users/$fromTextId/$toTextId")
        //this will let us be notified for all new messages that gets sent into our messages of users folder
        //list will be able to refresh itself
        referenceDatabase.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                //we can get our messages in chat this line because we saved everything as this chat message thus we can get our chat message out very easily.
                val messageInChat = p0.getValue(MessageInChat::class.java)

                if (messageInChat != null) {

                    if (messageInChat.fromTextId == FirebaseAuth.getInstance().uid) {
                        val loggedInUser = MessagesBoardActivity.loggedInUser?:return
                        adapterMessages.add(messageItemFrom(messageInChat.text,loggedInUser))
                    } else {
                        adapterMessages.add(messageItemTo(messageInChat.text,toOtherUser!!))
                    }
                }

                //scrolls all the way to the bottom of the chat log
                findViewById<RecyclerView>(R.id.recycler_view_message_display).scrollToPosition(adapterMessages.itemCount-1)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    //allows users to send messages
    private fun sendMessages(){
        //sending text in our chat class
        val textSend = findViewById<EditText>(R.id.editTextMessage).text.toString()

        //grabbing values for our messageInChat class
        val fromTextId = FirebaseAuth.getInstance().uid//grabbing sign in user id because they're the one sending the message

        val userData = intent.getParcelableExtra<User>(NewConversationActivity.NAMEKEYVALUE)
        val toTextId = userData!!.uid//grabbing the user id of who we're sending the message to

        //generate a automatic node to start sending data in
        //val referenceDatabase = FirebaseDatabase.getInstance().getReference("/Messages of users").push()

        /*as seen we have an issue where we are getting the same chat logs for all our users
        * in order to essentially fix that we will be creating a new node but with this new node
        * it'll be able to send messages to the users that we want to send to rather than having the same
        * chatlog for each of them*/
        val referenceDatabase = FirebaseDatabase.getInstance().getReference("/Message-separate-users/$fromTextId/$toTextId").push()

        //doing the same thing here but reversing it so that the people who you message also can see your messages as well
        val toReferenceDatabase = FirebaseDatabase.getInstance().getReference("/Message-separate-users/$toTextId/$fromTextId").push()
        val messageInChat = MessageInChat(textSend,referenceDatabase.key!!,fromTextId!!, toTextId,System.currentTimeMillis())

        referenceDatabase.setValue(messageInChat)
            .addOnSuccessListener { //sends message successfully
                //clears text in the edittext
                findViewById<EditText>(R.id.editTextMessage).text.clear()
                //goes all the way to the end of recyclerveiw so users can see new messages
                findViewById<RecyclerView>(R.id.recycler_view_message_display).scrollToPosition(adapterMessages.itemCount-1)
            }

        toReferenceDatabase.setValue(messageInChat)

        //sends our data to the messages board by first creating separate node in our database to keep the latest messages from current user to receiving users.
        val messageBoardReferenceCurrUser = FirebaseDatabase.getInstance().getReference("/lastest Messages board/$fromTextId/$toTextId")
        messageBoardReferenceCurrUser.setValue(messageInChat)
        //sends our data to the messages board by first creating separate node in our database to keep the latest messages from receiving users to current users.
        val messageBoardReferenceToUser = FirebaseDatabase.getInstance().getReference("/lastest Messages board/$toTextId/$fromTextId")
        messageBoardReferenceToUser.setValue(messageInChat)
    }
}

//holds the view for our chat boxes of person who's sending the message
class messageItemFrom (val textMessage:String, val userDisplays: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textViewChatTo).text = textMessage
        //loads user image into imageview
        val userURI = userDisplays.profileImageUrl
        val imageDisplays = viewHolder.itemView.findViewById<ImageView>(R.id.displayPhoto)
        Picasso.get().load(userURI).into(imageDisplays)
    }

    override fun getLayout(): Int {
        return R.layout.chat_display
    }
}
//holds the view for our chat boxes of person who's receiving the message
class messageItemTo(val textMessage: String, val userDisplays: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textViewChatFrom).text = textMessage
        //loads user image into imageview
        val userURI = userDisplays.profileImageUrl
        val imageDisplays = viewHolder.itemView.findViewById<ImageView>(R.id.displayPhoto)
        Picasso.get().load(userURI).into(imageDisplays)
    }

    override fun getLayout(): Int {
        return R.layout.chat_display_to
    }
}

//class is used to send our data into firebase
class MessageInChat(val text:String,val textId:String, val fromTextId: String, val toTextId:String, val timestampOfMessage:Long){
    constructor():this("","","","",-1)
}
