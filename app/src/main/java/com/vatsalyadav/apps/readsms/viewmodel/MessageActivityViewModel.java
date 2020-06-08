package com.vatsalyadav.apps.readsms.viewmodel;

import android.app.Application;

import com.vatsalyadav.apps.readsms.model.Message;
import com.vatsalyadav.apps.readsms.repository.MessageRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MessageActivityViewModel extends AndroidViewModel {

    private MessageRepository mRepository;

    private LiveData<List<Message>> messageList;
    private Boolean backupState = false;
    private MutableLiveData<Boolean> smsReadPermissions = new MutableLiveData<>();

    public MessageActivityViewModel(Application application) {
        super(application);
    }

    public void init() {
        if (mRepository != null)
            return;
        mRepository = MessageRepository.getInstance();
        mRepository.fetchMessagesList();
        messageList = mRepository.getMessagesList();
    }

    public void backupMessages() {
        if (!backupState) {
            mRepository.readLastSevenDaysMessages(getApplication().getApplicationContext());
            backupState = true;
        }
    }

    public LiveData<List<Message>> getMessageList() {
        return messageList;
    }

    public void insert(Message message) {
        mRepository.insertMessage(message);
    }

    public void setSmsReadPermissions(Boolean smsReadPermissions) {
        this.smsReadPermissions.postValue(smsReadPermissions);
    }

    public LiveData<Boolean> getSmsReadPermissions() {
        return smsReadPermissions;
    }
}