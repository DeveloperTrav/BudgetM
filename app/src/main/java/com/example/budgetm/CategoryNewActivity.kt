package com.example.budgetm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_category_new.*

class CategoryNewActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_new)

        buttonSaveCategory.setOnClickListener {
            val name = editTextCategoryName.text.toString().trim()

            if (name.isNotEmpty()) {
                val tbl = db.collection("categories")
                val id = tbl.document().id
                val category = Category(id, name)
                tbl.document(id).set(category)

                editTextCategoryName.setText("")
                this.finish()
            } else {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
