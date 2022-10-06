package ru.netology;

import javax.sound.midi.MetaMessage;
import java.lang.reflect.Method;

public class Request {
    private String method;
    private String path;
    private String version;

    Request(String requestLine) {
        final var parts = requestLine.split(" ");
        this.method = parts[0];
        this.path = parts[1];
        this.version = parts[2];
    }
    public String getMethod(){
        return method;
    }
    public String getPath(){
        return path;
    }

    public String getVersion(){
        return version;
    }
}
