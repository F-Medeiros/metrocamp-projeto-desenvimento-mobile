package lexus.com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.*;
import android.os.Environment;
import java.text.SimpleDateFormat;
import android.util.Log;
import java.io.IOException;
import android.net.wifi.WifiManager;
import android.widget.TextView;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;


public class MainActivity extends AppCompatActivity {

    private WebServer server;
    private String ip = "WIFI nao habilitado";
    private int port = 8080;
    private NanoHTTPD.Response.Status HTTP_CODE = NanoHTTPD.Response.Status.OK;


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

            HTTP_CODE = Response.Status.OK;

            String requestURL = session.getUri();
            String MIME_type = getMimeType(requestURL);



            if(requestURL.equals("/")) {
                return newFixedLengthResponse(HTTP_CODE,"text/html" , "<script>window.location.href='index.html'</script>");
            }else if(requestURL.equals("/routes") || requestURL.equals("/routes/")){
                String processecedRoute = routes(session);
                return newFixedLengthResponse(HTTP_CODE,"application/json" , processecedRoute);
            }

            try{

                InputStream inputStream = getResources().getAssets().open("web"+requestURL);
                return createResponse(HTTP_CODE, MIME_type, inputStream);

            }catch(IOException e){
                HTTP_CODE = Response.Status.NOT_FOUND;
                return newFixedLengthResponse(HTTP_CODE ,"text/plain" , "erro..");
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



    public String routes(NanoHTTPD.IHTTPSession session)
    {

        String retorno = "";

        HTTP_CODE = NanoHTTPD.Response.Status.NOT_FOUND;


        try{

            String route  = session.getParms().get("route");
            String variables  = session.getParms().get("variables");

            JSONObject obj_variables = new JSONObject(variables);

            if(route.equals("listResources")) {
                retorno = listResources(obj_variables);
            }else{
                retorno = "Nao chamou";
            }

        }catch (JSONException e){
            retorno = "no variables '"+ e.getMessage() +"'";
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }catch(Exception e){
            retorno = "erro aqui: " + session.getParms().toString();// .get("route");
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }



        return retorno;
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

     public String listResources(JSONObject variables){

        String retorno = "";

        try{

            String path = variables.getString("path");

            if(path.equals(""))
                path = Environment.getExternalStorageDirectory().getPath();

            if(!path.substring(path.length()-1).equals("/"))
                path = path + "/";

            JSONObject aux_retorno = new JSONObject();
            JSONArray folders = new JSONArray();
            JSONArray files = new JSONArray();

            File resource = new File(path);

            if(resource.exists()) {

                File[] resources = resource.listFiles();

                for (int i = 0; i < resources.length; i++) {

                    JSONObject aux_jo = new JSONObject();

                    aux_jo.put("name", resources[i].getName());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    aux_jo.put("updated_at", sdf.format(resources[i].lastModified()));

                    if (resources[i].isFile()) {
                        aux_jo.put("size", resources[i].length());
                        files.put(aux_jo);
                    } else if (resources[i].isDirectory()) {
                        folders.put(aux_jo);
                    }

                }

                aux_retorno.put("path", path);
                aux_retorno.put("folders", folders);
                aux_retorno.put("files", files);

                HTTP_CODE = NanoHTTPD.Response.Status.OK;
                retorno = aux_retorno.toString();

            }else{
                HTTP_CODE = NanoHTTPD.Response.Status.NOT_FOUND;
                retorno = "Diretorio nao encontrado";
            }
        }catch(JSONException e){
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
            retorno = "erro ao listar arquivos '"+e.getMessage()+"'";
        }catch(Exception e){
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
            retorno = "erro ao listar arquivos '"+e.getMessage()+"'";
        }

        return retorno;
     }

}
