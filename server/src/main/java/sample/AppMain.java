package sample;

import com.corundumstudio.socketio.*;

public class AppMain {
  public static void main(String[] args) throws Exception {

    // load
    if (args.length > 0 && args[0].equals("load")) {
      LoadData load = new LoadData();
      load.load();
      return;
    }

    // socket init
    Configuration config = new Configuration();
    config.setHostname("localhost");
    config.setPort(9092);
    final SocketIOServer server = new SocketIOServer(config);

    // AppServer実行
    AppServer app = new AppServer();
    app.start(server);

  }
}