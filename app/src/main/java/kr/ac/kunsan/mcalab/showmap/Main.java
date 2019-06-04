package kr.ac.kunsan.mcalab.showmap;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;




public class Main extends AppCompatActivity {
    AppCompatDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Switch sw = findViewById(R.id.switch1);
        final EditText Lon = findViewById(R.id.editText);
        final EditText Lat = findViewById(R.id.editText2);
        final EditText path = findViewById(R.id.editText3);
        final Button btn = findViewById(R.id.button);

        Lon.setEnabled(false);
        Lat.setEnabled(false);
        path.setEnabled(true);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    Lon.setEnabled(false);
                    Lat.setEnabled(false);
                    path.setEnabled(true);
                } else {
                    Lon.setEnabled(true);
                    Lat.setEnabled(true);
                    path.setEnabled(false);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw.isChecked()) {
                    String sPath = path.getText().toString();
                    ToLonLatFromAPI(sPath);
                } else {
                    double fLon = 0;
                    double fLat = 0;
                    try {
                        fLon = Double.parseDouble(Lon.getText().toString());
                        fLat = Double.parseDouble(Lat.getText().toString());
                    } catch (java.lang.NumberFormatException e) {

                    }
                    openMap(fLon, fLat, "Your data");
                }
            }
        });
    }

    public double[] parse(String html) {
        String[] pos = html.replace("{'lat':'","").replace("','lng':'",",").replace("'}","").split(",");
        double[] pos2 = new double[2];
        pos2[1] = Double.parseDouble(pos[0]);
        pos2[0] = Double.parseDouble(pos[1]);

        return pos2;
    }


    public void ToLonLatFromAPI(final String PATH) {
        progressON(this, "loading...");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                InputStream iStream =null;
                String s = null;
                try {
                    URL url = new URL("https://www.leelab.co.kr/api/geocode.php");
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setRequestMethod("POST");
                    OutputStream outputStream = httpConn.getOutputStream();
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                    BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                    bufferedWriter.write("addr=" + PATH);
                    bufferedWriter.flush();
                    iStream = httpConn.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(iStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line = bufferedReader.readLine();
                    StringBuffer readTextBuf = new StringBuffer();

                    while (line !=null){
                        readTextBuf.append(line);
                        line = bufferedReader.readLine();
                    }
                    httpConn.disconnect();
                    s=readTextBuf.toString();

                    Log.v("tttttttt",s);
                    double[] pos= parse(s);
                    openMap(pos[0], pos[1], PATH);
                }catch(Exception e){

                }finally {
                    try {
                        iStream.close();
                    }catch (Exception e){

                    }
                    progressOFF();
                }
            }
        });
        thread.start();
    }

    public void openMap(final double lon, final double lat, final String name) {
        Intent intent = new Intent(Main.this, MapsActivity.class);
        intent.putExtra("lon", lon);
        intent.putExtra("lat", lat);
        intent.putExtra("path", name);
        startActivity(intent);
    }

    public void progressON(AppCompatActivity activity, String message) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressSET(message);
        } else {
            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.loading);
            progressDialog.show();
        }
        final ImageView img_loading_frame = progressDialog.findViewById(R.id.iv_frame_loading);
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(new Runnable() {
            @Override
            public void run() {
                frameAnimation.start();
            }
        });
        TextView tv_progress_message = progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressSET(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        TextView tv_progress_message = progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}