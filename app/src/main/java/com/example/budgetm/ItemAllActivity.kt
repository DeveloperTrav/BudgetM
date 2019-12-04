package com.example.budgetm

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_item_all.*
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat


class ItemAllActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
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
                    val item = adapter!!.getItem(viewHolder.adapterPosition)

                    category!!.itemIds.remove(item.id)
                    category!!.total -= item.cost!!
                    db.collection("categories").document(category!!.id.toString())
                        .set(category!!)

                    db.collection("items").document(item.id.toString())
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
            }).attachToRecyclerView(recyclerViewAllItems)

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

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
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
                val i = Intent(applicationContext, ItemNewActivity::class.java)
                i.putExtra("item", model)
                i.putExtra("category", category)
                startActivity(i)
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_item, p0, false)
            return ViewHolder(view)
        }
    }
}
