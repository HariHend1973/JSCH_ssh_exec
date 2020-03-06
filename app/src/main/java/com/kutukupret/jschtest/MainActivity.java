package com.kutukupret.jschtest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    EditText etIpAddress;
    EditText etPort;
    EditText etUsername;
    EditText etPassword;
    EditText etCommand;
    Button btnExec;

    TextView tvResult;

    String IpAddress;
    String Port;
    String Uname;
    String Passwd;
    static String Cmd;

    MyTask mt;

    @SuppressLint({"StaticFieldLeak", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("SSH Remote Exec");

        etIpAddress = findViewById(R.id.etIpAAddress);
        etPort = findViewById(R.id.etPort);
        etUsername = findViewById(R.id.etUname);
        etPassword = findViewById(R.id.etPasswd);
        etCommand = findViewById(R.id.etCommand);
        btnExec = findViewById(R.id.btnConnect);

        tvResult = (TextView)findViewById(R.id.tvResult);
        tvResult.setMovementMethod(new ScrollingMovementMethod());

        View.OnTouchListener listener = new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isLarger;
                isLarger = ((TextView) v).getLineCount() * ((TextView) v).getLineHeight() > v.getHeight();
                if (event.getAction() == MotionEvent.ACTION_MOVE && isLarger) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        };
        tvResult.setOnTouchListener(listener);

        btnExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IpAddress = etIpAddress.getText().toString();
                Port = etPort.getText().toString();
                Uname = etUsername.getText().toString();
                Passwd = etPassword.getText().toString();
                Cmd = etCommand.getText().toString();


                tvResult.setText("");
                tvResult.scrollTo(0, 0);
                mt = new MyTask();
                mt.execute();
            }
        });

    }


    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<Integer, Void, String> {
        String output;
        @Override
        protected String doInBackground(Integer... params) {
            try {
                output = executeRemoteCommand(Uname, Passwd, IpAddress, Integer.parseInt(Port));
                Log.d("JSCH: ", output);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String Res) {
            super.onPostExecute(Res);
            tvResult.setText("");
            tvResult.setText("Result: \n");
            tvResult.setTextSize(14f);
            try {
                tvResult.append("========== \n");
                tvResult.append(Res + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //tvResult.setText("End");
            Toast.makeText(getApplicationContext(),"Done!",Toast.LENGTH_SHORT).show();
        }
    }

    public static String executeRemoteCommand(String username,String password,String hostname,int port)
            throws Exception {
        int i=0;
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();
        if(session.isConnected()){
            Log.d("JSCH: ", "Connected Successfully");
        }
        else{
            Log.d("JSCH: ", "Connection Failed");
        }

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        channelssh.setCommand(Cmd);
        channelssh.setInputStream(null);
        channelssh.setErrStream(System.err);
        InputStream in=channelssh.getInputStream();

        channelssh.connect();
        if(channelssh.isConnected()){
            //System.out.println("channel connected");
            Log.d("JSCH: ", "channel connected");
        }
        if(!channelssh.isClosed()){
            //System.out.println("channel not closed");
            Log.d("JSCH: ", "channel not closed");
        }

        byte[] tmp=new byte[4096];
        while(true){
            while(in.available()>0){
                i=in.read(tmp, 0, 4096);
                if(i<0)break;
                //System.out.print(new String(tmp, 0, i));
                //Log.d("JSCH: ", new String(tmp, 0, i));
            }
            if(channelssh.isClosed()){
                System.out.println("exit-status: "+channelssh.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        channelssh.disconnect();
        session.disconnect();

        //Log.d("JSCH: ", baos.toString());
        //Log.d("JSCH: ", new String(tmp, 0, i));
        //return baos.toString();
        return(new String(tmp, 0, i));

    }
}
