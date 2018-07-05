package com.nio;

import com.nio.domain.Message;
import com.nio.domain.PicMessage;

import java.nio.channels.Selector;

public class ClientMessageService extends MessageService {


    public ClientMessageService(Selector mainSelector) {
        super(mainSelector);
    }

    @Override
    protected void _doMessage(Message message) {
            //todo 业务逻辑
        if(message instanceof PicMessage){
            System.out.println(((PicMessage) message).getPicUrl());
        }
    }
}
