package com.chainedminds.telegram.api;

import com.chainedminds._Codes;
import com.chainedminds.api._API;
import com.chainedminds.models._FileData;
import com.chainedminds.telegram.api.models._TelegramData;
import com.chainedminds.utilities.SocketPool;
import com.chainedminds.utilities.json.Json;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class _TelegramAPI extends _API {

    public static _TelegramAPI INSTANCE;
    public static final SocketPool MESSAGE_POOL = new SocketPool("engine-telegram.chainedminds.com", 4495, 1);
    public static final SocketPool FILE_POOL = new SocketPool("engine-telegram.chainedminds.com", 4495, 1);
    public static final ExecutorService ASYNC_POOL_EXECUTOR = Executors.newCachedThreadPool();

    public static synchronized _TelegramAPI get() {

        if (INSTANCE == null) {

            INSTANCE = new _TelegramAPI();
        }

        return INSTANCE;
    }

    public static void config(int id, String credential, String appName, String language) {

        _TelegramData.accountID = id;
        _TelegramData.accountCredential = credential;
        _TelegramData.clientAppName = appName;
        _TelegramData.clientLanguage = language;
    }

    public void call(_TelegramData request, boolean async, ApiCallback callback) {

        callPool(request, async, callback);
    }

    public void callHttps(_TelegramData request, boolean async, ApiCallback callback) {
        String requestJson = Json.getString(request);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(requestJson, mediaType);

        Request.Builder builder = new Request.Builder();
        builder.url("https://api-telegram.chainedminds.com/v2/");
        builder.post(body);

        call(builder, async, callback);
    }

    public void callPool(_TelegramData request, boolean async, ApiCallback callback) {

        if (async) {

            ASYNC_POOL_EXECUTOR.execute(() -> {

                byte[] requestBytes = Json.getBytes(request);

                byte[] responseBytes = SocketPool.transfer(MESSAGE_POOL, requestBytes);

                if (callback != null) {

                    if (responseBytes != null) {

                        String responseString = new String(responseBytes);

                        callback.onResponse(200, responseString);
                        callback.onResponse(200, (Map<String, List<String>>) null, responseString);
                        callback.onResponse(200, (Headers) null, responseString);

                    } else {

                        callback.onError(new RuntimeException("Unknown error"));
                        callback.onError("Unknown error", "Unknown error");
                        callback.onError("Unknown error", new RuntimeException("Unknown error"));
                    }
                }
            });

        } else {

            byte[] requestBytes = Json.getBytes(request);

            byte[] responseBytes = SocketPool.transfer(MESSAGE_POOL, requestBytes);

            if (callback != null) {

                if (responseBytes != null) {

                    String responseString = new String(responseBytes);

                    callback.onResponse(200, responseString);
                    callback.onResponse(200, (Map<String, List<String>>) null, responseString);
                    callback.onResponse(200, (Headers) null, responseString);

                } else {

                    callback.onError(new RuntimeException("Unknown error"));
                    callback.onError("Unknown error", "Unknown error");
                    callback.onError("Unknown error", new RuntimeException("Unknown error"));
                }
            }
        }
    }

    public static boolean upload(File file) {

        AtomicBoolean wasSuccessful = new AtomicBoolean(false);

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));

        Request.Builder builder = new Request.Builder();
        builder.url("https://upload-telegram.chainedminds.com/" + file.getName());
        builder.post(fileBody);

        _API.instance().call(builder, false, new ApiCallback() {
            @Override
            public void onError(String error, String message) {

                System.err.println("TelegramAPI upload " + error + " : " + message);
            }

            @Override
            public void onResponse(int code, Map<String, List<String>> headers, String response) {

                if (code != 200) {

                    throw new RuntimeException("Response code : " + code);
                }

                _TelegramData responseData = Json.getObject(response, _TelegramData.class);

                if (responseData != null && responseData.response == _Codes.RESPONSE_OK) {

                    wasSuccessful.set(true);

                } else {

                    throw new RuntimeException("Response  : " + response);
                }
            }
        });

        return wasSuccessful.get();
    }

    public void sendMessage(long chatID, String message) {

        sendMessage(chatID, List.of(message), null);
    }

    public void sendMessage(long chatID, File file) {

        sendMessage(chatID, (List<String>) null, List.of(file));
    }

    public void sendMessage(long chatID, String message, File file) {

        sendMessage(chatID, List.of(message), List.of(file));
    }

    public void sendMessage(long chatID, String message, List<File> files) {

        sendMessage(chatID, List.of(message), files);
    }

    public void sendMessage(long chatID, List<String> messages, List<File> files) {

        List<_FileData> fileDataList = null;

        if (files != null) {

            fileDataList = new ArrayList<>();

            for (File file : files) {

                if (upload(file)) {

                    _FileData fileData = new _FileData();
                    fileData.name = file.getName();

                    fileDataList.add(fileData);

                } else {

                    return;
                }
            }
        }

        _TelegramData requestData = new _TelegramData();
        requestData.request = 1009;
        requestData.subRequest = 2000;
        requestData.chatID = chatID;
        requestData.messages = messages;
        requestData.files = fileDataList;

        Socket socket = FILE_POOL.connect();

        call(requestData, true, new ApiCallback() {
            @Override
            public void onError(String error, String message) {

                System.err.println("TelegramAPI sendMessage " + error + " : " + message);
            }

            @Override
            public void onResponse(int code, String response) {

                if (code != 200) {

                    throw new RuntimeException("Response code : " + code);
                }

                _TelegramData responseData = Json.getObject(response, _TelegramData.class);

                if (responseData != null && responseData.response == _Codes.RESPONSE_OK) {

                    //System.out.println(responseData.response);

                } else {

                    throw new RuntimeException("Response  : " + response);
                }
            }
        });
    }
}