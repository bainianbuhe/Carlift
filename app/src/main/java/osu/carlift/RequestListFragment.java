package osu.carlift;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RequestListFragment extends Fragment {
    private RecyclerView mRequestRecyclerView;
    private RequestAdapter mAdapter;
    private String driverUserName;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.fragment_request_list,container,false);
        mRequestRecyclerView=(RecyclerView)view.findViewById(R.id.request_recycler_view);
        mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;

    }
    public void updateUI()

    {
        ArrayList<CarliftRequest> carliftrequests=SeeRequestsRequest(new VolleyCallback() {
            @Override
            public ArrayList<CarliftRequest> onSuccess(ArrayList<CarliftRequest> result) {
                return result;
            }
        });
        Log.e("TAG3","updateuilength"+carliftrequests.size());
      mAdapter=new RequestAdapter(carliftrequests);
      mRequestRecyclerView.setAdapter(mAdapter);
    }
    private class RequestHolder extends RecyclerView.ViewHolder
    {
        private CarliftRequest mRequest;
        private TextView mUserNameTextView;
        private TextView mStartPointTextView;
        private TextView mDestinationTextView;
        private TextView mStartTimeTextView;
        public RequestHolder(LayoutInflater inflater,ViewGroup parent)
        {
            super(inflater.inflate(R.layout.list_item_request,parent,false));
            mUserNameTextView=(TextView) itemView.findViewById(R.id.user_name_text);
            mStartPointTextView =(TextView) itemView.findViewById(R.id.starting_point_text);
            mDestinationTextView=(TextView) itemView.findViewById(R.id.destination_text);
            mStartTimeTextView=(TextView) itemView.findViewById(R.id.start_time_text);

        }
        public void bind(CarliftRequest carliftRequest)
        {
            mRequest=carliftRequest;
            mUserNameTextView.setText("Username:"+mRequest.getUserName());
            mStartPointTextView.setText("From:"+mRequest.getStartPoint());
            mDestinationTextView.setText("To:"+mRequest.getDestination());
            mStartTimeTextView.setText("Starttime:"+mRequest.getStartTime());

        }
    }
    private class RequestAdapter extends RecyclerView.Adapter<RequestHolder>
    {
        private ArrayList<CarliftRequest> mrequests;
        public RequestAdapter(ArrayList<CarliftRequest> requests)
        {mrequests=requests;}
        @Override
        public RequestHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
            return new RequestHolder(layoutInflater,parent);
        }
        @Override
        public void onBindViewHolder(RequestHolder holder,int position)
        {
            CarliftRequest carliftRequest=mrequests.get(position);
            holder.bind(carliftRequest);
        }
        @Override
        public int getItemCount()
        {return mrequests.size();}
    }

    public ArrayList<CarliftRequest> SeeRequestsRequest(final VolleyCallback callback) {
        //请求地址
        final ArrayList<CarliftRequest> carlift_requests=new ArrayList<>();
        String url = "http://www.hygg.com.ngrok.io/Carlift_Hanyin/SeeRequestServlet";
        String tag = "SeeRequestServlet"+driverUserName;    //注②
        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

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
                            Log.e("TAG", "in request list "+requests.length()+"requests");
                            Log.e("TAG", "length"+requests.length());
                            for (int i = 0; i < requests.length(); i++) {
                                JSONObject request = requests.getJSONObject(i);    //注②
                                String passengerUserName=request.getString("UserName");
                                String startingPoint=request.getString("StartAddress");
                                String destination=request.getString("DestiAddress");
                                String startTime=request.getString("StartTime");
                                CarliftRequest requestobject=new CarliftRequest();
                                requestobject.setUserName(passengerUserName);
                                requestobject.setStartPoint(startingPoint);
                                requestobject.setDestination(destination);
                                requestobject.setStartTime(startTime);
                                carlift_requests.add(requestobject);

                            }
                            Log.e("TAG3","inseerequestlength"+carlift_requests.size());
                            callback.onSuccess(carlift_requests);

                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Toast.makeText(getActivity(),R.string.connection_failed,Toast.LENGTH_SHORT).show();
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null){
                    if(error instanceof TimeoutError){
                        Toast.makeText(getActivity(),"网络请求超时，请重试！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof ServerError) {
                        Toast.makeText(getActivity(),"服务器异常",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof NetworkError) {
                        Toast.makeText(getActivity(),"请检查网络",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(error instanceof ParseError) {
                        Toast.makeText(getActivity(),"数据格式错误",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("TAG", error.getMessage(), error);
            }

        }) {
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
        return carlift_requests;
    }
    private interface VolleyCallback{
        ArrayList<CarliftRequest> onSuccess(ArrayList<CarliftRequest> result);
    }



}



