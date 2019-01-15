package com.stardust.autojs.runtime.api;

import android.util.Log;

import com.stardust.autojs.core.eventloop.EventEmitter;
import com.stardust.autojs.core.looper.Loopers;
import com.stardust.autojs.runtime.ScriptBridges;
import com.stardust.autojs.runtime.ScriptRuntime;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocket extends EventEmitter implements Loopers.LooperQuitHandler {
    private final String TAG = "WebSocket";
    OkHttpClient client;
    okhttp3.WebSocket webSocket;

    public WebSocket(ScriptRuntime scriptRuntime) {
        super(scriptRuntime.bridges);
        scriptRuntime.loopers.addLooperQuiteHandler(this);
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public WebSocketEventEmitter start(String wsUrl) {
        WebSocketEventEmitter emitter = new WebSocketEventEmitter(mBridges);

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(okhttp3.WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                emitter.change(new Object[]{"onOpen", response.message()});
            }

            @Override
            public void onMessage(okhttp3.WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                emitter.change(new Object[]{"onMessage", text});
            }

            @Override
            public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                emitter.change(new Object[]{"onMessage", ""});
            }

            @Override
            public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                emitter.change(new Object[]{"onClosing", reason});

            }

            @Override
            public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                emitter.change(new Object[]{"onClosed", reason});


            }

            @Override
            public void onFailure(okhttp3.WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                emitter.change(new Object[]{"onFailure", t.getMessage()});

            }
        });
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
        return emitter;
    }

    public void sendMessage(String msg) {
        webSocket.send(msg);
    }

    public class WebSocketEventEmitter extends EventEmitter {
        public WebSocketEventEmitter(ScriptBridges bridges) {
            super(bridges);
        }

        public void change(Object[] objects) {
            System.out.println();
            Log.d(TAG, "change:" + Arrays.toString(objects));
            emit("change", objects);
        }
    }

    @Override
    public boolean shouldQuit() {
        return false;
    }


    public void stop() {
        webSocket.close(-200, "exit");
    }
}
