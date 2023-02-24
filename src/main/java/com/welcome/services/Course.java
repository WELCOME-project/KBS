package com.welcome.services;

public class Course {

  private String courseName;
  private String hasHours;

  public Course(String courseName, String hasHours) {
    super();
    this.courseName = courseName;
    this.hasHours = hasHours;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public String getHasHours() {
    return hasHours;
  }

  public void setHasHours(String hasHours) {
    this.hasHours = hasHours;
  }

}
