package in.m.picks.pool;

public final class AppenderPoolService extends Pool {

    private static final AppenderPoolService INSTANCE = new AppenderPoolService();

    private AppenderPoolService() {
    }

    public static AppenderPoolService getInstance() {
        return INSTANCE;
    }
}
