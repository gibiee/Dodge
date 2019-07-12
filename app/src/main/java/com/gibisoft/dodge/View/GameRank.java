package com.gibisoft.dodge.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.gibisoft.dodge.ListView.ListViewAdapter;
import com.gibisoft.dodge.ListView.RankData;
import com.gibisoft.dodge.MainActivity;
import com.gibisoft.dodge.NetworkStatus;
import com.gibisoft.dodge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameRank extends Fragment {
    String myDeviceId;
    MediaPlayer mediaPlayer;

    FirebaseFirestore db;
    ArrayList<RankData> dataList = new ArrayList();

    String deviceId, initials;
    Long score;
    ListView lv_rank;

    MainActivity mainActivity;
    ImageButton btn_returnMenu;

    ProgressDialog mProgressDialog;

    ImageButton btn_edit_save;
    String btnStatus = "edit";
    EditText edt_message;
    String deviceId_1st = "초기값";

    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        myDeviceId = mainActivity.myDeviceId;

        mProgressDialog = ProgressDialog.show(getContext(), "",
                "랭킹 정보를 불러오는 중...", true);
        mProgressDialog.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.game_rank , container, false);

        btn_returnMenu = rootView.findViewById(R.id.btn_returnMenu);
        lv_rank = rootView.findViewById(R.id.lv_rank);
        btn_edit_save = rootView.findViewById(R.id.btn_edit_save);
        edt_message = rootView.findViewById(R.id.edt_message);

        if(NetworkStatus.getConnectivityStatus(getContext()) == false) {
            Toast toast = Toast.makeText(getContext(), "인터넷에 연결되어있지 않습니다.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            register_button_listener();

            mProgressDialog.dismiss();
        }
        else {
            db = FirebaseFirestore.getInstance();
            db.collection("Ranks")
                    .orderBy("score", Query.Direction.DESCENDING).limit(10)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                dataList.add(new RankData("", "이니셜", (long) 0));

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    deviceId = document.getData().get("deviceId").toString();
                                    if (deviceId_1st == "초기값") deviceId_1st = deviceId;
                                    initials = document.getData().get("initials").toString();
                                    score = (long) document.getData().get("score");

                                    dataList.add(new RankData(deviceId, initials, score));
                                }
                            } else {
                                Log.e("TAG", "Error getting documents: ", task.getException());
                            }
                            //값을 가져와서 리스트뷰로 표현
                            ListViewAdapter adapter = new ListViewAdapter(getContext(), R.layout.rank_item, dataList, myDeviceId);
                            lv_rank.setAdapter(adapter);

                            //랭킹 1위의 메세지 가져오기
                            db.collection("Message").document("Rank_1st").get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            edt_message.setText(task.getResult().getData().get("message").toString());
                                        }
                                    });

                            //DB 작업이 끝나면 버튼리스너 등록(그 전에 수행하면 에러남.)
                            register_button_listener();

                            mProgressDialog.dismiss();
                        }
                    });
        }
        return rootView;
    }

    void register_button_listener() {
        btn_returnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.onFragmentChange("메뉴화면");
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.cancel);
                mediaPlayer.start();
            }
        });

        btn_edit_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.ok);
                mediaPlayer.start();

                if (btnStatus == "edit") {
                    if (myDeviceId.equals(deviceId_1st)) {
                        edt_message.setEnabled(true);
                        btn_edit_save.setBackgroundResource(R.drawable.btn_save);
                        btnStatus = "save";
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle("랭킹 1위의 특권")
                                .setMessage("랭킹 1위만 편집이 가능합니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                } else if (btnStatus == "save") {
                    Map<String, Object> map = new HashMap<>();
                    map.put("message", edt_message.getText().toString());
                    db.collection("Message").document("Rank_1st").set(map);
                    edt_message.setEnabled(false);
                    Toast.makeText(getContext(), "메세지가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    btn_edit_save.setBackgroundResource(R.drawable.btn_edit);
                    btnStatus = "edit";
                }
            }
        });
    }
}
