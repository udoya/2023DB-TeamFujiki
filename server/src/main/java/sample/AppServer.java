package sample;

import java.io.File;
import java.util.Optional;
import com.scalar.db.api.Result;

import classes.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

public class AppServer {
    static ScalarOperations scalar;
    public static int member = 0;
    public static int time = 0;

    public static Map<String, Integer> userMap = new HashMap<>();

    public void start(SocketIOServer server) throws InterruptedException {
        userMap.put("Yamada", 1);
        userMap.put("Fujiki", 2);

        System.out.println("test");

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                // TODO member get from DB
                member++; // クライアント接続時にインスタンス変数を増やす
                System.out.println("Client connected. Current members: " + member);
                NotifyNumOfParticipantsResponse notify = new NotifyNumOfParticipantsResponse();
                notify.number = member;
                server.getBroadcastOperations().sendEvent("NOTIFY_NUM_OF_PARTICIPANTS",
                        notify);
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                // TODO member get from DB
                member--; // クライアント切断時にインスタンス変数を減らす
                System.out.println("Client disconnected. Current members: " + member);
                NotifyNumOfParticipantsResponse notify = new NotifyNumOfParticipantsResponse();
                notify.number = member;
                server.getBroadcastOperations().sendEvent("NOTIFY_NUM_OF_PARTICIPANTS",
                        notify);
            }
        });

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

                // TODO if succeed;
                // server.getBroadcastOperations().sendEvent("RAISE_HAND", data);
                RaiseHandsResponse r_hands = new RaiseHandsResponse();

                // TODO get from DB
                r_hands.item_id = 1;
                r_hands.item_name = "aa";
                r_hands.user_id = 1;

                time = 120;
                ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);
                execService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (time > 0) {
                            time--;
                        } else {
                            SuccessfulBidResponse success_bid = new SuccessfulBidResponse();

                            // TODO get from DB
                            success_bid.price = 100;
                            success_bid.user_id = 1;

                            server.getBroadcastOperations().sendEvent("SUCCESSFUL_BID", success_bid);
                            execService.shutdown();
                        }
                    }
                }, 0, 1, TimeUnit.SECONDS);

            }
        });
        server.addEventListener("BID-ON", BidOnRequest.class, new DataListener<BidOnRequest>() {
            @Override
            public void onData(SocketIOClient client, BidOnRequest data, AckRequest ackRequest) {
                // server.getBroadcastOperations().sendEvent("BID-ON", data);
            }
        });

        server.addEventListener("NOTIFY_NUM_OF_PARTICIPANTS", NotifyNumOfParticipantsResponse.class,
                new DataListener<NotifyNumOfParticipantsResponse>() {
                    @Override
                    public void onData(SocketIOClient client, NotifyNumOfParticipantsResponse data,
                            AckRequest ackRequest) {
                        server.getBroadcastOperations().sendEvent("NOTIFY_NUM_OF_PARTICIPANTS",
                                data);
                    }
                });

        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}