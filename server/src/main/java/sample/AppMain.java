package sample;


public class AppMain {
  public static void main(String[] args) throws Exception {

    // load
    if (args.length > 0 && args[0].equals("load")) {
      LoadData load = new LoadData();
      load.load();
      return;
    }

    // socket init

    // AppServer実行
    AppServer app = new AppServer();
    app.start();
  }
}