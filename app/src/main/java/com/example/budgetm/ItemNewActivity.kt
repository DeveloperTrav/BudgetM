package com.example.budgetm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.text.toSpannable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_item_new.*

class ItemNewActivity : AppCompatActivity() {

    var db = FirebaseFirestore.getInstance()
    private var item: Item? = null
    private var category: Category? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miHome) {
            startActivity(Intent(applicationContext, CategoryAllActivity::class.java))
            finish()
        } else if (item.itemId == R.id.miLogin) {
            if (FirebaseAuth.getInstance().currentUser == null) {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            } else
                Toast.makeText(this, "Logout first!", Toast.LENGTH_LONG).show()
        } else if (item.itemId == R.id.miLogout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(Intent(applicationContext, CategoryAllActivity::class.java))
            Toast.makeText(this, "Logged out!", Toast.LENGTH_LONG).show()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_new)

        item = intent.getSerializableExtra("item") as? Item
        category = intent.getSerializableExtra("category") as? Category

        if (item != null) {
            toolbarItemNew.title = getString(R.string.edit_item)
            editTextItemName.setText(item!!.name)
            editTextItemCost.setText(item!!.cost.toString())
            editTextItemDescription.setText(item!!.description)
            category!!.total -= item!!.cost!!
        } else
            toolbarItemNew.title = getString(R.string.new_item)

        setSupportActionBar(toolbarItemNew)

        buttonSaveItem.setOnClickListener {
            val name = editTextItemName.text.toString().trim()
            val cost = editTextItemCost.text.toString()
            var desc = editTextItemDescription.text.toString().trim()

            if (item != null) {
                item!!.name = name
                item!!.cost = cost.toDouble()
                item!!.description = desc

                category!!.total += item!!.cost!!

                db.collection("categories").document(category!!.id.toString())
                    .set(category!!)

                db.collection("items").document(item!!.id.toString())
                    .set(item!!)
                    .addOnSuccessListener { Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_LONG).show() }
                finish()
            } else {
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
                        finish()
                    }
                }
            }
        }
    }
}
