package com.example.vcampusexpenses.services;

import android.content.Context;
import com.example.vcampusexpenses.datamanager.SettingDataManager;
import com.example.vcampusexpenses.model.Setting;
import com.example.vcampusexpenses.utils.DisplayToast;
import java.util.Arrays;

public class SettingService {
    private final SettingDataManager settingFile;
    private final Setting setting;

    public SettingService(Context context) {
        this.settingFile = new SettingDataManager(context);
        this.setting = settingFile.getSetting();
    }

    public String getDisplayName() {
        return setting.getDisplayName();
    }

    public String getCurrency() {
        return setting.getCurrency();
    }

    public boolean getNotification() {
        return setting.getNotification();
    }

    public void setDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            DisplayToast.Display(settingFile.getContext(), "Invalid real name");
            return;
        }
        setting.setDisplayName(displayName);
        settingFile.saveData();
    }

    public void setCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()){
            DisplayToast.Display(settingFile.getContext(), "Invalid currency");
            return;
        }
        setting.setCurrency(currency);
        settingFile.saveData();
    }

    public void setNotification(boolean notification) {
        setting.setNotification(notification);
        settingFile.saveData();
    }
}