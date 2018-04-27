package lexus.com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.*;
import android.util.Log;
import java.io.IOException;
import android.net.wifi.WifiManager;
import android.widget.TextView;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import org.json.JSONException;


public class MainActivity extends AppCompatActivity {

    private WebServer server;
    private String ip = "WIFI nao habilitado";
    private int port = 8080;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        server = new WebServer();
        try {
            server.start();
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            ip = "http://"+formatarIP(wm.getConnectionInfo().getIpAddress())+":"+ String.valueOf(port);
        }catch (IOException e){
            ip = e.getMessage();
        }

        TextView textView = findViewById(R.id.ip);
        textView.setText(ip);


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
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {

            String requestURL = session.getUri();
            String MIME_type = getMimeType(requestURL);
            String fileLoader = requestURL;



            if(fileLoader.equals("/"))
                return newFixedLengthResponse(Response.Status.OK,"text/html" , "<script>window.location.href='index.html'</script>");

            try{

                InputStream inputStream = getResources().getAssets().open("web"+fileLoader);
                return createResponse(Response.Status.OK, MIME_type, inputStream);

            }catch(IOException e){
                return newFixedLengthResponse(Response.Status.NOT_FOUND ,"text/plain" , "erro..");
            }


        }

        //Announce that the file server accepts partial content requests
        private Response createResponse(Response.Status status, String mimeType, InputStream message) {
            return newChunkedResponse(status, mimeType, message);
        }


    }


    public String getExtension(String texto) {

        String extensao = "";

        int result = texto.lastIndexOf(".");

        if(result > -1)
            extensao = texto.substring(result+1);

        return extensao;
    }

    private String formatarIP(int ip){
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
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
            MIME_TYPE = "audio/mpeg";

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

    public String routes(String route, String variables)
    {

        String retorno = "no route";
        int status_code = 404;


        try{

            JSONObject obj = new JSONObject(variables);

            Log.d("My App", obj.toString());

        }catch (JSONException e){
            retorno = "no variables";
            status_code = 402;
        }



        return retorno;
    }

}
