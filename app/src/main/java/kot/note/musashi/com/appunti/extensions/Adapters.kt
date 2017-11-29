package kot.note.musashi.com.appunti.extensions

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import kot.note.musashi.com.appunti.R
import kotlinx.android.synthetic.main.university_card.view.*
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

interface ItemTouchHelpedViewHolder{
    fun onItemSelected()
    fun onItemClear()
}

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition : Int, toPosition : Int) : Boolean
    fun onItemDismiss(position : Int)
}

interface OnStartDragListener {

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}
// insieme alle 3 intenfacce sopra serve per il drag & drop del RecyclerView delle immagini in upload
class SimpleItemTouchHelperCallback(adapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
    var mAdapter = adapter
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

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // seleziona solo l'item corrente e chiama la funzione rispettiva direttamente nell'adapter
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is ItemTouchHelpedViewHolder) {
                val itemViewHolder = viewHolder as ItemTouchHelpedViewHolder
                itemViewHolder.onItemSelected()
            }
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
        super.clearView(recyclerView, viewHolder)

        if (viewHolder is ItemTouchHelpedViewHolder) {
            val itemViewHolder = viewHolder as ItemTouchHelpedViewHolder
            itemViewHolder.onItemClear()
        }
    }
}

// adapter Viewpager
class SimpleViewPagerAdapter(fm : FragmentManager,private val list : List<Fragment>) : FragmentStatePagerAdapter(fm){
    override fun getItem(position: Int): Fragment {
       return list[position]
    }

    override fun getCount(): Int = list.size

}




