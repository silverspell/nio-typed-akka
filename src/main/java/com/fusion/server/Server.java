package com.fusion.server;

import akka.actor.typed.ActorSystem;
import com.fusion.actors.ManagerActor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private final ActorSystem<ManagerActor.Command> actorSystem;

    public Server() throws Exception {
        actorSystem = ActorSystem.create(ManagerActor.create(), "actorSystem");
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(5454), 1000);
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    register(actorSystem, selector, serverSocket);
                }

                if (key.isReadable()) {
                    answerWithEcho(actorSystem, key);
                }

                iter.remove();
            }
        }
    }


    private static void answerWithEcho(ActorSystem<ManagerActor.Command> actorSystem, SelectionKey key) throws IOException {
        //System.out.println("Got message");
        SocketChannel connection = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        connection.read(buffer);
        actorSystem.tell(new ManagerActor.MessageCommand(connection, buffer.flip().array()));
        buffer.clear();
    }

    private static void register(ActorSystem<ManagerActor.Command> actorSystem, Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel socketConn = serverSocket.accept();
        socketConn.configureBlocking(false);
        socketConn.register(selector, SelectionKey.OP_READ);
        actorSystem.tell(new ManagerActor.RegisterCommand(socketConn));
    }

}
