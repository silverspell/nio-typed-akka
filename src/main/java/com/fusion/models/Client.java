package com.fusion.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.nio.channels.SocketChannel;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Client implements Serializable {

    private String ID;
    private SocketChannel channel;

}
