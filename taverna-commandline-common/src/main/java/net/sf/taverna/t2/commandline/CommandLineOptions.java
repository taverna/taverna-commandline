package net.sf.taverna.t2.commandline;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class CommandLineOptions {

	private static final Logger logger = Logger.getLogger(CommandLineOptions.class);
	private Options options;
	private CommandLine commandLine;			

	public CommandLineOptions(String [] args) throws InvalidOptionException{
		this.options = intitialiseOptions();
		this.commandLine = processArgs(args);	
		checkForInvalid();
		checkForHelp();		
	}
	
	protected void checkForInvalid() throws InvalidOptionException {
		if (hasOption("provenance") && !(hasOption("embedded") || hasOption("clientserver") || hasOption("dbproperties"))) throw new InvalidOptionException("You should be running with a database to use provenance");
		if (hasOption("provenance") && hasOption("inmemory")) throw new InvalidOptionException("You should be running with a database to use provenance");
		
		if (getArgs().length!=1 && !hasOption("help")) throw new InvalidOptionException("You must specify a workflow");
		if (hasOption("inmemory") && hasOption("embedded")) throw new InvalidOptionException("The options -embedded, -clientserver and -inmemory cannot be used together");
		if (hasOption("inmemory") && hasOption("clientserver")) throw new InvalidOptionException("The options -embedded, -clientserver and -inmemory cannot be used together");
		if (hasOption("embedded") && hasOption("clientserver")) throw new InvalidOptionException("The options -embedded, -clientserver and -inmemory cannot be used together");
	}
		
	
	public String getWorkflow() throws InvalidOptionException {
		if (getArgs().length!=1) {
			throw new InvalidOptionException("You must specify a workflow");
		}
		return getArgs()[0];
	}
	
	public String[] getArgs() {
		return commandLine.getArgs();
	}
	
	private void checkForHelp() {
		if (askedForHelp()) {
			InputStream helpStream = CommandLineOptions.class.getClassLoader().getResourceAsStream("help.txt");
			HelpFormatter formatter = new HelpFormatter();			
			try {
				formatter.printHelp(IOUtils.toString(helpStream), options);
			} catch (IOException e) {
				logger.error("Error reading the help document",e);	
				System.exit(-1);
			}						
		}		
	}
	
	private String getOptionValue(String opt) {
		return commandLine.getOptionValue(opt);
	}

	private String [] getOptionValues(String arg0) {
		return commandLine.getOptionValues(arg0);
	}
	
	/**
	 * 
	 * @return a path to a properties file that contains database configuration settings
	 */
	public String getDatabaseProperties() {
		return getOptionValue("dbproperties");
	}
	
	/**
	 * Save the results to a directory if -output has been explicitly defined, and/or if -outputdoc hasn't been defined
	 * @return boolean
	 */
	public boolean saveResultsToDirectory() {
		return (options.hasOption("outputdir") || !options.hasOption("outputdoc"));
	}
	
	/**
	 * 
	 * @return the path to the output document
	 */
	public String getOutputDocument() {
		return getOptionValue("outputdoc");
	}
	
	/**
	 * 
	 * @return the directory to write the results to
	 */
	public String getOutputDirectory() {
		return getOptionValue("outputdir");
	}	
	
	/**
	 * 
	 * @return the port that the database should run on
	 */
	public String getDatabasePort() {
		return getOptionValue("port");
	}
	
	/**
	 * 
	 * @return the path to the input document
	 */
	public String getInputDocument() {
		return getOptionValue("inputdoc");
	}
	
	/**
	 * Returns an array that alternates between a portname and path to a file containing the input values.
	 * Therefore the array will always contain an even number of elements
	 * 
	 * @return an array of portname and path to files containing individual inputs.
	 */
	public String [] getInputs() {
		return getOptionValues("input");
	}

	public boolean hasOption(String option) {
		return commandLine.hasOption(option);
	}

	private CommandLine processArgs(String[] args) {
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;
		try {
			// parse the command line arguments
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			System.exit(1);			
		}
		return line;
	}

	private Options intitialiseOptions() {
		Option helpOption = new Option("help", "print this message");

		Option outputOption = OptionBuilder
				.withArgName("directory")
				.hasArg()
				.withDescription(
						"save outputs as files in directory, default "
								+ "is to make a new directory workflowName_output")
				.create("outputdir");
		Option outputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("save outputs to a new XML document").create(
						"outputdoc");		
		Option inputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("load inputs from XML document").create(
						"inputdoc");

		Option inputOption = OptionBuilder.withArgName("name filename")
				.hasArgs(2).withValueSeparator('=').withDescription(
						"load the named input from file or URL")
				.create("input");
		
		Option dbProperties = OptionBuilder.withArgName("filename").hasArg().withDescription(
				"loads a properties file to configure the database").create("dbproperties");
		
		Option port = OptionBuilder.withArgName("portnumber").hasArg().withDescription(
		"the port that the database is running on. If set requested to start its own internal server, this is the start port that will be used.").create("port");
		
		

		Option embedded = new Option("embedded","connects to an embedded Derby database. This can prevent mulitple invocations");
		Option clientserver = new Option("clientserver","connects as a client to a derby server instance.");
		Option inMemOption = new Option("inmemory","runs the workflow with data stored in-memory rather than in a database. This can give performance inprovements, at the cost of overall memory usage");
		Option startDB = new Option("startdb","automatically starts an internal Derby database server.");
		Option provenance = new Option("provenance","generates provenance information and stores it in the database.");
		
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inputOption);
		options.addOption(inputdocOption);
		options.addOption(outputOption);
		options.addOption(outputdocOption);		
		options.addOption(inMemOption);
		options.addOption(embedded);
		options.addOption(clientserver);
		options.addOption(dbProperties);
		options.addOption(port);
		options.addOption(startDB);
		options.addOption(provenance);
		
		return options;
		
	}

	public boolean askedForHelp() {
		return hasOption("help");
	}
}