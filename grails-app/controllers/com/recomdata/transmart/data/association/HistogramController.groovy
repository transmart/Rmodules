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

class HistogramController {

  def RModulesOutputRenderService

  def histogramOut = {
    def ArrayList<String> imageLinks = new ArrayList<String>()

    //Gather the image links.
    RModulesOutputRenderService.initializeAttributes(params.jobName, "Histogram", imageLinks)

    render(template: "/plugin/histogram_out",
        model:[
          imageLocations: imageLinks,
          zipLink: RModulesOutputRenderService.zipLink
        ],
        contextPath: pluginContextPath)
  }

}
