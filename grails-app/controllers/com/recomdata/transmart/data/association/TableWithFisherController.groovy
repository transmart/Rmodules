/*************************************************************************   
 * Copyright 2008-2012 Janssen Research & Development, LLC.
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
 ******************************************************************/

package com.recomdata.transmart.data.association

class TableWithFisherController {

    def RModulesOutputRenderService

    def fisherTableOut = {
        //Grab the job ID from the query string.
        String jobName = params.jobName

        //Gather the image links.
        RModulesOutputRenderService.initializeAttributes(jobName, null, null)

        String tempDirectory = RModulesOutputRenderService.tempDirectory

        //Traverse the temporary directory for the LinearRegression files.
        def tempDirectoryFile = new File(tempDirectory)

        def countDataFilesMap = tempDirectoryFile.listFiles().collectEntries { File file ->
            def matcher = file.name =~ /Count(.*).txt/
            if (matcher.matches()) {
                [matcher[0][1], file]
            } else {
                [:]
            }
        }

        def statisticsDataFilesMap = tempDirectoryFile.listFiles().collectEntries { File file ->
            def matcher = file.name =~ /statisticalTests(.*).txt/
            if (matcher.matches()) {
                [matcher[0][1], file]
            } else {
                [:]
            }
        }

        render(template: "/plugin/tableWithFisher_out", model: [countDataFilesMap: countDataFilesMap,
                                                                statisticsDataFilesMap: statisticsDataFilesMap,
                                                                zipLink: RModulesOutputRenderService.zipLink],
                                                                contextPath: pluginContextPath)
    }
}
