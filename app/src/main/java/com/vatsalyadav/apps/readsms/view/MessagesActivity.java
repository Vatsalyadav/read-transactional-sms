package com.vatsalyadav.apps.readsms.view;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vatsalyadav.apps.readsms.R;
import com.vatsalyadav.apps.readsms.SmsBroadcastReceiver;
import com.vatsalyadav.apps.readsms.adapter.MessageListAdapter;
import com.vatsalyadav.apps.readsms.model.Message;
import com.vatsalyadav.apps.readsms.viewmodel.MessageActivityViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MessagesActivity extends AppCompatActivity implements SmsBroadcastReceiver.Listener {

    private static final int SMS_PERMISSION_CODE = 1000;
    private MessageActivityViewModel messageActivityViewModel;
    private MessageListAdapter adapter;
    private SmsBroadcastReceiver smsBroadcastReceiver;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        progressBar = findViewById(R.id.progress_bar);
        setupViewModel();
        getSMSReadPermission();
        registerSmsBroadcastReceiver();
        setupRecyclerView();
        backupLastSevenDayMessages();
        getMessagesList();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new MessageListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViewModel() {
        messageActivityViewModel = new ViewModelProvider(this).get(MessageActivityViewModel.class);
        messageActivityViewModel.init();
    }

    private void registerSmsBroadcastReceiver() {
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        smsBroadcastReceiver.setListener(this);
        IntentFilter smsReceivedIntent = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        smsReceivedIntent.setPriority(999);
        registerReceiver(smsBroadcastReceiver, smsReceivedIntent);
    }

    private void backupLastSevenDayMessages() {
        messageActivityViewModel.getSmsReadPermissions().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean permissionState) {
                if (permissionState) {
                    messageActivityViewModel.backupMessages();
                }
            }
        });
    }

    private void getMessagesList() {
        messageActivityViewModel.getMessageList().observe(this, messages -> {
            // Update the cached copy of the words in the adapter.
            progressBar.setVisibility(View.GONE);
            adapter.setMessages(messages);
        });
    }

    private void getSMSReadPermission() {
        if (!isSmsPermissionGranted()) {
            requestReadSmsPermission();
        } else {
            messageActivityViewModel.setSmsReadPermissions(true);
        }
    }

    /**
     * Check if we have SMS permission
     */
    public boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request runtime SMS permission
     */
    private void requestReadSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SMS_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                    messageActivityViewModel.setSmsReadPermissions(true);
                } else {
                    // permission was denied
                    Toast.makeText(this, "SMS Read Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsBroadcastReceiver);
    }

    @Override
    public void onTextReceived(Message newMessage) {
        messageActivityViewModel.insert(newMessage);
    }
}