package com.lotus.demo.util.socket;

import android.util.Log;

import com.lotus.demo.constant.Constants;
import com.lotus.demo.constant.SocketEvents;
import com.lotus.demo.data.AppData;
import com.lotus.demo.model.Client;
import com.lotus.demo.util.api.APIClient;
import com.lotus.demo.util.api.APIInterface;
import com.lotus.demo.util.event.CallAnswerEvent;
import com.lotus.demo.util.event.CallCancelEvent;
import com.lotus.demo.util.event.CallFinishEvent;
import com.lotus.demo.util.event.CallMakeEvent;
import com.lotus.demo.util.event.CallDisconnectEvent;
import com.lotus.demo.util.event.CallIceCandidateEvent;
import com.lotus.demo.util.event.CallOfferEvent;
import com.lotus.demo.util.event.OfflineEvent;
import com.lotus.demo.util.event.OnlineEvent;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocketUtils {

    private static final String TAG = "SOCKET";

    private static Socket socket;

    private static APIInterface apiInterface;

    static {
        apiInterface = APIClient.getClient().create(APIInterface.class);
    }

    public static void connectToSignallingServer() {
        try {
            socket = IO.socket(Constants.BASE_URL);
            socket.on(SocketEvents.CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CONNECT);
                    Call<Client> call = apiInterface.getClientID(socket.id());
                    call.enqueue(new Callback<Client>() {
                        @Override
                        public void onResponse(Call<Client> call, Response<Client> response) {
                            Client user = response.body();
                            AppData.currentUser = user;
                            AppData.isOnline = true;
                            EventBus.getDefault().post(new OnlineEvent());
                        }

                        @Override
                        public void onFailure(Call<Client> call, Throwable t) {
                            AppData.currentUser = null;
                            AppData.isOnline = false;
                            call.cancel();
                        }
                    });
                }
            });
            socket.on(SocketEvents.CALL_MAKE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CALL_MAKE);
                    String from = null;
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        from = obj.getString("from");
                    } catch (JSONException e) {
                        from = null;
                    }
                    if (!StringUtils.isEmpty(from)) {
                        EventBus.getDefault().post(new CallMakeEvent(from));
                    }
                }
            });

            socket.on(SocketEvents.CALL_FINISH, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CALL_FINISH);
                    String from = null;
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        from = obj.getString("from");
                    } catch (JSONException e) {
                        from = null;
                    }
                    if (!StringUtils.isEmpty(from)) {
                        EventBus.getDefault().post(new CallFinishEvent(from));
                    }
                }
            });


            socket.on(SocketEvents.CALL_CANCEL, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CALL_CANCEL);
                    String from = null;
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        from = obj.getString("from");
                    } catch (JSONException e) {
                        from = null;
                    }
                    if (!StringUtils.isEmpty(from)) {
                        EventBus.getDefault().post(new CallCancelEvent(from));
                    }
                }
            });

            socket.on(SocketEvents.CALL_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CALL_CANCEL);
                    String from = null;
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        from = obj.getString("from");
                    } catch (JSONException e) {
                        from = null;
                    }
                    if (!StringUtils.isEmpty(from)) {
                        EventBus.getDefault().post(new CallDisconnectEvent(from));
                    }
                }
            });

            socket.on(SocketEvents.CALL_OFFER, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CALL_OFFER);
                    String sdb = null;
                    String from = null;
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        from = obj.getString("from");
                        JSONObject offer = obj.getJSONObject("offer");
                        if (offer != null) {
                            sdb = offer.getString("sdp");
                        }
                    } catch (JSONException e) {
                        sdb = null;
                        from = null;
                    }
                    EventBus.getDefault().post(new CallOfferEvent(from, sdb));
                }
            });

            socket.on(SocketEvents.CALL_ANSWER, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CALL_ANSWER);
                    String sdb = null;
                    String from = null;
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        from = obj.getString("from");
                        JSONObject answer = obj.getJSONObject("answer");
                        if (answer != null) {
                            sdb = answer.getString("sdp");
                        }
                    } catch (JSONException e) {
                        sdb = null;
                        from = null;
                    }
                    EventBus.getDefault().post(new CallAnswerEvent(from, sdb));
                }
            });

            socket.on(SocketEvents.CALL_ICE_CANDIDATE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, SocketEvents.CALL_ICE_CANDIDATE);
                    IceCandidate iceCandidate = null;
                    String from = null;
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        from = obj.getString("from");
                        JSONObject candidateObj = obj.getJSONObject("candidate");
                        if (candidateObj != null) {
                            iceCandidate = new IceCandidate(candidateObj.getString("sdpMid"),
                                    candidateObj.getInt("sdpMLineIndex"), candidateObj.getString("sdp"));
                        }
                    } catch (JSONException e) {
                        iceCandidate = null;
                        from = null;
                    }
                    EventBus.getDefault().post(new CallIceCandidateEvent(from, iceCandidate));
                }
            });

            socket.on(SocketEvents.CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, SocketEvents.CONNECT_ERROR);
                    AppData.currentUser = null;
                    AppData.isOnline = false;
                    EventBus.getDefault().post(new OfflineEvent());
                }
            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void emit(final String event, final Object... args) {
        if (socket != null && socket.connected()) {
            socket.emit(event, args);
        }
    }

    public static void disconnect() {
        if (socket != null) {
//            sendMessage("bye");
            socket.disconnect();
        }
    }
}
