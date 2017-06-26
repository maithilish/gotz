package org.codetab.gotz.appender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.codetab.gotz.model.Activity.Type;
import org.codetab.gotz.shared.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ListAppender extends Appender {

    static final Logger LOGGER = LoggerFactory.getLogger(ListAppender.class);

    private static final int QUEUE_CAPACITY = 1024;

    private BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(QUEUE_CAPACITY);

    private List<String> list = new ArrayList<>();

    @Inject
    private ActivityService activityService;

    @Inject
    private ListAppender() {
    }

    @Override
    public void run() {
        for (;;) {
            Object item;
            try {
                item = queue.take();
                if (item == Marker.EOF) {
                    break;
                }
                String str = item.toString();
                list.add(str);
            } catch (InterruptedException e) {
                String message = "list appender";
                LOGGER.debug("{}", e);
                activityService.addActivity(Type.GIVENUP, message, e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void append(final Object object) throws InterruptedException {
        queue.put(object);
    }

    public List<String> getList(){
        return list;
    }
}
