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
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_category_all.*
import java.math.BigDecimal
import java.math.RoundingMode

class CategoryAllActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    private var adapter: Adapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_all)

        val user = FirebaseAuth.getInstance().currentUser

        if (!db.firestoreSettings.areTimestampsInSnapshotsEnabled()) {
            val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
            db.firestoreSettings = settings
        }

        val query = db.collection("categories").orderBy("name", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<Category>()
            .setQuery(query, Category::class.java).build()
        adapter = Adapter(options)

        recyclerViewAllCategories.layoutManager = LinearLayoutManager(this)
        recyclerViewAllCategories.adapter = adapter

        fabNewCategory.setOnClickListener{
            if (user == null) {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
            else
                startActivity(Intent(applicationContext, CategoryNewActivity::class.java))
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

    private inner class ViewHolder internal constructor(private val view: View) :
        RecyclerView.ViewHolder(view)

    private inner class Adapter internal constructor(options: FirestoreRecyclerOptions<Category>) :
        FirestoreRecyclerAdapter<Category, ViewHolder>(options) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Category) {
            holder.itemView.findViewById<TextView>(R.id.textViewCategoryName).text = model.name
            holder.itemView.findViewById<TextView>(R.id.textViewCategoryTotal).text =
                "Total: $${BigDecimal(model.total).setScale(2, RoundingMode.HALF_EVEN)}"
            holder.itemView.findViewById<TextView>(R.id.textViewNumberOfItems).text = "Items: ${model.itemIds.size}"

            holder.itemView.setOnClickListener {
                var i = Intent(applicationContext, ItemAllActivity::class.java)
                i.putExtra("category", model)
                startActivity(i)
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_category, p0, false)
            return ViewHolder(view)
        }
    }
}
