package com.gibisoft.dodge.ListView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.gibisoft.dodge.R;
import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int layout;
    private ArrayList<RankData> data;

    String myDeviceId;

    public ListViewAdapter(Context context, int layout, ArrayList<RankData> data, String myDeviceId){
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
        this.data = data;
        this.myDeviceId = myDeviceId;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = inflater.inflate(layout, viewGroup, false);
        }
        TextView rankNumber = view.findViewById(R.id.rank_number);
        TextView initials = view.findViewById(R.id.rank_initials);
        TextView score = view.findViewById(R.id.rank_score);

        RankData rankdata = data.get(i);

        rankNumber.setText(i + "위");
        initials.setText(rankdata.getInitials());
        score.setText(rankdata.getScore().toString());

        if(i == 0) {
            rankNumber.setText("순위");
            rankNumber.setTextColor(Color.WHITE);
            initials.setTextColor(Color.WHITE);
            score.setText("점수");
            score.setTextColor(Color.WHITE);
        }

        if(rankdata.getDeviceId().equals(myDeviceId)) {
            rankNumber.setTextColor(Color.YELLOW);
            initials.setTextColor(Color.YELLOW);
            score.setTextColor(Color.YELLOW);
        }
        return view;
    }
}
