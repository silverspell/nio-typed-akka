package com.fusion.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fusion.models.Client;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManagerActor extends AbstractBehavior<ManagerActor.Command> {

    private static ConcurrentHashMap<SocketChannel, Client> clientMap;

    private ManagerActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        clientMap = new ConcurrentHashMap<>();
        return Behaviors.setup(ManagerActor::new);
    }

    public interface Command extends Serializable {}

    @Getter
    @AllArgsConstructor
    public static class RegisterCommand implements Command {
        private SocketChannel channel;
    }

    @Getter
    @AllArgsConstructor
    public static class QuitCommand implements Command {
        private SocketChannel channel;
    }

    @Getter
    @AllArgsConstructor
    public static class MessageCommand implements Command {
        private SocketChannel channel;
        private byte[] message;

    }

    @Getter
    @AllArgsConstructor
    public static class ReplyCommand implements Command {
        private ActorRef<WorkerActor.Command> sender;
        private Client client;
        private String message;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RegisterCommand.class, command -> {
                    System.out.println("Got Register ");
                    Client client = Client
                                        .builder()
                                        .ID(UUID.randomUUID().toString())
                                        .channel(command.getChannel())
                                        .build();
                    clientMap.put(command.channel, client);
                    return Behaviors.same();
                })
                .onMessage(QuitCommand.class, command -> {
                    System.out.println("Got Quit");
                    command.getChannel().close();
                    clientMap.remove(command.getChannel());
                    return Behaviors.same();
                })
                .onMessage(MessageCommand.class, command -> {
                    System.out.println("Got Message");
                    String message = new String(command.getMessage()).trim();

                    if (message.equals("QUIT")) {
                        getContext().getSelf().tell(new QuitCommand(command.getChannel()));
                        return Behaviors.same();
                    }
                    Client client = clientMap.get(command.getChannel());
                    ActorRef<WorkerActor.Command> worker = getContext().spawn(WorkerActor.create(), "Worker" + new Random().nextInt(100));
                    worker.tell(new WorkerActor.MessageCommand(getContext().getSelf(), client, message));
                    return Behaviors.same();
                })
                .onMessage(ReplyCommand.class, command -> {
                    command.getClient().getChannel().write(ByteBuffer.wrap(command.getMessage().getBytes()));
                    getContext().stop(command.getSender());
                    return Behaviors.same();
                })
                .build();
    }



}
