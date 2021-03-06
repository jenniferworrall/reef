/**
 * Copyright 2011 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. Green Energy
 * Corp licenses this file to you under the GNU Affero General Public License
 * Version 3.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.totalgrid.reef.shell.proto

import org.apache.felix.gogo.commands.{ Argument, Command }

import scala.collection.JavaConversions._
import presentation.ApplicationView

@Command(scope = "application", name = "list", description = "Prints application information")
class ApplicationListCommand extends ReefCommandSupport {

  def doCommand() = {
    ApplicationView.printTable(services.getApplications.toList)
  }
}

@Command(scope = "application", name = "view", description = "Prints detailed application information")
class ApplicationViewCommand extends ReefCommandSupport {

  @Argument(index = 0, name = "application name", description = "Name of the application we want to inspect.", required = true, multiValued = false)
  var applicationName: String = null

  def doCommand() = {
    val appConfig = services.getApplicationByName(applicationName)
    ApplicationView.printInspect(appConfig)
  }
}

@Command(scope = "application", name = "remove", description = "Remove an application from list. If we try to remove a running application it will cause that app to restart. (experts-only!)")
class ApplicationRemoveCommand extends ReefCommandSupport {

  @Argument(index = 0, name = "application name", description = "Name of the application we want to remove.", required = true, multiValued = false)
  var applicationName: String = null

  def doCommand() = {
    val appConfig = services.getApplicationByName(applicationName)

    services.unregisterApplication(appConfig)
    ApplicationView.printTable(services.getApplications.toList)
  }
}