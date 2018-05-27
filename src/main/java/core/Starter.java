package core;

public class Starter {
    public static void main(String[] args) {
        new Starter().start(args);
    }

    private void start(String[] args) {
        try {
            switch (Config.FUNCTIONALITY) {
                case SERVICE:
                    ServiceStarter.main(args);
                    break;
                case MANAGEMENT:
                    ManagementStarter.main(args);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
