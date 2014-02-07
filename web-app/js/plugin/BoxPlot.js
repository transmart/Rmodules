/**
 * Where everything starts
 */
function loadBoxPlotView(){
    boxPlotView.register_drag_drop();
    boxPlotView.clear_high_dimensional_input('divIndependentVariable');
    boxPlotView.clear_high_dimensional_input('divDependentVariable');
    boxPlotView.toggle_binning();
}

/**
 * Constructor
 * @constructor
 */
var BoxPlotView = function () {
    RmodulesView.call(this);
}

/**
 * Inherit RModulesView
 * @type {RmodulesView}
 */
BoxPlotView.prototype = new RmodulesView();

/**
 * Correct pointer
 * @type {BoxPlotView}
 */
BoxPlotView.prototype.constructor = BoxPlotView;

BoxPlotView.prototype.illegalBinning = function(independentVariables, dependentVariables) {
  if (!GLOBAL.Binning) {
    return false;
  }

  // I would rather have kept this function ignorant of the outside world.
  var variableToBeBinned = Ext.get("selBinVariableSelection").getValue();

  if (variableToBeBinned == "IND" && independentVariables.length > 1) {
    Ext.Msg.alert('Illegal binning', 'More that 1 independent variable selected');
    return true;
  }

  if (variableToBeBinned == "DEP" && dependentVariables.length > 1) {
    Ext.Msg.alert('Illegal binning', 'More that 1 dependent variable selected');
    return true;
  }

  return false;
}

/**
 * Get form parameters
 * TODO: Refactor the validation to define validation in FormValidator.js instead here
 * @param form
 * @returns {*}
 */
BoxPlotView.prototype.get_form_params = function (form) {

    var dependentVariableEle = Ext.get("divDependentVariable");
    var independentVariableEle = Ext.get("divIndependentVariable");

    var dependentNodeList = createNodeTypeArrayFromDiv(dependentVariableEle,"setnodetype")
    var independentNodeList = createNodeTypeArrayFromDiv(independentVariableEle,"setnodetype")

    //If the user dragged in multiple node types, throw an error.
    if (dependentNodeList.length > 1) {
        Ext.Msg.alert('Error', 'Dependent variable must have same type');
        return;
    }

    if (independentNodeList.length > 1) {
        Ext.Msg.alert('Error', 'Independent variable must have same type');
        return;
    }

    /**
     * To check if node is categorical or not
     * @param nodeTypes
     * @returns {boolean}
     * @private
     */
    var _isCategorical = function (nodeTypes) {
        return (nodeTypes[0] == "null") ? true : false;
    } //

    var dependentVariableConceptCode = "";
    var independentVariableConceptCode = "";

    //If we have multiple items in the Dependent variable box, or if we are binning on it, then we have to flip the graph image.
    var flipImage = false;

    if ((dependentVariableEle.dom.childNodes.length > 1) || (GLOBAL.Binning && document.getElementById("selBinVariableSelection").value == "DEP")) {
        flipImage = true;
    }

    //If the category variable element has children, we need to parse them and concatenate their values.
    if (independentVariableEle.dom.childNodes[0]) {
        //Loop through the category variables and add them to a comma seperated list.
        for (nodeIndex = 0; nodeIndex < independentVariableEle.dom.childNodes.length; nodeIndex++) {
            //If we already have a value, add the seperator.
            if (independentVariableConceptCode != '') independentVariableConceptCode += '|'

            //Add the concept path to the string.
            independentVariableConceptCode += getQuerySummaryItem(independentVariableEle.dom.childNodes[nodeIndex]).trim()
        }
    }

    //If the category variable element has children, we need to parse them and concatenate their values.
    if (dependentVariableEle.dom.childNodes[0]) {
        //Loop through the category variables and add them to a comma seperated list.
        for (nodeIndex = 0; nodeIndex < dependentVariableEle.dom.childNodes.length; nodeIndex++) {
            //If we already have a value, add the seperator.
            if (dependentVariableConceptCode != '') dependentVariableConceptCode += '|'

            //Add the concept path to the string.
            dependentVariableConceptCode += getQuerySummaryItem(dependentVariableEle.dom.childNodes[nodeIndex]).trim()
        }
    }

    //Make sure the user entered some items into the variable selection boxes.
    if (dependentVariableConceptCode == '') {
        Ext.Msg.alert('Missing input!', 'Please drag at least one concept into the dependent variable box.');
        return;
    }

    if (independentVariableConceptCode == '') {
        Ext.Msg.alert('Missing input!', 'Please drag at least one concept into the independent variable box.');
        return;
    }

    if (this.illegalBinning(independentVariableEle.dom.childNodes, dependentVariableEle.dom.childNodes)) {
      return;
    }

    var variablesConceptCode = dependentVariableConceptCode + "|" + independentVariableConceptCode;

    var formParams = {
        dependentVariable: dependentVariableConceptCode,
        dependentVariableCategorical: _isCategorical(dependentNodeList),
        independentVariable: independentVariableConceptCode,
        independentVariableCategorical: _isCategorical(independentNodeList),
        jobType: 'BoxPlot',
        variablesConceptPaths: variablesConceptCode
    };

    if (!highDimensionalData.load_parameters(formParams)) return false;
    this.load_binning_parameters(formParams);

    //Pass in our flag that tells us whether to flip or not.
    formParams["flipImage"] = (flipImage) ? 'TRUE' : 'FALSE';

    return formParams;
}

/**
 * update manual binning
 * TODO: refactor to have efficient function that is reusable for other analysis (table fisher, survival analysis)
 */
BoxPlotView.prototype.update_manual_binning = function () {
    // Change the ManualBinning flag.
    GLOBAL.ManualBinning = document.getElementById('chkManualBin').checked;

    // Get the type of the variable we are dealing with.
    variableType = Ext.get('variableType').getValue();

    // Hide both DIVs.
    var divContinuous = Ext.get('divManualBinContinuous');
    var divCategorical = Ext.get('divManualBinCategorical');
    divContinuous.setVisibilityMode(Ext.Element.DISPLAY);
    divCategorical.setVisibilityMode(Ext.Element.DISPLAY);
    divContinuous.hide();
    divCategorical.hide();
    // Show the div with the binning options relevant to our variable type.
    if (document.getElementById('chkManualBin').checked) {
        if (variableType == "Continuous") {
            divContinuous.show();
            divCategorical.hide();
        } else {
            // Find out which variable we are binning.
            var binningVariable = Ext.get("selBinVariableSelection").getValue()

            //This will be the box we pull binning choices from.
            var binningSource = ""

            //Depending on the variable we are binning, we fill the categorical items box. Handle that logic here.
            if(binningVariable=="DEP")
            {
                binningSource = "divDependentVariable"
            }
            else
            {
                binningSource = "divIndependentVariable"
            }

            divContinuous.hide();
            divCategorical.show();

            setupCategoricalItemsList(binningSource,"divCategoricalItems");
        }
    }
}

/**
 * Toggle global binning
 */
BoxPlotView.prototype.toggle_binning = function () {
    if ($j("#isBinning").prop('checked') ) {
        GLOBAL.Binning = true;
        $j(".binningDiv").show();
    } else {
        GLOBAL.Binning = false;
        $j(".binningDiv").hide();
    }
}

/**
 * When we change the number of bins in the "Number of Bins" input, we have to
 * change the number of bins on the screen.
 * @param newNumberOfBins
 */
BoxPlotView.prototype.manage_bins = function (newNumberOfBins) {

    // This is the row template for a continousBinningRow.
    var tpl = new Ext.Template(
        '<tr id="binningContinousRow{0}">',
        '<td>Bin {0}</td><td><input type="text" id="txtBin{0}RangeLow" /> - <input type="text" id="txtBin{0}RangeHigh" /></td>',
        '</tr>');
    var tplcat = new Ext.Template(
        '<tr id="binningCategoricalRow{0}">',
        '<td><b>Bin {0}</b><div id="divCategoricalBin{0}" class="manualBinningBin"></div></td>',
        '</tr>');

    // This is the table we add continuous variables to.
    var continuousBinningTable = Ext.get('tblBinContinuous');
    var categoricalBinningTable = Ext.get('tblBinCategorical');
    // Clear all old rows out of the table.

    // For each bin, we add a row to the binning table.
    for (i = 1; i <= newNumberOfBins; i++) {
        // If the object isn't already on the screen, add it.
        if (!(Ext.get("binningContinousRow" + i))) {
            tpl.append(continuousBinningTable, [ i ]);
        } else {
            Ext.get("binningContinousRow" + i).show()
        }

        // If the object isn't already on the screen, add it-Categorical
        if (!(Ext.get("binningCategoricalRow" + i))) {
            tplcat.append(categoricalBinningTable, [ i ]);
            // Add the drop targets and handler function.
            var bin = Ext.get("divCategoricalBin" + i);
            var dragZone = new Ext.dd.DragZone(bin, {
                ddGroup : 'makeBin',
                isTarget: true,
                ignoreSelf: false

            });
            var dropZone = new Ext.dd.DropTarget(bin, {
                ddGroup : 'makeBin',
                isTarget: true,
                ignoreSelf: false
            });
            // dropZone.notifyEnter = test;
            dropZone.notifyDrop = this.drop_onto_bin; // dont forget to make each
            // dropped
            // node a drag target
        } else {
            Ext.get("binningCategoricalRow" + i).show()
        }
    }

    // If the new number of bins is less than the old, hide the old bins.
    if (newNumberOfBins < GLOBAL.NumberOfBins) {
        // For each bin, we add a row to the binning table.
        for (i = parseInt(newNumberOfBins) + 1; i <= GLOBAL.NumberOfBins; i++) {
            // If the object isn't already on the screen, add it.
            if (Ext.get("binningContinousRow" + i)) {
                Ext.get("binningContinousRow" + i).hide();
            }
            // If the object isn't already on the screen, add it.
            if (Ext.get("binningCategoricalRow" + i)) {
                Ext.get("binningCategoricalRow" + i).hide();
            }
        }
    }

    // Set the global variable to reflect the new bin count.
    GLOBAL.NumberOfBins = newNumberOfBins;
    boxPlotView.update_manual_binning();
}

BoxPlotView.prototype.load_binning_parameters = function (formParams) {

    //These default to FALSE
    formParams["binning"] = "FALSE";
    formParams["manualBinning"] = "FALSE";

    // Gather the data from the optional binning items, if we had selected to
    // enable binning.
    if (GLOBAL.Binning) {
        // Get the number of bins the user entered.
        var numberOfBins = Ext.get("txtNumberOfBins").getValue()

        // Get the value from the dropdown that specifies the type of
        // binning.
        var binningType = Ext.get("selBinDistribution").getValue()

        //Get the value from the dropdown that tells us which variable to bin.
        var binningVariable = Ext.get("selBinVariableSelection").getValue()

        // Add these items to our form parameters.
        formParams["binning"] = "TRUE";
        formParams["numberOfBins"] = numberOfBins;
        formParams["binDistribution"] = binningType;
        formParams["binVariable"] = binningVariable;

        // If we are using Manual Binning we need to add the parameters
        // here.
        if (GLOBAL.ManualBinning) {

            // Get a bar separated list of bins and their ranges.
            var binRanges = ""

            // Loop over each row in the HTML table.
            var variableType = Ext.get('variableType').getValue();
            if (variableType == "Continuous") {
                for (i = 1; i <= GLOBAL.NumberOfBins; i++) {
                    binRanges += "bin" + i + ","
                    binRanges += Ext.get('txtBin' + i + 'RangeLow').getValue()
                        + ","
                    binRanges += Ext.get('txtBin' + i + 'RangeHigh').getValue()
                        + "|"
                }
            } else {
                for (i = 1; i <= GLOBAL.NumberOfBins; i++) {
                    binRanges += "bin" + i + "<>"
                    var bin = Ext.get('divCategoricalBin' + i);
                    for (x = 0; x < bin.dom.childNodes.length; x++) {
                        binRanges+=bin.dom.childNodes[x].getAttribute('conceptdimcode') + "<>"
                    }
                    binRanges=binRanges.substring(0, binRanges.length - 2);
                    binRanges=binRanges+"|";
                }
            }
            formParams["manualBinning"] = "TRUE";
            formParams["binRanges"] = binRanges.substring(0,binRanges.length - 1);
            formParams["variableType"] = Ext.get('variableType').getValue();
        }
    }
}

/**
 * Submit the job
 * @param form
 */
BoxPlotView.prototype.submit_job = function (form) {

    // get formParams
    var formParams = this.get_form_params(form);

    if (formParams) { // if formParams is not null
        submitJob(formParams);
    }

}

// instantiate table fisher instance
var boxPlotView = new BoxPlotView();
