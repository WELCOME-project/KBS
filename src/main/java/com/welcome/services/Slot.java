package com.welcome.services;

import java.util.ArrayList;

public class Slot {

  private String opens;
  private String closes;
  private String skypeInfo;
  private ArrayList<String> days;
  private ArrayList<String> languages;

  public Slot(String opens, String closes, String skypeInfo, ArrayList<String> days,
      ArrayList<String> languages) {
    super();
    this.opens = opens;
    this.closes = closes;
    this.skypeInfo = skypeInfo;
    this.days = days;
    this.languages = languages;
  }

  public ArrayList<String> getLanguages() {
    return languages;
  }

  public void setLanguages(ArrayList<String> languages) {
    this.languages = languages;
  }

  public String getOpens() {
    return opens;
  }

  public void setOpens(String opens) {
    this.opens = opens;
  }

  public String getCloses() {
    return closes;
  }

  public void setCloses(String closes) {
    this.closes = closes;
  }

  public String getSkypeInfo() {
    return skypeInfo;
  }

  public void setSkypeInfo(String skypeInfo) {
    this.skypeInfo = skypeInfo;
  }

  public ArrayList<String> getDays() {
    return days;
  }

  public void setDays(ArrayList<String> days) {
    this.days = days;
  }


}
