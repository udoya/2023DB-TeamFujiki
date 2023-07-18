package classes;

public class InitStateResponse {
    public RaiseHandsRequest items;
    public int user_id;
    public int remaining_time;
    public CurrentItem current_item;
    public boolean is_exhibitor;
    public AuctionHistory auction_history;

    class Item {
        public String item_name;
        public int item_id;
        public boolean is_sold;
    }

    class CurrentItem {
        public String item_name;
        public int item_id;
        public History history;

        class History {
            public String user_name;
            public int price;
            public int time;
        }
    }

    class AuctionHistory {
        public String item_name;
        public boolean is_sold;
        public int final_price;
        public String exhibitor_name;
        public String bidder_name;
    }

}
