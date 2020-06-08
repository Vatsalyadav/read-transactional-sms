package com.vatsalyadav.apps.readsms.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vatsalyadav.apps.readsms.AppExecutors;
import com.vatsalyadav.apps.readsms.model.Message;
import com.vatsalyadav.apps.readsms.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.annotations.NonNull;

public class MessageRepository {

    private MutableLiveData<List<Message>> messagesList = new MutableLiveData<>();

    private static MessageRepository instance;
    private static DatabaseReference databaseReference;

    public static MessageRepository getInstance() {
        if (instance == null) {
            instance = new MessageRepository();
            setupFirebase();
        }
        return instance;
    }

    private static void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("transactionalMessages");
    }

    public void insertMessage(Message message) {
        databaseReference.child(message.getDate() + "_" + message.getAddress()).setValue(message);
    }

    public void fetchMessagesList() {
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Message> messageListFromServer = new ArrayList<>();
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        messageListFromServer.clear();
                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            Message message = messageSnapshot.getValue(Message.class);
                            messageListFromServer.add(message);
                        }
                        messagesList.postValue(messageListFromServer);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        messagesList.postValue(null);
                    }
                });
            }
        });
    }

    public LiveData<List<Message>> getMessagesList() {
        return messagesList;
    }

    public void readLastSevenDaysMessages(Context context) {
        ContentResolver cr = context.getContentResolver();
        Pattern amountPatter = Pattern.compile(Utils.AMOUNT_PATTERN);
        Message newMessage = new Message();
        String amount = "";
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, new String[]{Telephony.Sms.DATE, Telephony.Sms.ADDRESS, Telephony.Sms.BODY},
                Utils.filterOptions(), new String[]{Utils.TRANSACTIONAL_MESSAGE_ADDRESS_PATTERN}, null);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    Matcher m = amountPatter.matcher(body);
                    amount = "";
                    while (m.find()) {
                        amount = m.group(1);
                    }
                    c.moveToNext();
                    if (amount != null && amount.length() != 0) {
                        newMessage.setAddress(number);
                        newMessage.setBody(amount);
                        newMessage.setDate(smsDate);
                        insertMessage(newMessage);
                    }
                }
            }
            c.close();
        } else {
            Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show();
        }
    }
}
