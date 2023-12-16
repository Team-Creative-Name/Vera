package com.tcn.vera.exceptions;

public class SettingNotFoundException extends RuntimeException {
    public SettingNotFoundException(String settingName){
        super("\"" + settingName + "\" is not a setting that exists. Either check the spelling or create a new setting with this name");
    }
}
