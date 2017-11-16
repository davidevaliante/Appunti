package kot.note.musashi.com.appunti;

import android.app.Application;

import com.facebook.FacebookSdk;

/**
 * Created by akain on 16/11/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());

    }
}
