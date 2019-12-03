package com.example.budgetm

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_item_all.*

class ItemAllActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    private var adapter: Adapter? = null
    private var category: Category? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_all, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        category = intent.getSerializableExtra("category") as? Category

        if (item.itemId == R.id.miItemHome) {
            startActivity(Intent(applicationContext, CategoryAllActivity::class.java))
            finish()
        } else if (item.itemId == R.id.miItemEditCategory) {
            val i = Intent(applicationContext, CategoryNewActivity::class.java)
            i.putExtra("category", category)
            startActivity(i)
            finish()
        } else if (item.itemId == R.id.miItemDelCategory) {
            for (item in category!!.itemIds) {
                db.collection("items").document(item)
                    .delete()
            }

            db.collection("categories").document(category!!.id.toString())
                .delete()
                .addOnSuccessListener { Toast.makeText(this, "Category Successfully Deleted!", Toast.LENGTH_LONG).show() }

            startActivity(Intent(applicationContext, CategoryAllActivity::class.java))
        } else if (item.itemId == R.id.miItemLogout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(Intent(applicationContext, CategoryAllActivity::class.java))
            Toast.makeText(this, "Logged out!", Toast.LENGTH_LONG).show()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_all)

        category = intent.getSerializableExtra("category") as? Category

        toolbarItemAll.title = category?.name
        setSupportActionBar(toolbarItemAll)

        val user = FirebaseAuth.getInstance().currentUser

        val query = db.collection("items").whereEqualTo("category.id", category?.id)
        val options = FirestoreRecyclerOptions.Builder<Item>()
            .setQuery(query, Item::class.java).build()
        adapter = Adapter(options)

        recyclerViewAllItems.layoutManager = LinearLayoutManager(this)
        recyclerViewAllItems.adapter = adapter

        fabNewItem.setOnClickListener {
            if (user == null) {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
            else {
                val i = Intent(applicationContext, ItemNewActivity::class.java)
                i.putExtra("category", intent.getSerializableExtra("category"))
                startActivity(i)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }

    private inner class ViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view)

    private inner class Adapter internal constructor(options: FirestoreRecyclerOptions<Item>) :
        FirestoreRecyclerAdapter<Item, ViewHolder>(options) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Item) {
            holder.itemView.findViewById<TextView>(R.id.textViewItemName).text = model.name
            holder.itemView.findViewById<TextView>(R.id.textViewItemCost).text = "Cost: $${model.cost}"
            holder.itemView.findViewById<TextView>(R.id.textViewItemDescription).text = model.description

            holder.itemView.setOnClickListener {

            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_item, p0, false)
            return ViewHolder(view)
        }
    }
}
