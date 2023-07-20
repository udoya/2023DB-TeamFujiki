package classes;

import java.util.List;

public class InitStateResponse {
    // public List<Item> items;
    // ここだけやばいのでDBの値をそのままマッピングさせることにする
    public List<Object> items;
    public int user_id;
    public int remaining_time;
    public CurrentItem current_item;
    public boolean is_exhibitor;

    // public AuctionHistory auction_history;

    public void setIs_exhibitor(boolean is_exhibitor) {
        this.is_exhibitor = is_exhibitor;
    }

    public void setRemaining_time(int remaining_time) {
        this.remaining_time = remaining_time;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setCurrent_item(CurrentItem current_item) {
        this.current_item = current_item;
    }

    public class Item {
        public String item_name;
        public int item_id;
        public boolean is_sold;

        public void setIs_sold(boolean is_sold) {
            this.is_sold = is_sold;
        }

        public void setItem_name(String item_name) {
            this.item_name = item_name;
        }

        public void setItem_id(int item_id) {
            this.item_id = item_id;
        }
    }

    public class CurrentItem {
        public String item_name;
        public int item_id;
        public int user_id;
        public List<History> history = new java.util.ArrayList<History>();

        public class History {
            public String user_name;
            public int price;
            public int time;

            public void setPrice(int price) {
                this.price = price;
            }

            public void setTime(int time) {
                this.time = time;
            }

            public void setUser_name(String user_name) {
                this.user_name = user_name;
            }
        }

        public void setItem_id(int item_id) {
            this.item_id = item_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public void setItem_name(String item_name) {
            this.item_name = item_name;
        }

        public void setHistory(History his) {
            this.history.add(his);
        }

    }

    public class AuctionHistory {
        public String item_name;
        public boolean is_sold;
        public int final_price;
        public String exhibitor_name;
        public String bidder_name;
    }

}
