package com.example.budgetm

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_category_all.*
import kotlinx.android.synthetic.main.activity_item_all.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class CategoryAllActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private var adapter: Adapter? = null

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
        setContentView(R.layout.activity_category_all)

        navbar.title = getString(R.string.category_all_title)
        setSupportActionBar(navbar)

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
        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder):
                        Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val category = adapter!!.getItem(viewHolder.adapterPosition)

                    for (item in category.itemIds) {
                        db.collection("items").document(item)
                            .delete()
                    }

                    db.collection("categories").document(category.id.toString())
                        .delete()
                }

                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float,
                                         dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                    val itemView = viewHolder.itemView
                    val itemHeight = itemView.bottom - itemView.top
                    val background = ColorDrawable(Color.RED)
                    val icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_delete)
                    val iconTop = itemView.top + (itemHeight - icon!!.intrinsicHeight) / 2
                    val iconMargin = (itemHeight - icon!!.intrinsicHeight) / 2
                    val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + icon.intrinsicHeight
                    val isCanceled = dX == 0f && !isCurrentlyActive

                    if (isCanceled) {
                        clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        return
                    }

                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)
                }
            }).attachToRecyclerView(recyclerViewAllCategories)

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
                if (FirebaseAuth.getInstance().currentUser != null) {
                    val i = Intent(applicationContext, ItemAllActivity::class.java)
                    i.putExtra("category", model)
                    startActivity(i)
                } else {
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                    finish()
                }
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_category, p0, false)
            return ViewHolder(view)
        }
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}
