package sample;

import java.util.Properties;

import com.scalar.db.api.DistributedTransactionAdmin;
import com.scalar.db.api.TableMetadata;
import com.scalar.db.io.DataType;
import com.scalar.db.service.TransactionFactory;
import com.scalar.db.api.Scan;
import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;
import java.io.IOException;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.util.Random;

public class ScalarOperations {
    private static final String NAMESPACE = "app";
    Properties properties = new Properties();
    TransactionFactory factory;
    private final DistributedTransactionManager manager;
    private Random random = new Random();

    public ScalarOperations(){
        properties.setProperty("scalar.db.contact_points", "localhost");
        properties.setProperty("scalar.db.contact_port", "9042");
        properties.setProperty("scalar.db.storage", "cassandra");
        properties.setProperty("scalar.db.username", "cassandra");
        properties.setProperty("scalar.db.password", "cassandra");
        factory = TransactionFactory.create(properties);
        manager = factory.getTransactionManager();
    }

    //ユーザ追加
    public void addUser(int user_id, String user_name) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Put put = 
                Put.newBuilder()
                    .namespace(NAMESPACE)
                    .table("users")
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .textValue("user_name", user_name)
                    .build();

            tx.put(put);

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //ユーザのアイテム追加
    public void addItem(int user_id, int item_id, String item_name, boolean is_sold) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Put put = 
                Put.newBuilder()
                    .namespace(NAMESPACE)
                    .table("items")
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .clusteringKey(Key.ofInt("item_id", item_id))
                    .textValue("item_name", item_name)
                    .booleanValue("is_sold",is_sold)
                    .build();

            tx.put(put);

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //purchase追加
    public void addPurchase(int user_id, int purchase_id, int auction_id, String item_name, int price) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Put put = 
                Put.newBuilder()
                    .namespace(NAMESPACE)
                    .table("purchases")
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .clusteringKey(Key.ofInt("purchase_id", purchase_id))
                    .intValue("auction_id", auction_id)
                    .textValue("item_name", item_name)
                    .intValue("price", price)
                    .build();

            tx.put(put);

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //auction追加
    public void addAuction(int user_id, int auction_id, int item_id, int attendee_count, long start_time) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Put put = 
                Put.newBuilder()
                    .namespace(NAMESPACE)
                    .table("auctions")
                    .partitionKey(Key.ofInt("auction_id", auction_id))
                    .intValue("user_id",user_id)
                    .intValue("item_id", item_id)
                    .intValue("attendee_count", attendee_count)
                    .bigIntValue("start_time", start_time)
                    .build();

            tx.put(put);

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //bid追加
    public void addBid(int bid_id,int auction_id,int user_id,int price,long time) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Put put = 
                Put.newBuilder()
                    .namespace(NAMESPACE)
                    .table("bids")
                    .partitionKey(Key.ofInt("auction_id", auction_id))
                    .clusteringKey(Key.ofInt("bid_id", bid_id))
                    .intValue("user_id",user_id)
                    .intValue("price", price)
                    .bigIntValue("time", time)
                    .build();

            tx.put(put);

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //ユーザ情報取得
    public Map<String, Object> getUserInfo(int user_id) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Get get =
                Get.newBuilder()
                    .namespace(NAMESPACE)
                    .table("users")
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .build();
            Optional<Result> result = tx.get(get);

            tx.commit();
            Map<String, Object> mapResult = new HashMap<String, Object>();
            mapResult.put("user_id",result.get().getInt("user_id"));
            mapResult.put("user_name",result.get().getText("user_name"));
            return mapResult;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //アイテム一覧取得
    public List<Object> getAllUserItem(int user_id) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Scan scan =
            Scan.newBuilder()
                .namespace(NAMESPACE)
                .table("items")
                .partitionKey(Key.ofInt("user_id", user_id))
                .build();
            List<Result> results = tx.scan(scan);

            tx.commit();

            List<Object> values = new ArrayList<>();
            for(int i=0;i<results.size();i++){
                Map<String, Object> mapResult = new HashMap<String, Object>();
                Result result = results.get(i);
                mapResult.put("item_id",result.getInt("item_id"));
                mapResult.put("item_name",result.getText("item_name"));
                mapResult.put("is_sold",result.getBoolean("is_sold"));
                values.add(mapResult);
            }
            return values;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //アイテム取得
    public Map<String, Object> getItem(int user_id,int item_id) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Get get =
                Get.newBuilder()
                    .namespace(NAMESPACE)
                    .table("items")
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .clusteringKey(Key.ofInt("item_id", item_id))
                    .build();
            Optional<Result> result = tx.get(get);

            tx.commit();
            Map<String, Object> mapResult = new HashMap<String, Object>();
            mapResult.put("user_id",result.get().getInt("user_id"));
            mapResult.put("item_id",result.get().getInt("item_id"));
            mapResult.put("item_name",result.get().getText("item_name"));
            mapResult.put("is_sold",result.get().getBoolean("is_sold"));
            return mapResult;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //購入履歴取得
    public List<Object> getAllPurchase(int user_id) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Scan scan =
            Scan.newBuilder()
                .namespace(NAMESPACE)
                .table("purchases")
                .partitionKey(Key.ofInt("user_id", user_id))
                .build();
            List<Result> results = tx.scan(scan);

            tx.commit();

            List<Object> values = new ArrayList<>();
            for(int i=0;i<results.size();i++){
                Map<String, Object> mapResult = new HashMap<String, Object>();
                Result result = results.get(i);
                mapResult.put("purchase_id",result.getInt("purchase_id"));
                mapResult.put("user_id",result.getInt("user_id"));
                mapResult.put("auction_id",result.getInt("auction_id"));
                mapResult.put("price",result.getInt("price"));
                mapResult.put("item_name",result.getText("item_name"));
                values.add(mapResult);
            }
            return values;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //オークション取得
    public Map<String, Object> getAuction(int auction_id) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Get get =
                Get.newBuilder()
                    .namespace(NAMESPACE)
                    .table("auctions")
                    .partitionKey(Key.ofInt("auction_id", auction_id))
                    .build();
            Optional<Result> result = tx.get(get);

            tx.commit();
            Map<String, Object> mapResult = new HashMap<String, Object>();
            mapResult.put("item_id",result.get().getInt("item_id"));
            mapResult.put("user_id",result.get().getInt("user_id"));
            mapResult.put("auction_id",result.get().getInt("auction_id"));
            mapResult.put("attendee_count",result.get().getInt("attendee_count"));
            mapResult.put("start_time",result.get().getBigInt("start_time"));
            return mapResult;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //最新オークション取得
    public Map<String, Object> getLatestAuction() throws TransactionException {
        DistributedTransaction tx = manager.start();
        List<Result> results;
        try {
            Scan scan =
                Scan.newBuilder()
                    .namespace(NAMESPACE)
                    .table("auctions")
                    .all()
                    .build();
            results = tx.scan(scan);
            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
        Result result = results.get(0);
        long max = result.getBigInt("start_time");
        for(int i=1;i<results.size();i++){
            long time = results.get(i).getBigInt("start_time");
            if(max<time){
                max = time;
                result = results.get(i);
            }
        }

        Map<String, Object> mapResult = new HashMap<String, Object>();
        mapResult.put("item_id",result.getInt("item_id"));
        mapResult.put("user_id",result.getInt("user_id"));
        mapResult.put("auction_id",result.getInt("auction_id"));
        mapResult.put("attendee_count",result.getInt("attendee_count"));
        mapResult.put("start_time",result.getBigInt("start_time"));
        return mapResult;
    }

    //オークション開始時
    public int startAuction(int item_id, int user_id) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            // item get して !is_sold なら put
            Map<String, Object> item = this.getItem(user_id,item_id);//２重のtxはok?
            Map<String, Object> latestAuction = this.getLatestAuction();
            long startTimeStamp = (Long) latestAuction.get("start_time");
            Instant instant = Instant.now();
            long currentTimestamp = instant.toEpochMilli();
            boolean notAuctionNow = currentTimestamp - startTimeStamp > (60 * 60 * 1000);
            if(item.get("is_sold").equals("false") && notAuctionNow){
                int auction_id = (Integer) latestAuction.get("auction_id") + 1;
                Put put = 
                    Put.newBuilder()
                        .namespace(NAMESPACE)
                        .table("auctions")
                        .partitionKey(Key.ofInt("auction_id", auction_id))
                        .intValue("user_id", user_id)
                        .intValue("item_id", item_id)
                        .intValue("attendee_count", 1)
                        .bigIntValue("start_time", currentTimestamp)
                        .build();
                tx.put(put);

                tx.commit();
                return auction_id;
            }else{
                return -1;
            }
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //参加者数変更 is_addがtrueならインクリメント、falseならデクリメント
    public int modifyAttendeeCount(int auction_id, boolean is_add) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Map<String, Object> auction = getAuction(auction_id);
            int attendee_count = (Integer) auction.get("attendee_count");
            if(is_add){
                attendee_count++;
            }else{
                attendee_count--;
            }
            Put put = 
                    Put.newBuilder()
                        .namespace(NAMESPACE)
                        .table("auctions")
                        .partitionKey(Key.ofInt("auction_id", auction_id))
                        .intValue("attendee_count", attendee_count)
                        .build();
                tx.put(put);

                tx.commit();
                return attendee_count;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //入札
    public void placeBid(int auction_id,int user_id, int price) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            //前回の値と比較
            Scan scan =
                Scan.newBuilder()
                    .namespace(NAMESPACE)
                    .table("bids")
                    .partitionKey(Key.ofInt("auction_id", auction_id))
                    .orderings(Scan.Ordering.desc("price"))
                    .limit(1)
                    .build();
            List<Result> results = tx.scan(scan);

            if(results.get(0).getInt("price")<price){
                Put put = 
                    Put.newBuilder()
                        .namespace(NAMESPACE)
                        .table("bids")
                        .partitionKey(Key.ofInt("bid_id", results.get(0).getInt("bid_id")+1))
                        .clusteringKey(Key.ofInt("auction_id", auction_id))
                        .intValue("start_time", 1)
                        .build();

                tx.put(put);

                tx.commit();
            }else{
                tx.abort();
            }
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    //落札の処理
    public Map<String, Object> processWinningBid() throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            //bidsの中の一番大きい値をpurchaseに書き込み、itemsのis_soldを1に
            Map<String, Object> mapResult = new HashMap<String, Object>();
            List<Result> results;
            Scan scan =
                Scan.newBuilder()
                    .namespace(NAMESPACE)
                    .table("auctions")
                    .all()
                    .build();
            results = tx.scan(scan);
            Result result = results.get(0);
            long max = result.getBigInt("start_time");
            for(int i=1;i<results.size();i++){
                long time = results.get(i).getBigInt("start_time");
                if(max<time){
                    max = time;
                    result = results.get(i);
                }
            }
            int auction_id = result.getInt("auction_id");

            Get get =
                Get.newBuilder()
                    .namespace(NAMESPACE)
                    .table("items")
                    .partitionKey(Key.ofInt("user_id", result.get().getInt("user_id")))
                    .clusteringKey(Key.ofInt("item_id", result.get().getInt("item_id")))
                    .build();
            Optional<Result> item = tx.get(get);
            String item_name = item.get().getText("item_name");

            Put put = 
                Put.newBuilder()
                    .namespace(NAMESPACE)
                    .table("items")
                    .partitionKey(Key.ofInt("user_id", result.get().getInt("user_id")))
                    .clusteringKey(Key.ofInt("item_id", result.get().getInt("item_id")))
                    .booleanValue("is_sold",true)
                    .build();

            tx.put(put);

            Scan scan =
                Scan.newBuilder()
                    .namespace(NAMESPACE)
                    .table("bids")
                    .partitionKey(Key.ofInt("auction_id", auction_id))
                    .orderings(Scan.Ordering.desc("price"))
                    .limit(1)
                    .build();
            results = tx.scan(scan);
            Result maxBid = results.get(0);
            int user_id = maxBid.getInt("user_id");
            int price = maxBid.getInt("price");
            mapResult.put("user_id",user_id);
            mapResult.put("price",price);

            Put put = 
                Put.newBuilder()
                    .namespace(NAMESPACE)
                    .table("purchases")
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .clusteringKey(Key.ofInt("purchase_id", random.nextInt(2147483647)))
                    .intValue("auction_id", auction_id)
                    .intValue("price",price)
                    .textValue("item_name",item_name)
                    .build();

            tx.put(put);

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    public void close() {
        manager.close();
    }
}