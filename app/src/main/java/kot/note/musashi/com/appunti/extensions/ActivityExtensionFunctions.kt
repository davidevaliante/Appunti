package kot.note.musashi.com.appunti.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty




// mostra un Toast di base per le activity
fun Context.showMessage(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, "$message", duration).show()
}

// mostra un Toast di base per i Fragment
fun Fragment.showMessage(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toast.makeText(activity, "$message", duration).show()
}

// mostra messaggio di errore (background rosso) per le Activity
fun Context.showError (message : CharSequence, duration : Int = Toast.LENGTH_SHORT){
    Toasty.error(this, message, duration).show()
}


// mostra messaggio di errore (background rosso) per i Fragment
fun Fragment.showError(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toasty.error(context!!, "$message", duration).show()
}

// mostra messaggio di successo (background verde)
fun Context.showSuccess (message : CharSequence, duration : Int = Toast.LENGTH_SHORT){
    Toasty.success(this, message, duration).show()
}

// mostra messaggio di successo (background rosso) per i Fragment
fun Fragment.showSuccess(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toasty.success(context!!, "$message", duration).show()
}

// mostra messaggio di info (background blu)
fun Context.showInfo (message : CharSequence, duration : Int = Toast.LENGTH_SHORT){
    Toasty.info(this, message, duration).show()
}

// mostra messaggio di info (background rosso) per i Fragment
fun Fragment.showInfo(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toasty.info(context!!, "$message", duration).show()
}

// aggiunge al container X il fragment Y
fun AppCompatActivity.addFragment(container : Int, frag : Fragment) {
    supportFragmentManager.customTransaction { add(container, frag) }
}

// rimuove il fragment X
fun AppCompatActivity.removeFragment(frag : Fragment){
    supportFragmentManager.customTransaction { remove(frag) }
}

/* cambia Activity      ***NOTA va passato negli apici e il bundle è opzionale
*
*  ESEMPIO : dall'activity attuale chiama :
*
*  goToPage<NomeActivityDiDestinazione>()
*
*/
inline fun <reified T : Activity> AppCompatActivity.goToPage(bundle : Bundle? = null ){
    val intent = Intent(this, T::class.java)
    if(bundle == null) startActivity(intent) else startActivity(intent, bundle)
}

// aggiunge key / value alle sharedPreferences
fun Context.addAndCommitPreference(key : String, value : Any?, name : String = "APPUNTI"){
    val prefs_editor = getSharedPreferences(name, Context.MODE_PRIVATE).edit()
    when(value){
        is String -> prefs_editor.putString(key,value).apply()
        is Int -> prefs_editor.putInt(key,value).apply()
        is Boolean -> prefs_editor.putBoolean(key,value).apply()
        is Long -> prefs_editor.putLong(key,value).apply()
        is Float -> prefs_editor.putFloat(key,value).apply()
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

// legge key dalle sharedPreferences
inline fun <reified T : Any> Context.getFromPreferences(key : String, default : T? = null, from : String = "POWER_USER") : T?{
    return when (T::class){
        String::class -> this.getSharedPreferences(from, Context.MODE_PRIVATE).getString(key, default as String?) as T?
        Int::class -> this.getSharedPreferences(from, Context.MODE_PRIVATE).getInt(key, default as? Int ?: 0) as T?
        Boolean::class -> this.getSharedPreferences(from, Context.MODE_PRIVATE).getBoolean(key, default as? Boolean ?: false) as T?
        Long::class -> this.getSharedPreferences(from, Context.MODE_PRIVATE).getLong(key, default as? Long ?: 0) as T?
        Float::class -> this.getSharedPreferences(from, Context.MODE_PRIVATE).getFloat(key, default as? Float ?: 0f) as T?
        else -> throw UnsupportedOperationException("Unsupported type")
    }
}

// esegue una delle fragment Transaction a scelta
inline fun FragmentManager.customTransaction(func : FragmentTransaction.() -> FragmentTransaction){
    beginTransaction().func().commit()
}

// prende un colore
fun Context.takeColor(color : Int) : Int = ContextCompat.getColor(this, color)

// da usare per ottenere il ViewGroup in un Fragment
fun Fragment.inflateInContainer(root : Int, container : ViewGroup?) : ViewGroup {
    return this.layoutInflater.inflate(root, container, false) as ViewGroup
}