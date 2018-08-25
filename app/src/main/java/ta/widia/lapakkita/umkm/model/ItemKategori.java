package ta.widia.lapakkita.umkm.model;

/**
 * Created by user on 25/08/18.
 */

public class ItemKategori {

    String id_kategori, nama_kategori;

    public ItemKategori(String id_kategori, String nama_kategori){
        this.id_kategori = id_kategori;
        this.nama_kategori = nama_kategori;
    }

    public String getId_kategori() {
        return id_kategori;
    }

    public String getNama_kategori() {
        return nama_kategori;
    }
}
