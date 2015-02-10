package jobs

import jobs.misc.AnalysisConstraints
import jobs.steps.ParametersFileStep
import jobs.steps.Step
import jobs.steps.WriteFileStep
import org.apache.log4j.Logger
import org.quartz.JobExecutionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource

import javax.annotation.Resource

abstract class AbstractAnalysisJob {

    static final String PARAM_ANALYSIS_CONSTRAINTS = 'analysisConstraints'

    Logger log = Logger.getLogger(getClass())

    @Autowired
    UserParameters params

    @Autowired
    AnalysisConstraints analysisConstraints

    @Autowired
    MessageSource messageSource

    @Resource(name = 'jobName')
    String name /* The job instance name */

    /* manually injected properties
     *********************/

    Closure updateStatus

    Closure setStatusList

    //FIXME Where to get analysis name?
    String getAnalysisName() {
        name.split('-')[1]
    }

    protected String headerMessage(String header) {
        messageSource.getMessage("jobs.${analysisName}.outputFile.header.${header}", null, header, null)
    }

    File topTemporaryDirectory

    File scriptsDirectory

    /* TODO: Used to build temporary working directory for R processing phase.
             This is called subset1_<study name>. What about subset 2? Is this
             really needed or an arbitrary directory is enough? Is it required
             due to some interaction with clinical data? */
    String studyName

    /* end manually injected properties
     *************************/

    File temporaryDirectory /* the workingDirectory */


    final void run() {
        validateName()
        setupTemporaryDirectory()

        List<Step> stepList = [
                /* we need the parameters file not just for troubleshooting
                 * but also because we need later to read the result instance
                 * ids and determine if we should create the zip with the
                 * intermediate data */
                new ParametersFileStep(
                        temporaryDirectory: temporaryDirectory,
                        params: params),
                new WriteFileStep(
                        temporaryDirectory: temporaryDirectory,
                        fileName: 'README.txt',
                        fileContent: messageSource.getMessage("jobs.${analysisName}.readmeFileContent", null, null, null))
        ]
        stepList += prepareSteps()

        // build status list
        setStatusList(stepList.collect({ it.statusName }).grep())

        for (Step step in stepList) {
            if (step.statusName) {
                updateStatus step.statusName
            }

            step.execute()
        }

        updateStatus('Completed', forwardPath)
    }

    abstract protected List<Step> prepareSteps()

    abstract protected List<String> getRStatements()

    private void validateName() {
        if (!(name ==~ /^[0-9A-Za-z-]+$/)) {
            throw new JobExecutionException("Job name mangled")
        }
    }

    private void setupTemporaryDirectory() {
        temporaryDirectory = new File(new File(topTemporaryDirectory, name), 'workingDirectory')
        temporaryDirectory.mkdirs()
    }

    abstract protected getForwardPath()
}
