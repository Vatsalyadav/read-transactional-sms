package com.vatsalyadav.apps.readsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.vatsalyadav.apps.readsms.model.Message;
import com.vatsalyadav.apps.readsms.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(Utils.AMOUNT_PATTERN);
    private Listener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Message newMessage = new Message();
            String amount = "";
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String smsAddress = smsMessage.getDisplayOriginatingAddress();
                boolean transactionMessageAddressMatch = Pattern.matches(Utils.TRANSACTIONAL_MESSAGE_ADDRESS_PATTERN, smsAddress);

                if (listener != null && transactionMessageAddressMatch) {
                    newMessage.setAddress(smsAddress);
                    newMessage.setDate(String.valueOf(smsMessage.getTimestampMillis()));
                    Matcher amountMatcher = AMOUNT_PATTERN.matcher(smsMessage.getMessageBody());
                    while (amountMatcher.find()) {
                        amount = amountMatcher.group();
                    }
                    newMessage.setBody(amount);
                    listener.onTextReceived(newMessage);
                }
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onTextReceived(Message newMessage);
    }
}
