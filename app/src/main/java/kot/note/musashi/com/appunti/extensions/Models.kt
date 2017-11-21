package kot.note.musashi.com.appunti.extensions

//modello dell'utente dell'app
data class User(var name : String? = null,
                var surname : String? = null,
                var city : String? = null,
                var university : String? =null,
                var course : String? = null,
                var year : Int? = null,
                var gender : String? = null,
                var facebookPageLink : String? = null,
                var imageLink : String? = null,
                var provider : String?= null,
                var mail : String?= null)

data class University(var textPrimary : String?=null, var textSecondary : String?=null )

data class Course(var courseName : String?=null, var courseDescription : String?=null)