package com.humac.album.model;


import javax.persistence.*;

@Entity
public class Image {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isAvatar;

    private String imageUrl;

    //default constructor
    public Image() {

    }
    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }




    public boolean getType() {
        return isAvatar;
    }

    public void setType(boolean type) {
        isAvatar = type;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
