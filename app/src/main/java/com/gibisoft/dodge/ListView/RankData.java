package com.gibisoft.dodge.ListView;

import android.support.annotation.Keep;

import java.text.SimpleDateFormat;
import java.util.Date;

@Keep
public class RankData {
    private String deviceId, initials;
    Long score, date;

    public RankData(String deviceId, String initials, Long score) {
        this.deviceId = deviceId;
        this.initials = initials;
        this.score = score;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        this.date = Long.parseLong(simpleDateFormat.format(new Date()));
    }

    public String getDeviceId() { return deviceId; }
    public String getInitials() { return initials; }
    public Long getScore() { return score; }
}
