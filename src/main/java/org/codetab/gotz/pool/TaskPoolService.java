package org.codetab.gotz.pool;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskPoolService extends Pool {

    @Inject
    private TaskPoolService() {
    }
}
