/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import com.softwareaggov.messaging.libs.interop.MessageInterop;
import com.softwareaggov.messaging.libs.utils.JMSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import java.util.Map;

/**
 * Created by fabien.sanglier on 6/28/16.
 */

@Stateless(name = "JmsSendAndWaitService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MessageInteropLocal.class)
@Remote(MessageInterop.class)
public class JmsSendAndWaitBean extends JmsPublisherBase {
    private static Logger log = LoggerFactory.getLogger(JmsSendAndWaitBean.class);

    private ConnectionFactory jmsConnectionFactory;
    private Destination jmsDestination;
    private Destination jmsReplyToDestination;

    @Resource(name = "jmsResponseWaitMillis")
    private Long jmsResponseWaitMillis = null;

    @Resource(name = "forceCreateTempReplyTo")
    private Boolean forceCreateTempReplyTo = false;

    @Override
    public void ejbCreate() {
        super.ejbCreate();
        jmsConnectionFactory = (ConnectionFactory) lookupEnvResource(JmsPublisherBase.RESOURCE_NAME_CF);
        jmsDestination = (Destination) lookupEnvResource(JmsPublisherBase.RESOURCE_NAME_DEST);
        jmsReplyToDestination = (Destination) lookupEnvResource(JmsPublisherBase.RESOURCE_NAME_REPLYDEST);    }

    @Override
    protected ConnectionFactory getJmsConnectionFactory() {
        return jmsConnectionFactory;
    }

    @Override
    protected Destination getJmsDestination() {
        return jmsDestination;
    }

    @Override
    protected Destination getJmsReplyToDestination() {
        return jmsReplyToDestination;
    }

    @Override
    protected String sendMessage(ConnectionFactory jmsConnectionFactory, Destination destination, boolean sessionTransacted, int sessionAcknowledgeMode, Object payload, Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException {
        // if prefer to use temp queues instead of permanent reply queue, simply set the replyTo to null...
        // the sendAndWaitTextMessage() will take care of creating the temp queue if replyTo=null
        if(forceCreateTempReplyTo)
            replyTo = null;

        Map<JMSHelper.JMSHeadersType, Object> jmsProperties = JMSHelper.getMessageJMSHeaderPropsAsMap(destination, deliveryMode, priority, correlationID, replyTo);
        return JMSHelper.createSender(jmsConnectionFactory).sendAndWaitTextMessage(payload, sessionTransacted, sessionAcknowledgeMode, jmsProperties, headerProperties, jmsResponseWaitMillis);
    }
}
