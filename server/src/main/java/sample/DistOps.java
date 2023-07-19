package sample;

import com.scalar.db.api.DistributedTransaction;
import com.scalar.db.api.DistributedTransactionAdmin;
import com.scalar.db.api.DistributedTransactionManager;
import com.scalar.db.api.Get;
import com.scalar.db.api.Put;
import com.scalar.db.api.Result;
import com.scalar.db.api.Scan;
import com.scalar.db.api.TableMetadata;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.DataType;
import com.scalar.db.io.Key;
import com.scalar.db.service.TransactionFactory;
import java.io.File;
import java.io.IOException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

public class DistOps {

  private static final String[] NAMESPACES = { "app1", "app2", "app3" };
  Properties properties = new Properties();
  TransactionFactory factory;
  private final DistributedTransactionManager manager;
  private Random random = new Random();

  public DistOps() throws IOException {
    String scalarDBProperties =
      System.getProperty("user.dir") + File.separator + "scalardb.properties";
    factory = TransactionFactory.create(scalarDBProperties);
    manager = factory.getTransactionManager();
  }

  // ユーザ追加
  public void addUser(int user_id, String user_name)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = user_id % 3;
    try {
      Put put = Put
        .newBuilder()
        .namespace(NAMESPACES[mod])
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

  // ユーザのアイテム追加
  public void addItem(
    int user_id,
    int item_id,
    String item_name,
    boolean is_sold
  ) throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = item_id % 3;
    try {
      Put put = Put
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("items")
        .partitionKey(Key.ofInt("item_id", item_id))
        .textValue("item_name", item_name)
        .booleanValue("is_sold", is_sold)
        .intValue("user_id", user_id)
        .build();

      tx.put(put);

      tx.commit();
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // purchase追加
  public void addPurchase(
    int user_id,
    int purchase_id,
    int auction_id,
    String item_name,
    int price
  ) throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = user_id % 3;
    try {
      Put put = Put
        .newBuilder()
        .namespace(NAMESPACES[mod])
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

  // auction追加
  public void addAuction(
    int user_id,
    int auction_id,
    int item_id,
    int attendee_count,
    long start_time
  ) throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = auction_id % 3;
    try {
      Put put = Put
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("auctions")
        .partitionKey(Key.ofInt("auction_id", auction_id))
        .intValue("user_id", user_id)
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

  // bid追加
  public void addBid(
    int bid_id,
    int auction_id,
    int user_id,
    int price,
    long time
  ) throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = bid_id % 3;
    try {
      Put put = Put
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("bids")
        .partitionKey(Key.ofInt("bid_id", bid_id))
        .intValue("user_id", user_id)
        .intValue("price", price)
        .intValue("auction_id", auction_id)
        .bigIntValue("time", time)
        .build();

      tx.put(put);

      tx.commit();
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // ユーザ情報取得
  public Map<String, Object> getUserInfo(int user_id)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = user_id % 3;
    try {
      Get get = Get
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("users")
        .partitionKey(Key.ofInt("user_id", user_id))
        .build();
      Optional<Result> result = tx.get(get);

      tx.commit();
      Map<String, Object> mapResult = new HashMap<String, Object>();
      mapResult.put("user_id", result.get().getInt("user_id"));
      mapResult.put("user_name", result.get().getText("user_name"));
      return mapResult;
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // アイテム一覧取得
  public List<Object> getAllUserItem(int user_id) throws TransactionException {
    DistributedTransaction tx = manager.start();
    List<Result> results = new ArrayList<>();
    try {
      for (int i = 0; i < 3; i++) {
        Scan scan = Scan
          .newBuilder()
          .namespace(NAMESPACES[i])
          .table("items")
          .indexKey(Key.ofInt("user_id", user_id))
          .build();
        results.addAll(tx.scan(scan));
      }
      tx.commit();
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
    List<Object> values = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      Map<String, Object> mapResult = new HashMap<String, Object>();
      Result result = results.get(i);
      mapResult.put("item_id", result.getInt("item_id"));
      mapResult.put("item_name", result.getText("item_name"));
      mapResult.put("is_sold", result.getBoolean("is_sold"));
      values.add(mapResult);
    }
    return values;
  }

  // アイテム取得
  public Map<String, Object> getItem(int user_id, int item_id)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = item_id % 3;
    try {
      Get get = Get
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("items")
        .partitionKey(Key.ofInt("item_id", item_id))
        .build();
      Optional<Result> result = tx.get(get);

      tx.commit();
      Map<String, Object> mapResult = new HashMap<String, Object>();
      mapResult.put("user_id", result.get().getInt("user_id"));
      mapResult.put("item_id", result.get().getInt("item_id"));
      mapResult.put("item_name", result.get().getText("item_name"));
      mapResult.put("is_sold", result.get().getBoolean("is_sold"));
      return mapResult;
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // 購入履歴取得
  public List<Object> getAllPurchase(int user_id) throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = user_id % 3;
    try {
      Scan scan = Scan
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("purchases")
        .partitionKey(Key.ofInt("user_id", user_id))
        .build();
      List<Result> results = tx.scan(scan);

      tx.commit();

      List<Object> values = new ArrayList<>();
      for (int i = 0; i < results.size(); i++) {
        Map<String, Object> mapResult = new HashMap<String, Object>();
        Result result = results.get(i);
        mapResult.put("purchase_id", result.getInt("purchase_id"));
        mapResult.put("user_id", result.getInt("user_id"));
        mapResult.put("auction_id", result.getInt("auction_id"));
        mapResult.put("price", result.getInt("price"));
        mapResult.put("item_name", result.getText("item_name"));
        values.add(mapResult);
      }
      return values;
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // オークション取得
  public Map<String, Object> getAuction(int auction_id)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = auction_id % 3;
    try {
      Get get = Get
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("auctions")
        .partitionKey(Key.ofInt("auction_id", auction_id))
        .build();
      Optional<Result> result = tx.get(get);

      tx.commit();
      Map<String, Object> mapResult = new HashMap<String, Object>();
      mapResult.put("item_id", result.get().getInt("item_id"));
      mapResult.put("user_id", result.get().getInt("user_id"));
      mapResult.put("auction_id", result.get().getInt("auction_id"));
      mapResult.put("attendee_count", result.get().getInt("attendee_count"));
      mapResult.put("start_time", result.get().getBigInt("start_time"));
      return mapResult;
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // 最新オークション取得
  public Map<String, Object> getLatestAuction() throws TransactionException {
    DistributedTransaction tx = manager.start();
    List<Result> results = new ArrayList<>();
    try {
      for (int i = 0; i < 3; i++) {
        Scan scan = Scan
          .newBuilder()
          .namespace(NAMESPACES[i])
          .table("auctions")
          .all()
          .build();
        results.addAll(tx.scan(scan));
      }
      tx.commit();
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
    Result result = results.get(0);
    long max = result.getBigInt("start_time");
    for (int i = 1; i < results.size(); i++) {
      long time = results.get(i).getBigInt("start_time");
      if (max < time) {
        max = time;
        result = results.get(i);
      }
    }

    Map<String, Object> mapResult = new HashMap<String, Object>();
    mapResult.put("item_id", result.getInt("item_id"));
    mapResult.put("user_id", result.getInt("user_id"));
    mapResult.put("auction_id", result.getInt("auction_id"));
    mapResult.put("attendee_count", result.getInt("attendee_count"));
    mapResult.put("start_time", result.getBigInt("start_time"));
    return mapResult;
  }

  //おーくしょんの入札履歴を返す　時間の昇順
  public List<Object> getAllAuctionBids(int auction_id)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    List<Result> results = new ArrayList<>();
    try {
      for (int i = 0; i < 3; i++) {
        Scan scan = Scan
          .newBuilder()
          .namespace(NAMESPACES[i])
          .table("bids")
          .indexKey(Key.ofInt("auction_id", auction_id))
          .build();
        results.addAll(tx.scan(scan));
      }
      tx.commit();
    } catch (Exception e) {
      tx.abort();
      throw e;
    }

    //ソート処理
    Collections.sort(
      results,
      new Comparator<Person>() {
        @Override
        public int compare(Result r1, Result r2) {
          return Integer.compare(
            r1.get().getInt("time"),
            r2.get().getInt("time")
          );
        }
      }
    );

    List<Object> values = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      Map<String, Object> mapResult = new HashMap<String, Object>();
      Result result = results.get(i);
      mapResult.put("bid_id", result.getInt("bid_id"));
      mapResult.put("user_id", result.getInt("user_id"));
      mapResult.put("auction_id", result.getInt("auction_id"));
      mapResult.put("price", result.getInt("price"));
      mapResult.put("time", result.getBigInt("time"));
      values.add(mapResult);
    }
    return values;
  }

  // オークション開始時
  public int startAuction(int item_id, int user_id)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    try {
      // item get して !is_sold なら put
      int mod;
      mod = item_id % 3;
      Get get = Get
        .newBuilder()
        .namespace(NAMESPACES[mod])
        .table("items")
        .clusteringKey(Key.ofInt("item_id", item_id))
        .build();
      Optional<Result> item = tx.get(get);

      List<Result> results = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        Scan scan = Scan
          .newBuilder()
          .namespace(NAMESPACE[i])
          .table("auctions")
          .all()
          .build();
        results.addAll(tx.scan(scan));
      }
      Result latestAuction = results.get(0);
      long max = latestAuction.getBigInt("start_time");
      for (int i = 1; i < results.size(); i++) {
        long time = results.get(i).getBigInt("start_time");
        if (max < time) {
          max = time;
          latestAuction = results.get(i);
        }
      }

      long startTimeStamp = latestAuction.getBigInt("start_time");
      Instant instant = Instant.now();
      long currentTimestamp = instant.toEpochMilli();
      boolean notAuctionNow =
        currentTimestamp - startTimeStamp > (60 * 60 * 1000);
      boolean is_sold = item.get().getBoolean("is_sold");
      if (!is_sold && notAuctionNow) {
        int auction_id = latestAuction.getInt("auction_id") + 1;
        mod = auction_id % 3;
        Put put = Put
          .newBuilder()
          .namespace(NAMESPACES[mod])
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
      } else {
        return -1;
      }
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // 参加者数変更 is_addがtrueならインクリメント、falseならデクリメント
  public int modifyAttendeeCount(int auction_id, boolean is_add)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    int mod = auction_id % 3;
    try {
      Get get = Get
        .newBuilder()
        .namespace(NAMESPACEs[mod])
        .table("auctions")
        .partitionKey(Key.ofInt("auction_id", auction_id))
        .build();
      Optional<Result> result = tx.get(get);
      Result auction = result.get();
      int attendee_count = auction.getInt("attendee_count");
      if (is_add) {
        attendee_count++;
      } else {
        attendee_count--;
      }
      Put put = Put
        .newBuilder()
        .namespace(NAMESPACES[mod])
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

  // 入札
  public void placeBid(int auction_id, int user_id, int price)
    throws TransactionException {
    DistributedTransaction tx = manager.start();
    try {
      // 前回の値と比較

      List<Result> results = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        Scan scan = Scan
          .newBuilder()
          .namespace(NAMESPACES[i])
          .table("bids")
          .indexKey(Key.ofInt("auction_id", auction_id))
          .build();
        results.addAll(tx.scan(scan));
      }
      tx.commit();

      //ソート処理
      Collections.sort(
        results,
        new Comparator<Person>() {
          @Override
          public int compare(Result r1, Result r2) {
            return Integer.compare(
              r1.get().getInt("time"),
              r2.get().getInt("time")
            );
          }
        }
      );

      int next_id;
      if (results.isEmpty()) {
        next_id = 1;
      } else if (results.get(0).getInt("price") < price) {
        next_id = results.get(0).getInt("bid_id") + 1;
      } else {
        tx.abort();
        return;
      }
      Instant instant = Instant.now();
      long currentTimestamp = instant.toEpochMilli();
      int mod = next_id % 3;
      Put put = Put
        .newBuilder()
        .namespace(NAMESPACE[mod])
        .table("bids")
        .partitionKey(Key.ofInt("bid_id", next_id))
        .bigIntValue("time", currentTimestamp)
        .intValue("user_id", user_id)
        .intValue("price", price)
        .build();

      tx.put(put);

      tx.commit();
    } catch (Exception e) {
      tx.abort();
      throw e;
    }
  }

  // 落札の処理
  //   public Map<String, Object> processWinningBid() throws TransactionException {
  //     DistributedTransaction tx = manager.start();
  //     try {
  //       // bidsの中の一番大きい値をpurchaseに書き込み、itemsのis_soldを1に
  //       Map<String, Object> mapResult = new HashMap<String, Object>();
  //       List<Result> results;
  //       Scan scan1 = Scan
  //         .newBuilder()
  //         .namespace(NAMESPACE)
  //         .table("auctions")
  //         .all()
  //         .build();
  //       results = tx.scan(scan1);
  //       Result result = results.get(0);
  //       long max = result.getBigInt("start_time");
  //       for (int i = 1; i < results.size(); i++) {
  //         long time = results.get(i).getBigInt("start_time");
  //         if (max < time) {
  //           max = time;
  //           result = results.get(i);
  //         }
  //       }
  //       int auction_id = result.getInt("auction_id");

  //       Get get = Get
  //         .newBuilder()
  //         .namespace(NAMESPACE)
  //         .table("items")
  //         .partitionKey(Key.ofInt("user_id", result.getInt("user_id")))
  //         .clusteringKey(Key.ofInt("item_id", result.getInt("item_id")))
  //         .build();
  //       Optional<Result> item = tx.get(get);
  //       String item_name = item.get().getText("item_name");

  //       Put put1 = Put
  //         .newBuilder()
  //         .namespace(NAMESPACE)
  //         .table("items")
  //         .partitionKey(Key.ofInt("user_id", result.getInt("user_id")))
  //         .clusteringKey(Key.ofInt("item_id", result.getInt("item_id")))
  //         .booleanValue("is_sold", true)
  //         .build();

  //       tx.put(put1);

  //       Scan scan2 = Scan
  //         .newBuilder()
  //         .namespace(NAMESPACE)
  //         .table("bids")
  //         .partitionKey(Key.ofInt("auction_id", auction_id))
  //         // .orderings(Scan.Ordering.desc("price"))
  //         // .limit(1)
  //         .build();
  //       results = tx.scan(scan2);
  //       Result maxBid = results.get(0);
  //       long maxTime = maxBid.getBigInt("time");
  //       for (int i = 1; i < results.size(); i++) {
  //         long time = results.get(i).getBigInt("time");
  //         if (maxTime < time) {
  //           maxTime = time;
  //           maxBid = results.get(i);
  //         }
  //       }
  //       int user_id = maxBid.getInt("user_id");
  //       int price = maxBid.getInt("price");
  //       mapResult.put("user_id", user_id);
  //       mapResult.put("price", price);

  //       Put put2 = Put
  //         .newBuilder()
  //         .namespace(NAMESPACE)
  //         .table("purchases")
  //         .partitionKey(Key.ofInt("user_id", user_id))
  //         .clusteringKey(Key.ofInt("purchase_id", random.nextInt(2147483647)))
  //         .intValue("auction_id", auction_id)
  //         .intValue("price", price)
  //         .textValue("item_name", item_name)
  //         .build();

  //       tx.put(put2);

  //       tx.commit();
  //       return mapResult;
  //     } catch (Exception e) {
  //       tx.abort();
  //       throw e;
  //     }
  //   }

  public void close() {
    manager.close();
  }
}
