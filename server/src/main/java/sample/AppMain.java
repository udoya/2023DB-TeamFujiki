package sample;

import java.io.File;
import java.util.Optional;
import com.scalar.db.api.Result;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;

public class AppMain {
  public static void main(String[] args) throws Exception {

    Configuration config = new Configuration();
    config.setHostname("localhost");
    config.setPort(9092);

    final SocketIOServer server = new SocketIOServer(config);

    if (args.length > 0 && args[0].equals("load")) {
      LoadData load = new LoadData();
      load.load();
    } else {
      // AppServer実行
      AppServer app = new AppServer();
      app.start();
    }
  }
}