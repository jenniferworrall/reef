/**
 * Copyright 2011 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. Green Energy
 * Corp licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.totalgrid.reef.api.japi.client.rpc.impl.builders

import org.totalgrid.reef.api.japi.settings.NodeSettings
import org.totalgrid.reef.api.proto.Application.ApplicationConfig

object ApplicationConfigBuilders {

  def makeProto(config: NodeSettings, instanceName: String, caps: List[String]) = {
    val b = ApplicationConfig.newBuilder()

    def randomString(n: Int): String = {
      val sb = new StringBuilder
      for (i <- 1 to n) sb.append(util.Random.nextPrintableChar)
      sb.toString
    }

    b.setInstanceName(instanceName)
    b.setNetwork(config.getNetwork)
    b.setLocation(config.getLocation)
    caps.foreach(b.addCapabilites(_))
    b.setProcessId(randomString(8))
    b.build
  }
}