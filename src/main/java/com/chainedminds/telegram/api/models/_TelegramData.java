package com.chainedminds.telegram.api.models;
import com.chainedminds.models._FileData;
import com.chainedminds.utilities.DynamicConfig;
import java.util.List;

public class _TelegramData {

    public static int accountID;
    public static String accountCredential;
    public static String clientAppName = "API";
    public static String clientLanguage = "en";

    public AccountData account;
    public ClientData client;

    public int request;
    public Integer subRequest;
    public int response;

    public Long chatID;

    public String message;
    public List<String> messages;

    public _FileData file;
    public List<_FileData> files;

    public static class AccountData {

        public int id = accountID;
        public String credential = accountCredential;
        public String username;
        public String password;
    }

    public static class ClientData {

        public String appName = clientAppName;
        public String platform = "API";
        public String version = "1.0.0";
        public String language = clientLanguage;
    }
}
