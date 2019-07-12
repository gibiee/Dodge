package com.gibisoft.dodge;

import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

public class MainActivity extends AppCompatActivity {

    private long backKeyPressedTime = 0;
    public String myDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        getDeviceId();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.gibisoft.dodge.View.GameMenu()).commit();
    }

    public void onFragmentChange(String fragment) {
        switch(fragment) {
            case "메뉴화면" :
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.gibisoft.dodge.View.GameMenu()).commit();
                break;
            case "게임화면" :
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.gibisoft.dodge.View.GamePlay()).commit();
                break;
            case "랭크화면" :
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.gibisoft.dodge.View.GameRank()).commit();
                break;
            case "개발자 정보" :
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new com.gibisoft.dodge.View.InfoDeveloper()).commit();
                break;
        }
    }

    public void getDeviceId() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String advertId = null;
                try {
                    advertId = idInfo.getId();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return advertId;
            }
            @Override
            protected void onPostExecute(String advertId) {
                myDeviceId = advertId;
            }
        };
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.developer_info:
                onFragmentChange("개발자 정보");
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\'버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            ActivityCompat.finishAffinity(this);
            System.runFinalizersOnExit(true);
            System.exit(0);
        }
    }
}
