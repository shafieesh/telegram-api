package com.chainedminds.telegram.api.models;
import com.chainedminds.models._FileData;
import com.chainedminds.utilities.DynamicConfig;
import java.util.List;

public class _TelegramData {

    public final AccountData account = new AccountData();
    public final ClientData client = new ClientData();

    public int request;
    public Integer subRequest;
    public int response;

    public Long chatID;

    public String message;
    public List<String> messages;

    public _FileData file;
    public List<_FileData> files;

    public static class AccountData {

        public static int id = Integer.parseInt(DynamicConfig.getMap("API-Telegram-ID"));
        public static String credential = DynamicConfig.getMap("API-Telegram-Credential");
    }

    public static class ClientData {

        public static String appName = "TelegramAPI";
        public String platform = "API";
        public String version = "1.0.0";
        public static String language = "en";
    }
}
