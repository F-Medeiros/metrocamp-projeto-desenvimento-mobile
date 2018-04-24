package lexus.com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import java.io.*;
import java.util.*;
import fi.iki.elonen.NanoHTTPD;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private WebServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        server = new WebServer();
        try {
            server.start();
        }catch (IOException e){

        }

    }


    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (server != null)
            server.stop();
    }


    private class WebServer extends NanoHTTPD {

        public WebServer() {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {

            String html;
            String requestURL = session.getUri();
            String MIME_type = getMimeType(requestURL);
            String fileLoader = requestURL;


            html = readTextFile("web"+fileLoader);


            return newFixedLengthResponse(Response.Status.OK, MIME_type, html);
        }



    }


    public String readTextFile(String fileName) {


        try {
            InputStream inputStream = getResources().getAssets().open(fileName);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte buf[] = new byte[99999];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {

            }
            return outputStream.toString();

        }catch(IOException e){
            return "";
        }
    }

    public String getExtension(String texto) {

        String extensao = "";

        int result = texto.lastIndexOf(".");

        if(result > -1)
            extensao = texto.substring(result+1);

        return extensao;
    }


    private String getMimeType(String requestURL) {
        String MIME_TYPE = "text/plan";
        String extension = getExtension(requestURL);

        if(extension.equals("js"))
            MIME_TYPE = "text/javascript";

        else if(extension.equals("css"))
            MIME_TYPE = "text/css";

        else if(extension.equals("png"))
            MIME_TYPE = "image/png";

        else if(extension.equals("jpg"))
            MIME_TYPE = "image/jpeg";

        else if(extension.equals("jpeg"))
            MIME_TYPE = "image/pjpeg";

        else if(extension.equals("gif"))
            MIME_TYPE = "image/gif";

        else if(extension.equals("mp3"))
            MIME_TYPE = "audio/mpeg3";

        else if(extension.equals("html"))
            MIME_TYPE = "text/html";

        else if(extension.equals("woff"))
            MIME_TYPE = "font/woff";

        else if(extension.equals("woff2"))
            MIME_TYPE = "font/woff2";

        else if(extension.equals("ttf"))
            MIME_TYPE = "application/octet-stream";

        else if(extension.equals("eot"))
            MIME_TYPE = "application/vnd.ms-fontobject";

        else if(extension.equals("svg"))
            MIME_TYPE = "image/svg+xml";

        else if(extension.equals(""))
            MIME_TYPE = "";

        else if(extension.equals(""))
            MIME_TYPE = "";

        return MIME_TYPE;
    }


}
