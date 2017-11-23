package kot.note.musashi.com.appunti.extensions

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.university_card.view.*

class UniversityViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView){
    fun changeView(data : String){
        itemView.textPrimary.text = data.capitalize()
        itemView.textSecondary.text = data.toUpperCase()
    }
}
