package sample;

import java.io.File;
import java.util.Optional;
import com.scalar.db.api.Result;
import com.scalar.db.exception.transaction.TransactionException;

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
        // Hard codingで許してください
        userMap.put("John", 1);
        userMap.put("Bob", 2);
        userMap.put("Emma", 3);

        System.out.println("test");

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                int auction_id;
                try {
                    auction_id = (int) scalar.getLatestAuction().get("auction_id");
                    member = scalar.modifyAttendeeCount(auction_id, true);
                } catch (TransactionException e) {
                    auction_id = -1;
                    System.out.println("error!!!");
                    e.printStackTrace();
                }

                // member++; // クライアント接続時にインスタンス変数を増やす
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
                int auction_id;
                try {
                    auction_id = (int) scalar.getLatestAuction().get("auction_id");
                    member = scalar.modifyAttendeeCount(auction_id, false);
                } catch (TransactionException e) {
                    auction_id = -1;
                    System.out.println("error!!!");
                    e.printStackTrace();
                }

                // member--; // クライアント切断時にインスタンス変数を減らす
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
                InitStateResponse initResp = new InitStateResponse();

                // items
                int uid = userMap.get(data.user_name);
                Map<String, Object> latestAuction = new HashMap<>();
                List<Object> userItems = new ArrayList<>();
                try {
                    latestAuction = scalar.getLatestAuction();
                    userItems = scalar.getAllUserItem(uid);

                } catch (TransactionException e) {
                    System.out.println("error!!!!");
                    e.printStackTrace();
                }

                // NOTE: 無理やりDBとマッピングさせた
                initResp.items = userItems;

                initResp.setUser_id(uid);

                initResp.setRemaining_time(time);

                try {
                    int auc_uid = (int) latestAuction.get("user_id");
                    initResp.current_item.setItem_id((int) latestAuction.get("item_id"));
                    initResp.current_item.setItem_name(
                            (String) scalar.getItem(auc_uid, initResp.current_item.item_id)
                                    .get("item_name"));
                    // TODO ?
                    List<Object> allBids = new ArrayList<>();
                    allBids = scalar.getAllAuctionBids(auc_uid);
                    Object last = allBids.get(allBids.size() - 1);

                    initResp.current_item.history.setPrice((int) last);
                    initResp.current_item.history.setTime(time);
                    initResp.current_item.history.setUser_name((String) scalar.getUserInfo(auc_uid).get("user_name"));
                } catch (TransactionException e) {
                    e.printStackTrace();
                }

                boolean isEx = (uid == (int) latestAuction.get("user_id"));
                initResp.setIs_exhibitor(isEx);

                // scalar.getUserInfo(data.user_name);
                // server.getBroadcastOperations().sendEvent("INIT_STATE", data);
                client.sendEvent("INIT_STATE", initResp);
            }
        });
        server.addEventListener("RAISE_HAND", RaiseHandsRequest.class, new DataListener<RaiseHandsRequest>() {
            @Override
            public void onData(SocketIOClient client, RaiseHandsRequest data, AckRequest ackRequest) {

                int succeed = -1;
                RaiseHandsResponse r_hands = new RaiseHandsResponse();

                try {
                    succeed = scalar.startAuction(data.item_id, data.user_id);
                } catch (TransactionException e) {
                    succeed = -1;
                    e.printStackTrace();
                }

                if (succeed != -1) {
                    try {
                        r_hands.setItem_id(data.item_id);
                        r_hands.setItem_name((String) scalar.getItem(data.user_id, data.item_id).get("item_name"));
                        r_hands.setUser_name((String) scalar.getUserInfo(data.user_id).get("user_name"));
                        // r_hands.item_id = data.item_id;
                        // r_hands.item_name = (String) scalar.getItem(data.user_id,
                        // data.item_id).get("item_name");
                        // r_hands.user_id = data.user_id;
                    } catch (TransactionException e) {
                        e.printStackTrace();
                        System.out.println("error!");
                    }

                    server.getBroadcastOperations().sendEvent("RAISE_HANDS", r_hands);

                    time = 120;
                    ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);
                    execService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            if (time > 0) {
                                time--;
                            } else {
                                SuccessfulBidResponse success_bid = new SuccessfulBidResponse();

                                try {
                                    scalar.processWinningBid();
                                } catch (TransactionException e) {
                                    e.printStackTrace();
                                }

                                server.getBroadcastOperations().sendEvent("SUCCESSFUL_BID", success_bid);

                                execService.shutdown();
                            }
                        }
                    }, 0, 1, TimeUnit.SECONDS);

                    // add member
                    Map<String, Object> latestAuction = new HashMap<>();
                    try {
                        latestAuction = scalar.getLatestAuction();
                    } catch (TransactionException e) {
                        e.printStackTrace();
                    }

                } else {
                    r_hands.setItem_id(0);
                    r_hands.setItem_name("");
                    r_hands.setUser_name("");
                    // r_hands.item_id = 0;
                    // r_hands.item_name = "";
                    // r_hands.user_id = 0;
                    server.getBroadcastOperations().sendEvent("RAISE_HANDS", r_hands);
                }

            }
        });
        server.addEventListener("BID-ON", BidOnRequest.class, new DataListener<BidOnRequest>() {
            @Override
            public void onData(SocketIOClient client, BidOnRequest data, AckRequest ackRequest) {
                // serverの時間タイムスタンプを使わない感じになってしまった.....
                BidOnResponse bidResp = new BidOnResponse();
                int succeed = -1;

                int auction_id = -1;

                bidResp.setPrice(data.price);
                bidResp.setTime(time);
                bidResp.setUser_id(data.user_id);
                // bidResp.price = data.price;
                // bidResp.user_id = data.user_id;
                // bidResp.time = time;

                try {
                    auction_id = (int) scalar.getLatestAuction().get("auction_id");
                    scalar.placeBid(auction_id, bidResp.user_id, bidResp.price);
                } catch (TransactionException e) {
                    e.printStackTrace();
                }

                server.getBroadcastOperations().sendEvent("BID-ON", bidResp);
            }
        });

        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }
}