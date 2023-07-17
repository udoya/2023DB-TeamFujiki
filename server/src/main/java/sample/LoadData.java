package sample;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalar.db.exception.transaction.TransactionException;
import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

public class LoadData {
    public void load() throws JsonProcessingException, IOException, TransactionException  {
        ScalarOperations scalar = new ScalarOperations();
        ObjectMapper  objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(Paths.get(System.getProperty("user.dir") + File.separator + "sampleData.json").toFile());
        Iterator<String> fieldNames = json.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode node = json.get(fieldName);
            for(int i=0;i<node.size();i++){
                JsonNode v = node.get(i);
                if(fieldName.equals("users")){
                    scalar.addUser(v.get("user_id").intValue(),v.get("user_name").textValue());
                }else if(fieldName.equals("items")){
                    scalar.addItem(v.get("user_id").intValue(),v.get("item_id").intValue(),v.get("item_name").textValue(),v.get("is_sold").booleanValue());
                }else if(fieldName.equals("purchases")){
                    scalar.addPurchase(v.get("user_id").intValue(),v.get("purchase_id").intValue(),v.get("auction_id").intValue(),v.get("item_name").textValue(),v.get("price").intValue());
                }else if(fieldName.equals("auctions")){
                    scalar.addAuction(v.get("user_id").intValue(),v.get("auction_id").intValue(),v.get("item_id").intValue(),v.get("attendee_count").intValue(),v.get("start_time").longValue());
                }else if(fieldName.equals("bids")){
                    scalar.addBid(v.get("bid_id").intValue(),v.get("auction_id").intValue(),v.get("user_id").intValue(),v.get("price").intValue(),v.get("time").longValue());
                }
            }
        }
        scalar.close();
    }
}