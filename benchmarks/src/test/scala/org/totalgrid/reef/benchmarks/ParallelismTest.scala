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
package org.totalgrid.reef.benchmarks

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.totalgrid.reef.benchmarks.system.EndpointLoaderBenchmark

@RunWith(classOf[JUnitRunner])
class ParallelismTest extends BenchmarkTestBase {

  var readings = List.empty[BenchmarkReading]

  override protected def afterAll() {
    val results = readings.groupBy(_.csvName)
    val histogramResults = Histogram.getHistograms(results)

    BenchmarkUtilities.writeHistogramCsvFiles(histogramResults, "endpointLoading")
    BenchmarkUtilities.writeCsvFiles(results, "endpointLoading-")
  }

  val endpoints = 10
  val pointsPerEndpoint = 20
  val parallelisms = List(1, 5, 10)
  val batchSize = 1000

  parallelisms.foreach { threads =>
    test("Loading " + endpoints + " endpoints with " + pointsPerEndpoint + " points each with " + threads + " loaders") {

      val test = new EndpointLoaderBenchmark(endpoints, pointsPerEndpoint, threads, batchSize)
      readings :::= test.runTest(client, Some(Console.out))
    }
  }

}