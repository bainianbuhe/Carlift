package osu.carlift;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class RequestListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment(){
        return new RequestListFragment();
    }
}
