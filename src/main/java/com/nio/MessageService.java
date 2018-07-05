package com.nio;

import com.nio.domain.IdMessage;
import com.nio.domain.Message;
import com.nio.domain.MessageFactory;
import com.nio.domain.MessageType;
import com.nio.util.SerializationUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  one loop per thread + thread pool
 */
public abstract class MessageService {

    private  final BlockingQueue<Message> messageQueue=new ArrayBlockingQueue<>(20);
     static final Map<String,MessageService> userChannel=new ConcurrentHashMap<>(); //  userName-Channel

    private  volatile AtomicLong sleepTime=new AtomicLong(Per_SLEEP_TIME);// init sleep 1 second
    private static final long Per_SLEEP_TIME=1000L;

    private ExecutorService servicePool=Executors.newWorkStealingPool();//cpu密集型

    private Selector selector;


    /**
     *对于client来说不需要考虑工作线程和接收线程
     */
    public MessageService(Selector selector){
       this.selector=selector;
    }

    private boolean doMessage(SocketChannel channel){
        ByteBuffer byteBuffer=ByteBuffer.allocate(4);
        int byteCount = 0;
        try {
            byteCount=  channel.read(byteBuffer);
            if(byteCount>0) {
                byteBuffer.flip();
                int bodyLen = byteBuffer.getInt();  // 文件长度
                byteBuffer = ByteBuffer.allocate(1);
                channel.read(byteBuffer);           // 类型
                byteBuffer.flip();
                byte messageType = byteBuffer.get();
                byteBuffer = ByteBuffer.allocate(bodyLen);
                byteBuffer.clear();
                int readIndex=0;
                while (true) {      //对于nio来说你必须保证业务层解包的完整性。因为每次最多 读tcp内核态缓冲区大小 cacheSize。
                    int count ;     // 所以 至少读 (bodyLen/cacheSize)次
                    if (readIndex<bodyLen) {
                        count = channel.read(byteBuffer);
                        if (count < 0) {
                            throw new IOException("异常客服端关闭");
                        }
                        readIndex+=count;
                    }else{
                        break;
                    }
                }
                byteBuffer.flip();
               final byte[] body = byteBuffer.array();
                servicePool.execute(()->{ //如果觉得性能(主要的单loop 的吞吐指标)存在问题会阻塞主IO线程 从这里开始的操作都放到业务线程池重处理
                    Class clazz = MessageFactory.getMessageClass(messageType);
                    if (clazz == null) {
                        throw new RuntimeException("未支持的消息类型");
                    }
                   Message message = (Message) SerializationUtils.deserialize(body, clazz);
                    if(message instanceof IdMessage){
                        userChannel.put(  message.getId(),this);
                    }else {
                        _doMessage(message);
                    }
                });

            }
        } catch (IOException e) {
           if(byteCount<=0){
               try {
                   channel.close();

               } catch (IOException e1) {
                   e1.printStackTrace();
               }
           }
        }
        return byteCount>0;
    }


    /**
     *
     todo  do something with your's business
     */
    protected  abstract   void _doMessage(Message message);


    private void sendMessage(SocketChannel channel){
        if(messageQueue.isEmpty()){
            try {
                Thread.sleep(sleepTime.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            Message message=messageQueue.poll();
            if(message instanceof IdMessage){   //在connect会触发这个动作
                //先保存一下channel
                userChannel.put(message.getId(),this);
            }
            _sendMessage(channel,message);
        }

    }

    private void _sendMessage(SocketChannel channel,Message message){
        byte[] body= SerializationUtils.serialize(message);
        ByteBuffer byteBuffer=ByteBuffer.allocate(4+1+body.length);
        byteBuffer.clear();
        byteBuffer.putInt(body.length);
        byteBuffer.put(MessageType.getMessageType(message));
        byteBuffer.put(body);
        byteBuffer.flip();
        try {
            while(byteBuffer.hasRemaining()){   //同样大文件发送也需要注意包的完整性。
                channel.write(byteBuffer);
            }
        } catch (IOException e) {
            System.out.println("发送消息失败"+e.getMessage());
            try {
                channel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     *
     //todo  是否可写 是否需要注册 在win下有select空轮训bug，在linux下bind到epoll上就没问题了
     solution
     https://www.zhihu.com/question/22840801

      首先 electionKey.OP_WRITE 表达的是缓冲区是否可以写 只要注册了并且可以写就会一直触发

        对于 LT 模式下的 OP_WRITE  只有在写 数据发生 写不进去的时候需要 注册 该事件或者内存中有想要发出去的消息
        则主动触发一下（比如read完一般都要write点什么给客服端）
        比如 缓冲区 256k ，然后你写了一个 10m 的 Byte[] 进去。就会返回一个 EAGAIN 那么你需要自己
        记录写了多少然后注册写事件再下次从该offset开始写这个Byte[] 。LT 每次消耗事件需要从新注册

        对于ET 来说 则不会一直触发。它只有在 空变成有。或者有变成空这两种状态各触发一次。
        （对于 空变成有它触发 epoll_out ,对于 有变成空 则是写返回 EAGAIN）
        所以 只要在开始的时候注册一次读写后面就不需要再注册 OP_WRITE ，OP_READ事件。
     */

    public void handleKey() throws IOException {

                for(;;){
                    try {
                        selector.select();
                        Set<SelectionKey> selectionKeys=selector.selectedKeys();
                        Iterator<SelectionKey> iterator=selectionKeys.iterator();
                        while (iterator.hasNext()){
                            SelectionKey key=iterator.next();
                            iterator.remove();
                            this._handleKey(key);
                        }
                    }catch (IOException e){
                        // ignore
                        selector.close();
                    }

                }
            }



    public void _handleKey(SelectionKey selectionKey)throws IOException {
        SocketChannel socketChannel;
        ServerSocketChannel serverSocketChannel;
        if (selectionKey.isAcceptable()) {
            serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            // one loop one thread begin
            Selector subSelector=Selector.open();
            socketChannel.register(subSelector,SelectionKey.OP_READ |SelectionKey.OP_WRITE);
            MessageService sub=new ServerMessageService(subSelector);
            sub.handleKey();
            //one loop one thread end

            //socketChannel.register(selector, SelectionKey.OP_READ |SelectionKey.OP_WRITE);
        }
        else if (selectionKey.isConnectable()) {
            System.out.println("client connect");
            socketChannel = (SocketChannel) selectionKey.channel();
            if(socketChannel.isConnectionPending()) { //握手完成
                socketChannel.finishConnect();

                int ops = selectionKey.interestOps();
                ops &= ~SelectionKey.OP_CONNECT;
                selectionKey.interestOps(ops);

                sendMessage(socketChannel);
                socketChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
            }
        }
        else if (selectionKey.isReadable()) {   //在读完注册写事件 是因为一般服务器都是回射性质的
            socketChannel = (SocketChannel) selectionKey.channel();
            if(!socketChannel.isConnected()){
                return;
            }
            boolean canRead = doMessage(socketChannel);
            if (canRead) {
                socketChannel.register(selector,  SelectionKey.OP_READ|SelectionKey.OP_WRITE);
            }
        }else if (selectionKey.isWritable()) {
            socketChannel = (SocketChannel) selectionKey.channel();
            if(!socketChannel.isConnected()){
                return;
            }
            sendMessage(socketChannel);
            socketChannel.register(selector, SelectionKey.OP_READ |SelectionKey.OP_WRITE);
        }


    }

    public Selector getSelector() {
        return selector;
    }

    public void addMessage(Message message){
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Set<String>  loginUser(){
        return userChannel.keySet();
    }

}
