package com.nio;

import com.nio.domain.IdMessage;
import com.nio.domain.Message;
import com.nio.domain.PicMessage;
import com.nio.domain.StringMessage;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 对于客服端来说 不需要 eventLoopAccept,只需要 eventLoopOnReadOrWrite
 */
public class NioClient {




   private MessageService messageService;
   private InetSocketAddress remoteAddress=new InetSocketAddress("localhost", 8888);//default

    public NioClient(MessageService messageService) {
        this.messageService = messageService;
    }

    public void connect()throws IOException{
        SocketChannel socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(messageService.getSelector(), SelectionKey.OP_CONNECT);
        socketChannel.connect(remoteAddress);

        messageService.handleKey();



    }

    //todo GCQ 需要在收到登陆消息后调用这个添加一个 IdMessage 然后再调用 NioClient.connect
    public void addMessage(Message message){
            messageService.addMessage(message);
    }



    public static void main(String[] args)throws Exception{
        Selector selector=Selector.open();
        MessageService messageService = new ClientMessageService(selector);
        NioClient client = new NioClient(messageService);
        //测试发消息
        new Thread(() -> {
            IdMessage idMessage = new IdMessage();
            idMessage.setFrom("abc");
            messageService.addMessage(idMessage);
            try {
                client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        File file = new File("C:\\Users\\lenovo\\Desktop\\123.png");
        byte[] bytes = readFile(file);
        PicMessage picMessage = new PicMessage();
        picMessage.setPicName(file.getName());
        picMessage.setPicture(bytes);
        picMessage.setFrom("abc");
        picMessage.setTo("abc");
        messageService.addMessage(picMessage);
    }
    public static byte[] readFile(File file) {
        try {
            //创建一个字节输入流对象
            InputStream is = new FileInputStream(file);
            //根据文件大小来创建字节数组
            byte[] bytes = new byte[(int) file.length()];
            int len = is.read(bytes);//返回读取字节的长度
//            System.out.println("读取字节长度为：" + len);
//
//            System.out.println("读取的内容为： " + new String(bytes));//构建成字符串输出
            is.close();//关闭流
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}
