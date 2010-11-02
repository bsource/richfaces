/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.application.push.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.richfaces.application.push.EventAbortedException;
import org.richfaces.application.push.MessageDataSerializer;
import org.richfaces.application.push.MessageException;
import org.richfaces.application.push.Topic;
import org.richfaces.application.push.TopicEvent;
import org.richfaces.application.push.TopicKey;
import org.richfaces.application.push.TopicListener;
import org.richfaces.log.Logger;
import org.richfaces.log.RichfacesLogger;

/**
 * @author Nick Belaevski
 * 
 */
public abstract class AbstractTopic implements Topic {

    private static final Logger LOGGER = RichfacesLogger.APPLICATION.getLogger();
    
    private TopicKey key;
    
    private volatile MessageDataSerializer serializer;
    
    private volatile boolean allowSubtopics;
    
    private List<TopicListener> listeners = new CopyOnWriteArrayList<TopicListener>();
    
    public AbstractTopic(TopicKey key) {
        super();
        this.key = key;
    }
    
    public MessageDataSerializer getMessageDataSerializer() {
        if (serializer == null) {
            return DefaultMessageDataSerializer.instance();
        }
        
        return serializer;
    }

    public void setMessageDataSerializer(MessageDataSerializer serializer) {
        this.serializer = serializer;
    }

    public boolean isAllowSubtopics() {
        return allowSubtopics;
    }
    
    public void setAllowSubtopics(boolean allowSubtopics) {
        this.allowSubtopics = allowSubtopics;
    }
    
    public TopicKey getKey() {
        return key;
    }
    
    public void addTopicListener(TopicListener topicListener) {
        listeners.add(topicListener);
    }
    
    public void removeTopicListener(TopicListener topicListener) {
        listeners.remove(topicListener);
    }

    public void publishEvent(TopicEvent event) throws EventAbortedException {
        for (TopicListener listener: listeners) {
            if (event.isAppropriateListener(listener)) {
                try {
                    event.invokeListener(listener);
                } catch (EventAbortedException e) {
                    throw e;
                } catch (Exception e) {
                    LOGGER.error(MessageFormat.format("Exception invoking listener: {0}", e.getMessage()), e);
                }
            }
        }
    }
    
    public abstract void publish(String subtopic, Object messageData) throws MessageException;
    
}
