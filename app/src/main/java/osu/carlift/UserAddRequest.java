package osu.carlift;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class UserAddRequest extends AppCompatActivity {
    private EditText mStartAdddr;
    private EditText mDestinationAddr;
    private Button mCheckStart;
    private Button mCheckDesti;
    private Button mAddRequest;
    private EditText mStartTime;
    private String startAddr="";
    private String destinationAddr="";
    private String startTime="";
    private String userName="";
    private static final String EXTRA_USERNAME="userName";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add_request);
        userName=getIntent().getStringExtra(EXTRA_USERNAME);
        Log.e("TAG", "username is "+userName);
        mStartAdddr = (EditText) findViewById(R.id.exact_start_adress);
        mStartAdddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                startAddr = s.toString();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mDestinationAddr = (EditText) findViewById(R.id.exact_dest_adress);
        mDestinationAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                destinationAddr = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mStartTime = (EditText) findViewById(R.id.start_time);
        mStartTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                startTime=s.toString();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCheckStart = (Button) findViewById(R.id.check_starting_point);
        mCheckStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMapSearch(startAddr);
            }
        });
        mCheckDesti=(Button)findViewById(R.id.check_destination);
        mCheckDesti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMapSearch(destinationAddr);
            }
        });
        mAddRequest=(Button)findViewById(R.id.confirm_add_dest);
        mAddRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRequest(userName,startAddr,destinationAddr,startTime);
            }
        });



    }
    protected void googleMapSearch(String place)
    {
        if (isAvailible(UserAddRequest.this, "com.google.android.apps.maps")) {
            //Uri gmmIntentUri = Uri.parse("google.navigation:q="
            //   + mLatitude + "," + mLongitude
            //  );
            Uri gmmIntentUri = Uri.parse("geo:0,0?q="+place
            );
            //+"+Columbus," + "+Ohio,"+"United States"
            //"google.navigation:q="
            //                      + "344 Stinchcomb Drive"
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                    gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            UserAddRequest.this.startActivity(mapIntent);
        } else {
            Toast.makeText(UserAddRequest.this, "You should install GoogleMap first", Toast.LENGTH_LONG)
                    .show();
            Uri uri = Uri
                    .parse("market://details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            UserAddRequest.this.startActivity(intent);
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
    //Parameters that should be sent:username, starting point,destination,time,isActive


    public   void addRequest(final String userName, final String startingPoint, final String destination, final String startTime) {
        //请求地址
        String url = "http://13.68.221.218:8089/Carlift_Hanyin/AddRequestServlet";
        String tag = "AddRequestServlet"+userName;    //注②

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
                                //做自己的登录成功操作，如页面跳转
                                Toast.makeText(UserAddRequest.this,"Success adding a carlift request",Toast.LENGTH_LONG).show();

                            } else {
                                //做自己的登录失败操作，如Toast提示
                                Toast.makeText(UserAddRequest.this,"Failed in adding a carlift request",Toast.LENGTH_LONG).show();
                                Log.e("TAG","result is"+result);
                            }
                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Toast.makeText(UserAddRequest.this,R.string.connection_failed,Toast.LENGTH_SHORT).show();
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null){
                    if(error instanceof TimeoutError){
                        Toast.makeText(UserAddRequest.this,"网络请求超时，请重试！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof ServerError) {
                        Toast.makeText(UserAddRequest.this,"服务器异常",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof NetworkError) {
                        Toast.makeText(UserAddRequest.this,"请检查网络",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof ParseError) {
                        Toast.makeText(UserAddRequest.this,"数据格式错误",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                Toast.makeText(UserAddRequest.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams()  {
                Log.e("tag","username in addrequest is "+userName);
                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);  //注⑥
                params.put("startingPoint", startingPoint);
                params.put("destination",destination);
                params.put("startTime",startTime);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }



}

