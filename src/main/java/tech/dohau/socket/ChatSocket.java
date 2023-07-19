package tech.dohau.socket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value="/chat/{username}")
@ApplicationScoped
public class ChatSocket {

    private final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username){

        sessionMap.put(username, session);
        sendMessage(String.format("User %s logged in successfully", username));
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username){

        sessionMap.remove(username);
        sendMessage(String.format("User %s logged out successfully", username));
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        sessionMap.remove(username);
        throwable.printStackTrace();
        sendMessage(String.format("User %s logged out because of an error", username));
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username){
        sendMessage(String.format(" >> %s -> %s", username, message));
    }

    public void sendMessage(String message){
        sessionMap.values().forEach(it -> it.getAsyncRemote().sendObject(message,sendResult -> {if(sendResult.getException() != null) {
            sendResult.getException().printStackTrace();
        }
        }));
    }
}
