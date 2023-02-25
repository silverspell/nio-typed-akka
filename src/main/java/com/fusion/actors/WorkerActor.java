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

import java.io.Serial;
import java.io.Serializable;

public class WorkerActor extends AbstractBehavior<WorkerActor.Command> {

    public interface Command extends Serializable {}



    @Getter
    @AllArgsConstructor
    public static class MessageCommand implements Command {
        @Serial private static final long serialVersionUID = 1L;
        private ActorRef<ManagerActor.Command> sender;
        private Client client;
        private String message;
    }


    private WorkerActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerActor::new);
    }
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(MessageCommand.class, (command) -> {
                    System.out.println("Message received " + command.getMessage());
                    command.sender.tell(new ManagerActor.ReplyCommand(getContext().getSelf(), command.getClient(), "OK\n"));
                    return Behaviors.same();
                })
                .build();
    }

}
