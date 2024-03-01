package com.example.appblockr.model;

public class AppData {
     private String appName;
     private String bundle_id;
     private String email;
     private String duration;
     private String clicksCount;
     private  boolean isAppLocked;



     public String getAppName() {
         return appName;
     }

     public String getBundle_id() {
         return bundle_id;
     }

     public String getEmail() {
         return email;
     }

     public String getDuration() {
         return duration;
     }

     public String getClicksCount() {
         return clicksCount;
     }

     public boolean getIsAppLocked() {
         return isAppLocked;
     }

     public void setAppName(String appName) {
         this.appName = appName;
     }

     public void setBundle_id(String bundle_id) {
         this.bundle_id = bundle_id;
     }

     public void setEmail(String email) {
         this.email = email;
     }

     public void setDuration(String duration) {
         this.duration = duration;
     }

     public void setClicksCount(String clicksCount) {
         this.clicksCount = clicksCount;
     }

     public void setIsAppLocked(boolean isAppLocked) {
         this.isAppLocked = isAppLocked;
     }
 }
