package com.tcn.vera.settings;

import com.tcn.vera.exceptions.SettingNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SettingsProvider {
    private static SettingsProvider instance = null;

    private final Map<String, String> settings;

    Logger logger = LoggerFactory.getLogger("Vera: Settings Provider");

    private SettingsProvider(Builder builder){
        //load in the default values
        this.settings = new HashMap<>(builder.defaultSettings);
        //load in anything saved in the config file
        try {
            loadSettings();
        } catch (FileNotFoundException e) {
            logger.warn("The config file does not exist. Creating a new one");
        }
        //save the settings to the config file
        saveSettings();
        //set the instance to this
        instance = this;
    }

    /**
     * Gets the builder for the SettingsProvider. If the instance has already been built, an IllegalStateException will be thrown.
     * @return The builder for the SettingsProvider
     */
    public static Builder getBuilder(){
        if(instance != null){
            throw new IllegalStateException("SettingsV2 has already been built. Please use SettingsV2.getInstance() to get the instance");
        }
        return new Builder();
    }

    /**
     * Gets the instance of the SettingsProvider. If the instance has not been built yet, an IllegalStateException will be thrown.
     * @return The instance of the SettingsProvider
     */
    public static SettingsProvider getInstance(){
        if(instance == null){
            throw new IllegalStateException("SettingsV2 has not been built yet. Please build it via SettingsV2.builder()");
        }
        return instance;
    }

    /**
     * Adds a setting to the settings file. If the setting already exists, an IllegalArgumentException will be thrown.
     * @param settingName The name of the setting that you wish to add.
     * @param value The value of the setting that you wish to add.
     */
    public void addSetting(String settingName, String value){
        if(settings.containsKey(settingName)){
            throw new IllegalArgumentException("The setting \"" + settingName + "\" already exists. Please use modifySetting to change the value of this setting");
        }

        settings.put(settingName, value);
        saveSettings();
    }

    /**
     * Modifies a setting in the settings file. If the setting does not exist, a SettingNotFoundException will be thrown.
     * The changes are saved to the config file immediately after being modified.
     * @param settingName The name of the setting that you wish to modify.
     * @param value The new value of the setting that you wish to modify.
     */
    public void modifySetting(String settingName, String value){
        if(!settings.containsKey(settingName)){
            throw new SettingNotFoundException(settingName);
        }

        settings.put(settingName, value);
        saveSettings();
    }

    /**
     * Gets the value of a setting. If the setting does not exist, a SettingNotFoundException will be thrown.
     * @param settingName The name of the setting that you wish to get the value of.
     * @return The value of the setting.
     */
    public String getSetting(String settingName){
        if(!settings.containsKey(settingName)){
            throw new SettingNotFoundException(settingName);
        }

        return settings.get(settingName);
    }

    /**
     * Deletes a setting from the settings object and file. If the setting does not exist, a SettingNotFoundException will be thrown.
     * @param settingName The name of the setting that you wish to delete.
     */
    public void deleteSetting(String settingName){
        if(!settings.containsKey(settingName)){
            throw new SettingNotFoundException(settingName);
        }

        settings.remove(settingName);
        saveSettings();
    }
    
    /**
     * Gets the directory that the settings file is stored in. This is OS dependent.
     * @return The directory that the settings file is stored in.
     */
    public String getSettingsDir(){

        String osName = System.getProperty("os.name").toLowerCase();
        String programName = getSetting("programName");

        if(osName.contains("win")){
            return System.getenv("APPDATA") + File.separator + programName + File.separator;
        }else if(osName.contains("mac")){
            return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + programName + File.separator;
        }else{
            return System.getProperty("user.home") + File.separator + "./config" + File.separator;
        }
    }

    /**
     * Gets the path to the settings file. This is OS dependent.
     * @return The path to the settings file.
     */
    public String getSettingsPath(){
        return getSettingsDir() + getSetting("programName") + ".cfg";
    }

    /**
     * Loads the settings from the config file. Any changes to the settings object that are not in the config file will be overwritten.
     * If the config file does not exist, a FileNotFoundException will be thrown.
     */
    public void loadSettings() throws FileNotFoundException {
        // Chuck to see if the file exists
        File configFile = new File(getSettingsPath());
        if (!configFile.exists()) {
            throw new FileNotFoundException("The config file does not exist. Please ensure that the config file exists and is not corrupted");
        }
        // Load the properties from the file
        try (InputStream inputStream = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((key, value) -> settings.put((String) key, (String) value));
        } catch (IOException e) {
            logger.error("There was an issue reading from the settings file. Please ensure that the config file exists and is not corrupted");
        }

    }

    /**
     * Saves the settings to the config file. Any changes to the file not in the settings object will be overwritten.
     * If a settings file does not exist, one will be created.
     */
    public void saveSettings(){

        // Create directories if they don't exist
        File directory = new File(getSettingsDir());
        if (!directory.exists() && (!directory.mkdirs())) {
                logger.error("Error while creating config directory. Settings cannot be saved");
                return;
        }

        // Check if the file exists, if not, create it
        File configFile = new File(getSettingsPath());

        try (OutputStream outputStream = new FileOutputStream(configFile)) {
            // Save properties to the file
            Properties properties = new Properties();
            properties.putAll(settings);
            properties.store(outputStream, "This is the config file for " + getSetting("programName") + ".");
        } catch (IOException e) {
            logger.error("There was an error saving the config file. Please ensure that the config file exists and is not corrupted");
        }

    }

    /**
     * Clears all settings in the settings object and then loads everything from the config file. If the config file does not exist, a FileNotFoundException will be thrown.
     */
    public void reloadSettings(){
        try {
            settings.clear();
            loadSettings();
        } catch (FileNotFoundException e) {
            logger.error("The config file does not exist. Please ensure that the config file exists and is not corrupted");
        }
    }


    //BUILDER CLASS
    public static final class Builder{
        private final Map<String, String> defaultSettings;

        private Builder(){
            defaultSettings = new HashMap<>();
        }

        /**
         * Adds a setting to the settings object and file. If a setting with the same name already exists, it will be overwritten.
         * @param settingName The name of the setting that you wish to add.
         * @param settingValue The value of the setting that you wish to add.
         * @return This builder.
         */
        public Builder addSetting(String settingName, String settingValue){
            defaultSettings.put(settingName, settingValue);
            return this;
        }

        /**
         * Sets the name of the program. This is used to determine the name of the config file and the directory that the config file is stored in.
         * This setting is required.
         * @param programName The name of the program.
         * @return This builder.
         */
        public Builder setProgramName(String programName){
            defaultSettings.put("programName", programName);
            return this;
        }

        /**
         * Builds the SettingsProvider. This method will throw an IllegalArgumentException if the SettingsProvider is not valid.
         * @return A new SettingsProvider object.
         */
        public SettingsProvider build(){
            runChecks();
            return new SettingsProvider(this);
        }

        private void runChecks(){
            if(defaultSettings.isEmpty()){
                throw new IllegalArgumentException("Cannot build SettingsProvider without any settings! Please ensure that there " +
                        "is at least one setting added via the SettingsProviderBuilder.addSetting method!");
            }

            if(!defaultSettings.containsKey("programName")){
                throw new IllegalArgumentException("The name of the program must be set via the SettingsProviderBuilder.setProgramName method");
            }
        }
    }
}
