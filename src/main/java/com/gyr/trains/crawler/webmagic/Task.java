package com.gyr.trains.crawler.webmagic;

import com.gyr.trains.crawler.webmagic.pipeline.Pipeline;
import com.gyr.trains.crawler.webmagic.scheduler.Scheduler;

/**
 * Interface for identifying different tasks.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @see Scheduler
 * @see Pipeline
 * @since 0.1.0
 */
public interface Task {

    /**
     * unique id for a task.
     *
     * @return uuid
     */
    public String getUUID();

    /**
     * site of a task
     *
     * @return site
     */
    public Site getSite();

}
