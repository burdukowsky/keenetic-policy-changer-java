package top.burdukowsky.keeneticpolicychangerjava;

public interface IKeeneticApi {
    boolean setDefaultPolicy(String mac);
    boolean setOfflinePolicy(String mac);
    boolean setCustomPolicy(String mac, String policy);
}
