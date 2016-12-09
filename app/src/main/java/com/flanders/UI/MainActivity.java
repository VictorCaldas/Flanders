package com.flanders.UI;


import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;


import com.flanders.API.Rotas;
import com.flanders.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ENCODE = "UTF-8";
    private static final int REQUEST_RESOLVE_ERROR = 1;
    private EditText mEditText;
    private TextInputLayout mTextInputLayout;
    private MessageListAdapter mAdapter;
    private List<ChatMessage> mMessageList = new ArrayList<>();
    private boolean mResolvingError = false;
    private GoogleApiClient mGoogleApiClient;
    private NearbyConnectionCallbacks mConnectionCallbacks = new NearbyConnectionCallbacks();
    private NearbyConnectionFailedListener mFailedListener = new NearbyConnectionFailedListener();
    private Strategy mStrategy = new Strategy.Builder()
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT)
            .setDistanceType(Strategy.DISTANCE_TYPE_DEFAULT)
            .setTtlSeconds(Strategy.TTL_SECONDS_DEFAULT)
            .build();
    public ListView listview;
    private Message mActiveMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createGoogleApiClient();
        setupView();
    }

    public void setupView(){
        mAdapter = new MessageListAdapter(this, mMessageList);
        listview = (ListView) findViewById(R.id.list_view);
        listview.setAdapter(mAdapter);
    }

    private void createGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mFailedListener)
                .build();
    }

    private MessageListener mMessageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            if (message != null) {
                String json;
                try {
                    json = new String(message.getContent(), ENCODE);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.toString());
                    return;
                }
                addNewMessage(ChatMessage.fromJson(json));
            }
        }

        @Override
        public void onLost(Message message) {
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Lost sight of message: " + messageAsString);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void publish(String message) {
        Log.i(TAG, "Publishing message: " + message);
        mActiveMessage = new Message(message.getBytes());
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener);
            } else {
                Log.d(TAG, "Failed to resolve error with code " + resultCode);
            }
        }
    }

    /*private void sendMessage() {
        if (getCurrentFocus() != null) {
            // hide ime
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        byte[] content;
        try {
            content = chatMessage.toString().getBytes(ENCODE);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.toString());
            return;
        }
        Message message = new Message(content, ChatMessage.TYPE_USER_CHAT);

        Nearby.Messages.publish(mGoogleApiClient, message, mStrategy)
                .setResultCallback(new ResultCallback<Status>(){
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mTextInputLayout.setHint(getString(R.string.hint_input));
                            addNewMessage(chatMessage);
                        } else {
                            if (status.hasResolution()) {
                                handleStartSolution(status);
                            } else {
                                Log.e(TAG, "sendMessage failed." + status.toString());
                            }
                        }
                    }
                });
    }
*/
    private void handleStartSolution(Status status) {
        if (!mResolvingError) {
            try {
                status.startResolutionForResult(MainActivity.this, REQUEST_RESOLVE_ERROR);
                mResolvingError = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void addNewMessage(final ChatMessage message) {
        if (mMessageList.contains(message)) return;
        mMessageList.add(message);
        Collections.sort(mMessageList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createRoute(makeJsonRoute(message));
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class NearbyConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            Nearby.Messages.getPermissionStatus(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener);
                    } else {
                        if (status.hasResolution()){
                            if (!mResolvingError){
                                handleStartSolution(status);
                            }
                        } else {
                            Log.e(TAG, "Mensagem Não Enviada");
                        }
                    }
                }
            });
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

    private void createRoute(JsonObject objProfile) {

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Content-Type", "application/json");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient())
                .setEndpoint(getResources().getString(R.string.url_api))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(requestInterceptor)
                .build();
        Rotas api = restAdapter.create(Rotas.class);
        api.postRoute(objProfile, new Callback<Response>() {
            @Override
            public void success(retrofit.client.Response result, Response response) {
                BufferedReader reader = null;
                String output = "";
                try {
                    reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private JsonObject makeJsonRoute(ChatMessage message){
        JsonObject objRoute = new JsonObject();
        objRoute.addProperty("latitude", message.getLat());
        objRoute.addProperty("longitude",message.getLon());
        objRoute.addProperty("time", message.getTimestamp());
        objRoute.addProperty("speed", message.getVel());
        objRoute.addProperty("rvc_name", "cubatao_one" );
        objRoute.addProperty("street_name", "Rua Cubatão");
        objRoute.addProperty("car_name", "Uno");

        return objRoute;
    }

    private class NearbyConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "connect failed.");
        }
    }
}