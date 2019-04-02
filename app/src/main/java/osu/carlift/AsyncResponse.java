package osu.carlift;

import org.json.JSONObject;

public interface AsyncResponse {
    void processFinish(JSONObject output);
}