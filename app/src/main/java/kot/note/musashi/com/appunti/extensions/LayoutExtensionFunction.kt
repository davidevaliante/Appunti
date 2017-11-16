package kot.note.musashi.com.appunti.extensions

import android.view.View
import android.widget.EditText
import kot.note.musashi.com.appunti.R

// da il testo nell'editext
fun EditText.stringify() : String{
    return this.text.toString()
}

//cambia colore all'editext per indicare un errore
fun EditText.showError(){
    this.background = resources.getDrawable(R.color.colorAccent)
}

//rimuove il colore di errore in background
fun EditText.removeError(){
    this.background = resources.getDrawable(R.color.white)
}

// rende la view visinile
fun View.setVisible()  { this.visibility = View.VISIBLE }

// rende la view invisibile
fun View.setInvisible()  { this.visibility = View.INVISIBLE }
