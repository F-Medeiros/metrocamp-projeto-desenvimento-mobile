package lexus.com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.*;
import android.os.Environment;
import java.text.SimpleDateFormat;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;

import android.net.wifi.WifiManager;
import android.widget.TextView;
import fi.iki.elonen.NanoHTTPD;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private WebServer server;
    private String ip = "WIFI nao habilitado";
    private int port = 8080;
    private NanoHTTPD.Response.Status HTTP_CODE = NanoHTTPD.Response.Status.OK;
    private String tipoRetorno = "texto";
    private String MIME_type = "text/plain";


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


            String processecedRoute = routes(session);
            try{

                if(tipoRetorno.equals("texto")){
                    return newFixedLengthResponse(HTTP_CODE, MIME_type, processecedRoute);

                }else if(tipoRetorno.equals("download")) {

                    File file = new File(processecedRoute);
                    InputStream inputStream = new FileInputStream(file);
                    return createResponse(HTTP_CODE, MIME_type, inputStream);

                }else if(tipoRetorno.equals("resource")){

                    InputStream inputStream = getResources().getAssets().open("web"+requestURL);
                    return createResponse(HTTP_CODE, MIME_type, inputStream);
                }


            }catch(Exception e){
                HTTP_CODE = Response.Status.NOT_FOUND;
                return newFixedLengthResponse(HTTP_CODE ,"text/plain" , e.getMessage());
            }

            return newFixedLengthResponse(HTTP_CODE ,"text/plain" , "erro..");

        }

        //Announce that the file server accepts partial content requests
        private Response createResponse(Response.Status status, String mimeType, InputStream message) {
            return newChunkedResponse(status, mimeType, message);
        }


    }



    public String routes(NanoHTTPD.IHTTPSession session) {

        String retorno = "chamou routes";
        String requestURL = session.getUri();

        HTTP_CODE = NanoHTTPD.Response.Status.OK;



        try{

            HashMap<String, String> postmap = new HashMap<String, String>();

            session.parseBody(postmap);
            if(requestURL.equals("/")) {

                tipoRetorno = "texto";
                MIME_type = "text/html";
                retorno = "<script>window.location.href='index.html'</script>";

            }else if(requestURL.equals("/routes") || requestURL.equals("/routes/")) {

                tipoRetorno = "texto";
                MIME_type = "application/json";

                String route = session.getParms().get("route");

                String path = session.getParms().get("path");

                if (path.equals(""))
                   path = Environment.getExternalStorageDirectory().getPath();
                   //path = Environment.getDataDirectory().getPath();
                   // path = "/";

                if (!path.substring(path.length() - 1).equals("/"))
                    path = path + "/";


                if (route.equals("listResources")) {
                    retorno = listResources(path);

                } else if (route.equals("newFolder")) {
                    String folderName = session.getParms().get("folderName");
                    retorno = newFolder(path, folderName);

                } else if (route.equals("rename")) {
                    String oldName = session.getParms().get("oldName");
                    String newName = session.getParms().get("newName");
                    retorno = renomear(path, oldName, newName);

                } else if (route.equals("delete")) {

                    JSONArray files = new JSONArray(session.getParms().get("files"));
                    JSONArray folders = new JSONArray(session.getParms().get("folders"));

                    retorno = deletar(path, files, folders);

                } else if (route.equals("paste")) {

                    String action = session.getParms().get("action");
                    String origin = session.getParms().get("origin");
                    String destiny = session.getParms().get("destiny");
                    JSONArray xfiles = new JSONArray(session.getParms().get("files"));
                    JSONArray xfolders = new JSONArray(session.getParms().get("folders"));

                    retorno = colar(path, action, origin, destiny, xfiles, xfolders);

                } else if (route.equals("upload")) {

                    String tmpFileName = session.getParms().get("file");
                    InputStream tmpFile = session.getInputStream();

                    retorno = uploadFile(path, postmap.get("file"), tmpFileName);

                } else if (route.equals("download")) {

                    String fileName = session.getParms().get("fileName");
                    tipoRetorno = "download";
                    MIME_type = getMimeType(fileName);
                    retorno = path+fileName;

                } else {
                    retorno = "Nao chamou";
                }

            }else{

                MIME_type = getMimeType(requestURL);

                tipoRetorno = "resource";

            }

        }catch(Exception e){
            retorno = "erro aqui: " + session.getParms().toString() + " || " + e.getMessage() + " || " + e.getLocalizedMessage();
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }



        return retorno;
    }

    public String getMimeType(String requestURL) {
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

    public String listResources(String path){

        String retorno = "listResources";

        try{

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
            retorno = "erro ao listar arquivos. '"+e.getMessage()+"' " + path;
        }

        return retorno;
    }

    public String newFolder(String path, String folderName){
        String retorno = "";

        File folder = new File(path + folderName);

        try {

            JSONObject aux_retorno = new JSONObject();

            if(folder.exists()){

                aux_retorno.put("status", false);

            }else{

                try {
                    folder.mkdirs();

                    if(folder.exists()){
                        aux_retorno.put("status", true);
                    }else{

                        aux_retorno.put("status", false);
                    }

                }catch(Exception e){

                    aux_retorno.put("status", false);
                }
            }


            if(aux_retorno.getBoolean("status") == true)
                HTTP_CODE = NanoHTTPD.Response.Status.OK;
            else
                HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;

            retorno = aux_retorno.toString();


        }catch(JSONException e){

            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }


        return retorno;
    }

    public String renomear(String path, String oldName, String newName){
        String retorno = "";

        File oldFile = new File(path + oldName);
        File newFile = new File(path + newName);

        try {

            JSONObject aux_retorno = new JSONObject();

            if(!oldFile.exists() || newFile.exists()){
                aux_retorno.put("status", false);

            }else{

                oldFile.renameTo(newFile);

                if(newFile.exists()){
                    aux_retorno.put("status", true);
                }else{
                    aux_retorno.put("status", false);
                }

            }

            if(aux_retorno.getBoolean("status") == true)
                HTTP_CODE = NanoHTTPD.Response.Status.OK;
            else
                HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;


            retorno = aux_retorno.toString();

        }catch(JSONException e){
            retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;

        }catch(Exception e){
            retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }

        return retorno;
    }

    public String deletar(String path, JSONArray files, JSONArray folders){

        String retorno = "";

        try {

            JSONObject aux_retorno = new JSONObject();

            for (int i = 0; i < folders.length(); i++) {
                String folderName = folders.get(i).toString();
                deleteRecursive(new File(path + folderName));
            }


            for (int i = 0; i < files.length(); i++) {
                String fileName = files.get(i).toString();
                File file = new File(path + fileName);
                file.delete();
            }


            aux_retorno.put("status", true);

            if(aux_retorno.getBoolean("status") == true)
                HTTP_CODE = NanoHTTPD.Response.Status.OK;
            else
                HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;

            retorno = aux_retorno.toString();

        }catch (JSONException e){
            retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }catch(Exception e){
            retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }

        return retorno;
    }

    public String colar(String path, String action, String origin, String destiny, JSONArray files, JSONArray folders){
        String retorno = "colar";

        try {

            Boolean deleteFinishedCopy = false;

            if(action.equals("cut"))
                deleteFinishedCopy = true;
            else if(action.equals("copy"))
                deleteFinishedCopy = false;


            JSONObject aux_retorno = new JSONObject();

            for (int i = 0; i < files.length(); i++) {
                String file = files.get(i).toString();
                xcopy(origin + file, destiny + file, deleteFinishedCopy);
            }


            for (int i = 0; i < folders.length(); i++) {
                String folder = folders.get(i).toString();
                xcopy(origin + folder, destiny + folder, deleteFinishedCopy);
            }



            aux_retorno.put("status", true);

            if(aux_retorno.getBoolean("status") == true)
                HTTP_CODE = NanoHTTPD.Response.Status.OK;
            else
                HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;

            retorno = aux_retorno.toString();

        }catch (JSONException e){
            retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }catch(Exception e){
            retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
            HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
        }

        return retorno;
    }

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public void xcopy(String origin, String destiny, Boolean deleteFinishedCopy) throws Exception {

        try {
            File OriginLocation = new File(origin);
            File DestinyLocation = new File(destiny);

            if(OriginLocation.isFile()) {
                FileUtils.copyFile(OriginLocation, DestinyLocation);
            }else{
                FileUtils.copyDirectory(OriginLocation, DestinyLocation);
            }

            if(deleteFinishedCopy)
                deleteRecursive(OriginLocation);

        } catch(Exception e){

            throw new Exception("Erro no xcopy ["+origin+"] " + e.getMessage());

        }
    }

    public String uploadFile(String path, String tmpFile, String fileName){
       String retorno = "";

       try {

           JSONObject aux_retorno = new JSONObject();

           xcopy(tmpFile, path + fileName, true);

           aux_retorno.put("status", true);

           if(aux_retorno.getBoolean("status") == true)
               HTTP_CODE = NanoHTTPD.Response.Status.OK;
           else
               HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;

       }catch (JSONException e){
           retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
           HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
       }catch(Exception e){
           retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
           HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
       }

       return retorno;
    }

    public String download(String path, String fileName){
       String retorno = "";

       try {

           JSONObject aux_retorno = new JSONObject();




           aux_retorno.put("status", true);

           if(aux_retorno.getBoolean("status") == true)
               HTTP_CODE = NanoHTTPD.Response.Status.OK;
           else
               HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;

       }catch (JSONException e){
           retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
           HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
       }catch(Exception e){
           retorno = "{\"status\":false, \"message\": \""+e.getMessage()+"\"}";
           HTTP_CODE = NanoHTTPD.Response.Status.BAD_REQUEST;
       }

       return retorno;
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



}
