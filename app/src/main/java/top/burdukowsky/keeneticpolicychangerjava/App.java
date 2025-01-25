package top.burdukowsky.keeneticpolicychangerjava;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;

@Parameters(separators = "=")
public class App {
    @Parameter(names = "-ip")
    String ip = "192.168.1.1";
    @Parameter(names = "-port", validateWith = PositiveInteger.class)
    int port = 23;
    @Parameter(names = "-login")
    String login = "admin";
    @Parameter(names = "-password", password = true, required = true)
    String password;
    @Parameter(names = "-action", required = true)
    Action action;
    @Parameter(names = "-mac", required = true)
    String mac;
    @Parameter(names = "-policy")
    String policy;

    public static void main(String[] args) {
        App app = new App();
        JCommander.newBuilder()
                .addObject(app)
                .build()
                .parse(args);
        app.run();

    }

    public void run() {
        var api = new TelnetKeeneticApi(ip, port, login, password);
        switch (action) {
            case SET_DEFAULT:
                api.setDefaultPolicy(mac);
                break;
            case SET_CUSTOM:
                if (policy == null) {
                    System.err.println("Укажите параметр -policy");
                    return;
                }
                api.setCustomPolicy(mac, policy);
                break;
            case SET_OFFLINE:
                api.setOfflinePolicy(mac);
                break;
        }
    }
}
