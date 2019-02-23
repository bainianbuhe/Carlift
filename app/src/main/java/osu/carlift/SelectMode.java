package osu.carlift;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SelectMode extends AppCompatActivity {

    private Button mpassengermode;
    private Button mdrivermode;
    private String userName="";
    private static final String EXTRA_USERNAME="userName";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);
        userName=getIntent().getStringExtra(EXTRA_USERNAME);
        Log.e("TAG", "in selectmode username is "+userName);
        mpassengermode=(Button)findViewById(R.id.select_passenger_button);
        mpassengermode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectMode.this,UserAddRequest.class);
                intent.putExtra(EXTRA_USERNAME,userName);
                startActivity(intent);

            }
        });
        mdrivermode=(Button)findViewById(R.id.select_driver_button);
        mdrivermode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectMode.this,DebugActivity.class);
                //intent.putExtra(EXTRA_USERNAME,userName);
                startActivity(intent);

            }
        });
    }
}
