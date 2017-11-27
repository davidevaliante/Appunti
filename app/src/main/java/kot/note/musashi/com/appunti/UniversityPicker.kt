package kot.note.musashi.com.appunti


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.Places
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kot.note.musashi.com.appunti.R.id.*
import kot.note.musashi.com.appunti.extensions.*
import kotlinx.android.synthetic.main.course_card.view.*

import kotlinx.android.synthetic.main.fragment_university_picker.*
import kotlinx.android.synthetic.main.university_card.view.*



class UniversityPicker : Fragment() {

    val universityExclude by lazy { listOf("Uni ", "uni ", "Università ", "Universita ") }
    val politecnicoExclude by lazy { listOf("Pol", "poli", "politecnico","polite","polit")}
    val accademyExclude by lazy {listOf("acc","accademia","accade","accad","acca")}
    lateinit var uniListAdapter : UniversityAdapter
    lateinit var courseListAdapter : CourseAdapter
    var selection : String?= "Università"
    var pickedUniversity : University?=null
    var pickedCourse : Course?=null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflateInContainer(R.layout.fragment_university_picker, container)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dataClient = Places.getGeoDataClient(activity!!, null)

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        uniListAdapter = UniversityAdapter(emptyList())
        courseListAdapter = CourseAdapter(emptyList())
        // di base passa l'uni adapter vuoto
        recyclerView.adapter = uniListAdapter

        // switch per il tipo di struttura
        group_choices.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.select_university ->  selection = "Università"
                R.id.select_accademy -> selection = "Accademia"
                R.id.select_poli -> selection = "Politecnico"
            }
            // cambia l'hint dell'edit-text rispetto al pulsante scelto
            customAutocomplete.hint = "Cerca $selection..."
        }
        

        //ricerca università
        customAutocomplete?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(contenuto : Editable?) {

                // pulisce la stringa dell'utente dai caratteri non desiderati
                fun cleanedUserString() : String{
                    // lista di parole da evitare inizializzata dinamicamente grazie a
                    // (when) in base alla selezione dell'utente


                    var listToExclude: List<String> = when(selection){
                        "Università" -> universityExclude
                        "Accademia" -> accademyExclude
                        "Politecnico" -> politecnicoExclude
                        else -> universityExclude
                    }

                    // stringa vuota placeholder
                    var s = contenuto.toString()
                    // toglie Accademia, Università, Politecnico
                    for (words in listToExclude) {
                        s = s.replace(words, "", ignoreCase = true)
                    }
                    return s
                }

                // va ogni 400 millisecondi
                val runnable = Runnable {
                if (contenuto.toString().isNotEmpty()) {
                    if(recyclerView.adapter is CourseAdapter) recyclerView.adapter = uniListAdapter
                    // nasconde il picker per il corso
                    hideViewsForUniversitySearch()

                    // custom query a GoogleMaps (la parola iniziale *selection* è presa dai ButtonSwitch)
                    val result = dataClient.getAutocompletePredictions("$selection ${cleanedUserString()}", null, null)
                    result.addOnCompleteListener { if (it.isSuccessful) {
                            // dalla risposta dell' API crea una lista di AutocompletePrediction
                            // da passare all'adapter
                            var resultList : MutableList<AutocompletePrediction> = mutableListOf()
                            result.result.forEach {
                                resultList.add(it)
                            }
                            // passa all'adapter i dati della query
                            uniListAdapter?.updateList(resultList)
                        }
                    }
                }else{
                    //ripristina le view
                    restoreViewAfterUniversitySearch()
                    // se l'editText è vuoto passiamo all'adapter una lista vuota
                    uniListAdapter?.updateList(emptyList())
                }
                }
                Handler().postDelayed(runnable, 400)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        //ricerca corso
        searchCourse?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(string : Editable?) {
                if(string.toString().isNotEmpty()){
                    hideViewsForCourseSearch()
                    if(recyclerView.adapter is UniversityAdapter) recyclerView.adapter = courseListAdapter
                    val courses = FirebaseFirestore.getInstance()
                    courses.collection("Courses").get().addOnSuccessListener { querySnapshot: QuerySnapshot ->
                        if(!querySnapshot.isEmpty) {
                            var list = querySnapshot.toObjects(Course::class.java)
                            courseListAdapter?.updateList(list)
                        }else {
                            // suka
                        }
                    }
                }else{
                    courseListAdapter?.updateList(emptyList())
                    restoreViewAfterCourseSearch()
                }
            }

        })

    }



    // adapter per la query sulle università
    inner class UniversityAdapter(list: List<AutocompletePrediction>) : RecyclerView.Adapter<UniversityAdapter.UniversityViewHolder>() {
        private var university = list

        inner class UniversityViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView) {
            fun bindViews(data : AutocompletePrediction){
                itemView.textPrimary.text = data.getPrimaryText(null)
                itemView.textSecondary.text = data.getSecondaryText(null)
            }
        }

        override fun getItemCount(): Int = university.size

        override fun onBindViewHolder(holder: UniversityAdapter.UniversityViewHolder, position: Int) {
            val uni = university[position]
            holder.bindViews(uni)
            holder.itemView.setOnClickListener{
                customAutocomplete.text = null
                customAutocomplete.hint = uni.getPrimaryText(null).toString()
                customAutocomplete.clearFocus()
                hideKeyboard()
                updateList(emptyList())
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityAdapter.UniversityViewHolder? {
            val view = parent.inflate(R.layout.university_card, false)
            return UniversityViewHolder(view)
        }

        fun updateList(universities : List<AutocompletePrediction>){
            university = universities
            notifyDataSetChanged()
        }

    }

    // adapter per la query sui corsi
    inner class CourseAdapter(list : List<Course>) : RecyclerView.Adapter<CourseAdapter.CourseViewholder>(){
        private var courseList = list

        inner class CourseViewholder(itemView: View?) : RecyclerView.ViewHolder(itemView){
            fun bind(data : Course){
                itemView.setOnClickListener {
                    activity?.showSuccess("${data.courseName} selezionato")
                    activity?.hideKeyboard()
                    searchCourse.text = null
                    searchCourse.hint = "${data.courseName}"
                    updateList(emptyList())
                    restoreViewAfterCourseSearch()
                }
                itemView.courseTextPrimary.text = data.courseName
                itemView.courseTextSecondary.text = data.courseDescription
                when(data.courseName){
                    "Matematica" -> itemView.courseIcon.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_math_icon))
                    "Fisica" -> itemView.courseIcon.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_physics_icon))
                    "Lettere" -> itemView.courseIcon.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_letters_icon))
                    "Chimica" -> itemView.courseIcon.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_chemistry_icon))
                    "Ingegneria" -> itemView.courseIcon.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_engineering))
                }
                // inverte il gradiente della linea bassa
                if(adapterPosition%2==0){
                    itemView.bottom_line.background = ContextCompat.getDrawable(activity!!, R.drawable.inverse_main_gradient)
                }else{
                    itemView.bottom_line.background = ContextCompat.getDrawable(activity!!, R.drawable.main_gradient)

                }
            }
        }

        override fun onBindViewHolder(holder: CourseViewholder?, position: Int) {
            holder?.bind(courseList[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewholder{
            val view = parent.inflate(R.layout.course_card, false)
            return CourseViewholder(view)
        }

        override fun getItemCount(): Int = courseList.size

        fun updateList(newList : List<Course>){
            courseList = newList
            notifyDataSetChanged()
        }



    }


    // nasconde tutto quello che non serve durante la query per i corsi per avere più spazio
    fun hideViewsForCourseSearch(){
        choiceLayout.setGone()
        universityLayout.setGone()
        submitButton.setGone()
    }
    // ripristina tutte le view quando la query è finita (click sulla carta)
    fun restoreViewAfterCourseSearch(){
        choiceLayout.setVisible()
        universityLayout.setVisible()
        submitButton.setVisible()
    }
    // nasconde tutto quello che non serve durante la query delle università per avere più spazio
    fun hideViewsForUniversitySearch(){
        choiceLayout.setGone()
        submitButton.setGone()
        courseLayout.setGone()
    }
    // ripristina tutte le view quando la query è finita (click sulla carta)
    fun restoreViewAfterUniversitySearch(){
        choiceLayout.setVisible()
        submitButton.setVisible()
        courseLayout.setVisible()
    }

}// Required empty public constructor
