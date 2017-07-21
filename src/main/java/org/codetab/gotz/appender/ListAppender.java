package org.codetab.gotz.appender;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.gotz.model.Activity.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ListAppender extends Appender {

    static final Logger LOGGER = LoggerFactory.getLogger(ListAppender.class);

    private List<Object> list = new ArrayList<>();

    @Inject
    private ListAppender() {
    }

    @Override
    public void run() {
        for (;;) {
            Object item;
            try {
                item = getQueue().take();
                if (item == Marker.EOF) {
                    break;
                }
                list.add(item);
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
        getQueue().put(object);
    }

    public List<Object> getList() {
        return list;
    }
}
