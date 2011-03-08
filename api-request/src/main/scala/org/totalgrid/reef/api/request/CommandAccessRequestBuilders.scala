/**
 * Copyright 2011 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Green Energy Corp licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.totalgrid.reef.api.request

import scala.collection.JavaConversions._
import org.totalgrid.reef.proto.Commands.CommandAccess

object CommandAccessRequestBuilders {

  def allowAccessForCommand(command: String) =
    CommandAccess.newBuilder.addCommands(command).setAccess(CommandAccess.AccessMode.ALLOWED).build

  def allowAccessForCommands(commands: List[String]) =
    CommandAccess.newBuilder.addAllCommands(commands).setAccess(CommandAccess.AccessMode.ALLOWED).build

  def blockCommands(commands: List[String]) = {
    CommandAccess.newBuilder.addAllCommands(commands).setAccess(CommandAccess.AccessMode.BLOCKED).build
  }

  def allAccessEntries = CommandAccess.newBuilder.setUid("*").build

  def getForUid(uid: String) = CommandAccess.newBuilder.setUid(uid).build

  def getForUser(user: String) = CommandAccess.newBuilder.setUser(user).build

  def deleteByUid(uid: String) = CommandAccess.newBuilder.setUid(uid).build
  def delete(cmd: CommandAccess) = CommandAccess.newBuilder.setUid(cmd.getUid).build
}