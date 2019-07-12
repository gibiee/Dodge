package com.gibisoft.dodge.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.gibisoft.dodge.Bullet;
import com.gibisoft.dodge.ListView.RankData;
import com.gibisoft.dodge.MainActivity;
import com.gibisoft.dodge.NetworkStatus;
import com.gibisoft.dodge.R;
import com.gibisoft.dodge.RepeatListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GamePlay extends Fragment {
    MainActivity mainActivity;
    String myDeviceId;

    ViewGroup rootView;
    Timer timer;

    long score;
    TextView current_score;
    ImageButton btn_left, btn_up, btn_down, btn_right;
    ImageView craft;

    ArrayList<Bullet> bullets = new ArrayList();
    static ConstraintLayout gameLayout, rightWall;
    static LinearLayout bottomWall;

    MediaPlayer mediaPlayer;

    AlertDialog ad1,ad2;
    NumberPicker npName1,npName2, npName3;
    String initials;
    boolean registerRank = false;
    ProgressDialog mProgressDialog;
    FirebaseFirestore db;
    long score_10th;

    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        myDeviceId = mainActivity.myDeviceId;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.game_play, container, false);

        btn_left = rootView.findViewById(R.id.btn_left);
        btn_up = rootView.findViewById(R.id.btn_up);
        btn_down = rootView.findViewById(R.id.btn_down);
        btn_right = rootView.findViewById(R.id.btn_right);
        craft = rootView.findViewById(R.id.craft);
        gameLayout = rootView.findViewById(R.id.game_layout);
        bottomWall = rootView.findViewById(R.id.operation_keys);
        rightWall = rootView.findViewById(R.id.right_wall);
        current_score = rootView.findViewById(R.id.current_score);

        btn_left.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                craft.setImageResource(R.drawable.craft_left);
                if (craft.getX() > 0) {
                    craft.setX(craft.getX() - 5);
                }
            }
        }));
        btn_up.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                craft.setImageResource(R.drawable.craft);
                if (craft.getY() > 0) {
                    craft.setY(craft.getY() - 5);
                }
            }
        }));
        btn_down.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                craft.setImageResource(R.drawable.craft);
                if (craft.getY() + craft.getHeight() < bottomWall.getY()) {
                    craft.setY(craft.getY() + 5);
                }
            }
        }));
        btn_right.setOnTouchListener(new RepeatListener(0, 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                craft.setImageResource(R.drawable.craft_right);
                if (craft.getX() + craft.getWidth() < rightWall.getX()) {
                    craft.setX(craft.getX() + 5);
                }
            }
        }));

        /** post()를 통해 레이아웃이 생성된 후에 수행됨. */
        rootView.post(new Runnable() {
            @Override
            public void run() {
                if (new Random().nextInt(2) == 0) {
                    mediaPlayer = MediaPlayer.create(mainActivity , R.raw.background_1);
                }
                else {
                    mediaPlayer = MediaPlayer.create(mainActivity , R.raw.background_2);
                }
                mediaPlayer.start();
                mediaPlayer.setLooping(true);

                // 총알 생성
                createBullets("일반", 10);

                /** 핸들러와 쓰레드를 통해
                 * 1. 총알 이동
                 * 2. 충돌체크
                 * 3. 점수(초) 갱신
                 위 작업들을 주기적으로 반복 */
                final Handler handler = new Handler() {
                    //  게임 시작 시간 저장.
                    long startTime = System.currentTimeMillis();
                    long normalSeconds = 5;
                    long specialSeconds = 5;

                    public void handleMessage(Message msg) {
                        // 3. 점수 갱신
                        long currentTime = System.currentTimeMillis();
                        score = (currentTime - startTime) / 1000;
                        current_score.setText("현재 점수 : " + score);

                        if(((currentTime - startTime) / 1000) == normalSeconds) { //5초 마다
                            createBullets("일반", 1); //총알 1개씩 추가
                            normalSeconds += 5;
                        }
                        if(((currentTime - startTime) / 1000) == specialSeconds) { //10초 마다
                            createBullets("특수", 1); //총알 1개씩 추가
                            specialSeconds += 10;
                        }

                        for (int i = 0; i < bullets.size(); i++) {
                            // 1. 총알 이동과 반사
                            bullets.get(i).move();
                            bullets.get(i).reflection(bottomWall.getY() - bullets.get(i).getHeight(), rightWall.getX() - bullets.get(i).getWidth());

                            if(((int)bullets.get(i).getX() - 75 < (int)craft.getX() && (int)craft.getX() < (int)bullets.get(i).getX() + 75)
                                && ((int)bullets.get(i).getY() - 75 < (int)craft.getY() && (int)craft.getY() < (int)bullets.get(i).getY() + 75)) {
                                    if(isCollisionDetected(craft, (int)craft.getX(), (int)craft.getY(), bullets.get(i), (int)bullets.get(i).getX(), (int)bullets.get(i).getY())) {
                                        timer.cancel();
                                        btn_up.setEnabled(false);
                                        btn_down.setEnabled(false);
                                        btn_left.setEnabled(false);
                                        btn_right.setEnabled(false);

                                        mediaPlayer.stop();
                                        mediaPlayer = MediaPlayer.create(getContext(), R.raw.collision);
                                        mediaPlayer.start();
                                        showCollisionDialog();
                                        break;
                                    }
                            }
                        }
                    }
                };
                TimerTask timertask = new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                };
                timer = new Timer();
                timer.schedule(timertask, 0, 100);

            }
        });
        return rootView;
    }

    public void createBullets(String type, int count) {
        int deviceWidth = (int) rightWall.getX();
        int deviceHeight = (int) bottomWall.getY();
        int xsp_random, ysp_random, random;

        for (int i = 0; i < count; i++) {
            if(type == "일반") {
                xsp_random = new Random().nextInt(10) + 1;
                ysp_random = new Random().nextInt(10) + 1;
            }
            else {
                xsp_random = new Random().nextInt(10) + 21;
                ysp_random = new Random().nextInt(10) + 21;
            }
            random = new Random().nextInt(2 * (deviceWidth + deviceHeight));

            if (0 <= random && random < deviceWidth) {
                bullets.add(new Bullet(getContext(), random, 0, xsp_random, ysp_random));
            } else if (deviceWidth <= random && random < deviceWidth + deviceHeight) {
                bullets.add(new Bullet(getContext(), deviceWidth, random - deviceWidth, xsp_random, ysp_random));
            } else if (deviceWidth + deviceHeight <= random && random < 2 * deviceWidth + deviceHeight) {
                bullets.add(new Bullet(getContext(), random - (deviceWidth + deviceHeight), deviceHeight, xsp_random, ysp_random));
            } else {
                bullets.add(new Bullet(getContext(), 0, random - (2 * deviceWidth + deviceHeight), xsp_random, ysp_random));
            }

            if(type != "일반")    bullets.get(bullets.size()-1).setImageResource(R.drawable.bullet_special);
            gameLayout.addView(bullets.get(bullets.size()-1));
        }
    }

    public void showCollisionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.collision_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);   //뒤로가기 방지

        TextView gameOverScore = dialogView.findViewById(R.id.info_2);
        gameOverScore.setText(Long.toString(score));

        ImageButton btn_retry = dialogView.findViewById(R.id.btn_retry);
        ImageButton btn_rank_register = dialogView.findViewById(R.id.btn_rank_register);
        ImageButton btn_main = dialogView.findViewById(R.id.btn_main);

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = MediaPlayer.create(getContext() , R.raw.ok);
                mediaPlayer.start();

                ad1.dismiss();
                mainActivity.onFragmentChange("게임화면");
            }
        });

        btn_rank_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = MediaPlayer.create(getContext() , R.raw.ok);
                mediaPlayer.start();

                if(registerRank == false) {
                    mProgressDialog = ProgressDialog.show(getContext(), "",
                            "랭킹 등록 가능한지 확인 중...", true);
                    mProgressDialog.show();

                    if(NetworkStatus.getConnectivityStatus(getContext()) == false) {
                        Toast.makeText(getContext(), "인터넷에 연결되어있지 않습니다.", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                    else {
                        db = FirebaseFirestore.getInstance();
                        db.collection("Ranks").orderBy("score", Query.Direction.DESCENDING).limit(10)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                score_10th = (long) document.getData().get("score");
                                            }
                                        } else {
                                            Log.d("TAG", "Error getting documents: ", task.getException());
                                        }
                                        mProgressDialog.dismiss();
                                        if (score > score_10th) {
                                            showCollisionDialog_rank();
                                        } else {
                                            Toast.makeText(getContext(), "이런! 점수가 너무 낮아서 랭킹 등록을 할 수 없습니다. 랭킹 등록에 필요한 최소 점수는 " + (score_10th + 1) + "점입니다.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                }
                else { Toast.makeText(mainActivity, "이미 랭킹등록을 하셨습니다.", Toast.LENGTH_SHORT).show(); }
            }
        });

        btn_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = MediaPlayer.create(getContext() , R.raw.cancel);
                mediaPlayer.start();
                ad1.dismiss();
                mainActivity.onFragmentChange("메뉴화면");
            }
        });
        ad1 = builder.show();
    }

    public void showCollisionDialog_rank() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.collision_dialog_rank, null);
        builder.setView(dialogView);

        npName1 = dialogView.findViewById(R.id.npName1);
        npName2 = dialogView.findViewById(R.id.npName2);
        npName3 = dialogView.findViewById(R.id.npName3);

        String[] alphabet = {"A","B","C","D","E","F","G","H", "I", "J", "K","L",
                "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"}; //etc

        npName1.setMaxValue(0);
        npName1.setMaxValue(25);
        npName1.setDisplayedValues(alphabet);

        npName2.setMaxValue(0);
        npName2.setMaxValue(25);
        npName2.setDisplayedValues(alphabet);

        npName3.setMaxValue(0);
        npName3.setMaxValue(25);
        npName3.setDisplayedValues(alphabet);

        ImageButton btn_ok = dialogView.findViewById(R.id.btn_ok);
        ImageButton btn_cancel = dialogView.findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = MediaPlayer.create(getContext() , R.raw.ok);
                mediaPlayer.start();

                initials = String.valueOf((char)(npName1.getValue() + 65)) +
                        (char)(npName2.getValue() + 65) +
                        (char)(npName3.getValue() + 65);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                RankData data = new RankData(myDeviceId, initials, score);
                db.collection("Ranks").add(data);

                Toast.makeText(mainActivity, "랭킹등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                registerRank = true;
                ad2.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = MediaPlayer.create(getContext() , R.raw.cancel);
                mediaPlayer.start();

                ad2.dismiss();
            }
        });
        ad2 = builder.show();
    }

    public static boolean isCollisionDetected(View view1, int x1, int y1, View view2, int x2, int y2) {

        Bitmap bitmap1 = getViewBitmap(view1);
        Bitmap bitmap2 = getViewBitmap(view2);;
        if (bitmap1 == null || bitmap2 == null) { throw new IllegalArgumentException("bitmaps cannot be null"); }

        Rect bounds1 = new Rect(x1, y1, x1 + bitmap1.getWidth(), y1 + bitmap1.getHeight());
        Rect bounds2 = new Rect(x2, y2, x2 + bitmap2.getWidth(), y2 + bitmap2.getHeight());
        if (Rect.intersects(bounds1, bounds2)) {
            Rect collisionBounds = getCollisionBounds(bounds1, bounds2);

            for (int i = collisionBounds.left; i < collisionBounds.right; i++) {
                for (int j = collisionBounds.top; j < collisionBounds.bottom; j++) {
                    int bitmap1Pixel = bitmap1.getPixel(i - x1, j - y1);
                    int bitmap2Pixel = bitmap2.getPixel(i - x2, j - y2);
                    if (isFilled(bitmap1Pixel) && isFilled(bitmap2Pixel)) {
                        bitmap1.recycle();
                        bitmap2.recycle();
                        return true;
                    }
                }
            }
        }
        bitmap1.recycle();
        bitmap2.recycle();
        return false;
    }

    private static Bitmap getViewBitmap(View v) {
        if (v.getMeasuredHeight() <= 0) {
            int specWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            v.measure(specWidth, specWidth);
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            v.draw(c);
            return b;
        }
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    private static boolean isFilled(int pixel) {
        return pixel != Color.TRANSPARENT;
    }

    private static Rect getCollisionBounds(Rect rect1, Rect rect2) {
        int left = Math.max(rect1.left, rect2.left);
        int top = Math.max(rect1.top, rect2.top);
        int right = Math.min(rect1.right, rect2.right);
        int bottom = Math.min(rect1.bottom, rect2.bottom);
        return new Rect(left, top, right, bottom);
    }
}
