package osu.carlift;

public class CarliftRequest {
    private String us_na;
    private String st_po;
    private String desti;
    private String st_ti;


    public String getDestination() {
        return desti;
    }

    public String getStartPoint() {
        return st_po;
    }

    public String getUserName() {
        return us_na;
    }

    public String getStartTime() {
        return st_ti;
    }
    public void setDestination(String destination) {
        desti=destination;
    }

    public void setStartPoint(String startpoint) {
        st_po=startpoint;
    }

    public void setUserName(String username) {
        us_na=username;
    }

    public void setStartTime(String starttime) {
        st_ti=starttime;
    }
}
