package com.nio;

import com.nio.domain.Message;
import com.nio.domain.MessageFactory;
import com.nio.domain.MessageType;
import com.nio.domain.Request;
import com.nio.util.BitcoinInput;
import com.nio.util.BitcoinOutput;
import com.nio.util.SerializationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NioServer {

    private int port;

    public NioServer(int port)throws Exception{
        this.port=port;
        System.out.println("Server Start----8888:");

    }

    public void listen()throws IOException{
        Selector mainSelector;
        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open(); //打开服务器套接字通道
        serverSocketChannel.configureBlocking(false);               //服务器配置非阻塞

        ServerSocket serverSocket=serverSocketChannel.socket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));
        mainSelector=Selector.open();

        serverSocketChannel.register(mainSelector, SelectionKey.OP_ACCEPT);

        MessageService messageService=new ServerMessageService(mainSelector);
        messageService.handleKey();


    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        NioServer server = new NioServer(port);
        server.listen();
    }



}
