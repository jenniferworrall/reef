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
package org.totalgrid.reef.measurementstore.squeryl.activator

import org.osgi.framework.{ BundleContext, BundleActivator }
import com.weiglewilczek.scalamodules._
import org.totalgrid.reef.measurementstore.MeasurementStore
import org.totalgrid.reef.measurementstore.squeryl.SqlMeasurementStore
import org.totalgrid.reef.osgi.OsgiConfigReader
import org.totalgrid.reef.persistence.squeryl.{ DbInfo, DbConnector }

class Activator extends BundleActivator {

  def start(context: BundleContext) {

    // initialize the connection, expecting that the DbConnector is already registered
    val sql = new DbInfo(OsgiConfigReader(context, "org.totalgrid.reef.sql").getProperties)
    DbConnector.connect(sql, context)

    context.createService(SqlMeasurementStore, "org.totalgrid.reef.mstore" -> "sql", interface[MeasurementStore])
  }

  def stop(context: BundleContext) {}

}