package com.gyr.trains.crawler.webmagic.pipeline;

import com.gyr.trains.crawler.webmagic.Spider;

import java.util.List;

/**
 * Pipeline that can collect and store results. <br>
 * Used for {@link Spider#getAll(java.util.Collection)}
 *
 * @author code4crafter@gmail.com
 * @since 0.4.0
 */
public interface CollectorPipeline<T> extends Pipeline {

    /**
     * Get all results collected.
     *
     * @return collected results
     */
    public List<T> getCollected();
}
