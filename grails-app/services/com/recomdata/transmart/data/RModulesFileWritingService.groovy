package com.recomdata.transmart.data

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * This class contains methods for dealing with creating the temp directory and writing data files to be used in the Rmodules.
 * @author MMcDuffie
 *
 */
class RModulesFileWritingService {
	
	static transactional = true
	static scope = 'request'
	
	def config = ConfigurationHolder.config
	def String tempFolderDirectory = config.RModules.tempFolderDirectory
	
	//This will be used as the column delimiter in the method to write the data file below.
	private String valueDelimiter ="\t";
	
	/**
	 * This method will create a temporary directory based on the Rmodules configuration.
	 * @param jobName
	 * @return
	 */
	def createTemporaryDirectory(jobName)
	{
		try {
			
			//Initialize the jobTmpDirectory.
			def jobTmpDirectory = tempFolderDirectory + File.separator + "${jobName}" + File.separator
			jobTmpDirectory = jobTmpDirectory.replace("\\","\\\\")
			def jobTmpWorkingDirectory = jobTmpDirectory + "workingDirectory"
			
			//Try to make the working directory.
			File jtd = new File(jobTmpWorkingDirectory)
			jtd.mkdirs();
			
			return jobTmpDirectory
			
		} catch (Exception e) {
			throw new Exception('Failed to create Temporary Directories. Please contact an administrator.', e);
		}
	}
	
	/*
	 * Writes a file based on a passed in array of arrays.
	 */
	def writeDataFile(tempDirectory,dataToWrite,fileName)
	{
		//Construct the path to the temporary directory we will do our work in.
		def fullDirectoryPath = tempDirectory + File.separator
		
		//This is the path to the file that we will return from this function.
		def filePath = ""
		
		//Create a new file to write our data to.
		def outputFile = new File(fullDirectoryPath, fileName);
	
		//Create the buffered writer which will write to our data file.
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(outputFile), 1024 * 64000);
		
		//Initialize a CSVWriter, tab delimited.
		def writer = new CSVWriter(bufWriter, '\t' as char);
		def output = outputFile.newWriter(true)
		
		//Attempt to write the data to the file.
		try
		{
			//Loop through the outside array.
			dataToWrite.each()
			{
				//Loop through the inside array.
				it.each()
				{
					//Write each value to the file.
					output.write(it.toString());
					
					//Write the record delimiter.
					output.write(valueDelimiter);
				}
				
				//Write a new line to the file.
				output.newLine();
			}
			
		} catch(Exception e) {
			throw new Exception('Failed when writing data to file.', e);
		} finally {
			output?.flush();
			output?.close()
			filePath = outputFile?.getAbsolutePath()
		}
		
		return filePath
	}
	
}