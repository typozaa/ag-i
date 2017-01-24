package com.example.user_pc.ag_scanner;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import me.dm7.barcodescanner.zxing.ConstantNode;

import static com.example.user_pc.ag_scanner.R.id.scannerNumber;

/**
 * Created by user-pc on 14/10/2016.
 */

public class mainMenuActivity extends AppCompatActivity implements ConstantNode {


    private static final int REQUEST_CODE = 99;
    private ImageButton mediaBtn;
    private ImageButton cameraBtn;
    private ImageButton scanBarcodeBtn;
    private ImageButton uploadBtn;
    private ImageView scannedImageView;
    public EditText ScannerNumber;
    private File tempDirForConfigFile = null;
    private File tempDir = null;
    public String randomUUID = UUID.randomUUID().toString();
    final File filename=new File(getTempDir(),UUID.randomUUID().toString()+".png");
    public Spinner spinner;
    public ArrayList<String> nodeName=new ArrayList<>();
    String jsonO = "";
    public SimpleDateFormat timeFormat= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public String node_type_id="";
    public String node_type_name="";
    public String selected;
    public String nodeIdOutput;
    public File output = new File(getTempDir(), filename.getName());
    public EditText deliveryNum;
    public TextView barcodeScan;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        deliveryNum=(EditText)findViewById(R.id.deliveryNo);
        uploadBtn = (ImageButton) findViewById(R.id.uploadButton);
        uploadBtn.setOnClickListener(new uploadClickListener());
        scanBarcodeBtn = (ImageButton) findViewById(R.id.barcodeScanbtn);
        scanBarcodeBtn.setOnClickListener(new barcodeClickListener());
        mediaBtn = (ImageButton) findViewById(R.id.mediaButton);
        cameraBtn = (ImageButton) findViewById(R.id.cameraButton);
        cameraBtn.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaBtn = (ImageButton) findViewById(R.id.mediaButton);
        mediaBtn.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);
        spinner=(Spinner) findViewById(R.id.nodeSpinner);
        ScannerNumber = (EditText) findViewById(scannerNumber);

        String res=getIntent().getStringExtra("result");
        ScannerNumber.setText(res);
        String strscanner=ScannerNumber.getText().toString();
        barcodeScan=(TextView) findViewById(R.id.textView9);
       if (strscanner!=null){
           barcodeScan.setVisibility(View.VISIBLE);
       }

        final String urlset=loginActivity.urltext.getText().toString();
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String jsonlist = "/NodeType?json={'ebikko_session_id':%22" + loginActivity.ebikko_session_id + "%22,'method':'" + "NODETYPE_USABLE_LIST" + "','node_type_id':''}";
            System.out.println(loginActivity.ebikko_session_id);
            jsonlist = jsonlist.replace("'", "%22");
            URL recordUrl = new URL(urlset + jsonlist);
            System.out.println(recordUrl);
            HttpURLConnection connection = (HttpURLConnection) recordUrl.openConnection();
            if(loginActivity.msCookieManager.getCookieStore().getCookies().size()>0) {
                connection.setRequestProperty("Cookie", TextUtils.join(";", loginActivity.msCookieManager.getCookieStore().getCookies()));
                connection.connect();
                connection.getResponseCode();
                if (connection.getResponseCode() == 200 && connection.getResponseCode() <= 299) {
                    BufferedReader brs = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    System.out.println(brs);
                    StringBuilder sbj = new StringBuilder();
                    String inputLine = null;
                    while ((inputLine = brs.readLine()) != null) {

                        sbj.append(inputLine+"\n");
                        jsonO = sbj.toString();
                        Log.e("response of string", jsonO);
                    }
                    brs.close();
                } else {
                    BufferedReader errorBuffer = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder sbj = new StringBuilder();
                    String inputLine = null;
                    while ((inputLine = errorBuffer.readLine()) != null) {
                        String jsonO = "";
                        sbj.append(inputLine);
                        jsonO = sbj.toString();
                        Log.e("response of string", jsonO);

                    }
                    errorBuffer.close();
                }
            }
            try{
                JSONObject jsonObj=new JSONObject(jsonO);
                JSONArray queryjsonObj=jsonObj.getJSONArray("results");
                for(int i=0; i<queryjsonObj.length();i++){
                    JSONObject j=queryjsonObj.getJSONObject(i);
                    node_type_name=j.getString("name");
                    node_type_id=j.getString("node_type_id");
                    nodeName.add(node_type_name);
                }
                ArrayAdapter<String> nodeAdapter=new ArrayAdapter<String>(mainMenuActivity.this,android.R.layout.simple_spinner_item,nodeName);
                spinner.setAdapter(nodeAdapter);
                nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            }catch(Exception e){
                e.printStackTrace();
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = parent.getItemAtPosition(position).toString();
                System.out.println(selected);
                try {
                    JSONObject jsonObj = new JSONObject(jsonO);
                    JSONArray queryjsonObj = jsonObj.getJSONArray("results");
                    ArrayList<String>valuelist=new ArrayList<String>();
                    for (int i = 0; i < queryjsonObj.length(); i++) {
                        JSONObject j=queryjsonObj.getJSONObject(i);
                        String nodeID=j.getString("node_type_id");
                        valuelist.add(nodeID);
                    }
                    int index=nodeName.indexOf(selected);
                    nodeIdOutput=valuelist.get(index);
                    System.out.println(nodeIdOutput);
                    Toast.makeText(getApplicationContext(),""+nodeIdOutput,Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar();
    }

    public boolean isCameraAvailable(){
        PackageManager pm =getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private class uploadClickListener implements View.OnClickListener{
        Boolean success=false;
        public void onClick(View v){
            new UploadAction().execute();
            new jsonUpload().execute();
            success=true;
            get_ddms();
            if(success==true){
                Toast.makeText(getApplicationContext(),"Upload SuccessFul",Toast.LENGTH_SHORT).show();
                scannedImageView.setImageBitmap(null);
                ScannerNumber.setText("");
                deliveryNum.setText("");
            }

        }
    }


    private class barcodeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isCameraAvailable()) {
                Intent intent=new Intent(mainMenuActivity.this, scannerActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast toast = Toast.makeText(mainMenuActivity.this, "Rear Camera Unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    private class ScanButtonClickListener implements View.OnClickListener {

        private int preference;

        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }

        public ScanButtonClickListener() {
        }

        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(mainMenuActivity.this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE:
                if(resultCode==Activity.RESULT_OK){
                    Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        scannedImageView.setImageBitmap(bitmap);
                        Bitmap bitStore= MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                        ByteArrayOutputStream bos=new ByteArrayOutputStream();
                        bitStore.compress(Bitmap.CompressFormat.PNG,0,bos);
                        byte[] bitmapdata=bos.toByteArray();
                        OutputStream out=new FileOutputStream(filename);
                        out.write(bitmapdata);
                        out.flush();
                        out.close();
                        getContentResolver().delete(uri, null, null);
                        //TextView scanText=(TextView)findViewById(R.id.scannedImageText);
                        //scanText.setVisibility(View.GONE);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    private class UploadAction extends AsyncTask<Object, Object, Boolean> {
        boolean success=false;
        String charset="UTF-8";
        String requestURL=getScannerURL()+"/UploadContent?json=";
        protected Boolean doInBackground(Object... params) {
            try {

                MultipartUtility multipart = new MultipartUtility(requestURL, charset);
                multipart.addStringPart("ebikko-session-id",loginActivity.ebikko_session_id);
                multipart.addStringPart("ebikko-repo", getScannerRepo());
                multipart.addStringPart("ebikko-web-gui-id",randomUUID);
                multipart.addStringPart("desktop-scanner-tempFolder", randomUUID);
                multipart.addFilePart("file", output);
                List<String> response = multipart.finish();
                System.out.println("SERVER REPLIED:");
                System.out.println(response);
                success=true;
                get_ddms();
                if(success=true){
                    tempDir.delete();
                }


            }catch (Exception e){
                System.err.println("Fatal transport error: " + e.getMessage());
            }
            return success;
        }
    }

    private class jsonUpload extends AsyncTask<String, String, String> {
        String finalJsonURL=getScannerURL()+"/Node?json=";
        final String scannerNumber=ScannerNumber.getText().toString();
        final String deliveryNo=deliveryNum.getText().toString();
        final String specialChar="g2c4c7c6d5ec409ca95c58a2225aa74b";//"e027879078d44180a9385b38cbfa515e";
        final String specialCharacter="c17cb82ffa0a427f82a54d6288e8d1c8";//"f444a56388514d2da815a54bdb443f8c";
        @Override
        protected String doInBackground(String... params) {
            try {
                String jsonPost="%7B%22ebikko_session_id%22:%22"+loginActivity.ebikko_session_id+"%22,%22method%22:%22NODE_SAVE%22,%22node_type_id%22:%22"+nodeIdOutput+"%22,%22node_type_name%22:%22Delivery%20Note%22,%22node_id%22:%22%22,%22title%22:%22"+scannerNumber.toString()+"%22,%22date_closed%22:%22%22,%22enclosure_number%22:%22%5C%220%5C%22%22,%22client_id%22:%22%22,%22bypass_referenced_acls%22:false,%22security_based_on_container%22:false,%22description%22:%22%22,%22barcode%22:%22%22,%22classification%22:%22%22,%22secondary_classification%22:%22%22,%22tertiary_classification%22:%22%22,%22container_id%22:%22%22,%22current_assignee%22:%2200000000000000000000000000000000%22,%22disposition_status%22:%220%22,%22external_barcode%22:%22%22,%22external_id%22:%22%22,%22finalized%22:false,%22is_hybrid%22:false,%22is_declassify%22:false,%22home%22:%22%22,%22integrity_check%22:%22%22,%22owner_id%22:%2200000000000000000000000000000000%22,%22record_number%22:%22%22,%22retention_schedule_id%22:%22%22,%22master_document_link%22:%22%22,%22LibreOfficeFilepath%22:%22%22,%22notes%22:%22%22,%22AppededNotes%22:%22%22,%22date_registered%22:%22"+timeFormat.toString()+"%22,%22date_created%22:%22%22,%22date_archived%22:%22%22,%22date_made_inactive%22:%22%22,%22record_class%22:1,%22author%22:%22%22,%22batch_id%22:%22%22,%22is_auto_update_security_level%22:false,%22is_update_security_level_on_particular_date%22:false,%22security_level_trigger_date%22:null,%22is_update_security_level_on_triggered_event%22:false,%22security_level_triggered_event_duration%22:1,%22security_level_triggered_event_type%22:0,%22security_level_triggered_event%22:null,%22security_level_triggered_event_property%22:null,%22auto_update_security_level_id%22:%22%22,%22acl_id%22:%22%22,%22txtCatatan%22:%22%22,%22hybrid_remarks%22:%22%22,%22appended_hybrid_remarks%22:%22%22,%22security_level_id%22:%22%22,%22acl_list%22:%5B%5D,%22creator_name%22:%22%22,%22master%22:%22%22,%22creator_username%22:%22%22,%22is_container_removed_on_ret_trig%22:false,%22is_set_home_on_container_ret_trig%22:false,%22retention_schedule_new_home_uid%22:%22%22,%22content%22:%7B%22file_name%22:%22"+output.getName()+"%22,%22field_name%22:%22"+randomUUID+"%22%7D,%22data%22:%7B%22"+specialCharacter.toString()+"%22:%22"+scannerNumber.toString()+"%22,%22"+specialChar.toString()+"%22:%22"+deliveryNo.toString().replaceAll(" ","%20")+"%22%7D,%22unsavedNodeProperties%22:%7B%7D%7D";
                System.out.println(jsonPost.toString());
                URL recordUrl = new URL(finalJsonURL + jsonPost);
                System.out.println(recordUrl);
                HttpURLConnection connection = (HttpURLConnection) recordUrl.openConnection();
                if(loginActivity.msCookieManager.getCookieStore().getCookies().size()>0) {
                    connection.setRequestProperty("Cookie", TextUtils.join(";", loginActivity.msCookieManager.getCookieStore().getCookies()));
                    connection.setDoOutput(true); // indicates POST method
                    connection.setDoInput(true);
                    connection.setUseCaches(true);
                    connection.getResponseCode();
                    if (connection.getResponseCode() == 200 && connection.getResponseCode() <= 299) {
                        BufferedReader brs = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        System.out.println(brs);
                        StringBuilder sbj = new StringBuilder();
                        String inputLine = null;
                        while ((inputLine = brs.readLine()) != null) {

                            sbj.append(inputLine+"\n");
                            jsonO = sbj.toString();
                            Log.e("response of string", jsonO);
                        }
                        brs.close();
                    } else {
                        BufferedReader errorBuffer = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        StringBuilder sbj = new StringBuilder();
                        String inputLine = null;
                        while ((inputLine = errorBuffer.readLine()) != null) {
                            String jsonO = "";
                            sbj.append(inputLine);
                            jsonO = sbj.toString();
                            Log.e("response of string", jsonO);

                        }
                        errorBuffer.close();
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public String getScannerRepo(){
        Properties prop = new Properties();
        InputStream input = null;
        String repo = "";
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
            repo = prop.getProperty("username")+"";
            repo = repo.substring(repo.lastIndexOf('@')+1);

        }catch(Exception ex){
            //JOptionPane.showMessageDialog(null, ex);
        }
        return repo;
    }

    public String getScannerURL(){
        Properties prop = new Properties();
        InputStream input = null;
        String url = "";
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
            url = prop.getProperty("url")+"";

        }catch(Exception ex){
            //JOptionPane.showMessageDialog(null, ex);
        }
        return url;
    }
    private String get_ddms(){
        Properties prop = new Properties();
        InputStream input = null;
        String ddms = "";
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
            ddms = prop.getProperty("ddms.customize.menu")+"";

        }catch(Exception ex){
            //JOptionPane.showMessageDialog(null, ex);
        }
        return ddms;
    }
    private File getTempDir() {
        if (tempDir == null) {
            tempDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),".scanner.temp");
            tempDir=new File(tempDir,randomUUID);
            tempDir.mkdirs();
        }
        return tempDir;
    }
    private File getTempDirForConfigFile() {
        if (tempDirForConfigFile == null) {
            tempDirForConfigFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),".scanner.config");
            tempDirForConfigFile.mkdirs();
        }
        return tempDirForConfigFile;
    }

    private Bitmap convertByteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_logout:
                Intent logout=new Intent(mainMenuActivity.this, loginActivity.class);
                logout.putExtra("finish",true);
                logout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout);
                finish();
                Toast.makeText(getApplicationContext(),"See You Again",Toast.LENGTH_SHORT).show();
                return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    static{ System.loadLibrary("opencv_java3");}

}
