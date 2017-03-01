package dujuga.peruddos_v500;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.lang.String;
import dujuga.peruddos_v500.PdosClient;

import static android.R.color.holo_green_dark;
import static android.R.color.holo_red_dark;
import static android.R.color.holo_red_light;

public class MainActivity extends AppCompatActivity {

    Button btncurrent;
    public String canal = null;
    public EditText edtcurrent;
    public TextView tvcurrent;
    Socket sock;
    String tempString;
    PdosClient p;
    EditText n1, n2, n3, n4;

    public String askEntry(){
        return canal;
    }

    public int askNumber(){
        return Integer.parseInt(canal);
    }

    public void changeColor(String color){
        if(color.compareTo("green") == 0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btncurrent.setBackgroundColor(getResources().getColor(holo_green_dark));
                }
            });
        }
        else if(color.compareTo("red") == 0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btncurrent.setBackgroundColor(getResources().getColor(holo_red_dark));
                }
            });
        }
    }

    public void addToDisplay(String adding){
        setText(tvcurrent, adding/* + "\n" + tvcurrent.getText()*/);
    }

    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText("\n" + value);
            }
        });
    }

    protected synchronized void changeActivity(String value){
        /* begin with re-init of variables */
        btncurrent = null;
        edtcurrent = null;
        tvcurrent = null;
        canal = null;
        n1 = null;
        n2 = null;
        n3 = null;
        n4 = null;

        /* then changes the view */

        /* main activity */
        if(value.compareTo("mainActivity") == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.activity_main);
                }

            });
            //TextView tv3 = (TextView) findViewById(R.id.textView3);
        }

        /* named activity */
        if(value.compareTo("chooseName") == 0) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.choosename);
                    tvcurrent = (TextView) findViewById(R.id.cn_textView1);
                    btncurrent = (Button) findViewById(R.id.cn_button1);
                    edtcurrent = (EditText) findViewById(R.id.cn_name);
                }
            });
        }

        /* wait a second for let the thread ends his job */
        try {
            wait(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* then wakes the pdosclient */
        p.notifyMe();
    }


    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /* creation of the Pdos thread */
        p = new PdosClient(this);
        p.start();

        /* begin with connection */
        onConnection();

        /*btn = (Button) findViewById(R.id.button);
        edt = (EditText) findViewById(R.id.editText);
        tv3 = (TextView) findViewById(R.id.textView3);

        tv3.setMovementMethod(new ScrollingMovementMethod());

        p.start();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.notifyMe();
                edt.setText("");
            }
        });*/
    }

    protected void onConnection(){
        requestWindowFeature(R.layout.connection);
        setContentView(R.layout.connection);

        TextView tv2 = (TextView) findViewById(R.id.co_textView1);
        tvcurrent = tv2;

        Button btn = (Button) findViewById(R.id.button3);
        btncurrent = btn;
        n1 = (EditText) findViewById(R.id.editText2);
        n2 = (EditText) findViewById(R.id.editText3);
        n3 = (EditText) findViewById(R.id.editText4);
        n4 = (EditText) findViewById(R.id.editText5);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canal = n1.getText() + "." + n2.getText() + "." + n3.getText() + "." + n4.getText();
                p.notifyMe();
            }
        });
    }

    protected void mainActivity(){
        changeActivity("mainActivity");
    }

    protected void chooseName(){
        changeActivity("chooseName");

        btncurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canal = edtcurrent.getText().toString();
                p.notifyMe();
            }
        });
    }

}
