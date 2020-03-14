package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    static public Parcelable a = null;
    public Intent b;
    Button button;
    public static TextView text;

    public final String enviar(String str, IsoDep tarjeta) {//AQUI SE ESCRIBE
        byte[] bArr;
        byte[] a2 = aDecimal(str);//60 --> 96
        if (!tarjeta.isConnected()) {
            return null;
        }
        try {
            bArr = tarjeta.transceive(a2);//ESCRIBE
        } catch (IOException unused) {
            bArr = null;
        }
        if (bArr != null) {
            return a(bArr);
        }
        return null;
    }

    public static char a(byte b) {
        return "0123456789ABCDEF".charAt(b);
    }

    public static String a(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bArr.length; i++) {
            stringBuffer.append(a((byte) ((bArr[i] >> 4) & 15)));
            stringBuffer.append(a((byte) (bArr[i] & 15)));
        }
        return stringBuffer.toString();
    }

    public static byte[] aDecimal(String str) {
        byte[] bArr = new byte[(str.length() / 2)];
        byte[] bytes = str.getBytes();
        if (str.length() % 2 != 0) {
            return null;
        }
        for (int i = 0; i < bArr.length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) (b(bytes[i2]) << 4);
            bArr[i] = (byte) (b(bytes[i2 + 1]) | bArr[i]);
        }
        return bArr;
    }

    public static byte b(byte b) {
        return (byte) ("0123456789ABCDEF0123456789abcdef".indexOf(b) % 16);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(myhandler1);
        text = (TextView) findViewById(R.id.texto);
        this.b = this.getIntent();

    }

    View.OnClickListener myhandler1 = new View.OnClickListener() {
        public void onClick(View v) {
            final Tag tagFromIntent = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask().execute(tagFromIntent);

            handleIntent(getIntent());
        }
    };

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if ("text/plain".equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {

            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }


    private final class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Tag... params) {
            if (params[0] == null)
            {
                return null;
            }

            IsoDep c = IsoDep.get((params[0]));
            try {
                c.connect();
                Log.d("UNRUMANO",String.valueOf("Conectado"));
            } catch (IOException e) {
                Log.d("UNRUMANO",String.valueOf("NO HA FIUNCIONADO"));

                e.printStackTrace();
            }



            Log.d("UNRUMANO",String.valueOf(enviar("60",c)));
            Log.d("UNRUMANO",String.valueOf(enviar("AF",c)));
            Log.d("UNRUMANO",String.valueOf(enviar("AF",c)));
            Log.d("UNRUMANO",String.valueOf(enviar("5A000001",c)));
            Log.d("UNRUMANO",String.valueOf(enviar("0A02",c)));

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }



}
