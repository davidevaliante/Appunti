package kot.note.musashi.com.appunti.extensions

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import kot.note.musashi.com.appunti.R
import kotlinx.android.synthetic.main.university_card.view.*
import android.support.v7.widget.helper.ItemTouchHelper.Callback.makeMovementFlags
import android.support.v7.widget.helper.ItemTouchHelper



class UniAdapter (val list : List<String>, ctx : Activity) : RecyclerView.Adapter<UniversityViewHolder>(){

    val c = ctx
    override fun getItemCount(): Int = list.size  // ti ritorna quanti elementi ci sono nel recyclerview

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityViewHolder {
        val itemView = parent.inflate(R.layout.university_card, false)
        return UniversityViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UniversityViewHolder, position: Int) {
        holder.changeView(list[position])
        holder.itemView.textPrimary.setOnClickListener { c.showInfo("hai cliccato solo sul titolo") }
        holder.itemView.setOnClickListener{c.showInfo("cliccato tutta la carta in posizione $position")}
    }



}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition : Int, toPosition : Int) : Boolean
    fun onItemDismiss(position : Int)
}
interface OnStartDragListener {

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}


class SimpleItemTouchHelperCallback(private val adapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
    val mAdapter = adapter
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemDismiss(viewHolder.adapterPosition)
    }

}
