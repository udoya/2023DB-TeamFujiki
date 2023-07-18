package sample;

import java.io.File;
import java.util.Optional;
import com.scalar.db.api.Result;

import classes.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

public class AppServer {
    static ScalarOperations scalar;
    public static int member;

    public void start(SocketIOServer server) throws InterruptedException {

        System.out.println("test");

        // TODO: if connect -> member+; if disconnect -> member--;

        server.addEventListener("INIT_STATE", InitStateRequest.class, new DataListener<InitStateRequest>() {
            @Override
            public void onData(SocketIOClient client, InitStateRequest data, AckRequest ackRequest) {
                // scalar.getUserInfo(data.user_name);
                // broadcastじゃない
                // server.getBroadcastOperations().sendEvent("INIT_STATE", data);
                client.sendEvent("INIT_STATE", data);
            }
        });
        server.addEventListener("RAISE_HAND", RaiseHandsRequest.class, new DataListener<RaiseHandsRequest>() {
            @Override
            public void onData(SocketIOClient client, RaiseHandsRequest data, AckRequest ackRequest) {
                // server.getBroadcastOperations().sendEvent("RAISE_HAND", data);
            }
        });
        server.addEventListener("BID-ON", BidOnRequest.class, new DataListener<BidOnRequest>() {
            @Override
            public void onData(SocketIOClient client, BidOnRequest data, AckRequest ackRequest) {
                // server.getBroadcastOperations().sendEvent("BID-ON", data);
            }
        });
        server.addEventListener("SUCCESSFUL_BID", SuccessfulBidResponse.class,
                new DataListener<SuccessfulBidResponse>() {
                    @Override
                    public void onData(SocketIOClient client, SuccessfulBidResponse data, AckRequest ackRequest) {
                        // server.getBroadcastOperations().sendEvent("SUCCESSFUL_BID", data);
                    }
                });
        server.addEventListener("NOTIFY_NUM_OF_PARTICIPANTS", NotifyNumOfParticipantsResponse.class,
                new DataListener<NotifyNumOfParticipantsResponse>() {
                    @Override
                    public void onData(SocketIOClient client, NotifyNumOfParticipantsResponse data,
                            AckRequest ackRequest) {
                        // server.getBroadcastOperations().sendEvent("NOTIFY_NUM_OF_PARTICIPANTS",
                        // data);
                    }
                });

        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}