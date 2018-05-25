package core;

public class Starter {
    public static void main(String[] args) throws Exception {
        new Starter().start();
    }

    private void start() throws Exception {
        switch (Config.FUNCTIONALITY) {
            case SERVICE:
                new ServiceStarter().start();
                break;
            case MANAGEMENT:
                new ManagementStarter().start();
                break;
        }
    }
}
