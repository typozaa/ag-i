package com.example.user_pc.ag_scanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by user-pc on 10/11/2016.
 */
/**
 * Created by user-pc on 18/10/2016.
 */

public class loginProcess extends Activity {
    private Button loginBtn;
    private Button cancelBtn;
    public static EditText pwordtext;
    public static EditText urltext;
    public static EditText usernametext;
    private File tempDirForConfigFile = null ;
    protected static String ebikko_session_id;
    static java.net.CookieManager msCookieManager = new java.net.CookieManager();
    static final String COOKIES_HEADER = "Set-Cookie";
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        loginBtn = (Button) findViewById(R.id.LoginButton);
        cancelBtn = (Button) findViewById(R.id.CancelButton);
        pwordtext = (EditText) findViewById(R.id.PasswordText);
        urltext = (EditText) findViewById(R.id.UrlTextbox);
        usernametext = (EditText) findViewById(R.id.UsernameText);
        cancelBtn.setOnClickListener(new cancelClickListener());
        loginBtn.setOnClickListener(new loginClickListener());
        pwordtext.setText("");
        urltext.setText("");
        usernametext.setText("");
        mProgress=new ProgressDialog(this);
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please Wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

    }
    public class loginTask extends AsyncTask<Void, Void, Void> {

        final String urlset = urltext.getText().toString();
        public String username = usernametext.getText().toString();
        String password = pwordtext.getText().toString();
        String[] repo = null;

        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getTempDirForConfigFile();
            if (username.toString() != null && !username.toString().equalsIgnoreCase("")) {
                repo = username.split("@");
            }
            String isDDMS = "false";
            System.out.println(repo);
            String query = null;
            if (repo != null) {
                try {
                    query = "/Login?json={'username':%22" + repo[0] + "%22,'password':%22" + password + "%22,'repository_id':%22" + repo[1] + "%22}";
                    query = query.replace("'", "\"");
                    URL url = new URL(urlset + query);
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    Map<String, List<String>> headerFields = urlc.getHeaderFields();
                    List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                    if (cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                        }
                    }
                    System.out.println(cookiesHeader);
                    if (200 <= urlc.getResponseCode() && urlc.getResponseCode() <= 299) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "iso-8859-1"), 10);
                        System.out.println(br);
                        StringBuilder sb = new StringBuilder();
                        String l = null;
                        while ((l = br.readLine()) != null) {
                            String json = "";
                            sb.append(l);
                            json = sb.toString();
                            Log.d("response of string", json);
                            JSONObject obj = new JSONObject(json);
                            ebikko_session_id = obj.getString("ebikko_session_id");
                            if (obj.getString("isDDMS") != null && !obj.getString("isDDMS").equalsIgnoreCase("")) {
                                isDDMS = "true";
                            }
                        }
                        br.close();
                    }
                    Intent intent = new Intent(loginProcess.this,mainMenuActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }
            setScannerLoginFile(isDDMS);
            return null;
        }

        protected void onPostExecute(Void param){
        mProgress.dismiss();
        }
    }

    public void loginClickListener() throws IOException, JSONException {
        new loginTask().execute();
    }

    public void setScannerLoginFile(String isDDMS){
        Properties prop = new Properties();
        OutputStream output = null;
        try{
            String configFileName = getTempDirForConfigFile().getAbsolutePath()+"/EbikkoConfig.properties";
            output = new FileOutputStream(configFileName);

            // set the properties value
            prop.setProperty("url", urltext.getText().toString());
            prop.setProperty("username", usernametext.getText().toString());
            prop.setProperty("password", pwordtext.getText().toString());
            prop.setProperty("ddms.customize.menu", isDDMS);

            // save properties to project root folder
            prop.store(output, null);
        }catch(Exception ex){
            //JOptionPane.showMessageDialog(null, ex);
        }
    }
    public void getScannerLoginFile(){
        Properties prop = new Properties();
        InputStream input = null;
        try{
            String configFileName = getTempDirForConfigFile().getAbsolutePath()+"/EbikkoConfig.properties";
            //input = Login.class.getClassLoader().getResourceAsStream(configFileName);
            input = new FileInputStream(configFileName);
            if(input==null){
                System.out.println("Sorry, unable to find " + configFileName);
            }

            //load a properties file from class path, inside static method
            prop.load(input);

            //get the property value and print it out
            String url = prop.getProperty("url")+"";
            String username = prop.getProperty("username")+"";
            String password = prop.getProperty("password")+"";

            pwordtext.setText(password);
            usernametext.setText(username);
            urltext.setText(url);
        }catch(Exception ex){
            //JOptionPane.showMessageDialog(null, ex);

        }
    }
    private File getTempDirForConfigFile() {
        if (tempDirForConfigFile == null) {
            tempDirForConfigFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),".scanner.config");
            tempDirForConfigFile.mkdirs();
        }
        return tempDirForConfigFile;
    }

    private class cancelClickListener implements View.OnClickListener {
        public void onClick(View v){
            finish();
        }
    }

    private class loginClickListener implements View.OnClickListener {
        public void onClick(View v){
            try {
                loginClickListener();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}