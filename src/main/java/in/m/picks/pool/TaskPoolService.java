package in.m.picks.pool;

public class TaskPoolService extends Pool {

	private static final TaskPoolService INSTANCE = new TaskPoolService();

	private TaskPoolService() {
	}

	public static TaskPoolService getInstance() {
		return INSTANCE;
	}
}
