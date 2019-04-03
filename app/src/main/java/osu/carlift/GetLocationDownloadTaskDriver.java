package osu.carlift;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetLocationDownloadTaskDriver extends AsyncTask<String, Void, PlaceInfo> {
    public AsyncResponse delegate = null;
    Context context=SeePlaceInMapActivity.getContext();
    public interface AsyncIfc {
        void onComplete(PlaceInfo placeInfo);
    }
    public AsyncIfc completionCode;
    private static final String TAG = "SelectLocationActivity";
    JSONObject locationJSONObject;
    PlaceInfo placeInfo=new PlaceInfo();
    @Override
    protected PlaceInfo doInBackground(String... strings) {
        Handler handler=  new Handler(context.getMainLooper());
        String result = "";
        URL url;
        HttpURLConnection urlConnection;
        try {
            locationJSONObject=getLocationInfo(strings[0]);
            if(!locationJSONObject.getString("status").equals("ZERO_RESULTS")){
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(context, "Successfully finding the place you search",Toast.LENGTH_LONG).show();
                    }
                });
                double lat = locationJSONObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                double lng = locationJSONObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                placeInfo.setLatlng(new LatLng(lat, lng));
                placeInfo.setId(locationJSONObject.getJSONArray("results").getJSONObject(0).getString("place_id"));
                String formatted_address = locationJSONObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                formatted_address = formatted_address.replaceAll("United States", "美国");
                placeInfo.setAddress(formatted_address);
                Log.d(TAG,"placeinfo in the first step is"+placeInfo.toString());
                return placeInfo;
            }
            else{
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(SelectLocationActivity.getContext(), "Cannot find the place you search", Toast.LENGTH_SHORT).show();
                    }
                });
                return null;

            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Error in background"+e);
        }

        return null;
    }
    @Override
    protected void onPostExecute(PlaceInfo result) {

        if (result !=null) {
            GetDetailDownloadTask getDetailDownloadTask = new GetDetailDownloadTask();
            String placeId = result.getId();
            String searchDetailString = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&fields=name,rating,website,formatted_phone_number&key=AIzaSyAs32Ct45KI1dsEL6gYwni-BnW7pGDiYoA";
            Log.d(TAG, "url in the async task is" + searchDetailString);
            getDetailDownloadTask.execute(searchDetailString);
            getDetailDownloadTask.completionCode = new GetDetailDownloadTask.AsyncIfc() {

                @Override
                public void onComplete(JSONObject jsonObject) {
                    JSONObject infoJSONObject = jsonObject;
                    Log.d(TAG, "the returned info jSONObject is " + infoJSONObject);
                    try {
                        if (infoJSONObject.getJSONObject("result").getString("website").length()==0)
                        {
                            placeInfo.setWebsiteUri(Uri.parse(("Cannot find the website")));
                        }
                        else{
                            placeInfo.setWebsiteUri(Uri.parse(infoJSONObject.getJSONObject("result").getString("website")));}
                        placeInfo.setPhoneNumber(infoJSONObject.getJSONObject("result").getString("formatted_phone_number"));
                        placeInfo.setName(infoJSONObject.getJSONObject("result").getString("name"));
                        placeInfo.setRating((float)infoJSONObject.getJSONObject("result").getDouble("rating"));
                    } catch (JSONException e) {
                        Log.e(TAG,e.toString());
                    }
                    completionCode.onComplete(placeInfo);
                }
            };

        }
    }

    public JSONObject getLocationInfo(String url) {
        //Http Request
        HttpGet httpGet = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        //Create a JSON from the String that was return.
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}