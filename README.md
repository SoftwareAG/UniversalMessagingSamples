j2ee_messaging_apps: JMS Messaging Apps using common Java EE patterns and Resource Adapter JCA patterns
=========================================================================================================

Author: [Fabien Sanglier](mailto:Fabien.Sanglier@softwareag.com)

Project Source: <https://github.com/lanimall/j2ee_messaging_apps>

What is it?
--------------------------------------------
Modular J2EE messaging applications that rely on common JCA Resource Adapters to interact with Messaging provider such as SoftwareAG Universal Messaging.
Using these applications, it's easy to create different message pub/sub designs by "plugging" multiple apps together via EJB lookups.

These J2EE apps are compliant with EJB specs and JCA specs, and have been succesfully tested on both JBOSS EAP 6.x and IBM Websphere 8.x platforms,
interacting with the the [Software AG Universal Messaging Server](http://www2.softwareag.com/it/products/terracotta/universal_messaging.aspx) via its JCA-compliant Resource Adapter.

**For any issue**: 
Please create a ticket on github and the developper will make every attempts to address it as soon as possible.
Or even better: feel free to fork, fix it and submit a PR.

Licensing - Apache-2.0
--------------------------------------------

This project is Licensed under the Apache License, Version 2.0 (the "License");
You may not use this project except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Code Content
--------------------------------------------

* SimpleJmsSend
  * Sends JMS messages to UM queues/topics via JCA proxies (JCA Admin objects and Conection Factories) using the Resource Adapter. Multiple approach to test:
    * "Send And Forget"
    * "Send And Wait For Reply"
* SimpleJmsConsume
  * Consumes JMS messages from queues/topics using the UM Resource Adapter,
  * Ability to perform some mock processing (eg. sleep time, mock exceptions at intervals, etc...),
  * Ability to call remote EJBs that implement the JmsPublisherRemote interface (eg. the ones in the SimpleJmsSend application) in order to chain message comsumption with extra message sending,
  * Ability to reply if the "replyTo" field is specified in the message (or if a default "replyTo" is set)
* libs
  * Shared library that contains global code and especially the custom-built JMSHelper that encapsulate simple JMS contructs
  * A simple counter (accessible via servlet) is also made available to track various metrics (message sent, message consumed, processing success, processing errors, etc...)
  
Resource Adapter Guides
--------------------------------------------

Detail guides to setup Software AG Universal Messaging Resource Adapter on both JBOSS EAP 6 and IBM Websphere 8.x are availble at:

* [Integration and Configuration of SoftwareAG’s Universal Messaging with JBOSS EAP 6.1](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/Integration+and+Configuration+of+SoftwareAG’s+Universal+Messaging+with+JBOSS+EAP+6.1)
* [Integration and configuration of sofwareag’s universal messaging with ibm websphere application server](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/integration+and+configuration+of+sofwareag’s+universal+messaging+with+ibm+websphere+application+server)

Quick Start Guide with Different Samples
--------------------------------------------

All samples below require a JCA Resouyrce Adapter installed on the Application Server. 
Please refer to the section "Resource Adapter Guides" for detailed guides.

### Sample Profile 1: JMS "Send and Forget"

This sample sends JMS messages to a queue, and consume from that same queue (2 apps will be built and deployed)
The apps come with pre-configured resource names that must be created on the application server(s). 

#### Build

```
   ant -Dj2ee-jms-examples.deployment_profilename=profile1 assemble
```

2 applications should be created:
* dist/SimpleJmsSend.ear
* dist/SimpleJmsConsume.ear

#### Configuration Pre-requisites before deploying the apps on the Application Server

The application built in the previous stage requires some JCA Resource Adapter resources to be created on the application server.
While all the resource names can be easily modified, for the purpose of this quick start, the default apps require the following resources:

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/SimpleQueue
* Connection Factories:
  * SimpleJmsSendConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Managed Connection Factory:
  * SimpleJmsSendConnectionFactory
    * ConnectionFactoryJndiName=SimpleJmsSendConnectionFactory
* Managed Admin Object:
  * SimpleJmsSendDestination
    * DestinationJndiName=JMSSamples/SimpleQueue
* Managed Activation Specification:
  * SimpleJmsConsumer
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/SimpleQueue

**NOTE**: Only Websphere provides abstraction for activation specs.
For other app servers (eg. Jboss), the activation specs are directly provided in the MDB descriptor (eg. jboss-ejb3.xml)
And the application already has that sertup to consumes from JMSSamples/SimpleQueue (see SimpleJmsConsume/build.properties for details)

#### Deploy the app and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages, simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSend/JmsSendAndForget

To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSend/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsume/messagecounters

**NOTE**: In the event that the application does not start, it's very likely that something was not setup right with the resource adapter
and/or related configurations. Review the application server logs to see what may have gone wrong.

### Sample Profile 2: JMS "Send with Reply"

This sample sends JMS messages to a queue with a ReplyTo header. 
2 options for sends: 
* "Send and Forget" (Asynchronous send and reply - Reply consumed by another app)
* "Send and Wait" for reply (Synchronous send and wait - reply consumed in the same waiting thread)

Then, a consume app receives the message, processes it and reply to the appropriate destination basded on the ReplyTo header embedded in the message.
Finally, a 3rd app consumes the Asynchronous reply message (used only by the "Asynchronous send and reply" case)

#### Build

```
   ant -Dj2ee-jms-examples.deployment_profilename=profile2 assemble
```

3 applications should be created:
* dist/SimpleJmsSendWithReply.ear
* dist/SimpleJmsConsumeWaitAndReply.ear
* dist/SimpleJmsConsumeTheReply.ear

#### Configuration Pre-requisites before deploying the apps on the Application Server

The application built in the previous stage requires some JCA Resource Adapter resources to be created on the application server.
While all the resource names can be easily modified, for the purpose of this quick start, the default apps require the following resources:

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/RequestQueue
  * JMSSamples/ReplyQueueSync
  * JMSSamples/ReplyQueueAsync
* Connection Factories:
  * SimpleJmsSendConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Managed Connection Factory:
  * SimpleJmsSendConnectionFactory
    * ConnectionFactoryJndiName=SimpleJmsSendConnectionFactory
* Managed Admin Object:
  * SimpleJmsSendWithReplyDestination
    * DestinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsSendReplyToDestination
    * DestinationJndiName=JMSSamples/ReplyQueueAsync
* Managed Activation Specification:
  * SimpleJmsConsumeRequest
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsConsumeAsyncReply
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/ReplyQueueAsync
 
**NOTE**: Only Websphere provides abstraction for activation specs.
For other app servers (eg. Jboss), the activation specs are directly provided in the MDB descriptor (eg. jboss-ejb3.xml)
And the application already has that sertup to consumes from JMSSamples/SimpleQueue (see SimpleJmsConsume/build.properties for details)

#### Deploy the app and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages (using either "Send And Forget" or "Send and Wait"), simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/JmsSendAndForget
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/JmsSendAndWait

To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeWaitAndReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeTheReply/messagecounters

### Sample Profile 3a: JMS "Send to a Driver queue" which drives "Send And Forget With Reply" operations

This sample sends JMS messages to a "driver" queue 
Then, a consumer app consumes the driver queue messages, and calls the "Send And Forget" EJB from the "SimpleJmsSendWithReply.ear" app of Profile 2.
Then, as in profile 2, a consume app receives the message, processes it and reply to the appropriate destination basded on the ReplyTo header embedded in the message.
Finally, a 3rd app consumes the Asynchronous reply message (used only by the "Asynchronous send and reply" case)

**NOTE**: In order to better customize better what you send on that driver queue, you could use any JMS-capable program (eg. jmeter)
to send the desired JMS message with the right payload and headers to that driver queue (instead of the sample "SimpleJmsSendAndForgetToDriver.ear" app)
That way, the right message profile can flow through the various apps (as opposed to built-in sample messages)

#### Build

```
   ant -Dj2ee-jms-examples.deployment_profilename=profile3a assemble
```

5 applications should be created:
* dist/SimpleJmsSendAndForgetToDriver.ear
* dist/SimpleJmsConsumeAndForwardToEJB1.ear
* dist/SimpleJmsSendWithReply.ear
* dist/SimpleJmsConsumeWaitAndReply.ear
* dist/SimpleJmsConsumeTheReply.ear

#### Configuration Pre-requisites before deploying the apps on the Application Server

The application built in the previous stage requires some JCA Resource Adapter resources to be created on the application server.
While all the resource names can be easily modified, for the purpose of this quick start, the default apps require the following resources:

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/DriverQueue
  * JMSSamples/RequestQueue
  * JMSSamples/ReplyQueueAsync
* Connection Factories:
  * SimpleJmsSendConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Managed Connection Factory:
  * SimpleJmsSendConnectionFactory
    * ConnectionFactoryJndiName=SimpleJmsSendConnectionFactory
* Managed Admin Object:
  * SimpleJmsSendDriverDestination
    * DestinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsSendWithReplyDestination
    * DestinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsSendReplyToDestination
    * DestinationJndiName=JMSSamples/ReplyQueueAsync

* Managed Activation Specification:
  * SimpleJmsConsumerDriverQueue
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsConsumeRequest
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsConsumeAsyncReply
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/ReplyQueueAsync

**NOTE**: Only Websphere provides abstraction for activation specs.
For other app servers (eg. Jboss), the activation specs are directly provided in the MDB descriptor (eg. jboss-ejb3.xml)
And the application already has that sertup to consumes from JMSSamples/SimpleQueue (see SimpleJmsConsume/build.properties for details)

#### Deploy the app and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages to the driver queue, simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/JmsSendAndForget

OR use any JMS-capable program (eg. jmeter) to directly send the desired JMS message with the right payload and headers to that driver queue.
   
To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeAndForwardToEJB/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeWaitAndReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeTheReply/messagecounters

### Sample Profile 3b: JMS "Send to a Driver queue" which drives "Send And Wait" operations

Same as profile 3a, but in this case, the consumer app which consumes the driver queue messages will calls 
the "Send And Wait" EJB from the "SimpleJmsSendWithReply.ear" app of Profile 2.

#### Build

```
   ant -Dj2ee-jms-examples.deployment_profilename=profile3b assemble
```

5 applications should be created:
* dist/SimpleJmsSendAndForgetToDriver.ear
* dist/SimpleJmsConsumeAndForwardToEJB2.ear
* dist/SimpleJmsSendWithReply.ear
* dist/SimpleJmsConsumeWaitAndReply.ear
* dist/SimpleJmsConsumeTheReply.ear

#### Configuration Pre-requisites before deploying the apps on the Application Server

The application built in the previous stage requires some JCA Resource Adapter resources to be created on the application server.
While all the resource names can be easily modified, for the purpose of this quick start, the default apps require the following resources:

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/DriverQueue
  * JMSSamples/RequestQueue
  * JMSSamples/ReplyQueueSync

* Connection Factories:
  * SimpleJmsSendConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Managed Connection Factory:
  * SimpleJmsSendConnectionFactory
    * ConnectionFactoryJndiName=SimpleJmsSendConnectionFactory

* Managed Admin Object:
  * SimpleJmsSendDriverDestination
    * DestinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsSendWithReplyDestination
    * DestinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsSendReplyToDestination
     * DestinationJndiName=JMSSamples/ReplyQueueSync

* Managed Activation Specification:
  * SimpleJmsConsumerDriverQueue
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsConsumeRequest
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsConsumeAsyncReply
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/ReplyQueueAsync

**NOTE**: Only Websphere provides abstraction for activation specs.
For other app servers (eg. Jboss), the activation specs are directly provided in the MDB descriptor (eg. jboss-ejb3.xml)
And the application already has that sertup to consumes from JMSSamples/SimpleQueue (see SimpleJmsConsume/build.properties for details)

#### Deploy the app and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages to the driver queue, simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/JmsSendAndForget

OR use any JMS-capable program (eg. jmeter) to directly send the desired JMS message with the right payload and headers to that driver queue.

To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeAndForwardToEJB/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeWaitAndReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeTheReply/messagecounters