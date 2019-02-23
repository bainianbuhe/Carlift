package osu.carlift;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//写成类试试？
public class DebugActivity extends AppCompatActivity {
    public   ArrayList<CarliftRequest> rs=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        SeeRequestsRequest(new VolleyCallback() {
            @Override
            public void onSuccess(ArrayList<CarliftRequest> result) {
                rs= result;
                Log.e("TAG4","returned list sadsadsad length in debug"+rs.size());
            }
        });
    }
    public void SeeRequestsRequest(final VolleyCallback callback) {
        final ArrayList<CarliftRequest> carlift_requests=new ArrayList<>();
        //请求地址
        String url = "http://www.hygg.com.ngrok.io/Carlift_Hanyin/SeeRequestServlet";
        String tag = "SeeRequestServlet";   //注②
        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(DebugActivity.this);

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);
        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");
                            JSONArray requests = jsonObject.getJSONArray("Requests");
                            for (int i = 0; i < requests.length(); i++) {
                                JSONObject request = requests.getJSONObject(i);    //注②
                                String passengerUserName=request.getString("UserName");
                                String startingPoint=request.getString("StartAddress");
                                String destination=request.getString("DestiAddress");
                                String startTime=request.getString("StartTime");
                                Log.e("TAG","received StartAddress is "+startingPoint);
                                CarliftRequest requestobject=new CarliftRequest();
                                requestobject.setUserName(passengerUserName);
                                requestobject.setStartPoint(startingPoint);
                                requestobject.setDestination(destination);
                                requestobject.setStartTime(startTime);
                                carlift_requests.add(requestobject);


                            }
                            Log.e("TAG4","size of request in method "+carlift_requests.size());
                            callback.onSuccess(carlift_requests);



                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Toast.makeText(DebugActivity.this,R.string.connection_failed,Toast.LENGTH_SHORT).show();
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("TAG", error.getMessage(), error);
            }

        })

        {
            @Override
            protected Map<String, String> getParams()  {
                Map<String, String> params = new HashMap<>();
                params.put("result", "success");
                return params;
            }
        };


        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);




    }
    private interface VolleyCallback{
        void onSuccess(ArrayList<CarliftRequest> result);
    }

}



