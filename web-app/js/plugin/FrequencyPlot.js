var FP_JOB_TYPE = 'acghFrequencyPlot';

var frequencyPlotView;

/**
 * Buttons for Input Panel
 * @type {Array}
 */
var fpInputBarBtnList = ['->',{  // '->' making it right aligned
    xtype: 'button',
    text: 'Run Analysis',
    scale: 'medium',
    iconCls: 'runbutton',
    handler: function () {
        frequencyPlotView.submitFrequencyPlotJob();
    }
}];

var FrequencyPlotInputWidget = Ext.extend(GenericAnalysisInputBar, {

    acghPanel: null,
    groupPanel: null,

    constructor: function(config) {
        FrequencyPlotInputWidget.superclass.constructor.apply(this, arguments);
        this.init();
    },

    init: function() {

        // define child panel configs
        var childPanelConfig = [{
            title: 'Array CGH',
            id: 'fp-input-acgh',
            isDroppable: true,
            notifyFunc: dropOntoCategorySelection,
            toolTipTitle: 'Tip: array CGH',
            toolTipTxt: 'Drag and drop aCGH data here.',
            columnWidth:.5
        },{
            title: 'Group',
            id: 'fp-input-group',
            isDroppable: true,
            notifyFunc: dropOntoCategorySelection,
            toolTipTitle: 'Tip: Group',
            toolTipTxt: 'Drag and drop clinical variables to define multiple groups. '+
                'Please keep in mind that only one variable can be compared, '+
                'e.g. gender (female) with gender (male); '+
                'not gender (female) with age (>60)!',
            columnWidth:.5
        }];

        // create child panels
        this.acghPanel = this.createChildPanel(childPanelConfig[0]);
        this.groupPanel = this.createChildPanel(childPanelConfig[1]);

        // re-draw
        this.doLayout();
    }
});


/**
 * This class represents the whole Group Test view
 * @type {*|Object}
 */
var FrequencyPlotView = Ext.extend(GenericAnalysisView, {

    // input panel
    inputBar : null,

    // job info
    jobInfo : null,

    // constructor
    constructor: function() {
        this.init();
    },

    init: function() {

        // first of all, let's reset all major components
        this.resetAll();

        // draw input panel
        this.inputBar = new FrequencyPlotInputWidget({
            id: 'fpInputPanel',
            title: 'Input Parameters',
            iconCls: 'newbutton',
            renderTo: 'freq_plot_container',
            bbar: this.createInputToolBar()
        });
    },

    resetAll: function () {
        this.tabIndex = 0;
        Ext.destroy(this.inputBar);
    },

    createInputToolBar: function() {
        var _this = this;
        return new Ext.Toolbar({
            height: 30,
            items: fpInputBarBtnList
        });
    },

    createResultPlotPanel: function (jobName, view) {

        var _this = view;

        // initialize image path
        var imagePath = '';

        // get image path
        Ext.Ajax.request({
            url: pageInfo.basePath+"/AcghFrequencyPlot/imagePath",
            method: 'POST',
            success: function(result, request){

                imagePath = result.responseText;

                _this.resultPanel = new GenericPlotPanel({
                    id: 'plotResultCurve',
                    renderTo: 'freq_plot_wrapper',
                    width:'100%',
                    frame:true,
                    height:600,
                    defaults: {autoScroll:true}
                });

                // Getting the template as blue print for survival curve plot.
                // Template is defined in GroupTestRNASeq.gsp
                var frequencyPlotTpl = Ext.Template.from('template-freq-plot');

                var groupVariable = frequencyPlotView.jobInfo.jobInputsJson.groupVariable;
                var groupVariableHtml = groupVariable ? groupVariable.replace('|', '<br />') : '';

                // create data instance
                var region = {
                    filename: imagePath,
                    jobName: frequencyPlotView.jobInfo.name,
                    startDate: frequencyPlotView.jobInfo.startDate,
                    runTime: frequencyPlotView.jobInfo.runTime,
                    regionVariable: frequencyPlotView.jobInfo.jobInputsJson.regionVariable,
                    inputGroupVariable: groupVariableHtml,
                    inputjobType: frequencyPlotView.jobInfo.jobInputsJson.jobType,
                    inputCohort1: frequencyPlotView.jobInfo.jobInputsJson.result_instance_id1,
                    inputCohort2: frequencyPlotView.jobInfo.jobInputsJson.result_instance_id2
                };

                // generate template
                frequencyPlotTpl.overwrite(Ext.get('freq_plot_wrapper'), region);

                // generate download button
                var exportBtn = new Ext.Button ({
                    text : 'Download Result',
                    iconCls : 'downloadbutton',
                    renderTo: 'downloadBtn',
                    handler: function () {
                        _this.downloadFrequencyPlotResult(jobName);
                    }
                });
            },
            params: {
                jobName: jobName
            }
        });

    },

    downloadFrequencyPlotResult: function (jobName) {

        // clean up
        try {
            Ext.destroy(Ext.get('downloadIframe'));
        }
        catch(e) {}

        // get the file
        Ext.DomHelper.append(document.body, {
            tag: 'iframe',
            id:'downloadIframe',
            frameBorder: 0,
            width: 0,
            height: 0,
            css: 'display:none;visibility:hidden;height:0px;',
            src: pageInfo.basePath+"/AcghFrequencyPlot/zipFile?jobName=" + jobName
        });
    },

    onJobFinish: function(jobName, view) {
        this.renderResults(jobName, view);
    },

    getJobByName: function (jobName, view) {
        var _this = this;

        Ext.Ajax.request ({
            // retrieve information about the job (status, inputs, run-time, etc)
            url: pageInfo.basePath+"/asyncJob/getjobbyname",
            method: 'GET',
            success: function (result, request) {
                var resultJSON = JSON.parse(result.responseText);
                _this.jobInfo = resultJSON.jobs[0];
            },
            failure: function (result, request) {
                Ext.Msg.alert('Error', 'Cannot get job details.');
            },
            params: {
                jobName: jobName
            }
        });

    },

    renderResults: function(jobName, view) {
        this.getJobByName(jobName, view);
        this.createResultPlotPanel(jobName, view);
    },

    submitFrequencyPlotJob: function () {

        var formParameters = {}; // init

        // instantiate input elements object with their corresponding validations
        var inputArray = this.get_inputs();

        // define the validator for this form
        var formValidator = new FormValidator(inputArray);

        if (formValidator.validateInputForm()) {

            var acghVal = this.inputBar.acghPanel.getConceptCode();
            var groupVals = this.inputBar.groupPanel.getConceptCodes();

            // compose params
            var formParams = {
                regionVariable: acghVal,
                groupVariable: groupVals,
                jobType: FP_JOB_TYPE
            };

            var job = this.submitJob(formParams, this.onJobFinish, this);

        } else { // something is not correct in the validation
            // empty form parameters
            formParameters = null;
            // display the error message
            formValidator.display_errors();
        }
    },

    get_inputs: function () {

        return  [
            {
                "label" : "Array CGH Data",
                "el" : this.inputBar.acghPanel.getInputEl(),
                "validations" : [
                    {type:"REQUIRED"},
                    {type:"HIGH_DIMENSIONAL_ACGH"}
                ]
            },
            /** Group doesn't need to be defined for a frequency plot nor need to consist of at least two elements
             *   If no group is defined, a single frequency plot of all subjects in the cohort will be created
             *   If one group is defined, a single frequency plot of that group within the cohort will be created
             * {
             *    "label" : "Group",
             *    "el" : this.inputBar.groupPanel.getInputEl(),
             *    "validations" : [
             *       {type:"REQUIRED"},
             *       {type:"GROUP_VARIABLE"}
             *    ]
             * }
             */
        ];
    }

});

function loadAcghFrequencyPlotView(){
    // everything starts here ..
    frequencyPlotView = new FrequencyPlotView();
}

