package model;

import android.text.format.DateFormat;

import java.util.Date;

public class Call {
    private long id;
    private String number;
    private String name;
    private long timeStamp;
    private String duration;
    private CallType type;
    private String recordingPath;


    public long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        if (timeStamp == 0) return "";
        return DateFormat.format("hh:mm a  dd/MM/yyyy", timeStamp).toString();
    }

    public long getTimeInMillis() {
        return timeStamp;
    }

    public String getDuration() {
        return duration;
    }

    public CallType getType() {
        return type;
    }

    public String getRecordingPath() {
        return recordingPath;
    }


    public void setId(long id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setDuration(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;


        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;


        StringBuilder stringBuilder = new StringBuilder();
        if (elapsedHours > 0)
            stringBuilder.append(elapsedHours).append(" hours, ");
        if (elapsedMinutes > 0)
            stringBuilder.append(elapsedMinutes).append(" minutes, ");
        if (elapsedSeconds > 0)
            stringBuilder.append(elapsedSeconds).append(" seconds");

        this.duration = stringBuilder.toString();
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }


    public void setType(CallType type) {
        this.type = type;
    }

    public void setRecordingPath(String recordingPath) {
        this.recordingPath = recordingPath;
    }


    public String toString() {
        return "Call Data......" +
                "\nId: " + id +
                "\nName: " + name +
                "\nNumber: " + number +
                "\nTime: " + timeStamp +
                "\nDuration: " + duration +
                "\nType: " + type +
                "\nRecording Path: " + recordingPath;
    }


    public enum CallType {
        INCOMING, OUTGOING, MISSED;
    }
}
