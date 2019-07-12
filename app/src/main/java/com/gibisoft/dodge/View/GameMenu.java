package com.gibisoft.dodge.View;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.gibisoft.dodge.MainActivity;
import com.gibisoft.dodge.R;

public class GameMenu extends Fragment {

    MediaPlayer mediaPlayer;
    MainActivity mainActivity;

    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.game_menu , container, false);

        ImageButton btn_GameStart = rootView.findViewById(R.id.btn_GamePlay);
        ImageButton btn_GameRank = rootView.findViewById(R.id.btn_GameRank);

        btn_GameStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.ok);
                mediaPlayer.start();

                mainActivity.onFragmentChange("게임화면");
            }
        });
        btn_GameRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.ok);
                mediaPlayer.start();

                mainActivity.onFragmentChange("랭크화면");
            }
        });
        return rootView;
    }
}
