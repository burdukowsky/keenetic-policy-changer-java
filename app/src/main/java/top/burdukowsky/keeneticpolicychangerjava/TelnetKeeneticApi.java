package top.burdukowsky.keeneticpolicychangerjava;

public class TelnetKeeneticApi implements IKeeneticApi {
    private final String ip;
    private final int port;
    private final String login;
    private final String password;

    public TelnetKeeneticApi(String ip, int port, String login, String password) {
        this.ip = ip;
        this.port = port;
        this.login = login;
        this.password = password;
    }

    private boolean work(String[] commands) {
        try {
            SimpleTelnetClient client = new SimpleTelnetClient(this.ip, this.port);
            client.connect();

            String result = client.waitFor("Login:");
            System.out.println("Got " + result);
            client.send(this.login);

            result = client.waitFor("Password:");
            System.out.println("Got " + result);
            client.send(this.password);

            for (String command : commands) {
                result = client.waitFor(">");
                System.out.println("Got " + result);
                client.send(command);
            }

            result = client.waitFor(">");
            System.out.println("Got " + result);
            client.send("exit");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDefaultPolicy(String mac) {
        String[] commands = {
                String.format("ip hotspot host %s permit", mac),
                String.format("ip hotspot no host %s policy", mac)
        };
        return work(commands);
    }

    public boolean setOfflinePolicy(String mac) {
        String[] commands = {
                String.format("ip hotspot host %s deny", mac)
        };
        return work(commands);
    }

    public boolean setCustomPolicy(String mac, String policy) {
        String[] commands = {
                String.format("ip hotspot host %s permit", mac),
                String.format("ip hotspot host %s policy %s", mac, policy)
        };
        return work(commands);
    }
}
