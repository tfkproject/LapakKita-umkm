package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import ta.widia.lapakkita.R;
import ta.widia.lapakkita.umkm.util.Config;
import ta.widia.lapakkita.umkm.util.Request;
import ta.widia.lapakkita.umkm.util.SessionManager;

public class LoginUmkm extends AppCompatActivity {

    EditText edtEmail, edtPass;
    TextView btnReg;
    Button btnLogin;
    private ProgressDialog pDialog;
    private String url = Config.HOST+"login_umkm.php";
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Login UMKM");

        session = new SessionManager(getApplicationContext());

        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtPass = (EditText) findViewById(R.id.edt_pass);

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReg = (TextView) findViewById(R.id.btn_reg);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtEmail.getText().equals("") || edtPass.getText().equals("")){
                    Toast.makeText(LoginUmkm.this, "Tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                }else{
                    String email = edtEmail.getText().toString();
                    String pass = edtPass.getText().toString();
                    new prosesLogin(email, pass).execute();
                }
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginUmkm.this, RegistrasiUmkm.class);
                startActivity(intent);
            }
        });
    }

    private class prosesLogin extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String psn;

        String email, password;

        public prosesLogin(String email, String password){
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginUmkm.this);
            pDialog.setMessage("Memuat data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();
                detail.put("email", email);
                detail.put("password", password);

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getInt("success");

                    if (scs == 1) {
                        JSONArray products = ob.getJSONArray("field");

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);
                            String id_umkm = c.getString("id_umkm");
                            String nm_pmlk = c.getString("nama_pemilik");
                            String nm_umkm = c.getString("nama_umkm");
                            String noktp = c.getString("no_ktp");
                            String alamat = c.getString("alamat_umkm");
                            String nohp = c.getString("no_hp");
                            String email = c.getString("email");
                            String desk = c.getString("deskripsi");
                            String logo = c.getString("logo");
                            String lat = c.getString("lat");
                            String lon = c.getString("lon");

                            //buat sesi login
                            session.createLoginSession(
                                    id_umkm,
                                    nm_pmlk,
                                    nm_umkm,
                                    noktp,
                                    alamat,
                                    nohp,
                                    email,
                                    desk,
                                    logo,
                                    lat,
                                    lon);
                        }
                    } else {
                        // no data found
                        psn = ob.getString("message");

                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();

            if(scs == 0){
                Toast.makeText(LoginUmkm.this, ""+psn, Toast.LENGTH_SHORT).show();
            }else{
                //tutup activity ini
                finish();

                Intent intent = new Intent(LoginUmkm.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        }

    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*//noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

}
