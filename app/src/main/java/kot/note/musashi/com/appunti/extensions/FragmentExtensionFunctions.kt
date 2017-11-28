package kot.note.musashi.com.appunti.extensions

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty

// mostra un Toast di base per i Fragment
fun Fragment.showMessage(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toast.makeText(activity, "$message", duration).show()
}

// mostra messaggio di errore (background rosso) per i Fragment
fun Fragment.showError(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toasty.error(context!!, "$message", duration).show()
}

// mostra messaggio di successo (background rosso) per i Fragment
fun Fragment.showSuccess(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toasty.success(context!!, "$message", duration).show()
}

// mostra messaggio di info (background rosso) per i Fragment
fun Fragment.showInfo(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toasty.info(context!!, "$message", duration).show()
}

// aggiunge al container X il fragment Y
fun FragmentActivity.addFragment(container : Int, frag : Fragment) {
    supportFragmentManager.customTransaction { add(container, frag) }
}

// rimuove il fragment X
fun FragmentActivity.removeFragment(frag : Fragment){
    supportFragmentManager.customTransaction { remove(frag) }
}

// rimuove il fragment X
fun FragmentActivity.replaceFrag(containerId : Int,frag : Fragment){
    supportFragmentManager.customTransaction { replace(containerId,frag) }
}

// da usare per ottenere il ViewGroup in un Fragment
fun Fragment.inflateInContainer(root : Int, container : ViewGroup?) : ViewGroup {
    return this.layoutInflater.inflate(root, container, false) as ViewGroup
}

fun Fragment.hideKeyboard() {
    activity?.hideKeyboard(view!!)
}

// esegue una delle fragment Transaction a scelta
inline fun FragmentManager.customTransaction(func : FragmentTransaction.() -> FragmentTransaction){
    beginTransaction().func().commit()
}
