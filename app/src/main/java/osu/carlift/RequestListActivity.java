package osu.carlift;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class RequestListActivity extends SingleFragmentActivity {
    private String driverUserName;
    private static final String EXTRA_USERNAME="userName";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        driverUserName=getIntent().getStringExtra(EXTRA_USERNAME);
    }
    @Override
    protected Fragment createFragment(){
        RequestListFragment r=new RequestListFragment();
        r.setDriverUserName(driverUserName);
        return r;
    }
}
