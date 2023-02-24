package com.welcome.services;

public class BoundingBox {

  private String lat1;
  private String lat2;
  private String lon1;
  private String lon2;

  public BoundingBox() {};

  public BoundingBox(String lon1, String lat1, String lon2, String lat2) {
    this.lon1 = lon1;
    this.lat1 = lat1;
    this.lon2 = lon2;
    this.lat2 = lat2;
  }

  public String getLat1() {
    return lat1;
  }

  public void setLat1(String lat1) {
    this.lat1 = lat1;
  }

  public String getLat2() {
    return lat2;
  }

  public void setLat2(String lat2) {
    this.lat2 = lat2;
  }

  public String getLon1() {
    return lon1;
  }

  public void setLon1(String lon1) {
    this.lon1 = lon1;
  }

  public String getLon2() {
    return lon2;
  }

  public void setLon2(String lon2) {
    this.lon2 = lon2;
  };


}
