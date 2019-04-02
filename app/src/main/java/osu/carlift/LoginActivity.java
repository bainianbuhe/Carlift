package osu.carlift;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private String userName;
    private String passWord;
    private EditText mUserNameField;
    private EditText mPassWordField;
    private Button mConfirmLogin;
    private final String EXTRA_USERNAME="userName";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userName=new String();passWord=new String();
        mUserNameField=(EditText)findViewById(R.id.user_name_edit);
        mUserNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userName=s.toString();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPassWordField=(EditText)findViewById(R.id.pass_word_edit);
        mPassWordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passWord=s.toString();


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mConfirmLogin=(Button)findViewById(R.id.confirm_login_button);
        mConfirmLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginRequest(userName,passWord);

            }
        });

    }
    public   void LoginRequest(final String userName, final String password) {
        //请求地址
        String url = "http://13.68.221.218:8089/Carlift_Hanyin/LoginServlet";
        String tag = "LoginServlet"+userName;    //注②
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
                                Toast.makeText(LoginActivity.this,R.string.login_sucess,Toast.LENGTH_LONG).show();
                                Intent intent=new Intent(LoginActivity.this,SelectMode.class);
                                Log.e("TAG", "in login username is "+userName);
                                intent.putExtra(EXTRA_USERNAME,userName);
                                startActivity(intent);

                            } else {
                                //做自己的登录失败操作，如Toast提示
                                Toast.makeText(LoginActivity.this,R.string.login_fail,Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Toast.makeText(LoginActivity.this,R.string.connection_failed,Toast.LENGTH_SHORT).show();
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null){
                    if(error instanceof TimeoutError){
                        Toast.makeText(LoginActivity.this,"网络请求超时，请重试！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof ServerError) {
                        Toast.makeText(LoginActivity.this,"服务器异常",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof NetworkError) {
                        Toast.makeText(LoginActivity.this,"请检查网络",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof ParseError) {
                        Toast.makeText(LoginActivity.this,"数据格式错误",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                Toast.makeText(LoginActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams()  {
                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);  //注⑥
                params.put("passWord", password);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }



}





