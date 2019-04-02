package osu.carlift;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationActivity extends AppCompatActivity {
    private String driverUserName;
    private String passengerUserName;
    private String startingPoint;
    private String destination;
    private final String EXTRA_DRIVER_USERNAME="driver_user_name";
    private final String EXTRA_START_POINT="start_point";
    private final String EXTRA_DESTINATION="destination";
    private final String EXTRA_PASSENGER_USERNAME="passenger_user_name";
    private Button mNaviStart;
    private Button mNaviDesti;
    private Button mAbandon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        getStrings();
        mNaviStart=(Button)findViewById(R.id.navigation_start_point_button);
        mNaviStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMapNavigation(startingPoint);

            }
        });
        mNaviDesti=(Button)findViewById(R.id.navigation_destination_button);
        mNaviDesti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMapNavigation(destination);

            }
        });
        mAbandon=(Button)findViewById(R.id.navigation_abandon);
        mAbandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AbandonRequest( passengerUserName);
            }
        });


    }
    private void getStrings()
    {
        driverUserName=getIntent().getStringExtra(EXTRA_DRIVER_USERNAME);
        passengerUserName=getIntent().getStringExtra(EXTRA_PASSENGER_USERNAME);
        startingPoint=getIntent().getStringExtra(EXTRA_START_POINT);
        destination=getIntent().getStringExtra(EXTRA_DESTINATION);
    }
    protected void googleMapNavigation(String place)
    {
        if (isAvailible(NavigationActivity.this, "com.google.android.apps.maps")) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+place
            );
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                    gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            NavigationActivity.this.startActivity(mapIntent);
        } else {
            Toast.makeText(NavigationActivity.this, "You should install GoogleMap first", Toast.LENGTH_LONG)
                    .show();
            Uri uri = Uri
                    .parse("market://details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            NavigationActivity.this.startActivity(intent);
        }

    }
    public static boolean isAvailible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<String>();
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        return packageNames.contains(packageName);
    }
    public   void AbandonRequest(final String userName) {
        //请求地址
        String url = "http://13.68.221.218:8089/Carlift_Hanyin/HandleIsActiveServlet";
        String tag = "HandleRequestNaviServlet"+userName;    //注②
        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");  //注③
                            String result = jsonObject.getString("Result");  //注④
                            if (result.equals("success")) {  //注⑤
                                Toast.makeText(NavigationActivity.this,"Successfully abandon the request",Toast.LENGTH_LONG).show();
                            } else {
                                //做自己的登录失败操作，如Toast提示
                                Toast.makeText(NavigationActivity.this,"Failed in abandoning the request",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Toast.makeText(NavigationActivity.this,R.string.connection_failed,Toast.LENGTH_SHORT).show();
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(NavigationActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams()  {
                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);
                params.put("setIsActive","Y");
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }



}







