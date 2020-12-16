package com.chatslau.model;

public class pilih_nama {

    private String key;
    private String nama;
    private String gender;
    private String kota;
    private String lokasi;
    private String image_url;

    public pilih_nama(){
    }

    public pilih_nama(String nama, String gender, String kota, String lokasi, String image_url){
        this.nama = nama;
        this.gender = gender;
        this.kota = kota;
        this.lokasi = lokasi;
        this.image_url = image_url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getKota() {
        return kota;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
