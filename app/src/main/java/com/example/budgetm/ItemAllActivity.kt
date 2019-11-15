package com.example.budgetm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_all)

        val user = FirebaseAuth.getInstance().currentUser

        category = intent.getSerializableExtra("category") as? Category

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
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_item, p0, false)
            return ViewHolder(view)
        }
    }
}
