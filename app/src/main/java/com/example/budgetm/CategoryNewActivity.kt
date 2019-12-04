package com.example.budgetm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_category_new.*

class CategoryNewActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
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
        setContentView(R.layout.activity_category_new)

        category = intent.getSerializableExtra("category") as? Category

        if (category != null) {
            editTextCategoryName.setText(category!!.name)
            toolbarCategoryNew.title = getString(R.string.edit_category)
        } else
            toolbarCategoryNew.title = getString(R.string.new_category)

        setSupportActionBar(toolbarCategoryNew)

        buttonSaveCategory.setOnClickListener {
            val name = editTextCategoryName.text.toString().trim()

            if (name.isNotEmpty()) {
                if (category != null) {
                    if (category!!.name != name) {
                        category!!.name = name
                        db.collection("categories")
                            .document(category!!.id.toString())
                            .set(category!!)
                    }

                    val i = Intent(applicationContext, ItemAllActivity::class.java)
                    i.putExtra("category", category)
                    startActivity(i)
                    Toast.makeText(this, "Category updated successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val tbl = db.collection("categories")
                    val id = tbl.document().id
                    val category = Category(id, name)
                    tbl.document(id).set(category)

                    editTextCategoryName.setText("")
                    this.finish()
                }
            } else {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
