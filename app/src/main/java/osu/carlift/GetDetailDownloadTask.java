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

public class GetDetailDownloadTask extends AsyncTask<String, Void, JSONObject> {
    public AsyncResponse delegate = null;
    public interface AsyncIfc {
        public void onComplete(JSONObject jsonObject);
    }
    public AsyncIfc completionCode;
    private static final String TAG = "SelectLocationActivity";
    JSONObject detailJSONObject;
    @Override
    protected JSONObject doInBackground(String... strings) {

        Log.d(TAG,"get detail do in back ground");
        HttpURLConnection urlConnection;
        try {
            detailJSONObject=getLocationDetailInfo(strings[0]);
            return detailJSONObject;


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Error in background"+e);
        }

        return null;
    }
    @Override
    protected void onPostExecute(JSONObject result) {
            completionCode.onComplete(result);


    }

    public JSONObject getLocationDetailInfo(String url) {
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