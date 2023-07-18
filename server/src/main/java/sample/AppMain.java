package sample;

import java.io.File;
import java.util.Optional;
import com.scalar.db.api.Result;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class AppMain {
  public static void main(String[] args) throws Exception {
    if (args.length > 0 && args[0].equals("load")) {
      LoadData load = new LoadData();
      load.load();
    } else {
      //Appserver実行
    //   AppServer app = new AppServer();
    //   app.start();
    }
  }
}