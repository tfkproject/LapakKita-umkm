package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

public class PembayaranActivity extends AppCompatActivity {

    TextView txtNama, txtHp, txtTotalHarga;
    EditText txtAlamat;
    private static final String TAG = PembayaranActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    public String SERVER_POST = Config.HOST+"buat_invoice.php";
    Button btnInvoice;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembayaran);

        getSupportActionBar().setTitle("Pembayaran Produk");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        final String id_pelanggan = user.get(SessionManager.KEY_ID_UMKM);
        final String nm_pelanggan = user.get(SessionManager.KEY_NM_UMKM);
        //final String hp_pelanggan = user.get(SessionManager.KEY_NOHP_UMKM);
        //final String alamat_pelanggan = user.get(SessionManager.KEY_ALAMAT_UMKM);

        final String total_bayar = getIntent().getStringExtra("key_total_bayar");
        final String id_keranjang = getIntent().getStringExtra("key_id_keranjang");

        txtNama = (TextView) findViewById(R.id.txt_nama);
        txtNama.setText(nm_pelanggan);

        txtHp = (TextView) findViewById(R.id.txt_hp);
        //txtHp.setText(hp_pelanggan);

        txtAlamat = (EditText) findViewById(R.id.txt_alamat);
        //txtAlamat.setText(alamat_pelanggan);

        txtTotalHarga = (TextView) findViewById(R.id.txt_harga);
        txtTotalHarga.setText("Rp. "+total_bayar);


        btnInvoice = (Button) findViewById(R.id.btn_invoice);
        btnInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new postData(id_keranjang, id_pelanggan, total_bayar, "N", "").execute();
            }
        });
    }

    private class postData extends AsyncTask<Void,Void,String> {
        private String id_keranjang;
        private String id_pelanggan;
        private String total_bayar;
        private String status;
        private String alamat;

        public postData(String id_keranjang, String id_pelanggan, String total_bayar, String status, String alamat){
            this.id_keranjang = id_keranjang;
            this.id_pelanggan = id_pelanggan;
            this.total_bayar = total_bayar;
            this.status = status;
            this.alamat = alamat;
        }

        private String scs = "";
        private String psn = "";
        private String id_transaksi = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PembayaranActivity.this);
            pDialog.setMessage("Loading..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                //menganbil data-data yang akan dikirim

                //generate hashMap to store encodedImage and the name
                HashMap<String,String> detail = new HashMap<>();
                detail.put("id_keranjang", id_keranjang);
                detail.put("id_pelanggan", id_pelanggan);
                detail.put("total_bayar", total_bayar);
                detail.put("status", status);
                detail.put("alamat_orderan", alamat);

                try{
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(SERVER_POST,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getString("success");
                    psn = ob.getString("message");
                    id_transaksi = ob.getString("id_transaksi");

                }catch (JSONException e){
                    e.printStackTrace();
                    Log.e(TAG, "ERROR  " + e);
                    Toast.makeText(getApplicationContext(),"Maaf, terjadi error!",Toast.LENGTH_SHORT).show();
                    //return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();

            if(scs.contains("1")){
                Toast.makeText(PembayaranActivity.this, psn, Toast.LENGTH_SHORT).show();

                ///
                Intent intent = new Intent(PembayaranActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                //buka printed invoice
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://203.153.21.11/app/lapakkita-widia/api/print/?id_transaksi="+id_transaksi));
                startActivity(browserIntent);
                ///
            }
            if(scs.contains("0")){
                Toast.makeText(getApplicationContext(), psn,Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

}
