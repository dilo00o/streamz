/*
 * Copyright 2014 - 2017 the original author or authors.
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

package streamz.camel

import org.apache.camel.impl.DefaultMessage
import org.apache.camel.Message

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

case class StreamMessage[A](body: A, headers: Map[String, Any] = Map.empty) {
  def bodyAs[B](implicit streamContext: StreamContext, tag: ClassTag[B]): B =
    streamContext.convertObject(body)

  def headerAs[B](name: String)(implicit streamContext: StreamContext, tag: ClassTag[B]): B =
    headerOptionAs[B](name).get

  def headerOptionAs[B](name: String)(implicit streamContext: StreamContext, tag: ClassTag[B]): Option[B] =
    headers.get(name).map(streamContext.convertObject[B])

  private[camel] def camelMessage: Message = {
    val result = new DefaultMessage

    headers.foreach {
      case (k, v) => result.setHeader(k, v)
    }

    result.setBody(body)
    result
  }
}

object StreamMessage {
  private[camel] def from[A](camelMessage: Message)(implicit tag: ClassTag[A]): StreamMessage[A] =
    new StreamMessage(camelMessage.getBody(tag.runtimeClass.asInstanceOf[Class[A]]), camelMessage.getHeaders.asScala.toMap)
}
