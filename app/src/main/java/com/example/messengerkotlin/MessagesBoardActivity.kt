package com.example.messengerkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class MessagesBoardActivity : AppCompatActivity() {

    companion object {
        var loggedInUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages_board)

        /*let's first check if the user is logged in or not
       * if the user is logged in then they can view their
       * messages if not then they will have to either login
       * or register on our app.*/
        val checkUid = FirebaseAuth.getInstance().uid
        //if uid is null then user is not connected to our application
        if(checkUid == null){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //grabbing current user
        currentUser()

        findViewById<RecyclerView>(R.id.recyclerview_message_board).adapter = userAdapter

        //creates horizontal line to separate users conversations
        findViewById<RecyclerView>(R.id.recyclerview_message_board).addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))


        //item click listener on adapter so that you can go into the conversations
        userAdapter.setOnItemClickListener { item, view ->
            //startactivity to go to user conversation
            val intentUser = Intent(this,ChatActivity::class.java)
            //figuring out user id for each row
            val rowData = item as newMessages
            intentUser.putExtra(NewConversationActivity.NAMEKEYVALUE,rowData.chatUserId)
            startActivity(intentUser)
        }

        setUpRows()


    }

    //class is being used to display our message_row
    class newMessages(val messageInChat: MessageInChat): Item<GroupieViewHolder>(){
        //creating variable of type user to go into user chats
        var chatUserId: User? = null

        override fun getLayout(): Int {
            return R.layout.message_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            //accessing elements inside of the rows using viewholder
            viewHolder.itemView.findViewById<TextView>(R.id.textDisplay).text = messageInChat.text

            //figuring out who we're texting to
            val userChatID:String
            /*check is important as messages are going to and from users inside of your application
            * and you won't know what the chat id of the people are.*/
            if(messageInChat.fromTextId == FirebaseAuth.getInstance().uid){
                userChatID = messageInChat.toTextId
            }else{
                userChatID = messageInChat.fromTextId
            }

            //fetching user id
            val referenceUser = FirebaseDatabase.getInstance().getReference("/users/$userChatID")

            //setting name of the user with id
            referenceUser.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                   chatUserId = p0.getValue(User::class.java)
                    viewHolder.itemView.findViewById<TextView>(R.id.username_display).text = chatUserId?.name
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    val userAdapter = GroupAdapter<GroupieViewHolder>()
    private fun setUpRows(){
        val fromTextId = FirebaseAuth.getInstance().uid
        val referenceDatabase = FirebaseDatabase.getInstance().getReference("/lastest Messages board/$fromTextId")
        //now we must listen for new nodes that are appearing in our node: latest messages board
        referenceDatabase.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                //displays message for our new messages
                val messagesDisplay = p0.getValue(MessageInChat::class.java) ?:return
                newestMessages[p0.key!!] = messagesDisplay

                //clears the messages, not most efficient but we'll use that for now
                userAdapter.clear()

                //refreshes the board to see new message
                newestMessages.values.forEach {
                    userAdapter.add(newMessages(it))
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val messagesDisplay = p0.getValue(MessageInChat::class.java) ?:return
                newestMessages[p0.key!!] = messagesDisplay
                userAdapter.clear()
                newestMessages.values.forEach {
                    userAdapter.add(newMessages(it))
                }
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    //creating hashmap to send our messages in one chat from other user rather than having our application create a new chat everytime a message is sent
    val newestMessages = HashMap<String, MessageInChat>()

    //creating log out button all the way on the right side.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflate menu, so let's create a new menu directory in our res file and create the button for the top right using a resource file
        menuInflater.inflate(R.menu.topbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //switch case fires off action depending on what menu item we press
        when(item.itemId){
            R.id.new_message_item ->{
                //goes to another activity and allows user to start a conversation with people inside our database.
                startActivity(Intent(this,NewConversationActivity::class.java))
            }
            R.id.sign_out_item ->{
                FirebaseAuth.getInstance().signOut()//signs out user.
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //grabbing current user
    private fun currentUser() {

        val userUid = FirebaseAuth.getInstance().uid
        val referenceDatabase = FirebaseDatabase.getInstance().getReference("/users/$userUid")
        referenceDatabase.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                loggedInUser = p0.getValue(User::class.java)
                //grabbing user name and putting name on top
                supportActionBar?.title = loggedInUser?.name
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
}