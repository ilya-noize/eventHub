package com.event.manager.db;

import com.event.manager.domain.event.EventDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to track changes in fields of a Java object
 * @see {@link com.event.manager.domain.event.EventManager#updateEventById(Long, EventDto)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TrackChange {
}
