package com.fa.grubot.objects.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VkUserResponseWithPhoto {

@SerializedName("id")
@Expose
private Integer id;
@SerializedName("first_name")
@Expose
private String firstName;
@SerializedName("last_name")
@Expose
private String lastName;
@SerializedName("photo_100")
@Expose
private String photo100;
@SerializedName("photo_max")
@Expose
private String photoMax;
@SerializedName("domain")
@Expose
private String domain;

    /**
    * No args constructor for use in serialization
    *
    */
    public VkUserResponseWithPhoto() {
    }

    /**
    *
    * @param id
    * @param lastName
    * @param photo100
    * @param photoMax
    * @param firstName
    * @param domain
    */
    public VkUserResponseWithPhoto(Integer id, String firstName, String lastName, String photo100, String photoMax, String domain) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo100 = photo100;
        this.photoMax = photoMax;
        this.domain = domain;
    }

    public Integer getId() {
    return id;
    }

    public void setId(Integer id) {
    this.id = id;
    }

    public String getFirstName() {
    return firstName;
    }

    public void setFirstName(String firstName) {
    this.firstName = firstName;
    }

    public String getLastName() {
    return lastName;
    }

    public void setLastName(String lastName) {
    this.lastName = lastName;
    }

    public String getPhoto100() {
    return photo100;
    }

    public void setPhoto100(String photo100) {
    this.photo100 = photo100;
    }

    public String getPhotoMax() {
    return photoMax;
    }

    public void setPhotoMax(String photoMax) {
    this.photoMax = photoMax;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}