package com.gyr.trains.crawler.webmagic.pipeline;

import com.gyr.trains.crawler.webmagic.ResultItems;
import com.gyr.trains.crawler.webmagic.Task;

/**
 * Pipeline is the persistent and offline process part of crawler.<br>
 * The interface Pipeline can be implemented to customize ways of persistent.
 *
 * @author code4crafter@gmail.com <br>
 * @see ConsolePipeline
 * @see FilePipeline
 * @since 0.1.0
 */
public interface Pipeline {

    /**
     * Process extracted results.
     *
     * @param resultItems resultItems
     * @param task        task
     */
    public void process(ResultItems resultItems, Task task);
}
