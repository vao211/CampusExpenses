package com.example.vcampusexpenses.datamanager;

import android.content.Context;

import com.example.vcampusexpenses.model.Setting;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SettingDataManager {
    private static final String SETTINGS_FILE = "settings.json";
    private final Context context;
    private final File file;
    private Setting setting;

    public SettingDataManager(Context context) {
        this.context = context;
        this.file = new File(context.getFilesDir(), SETTINGS_FILE);
        initializeFile();
    }

    private void initializeFile() {
        if (!file.exists()) {
            setting = new Setting("Guest", "VND", true);
            saveData();
        } else {
            loadData();
        }
    }

    public synchronized void loadData() {
        if (!file.exists()) {
            DisplayToast.Display(context, "No data to load");
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(reader);
            if (!jsonElement.isJsonObject()) {
                DisplayToast.Display(context, "Invalid JSON format in file");
                return;
            }

            setting = gson.fromJson(jsonElement, Setting.class);

            if (setting == null) {
                DisplayToast.Display(context, "Failed to load setting data: JSON is invalid");
                setting = new Setting("Guest", "VND", true);
                saveData();
            }
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            DisplayToast.Display(context, "Load setting data error: " + e.getMessage());
            setting = new Setting("Guest", "VND", true);
            saveData();
        }
    }

    public synchronized void saveData() {
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(setting, writer);
            writer.flush(); // Ensure data is written
        } catch (IOException e) {
            DisplayToast.Display(context, "Save data error: " + e.getMessage());
        }
    }

    public Setting getSetting() {
        return setting;
    }

    public Context getContext() {
        return context;
    }
}