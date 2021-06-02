package com.example.messengerkotlin

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class NewConversationActivity : AppCompatActivity() {
    //this is how we create a static constant
    companion object{
        val NAMEKEYVALUE = "KEY_USER"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_conversation)

        //changes the actionbar to Select Users in our database
        supportActionBar?.title = "Please Select a user"

        //retrieving our user username
        getUsers()

    }


    private fun getUsers(){
        //grabbing user data from firebase
        val referenceUsers = FirebaseDatabase.getInstance().getReference("/users")
        referenceUsers.addListenerForSingleValueEvent(object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                /*whenever we're setting up a list view for our android app we must provide a
            * custom adapter for our recycler view but because we have to legit override alot of
            * methods we'll just use 3rd party libraries to present list of user on the screen
            * we'll use what we call groupie and that will be able to allow us to simply go and
            * show our list of users.*/
                val adapterUsers =  GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    //creating variable to hold our values of our user class
                    val usersdisplay = it.getValue(User::class.java)
                    //adding all our users as long as our User class returns a user
                    if(usersdisplay!=null) {
                        //adding user objects inside our adapter
                        adapterUsers.add(UserItem(usersdisplay))
                    }
                }

                /*Now we will add an action when we click on an item
                * to send us to another activity to get to a chatbox*/
                adapterUsers.setOnItemClickListener { item, view ->

                    val intent = Intent(view.context,ChatActivity::class.java)
                    //use put extra to retrieve it later on in the chatactitivty
                    //we can do this by grabbing the user out of the item we then have to
                    //type case the item into an userItem object. then we put a created key
                    //and our cased nameitem into our put extra parameters, we can do the
                    //same also with entire objects to retrieve information from our users that way as well if needed
                    val nameItem = item as UserItem

                    //just grabbing the name of the user but we will need to do it with photos eventually.
                    intent.putExtra(NAMEKEYVALUE,nameItem.user)


                    startActivity(intent)
                    //will send us back to message section
                    finish()
                }


                //displying the users on our recycler view
                findViewById<RecyclerView>(R.id.recyclerview_messageboard).adapter = adapterUsers
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}

/*we have to create objects to be put into our groupadapter*/
class UserItem(val user:User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //will be used to display all names of our users
        //grabs where the id of our user is in the textview and assigns it to the name of the user in our class User
        viewHolder.itemView.findViewById<TextView>(R.id.userName).text = user.name
        //picasso will be used to hold image
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.displayPhoto))
    }

    //layout will render the rows of our users pages
    override fun getLayout(): Int {
        //return some sort of layout file
        return R.layout.user_messages
    }
}