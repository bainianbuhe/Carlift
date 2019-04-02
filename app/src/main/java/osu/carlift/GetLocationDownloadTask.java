package osu.carlift;

import android.os.AsyncTask;
import android.util.Log;

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

public class GetLocationDownloadTask extends AsyncTask<String, Void, JSONObject> {
    public AsyncResponse delegate = null;
    public interface AsyncIfc {
        public void onComplete(JSONObject jsonObject);
    }
    public AsyncIfc completionCode;
    private static final String TAG = "SelectLocationActivity";
    JSONObject locationJSONObject;
    @Override
    protected JSONObject doInBackground(String... strings) {

        Log.d(TAG,"do in back ground");
        String result = "";
        URL url;
        HttpURLConnection urlConnection;
        try {
            locationJSONObject=getLocationInfo(strings[0]);
            //Log.d(TAG,"inbackground"+locationJSONObject.toString());
            return locationJSONObject;


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Error in background"+e);
        }

        return null;
    }
    @Override
    protected void onPostExecute(JSONObject result) {
        //Log.d(TAG,"onpost");
        JSONObject location;
        String location_string;
//Parse to get the value corresponding to `formatted_address` key.
        try {
            location = result.getJSONArray("results").getJSONObject(0);
            location_string = location.getString("formatted_address");
            //Log.d(TAG, "formattted address:" + location_string);
            completionCode.onComplete(result);
        } catch (JSONException e1) {
            e1.printStackTrace();

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