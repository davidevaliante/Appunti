package kot.note.musashi.com.appunti.extensions

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import kot.note.musashi.com.appunti.R
import kotlinx.android.synthetic.main.university_card.view.*

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


