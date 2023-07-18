package classes;

public class BidOnResponse {
    public int price;
    public int user_id;
    public int time;
    // 入札が失敗した場合(現在の価格を下回る入札金額)は全てのフィールドを0とする
}
