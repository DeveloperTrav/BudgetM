package com.example.budgetm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_item_new.*

class ItemNewActivity : AppCompatActivity() {

    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_new)

        buttonSaveItem.setOnClickListener {
            val name = editTextItemName.text.toString().trim()
            val cost = editTextItemCost.text.toString()
            var desc = editTextItemDescription.text.toString().trim()
            var category = intent.getSerializableExtra("category") as Category

            if (desc.isEmpty())
                desc = "$name in ${category.name}"

            when {
                name.isEmpty() -> Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_LONG).show()
                cost.isEmpty() -> Toast.makeText(this, "Please enter a cost!", Toast.LENGTH_LONG).show()
                else -> {
                    val tbl = db.collection("items")
                    val itemId = tbl.document().id
                    val item = Item(itemId, name, desc, cost.toDouble(), category)
                    tbl.document(itemId).set(item)

                    category.itemIds.add(itemId)
                    category.total = category.total + cost.toDouble()

                    db.collection("categories").document(category.id.toString())
                        .set(category, SetOptions.merge())
                        .addOnSuccessListener { e -> println("Success! $e") }
                        .addOnFailureListener { e-> println("Failure! $e") }
                    this.finish()
                }
            }
        }
    }
}
