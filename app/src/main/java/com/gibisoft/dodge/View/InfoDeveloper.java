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

public class InfoDeveloper extends Fragment {

    MainActivity mainActivity;
    MediaPlayer mediaPlayer;

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

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.info_developer , container, false);

        ImageButton btn_returnMenu = rootView.findViewById(R.id.btn_returnMenu);

        btn_returnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.onFragmentChange("메뉴화면");
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.cancel);
                mediaPlayer.start();
            }
        });
        return rootView;
    }
}
