package sample;

import java.io.File;
import java.util.Optional;
import com.scalar.db.api.Result;
import com.scalar.db.exception.transaction.TransactionException;

import classes.*;

import java.time.Instant;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

public class AppServer {
    static ScalarOperations scalar;
    public static int member = 0;
    public static int time = 0;

    public static Map<String, Integer> userMap = new HashMap<>();

    public void addMember() {
        int auction_id;
        try {
            auction_id = (int) scalar.getLatestAuction().get("auction_id");
            member = scalar.modifyAttendeeCount(auction_id, true);
            // this is mean member++
        } catch (TransactionException e) {
            auction_id = -1;
            System.out.println("error!!!");
            e.printStackTrace();
        }
    }

    public void subMember() {
        int auction_id;
        try {
            auction_id = (int) scalar.getLatestAuction().get("auction_id");
            member = scalar.modifyAttendeeCount(auction_id, false);
        } catch (TransactionException e) {
            auction_id = -1;
            System.out.println("error!!!");
            e.printStackTrace();
        }
    }

    public void resetMember() {
        member = 0;
    }

    public void resetFirstMember() {
        int auction_id;
        try {
            auction_id = (int) scalar.getLatestAuction().get("auction_id");
            member = scalar.modifyAttendeeCount(auction_id, false);
            while (member < 0) {
                member = scalar.modifyAttendeeCount(auction_id, true);
            }
            while (member > 0) {
                member = scalar.modifyAttendeeCount(auction_id, false);
            }
        } catch (TransactionException e) {
            auction_id = -1;
            System.out.println("error!!!");
            e.printStackTrace();
        }
    }

    public void start() throws InterruptedException {
        // Hard codingで許してください
        userMap.put("John", 1);
        userMap.put("Bob", 2);
        userMap.put("Emma", 3);
        scalar = new ScalarOperations();
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(10200);
        // config.setOrigin("*");
        // //websocket
        // config.setTransports(Transport.WEBSOCKET);
        SocketIOServer server = new SocketIOServer(config);

        System.out.println("test");

        resetFirstMember();

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                addMember();
                System.out.println("Client connected. Current members: " + member);
                NotifyNumOfParticipantsResponse notify = new NotifyNumOfParticipantsResponse();
                notify.number = member;
                server.getBroadcastOperations().sendEvent("notify-num-of-participants",
                        notify);
            }
        });
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                subMember();

                // member--; // クライアント切断時にインスタンス変数を減らす
                System.out.println("Client disconnected. Current members: " + member);
                NotifyNumOfParticipantsResponse notify = new NotifyNumOfParticipantsResponse();
                notify.number = member;
                server.getBroadcastOperations().sendEvent("notify-num-of-participants",
                        notify);
            }
        });

        server.addEventListener("RELOAD_MEMBER_ADD", BidOnRequest.class, new DataListener<BidOnRequest>() {
            @Override
            public void onData(SocketIOClient client, BidOnRequest data, AckRequest ackRequest) {
                // serverの時間タイムスタンプを使わない感じになってしまった.....
                addMember();
                NotifyNumOfParticipantsResponse notify = new NotifyNumOfParticipantsResponse();
                notify.number = member;
                server.getBroadcastOperations().sendEvent("notify-num-of-participants",
                        notify);
            }
        });
        server.addEventListener("RELOAD_MEMBER_SUB", BidOnRequest.class, new DataListener<BidOnRequest>() {
            @Override
            public void onData(SocketIOClient client, BidOnRequest data, AckRequest ackRequest) {
                // serverの時間タイムスタンプを使わない感じになってしまった.....
                subMember();
                System.out.println("Client disconnected. Current members: " + member);
                NotifyNumOfParticipantsResponse notify = new NotifyNumOfParticipantsResponse();
                notify.number = member;
                server.getBroadcastOperations().sendEvent("notify-num-of-participants",
                        notify);
            }
        });

        server.addEventListener("init-state", InitStateRequest.class, new DataListener<InitStateRequest>() {
            @Override
            public void onData(SocketIOClient client, InitStateRequest data, AckRequest ackRequest) {
                InitStateResponse initResp = new InitStateResponse();
                // items
                System.out.println("ON: INIT_STATE");
                System.out.println("data.user_name: " + data.user_name);
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
                System.out.println("userItems: " + userItems);
                initResp.items = userItems;

                initResp.setUser_id(uid);

                System.out.println("initResp: " + initResp);
                System.out.println("initResp.current_item: " + initResp.current_item);
                try {
                    InitStateResponse.CurrentItem current_item = initResp.new CurrentItem();
                    int auc_uid = (int) latestAuction.get("user_id");

                    long start_time = (long) latestAuction.get("start_time");
                    System.out.println("start_time: " + start_time);
                    Instant instant = Instant.now();
                    long currentTimestamp = instant.toEpochMilli();
                    long diff = currentTimestamp - start_time;
                    System.out.println("diff: " + diff);
                    // diffをsecondに変換
                    diff = diff / 1000;
                    System.out.println("diff: " + diff);
                    initResp.setRemaining_time((int) diff);

                    current_item.setItem_id((int) latestAuction.get("item_id"));
                    current_item.setItem_name(
                            (String) scalar.getItem(auc_uid, current_item.item_id)
                                    .get("item_name"));
                    current_item.setUser_id(
                            (int) scalar.getItem(auc_uid, current_item.item_id)
                                    .get("user_id"));

                    List<Object> allBids = new ArrayList<>();
                    allBids = scalar.getAllAuctionBids(auc_uid);
                    // allBidsの表示
                    for (int i = 0; i < allBids.size(); i++) {
                        System.out.println("allBids[" + i + "]:" + allBids.get(i));
                    }
                    for (int i = 0; i < allBids.size(); i++) {
                        InitStateResponse.CurrentItem.History history = current_item.new History();
                        history.setPrice((int) ((Map<String, Object>) allBids.get(i)).get("price"));
                        long bid_time = (long) ((Map<String, Object>) allBids.get(i)).get("time");
                        // bid_timeを生年月日に変更
                        bid_time = start_time - bid_time;
                        bid_time = bid_time / 1000;
                        System.out.println("bid_time: " + bid_time);
                        history.setTime((int) bid_time);
                        history.setUser_name((String) scalar.getUserInfo(auc_uid).get("user_name"));
                        current_item.setHistory(history);
                    }
                    initResp.setCurrent_item(current_item);
                } catch (TransactionException e) {
                    System.out.println("error!");
                    e.printStackTrace();
                }

                // boolean isEx = (uid == (int) latestAuction.get("user_id"));
                initResp.setIs_exhibitor(false);

                // scalar.getUserInfo(data.user_name);
                // server.getBroadcastOperations().sendEvent("init-state", data);
                client.sendEvent("init-state", initResp);
            }
        });

        server.addEventListener("raise-hands", RaiseHandsRequest.class, new DataListener<RaiseHandsRequest>() {
            @Override
            public void onData(SocketIOClient client, RaiseHandsRequest data, AckRequest ackRequest) {
                System.out.println("ON: RAISE_HANDS");
                int succeed = -1;
                RaiseHandsResponse r_hands = new RaiseHandsResponse();

                try {
                    System.out.println("item_id" + data.item_id + " user_id" + data.user_id);
                    succeed = scalar.startAuction(data.item_id, data.user_id);
                } catch (TransactionException e) {
                    succeed = -1;
                    e.printStackTrace();
                }
                System.out.println("====1====");

                if (succeed != -1) {
                    try {
                        r_hands.setItem_id(data.item_id);
                        r_hands.setItem_name((String) scalar.getItem(data.user_id, data.item_id).get("item_name"));
                        String user_name = (String) scalar.getUserInfo(data.user_id).get("user_name");
                        r_hands.setUser_name(user_name);
                        // userMapのuser_name to idで変換
                        r_hands.setUser_id((int) userMap.get(user_name));
                        // r_hands.item_id = data.item_id;
                        // r_hands.item_name = (String) scalar.getItem(data.user_id,
                        // data.item_id).get("item_name");
                        // r_hands.user_id = data.user_id;
                        System.out.println("====2====");
                    } catch (TransactionException e) {
                        e.printStackTrace();
                        System.out.println("error!");
                    }
                    System.out.println("====3====");
                    server.getBroadcastOperations().sendEvent("raise-hands", r_hands);

                    System.out.println("====4====");
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

                                server.getBroadcastOperations().sendEvent("successful-bid", success_bid);

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
                    resetMember();
                    server.getBroadcastOperations().sendEvent("reload_member_add", data);

                } else {
                    r_hands.setItem_id(0);
                    r_hands.setItem_name("");
                    r_hands.setUser_name("");
                    // r_hands.item_id = 0;
                    // r_hands.item_name = "";
                    // r_hands.user_id = 0;
                    server.getBroadcastOperations().sendEvent("raise-hands", r_hands);
                }

            }
        });

        server.addEventListener("bid-on", BidOnRequest.class, new DataListener<BidOnRequest>() {
            @Override
            public void onData(SocketIOClient client, BidOnRequest data, AckRequest ackRequest) {
                // serverの時間タイムスタンプを使わない感じになってしまった.....
                BidOnResponse bidResp = new BidOnResponse();
                boolean succeed = false;

                int auction_id = -1;

                // bidResp.price = data.price;
                // bidResp.user_id = data.user_id;
                // bidResp.time = time;

                try {
                    auction_id = (int) scalar.getLatestAuction().get("auction_id");
                    bidResp.setPrice(data.price);
                    bidResp.setTime(time);
                    bidResp.setUser_id(data.user_id);
                    succeed = scalar.placeBid(auction_id, bidResp.user_id, bidResp.price);
                } catch (TransactionException e) {
                    e.printStackTrace();
                }

                if (!succeed) {
                    bidResp.setPrice(0);
                    bidResp.setTime(0);
                    bidResp.setUser_id(0);
                }

                server.getBroadcastOperations().sendEvent("bid-on", bidResp);
            }
        });

        server.start();
        System.out.println("test22");

        // Thread.sleep(Integer.MAX_VALUE);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            member = 0;
            resetFirstMember();
            time = 0;
            System.out.println("Server stopped and port released");
        }));

        Thread.sleep(Integer.MAX_VALUE);
        // server.stop();
    }
}