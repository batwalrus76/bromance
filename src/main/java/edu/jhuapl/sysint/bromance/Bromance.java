package edu.jhuapl.sysint.bromance;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.ProcessBuilder;

/**
 * Created by Chancellor Pascale on 12/23/14.
 */
public class Bromance implements Runnable{

    private String nterface = null;
    private List<String> scripts = null;
    private long interval;
    private boolean runBro = false;
    private String outputDir = null;
    private long fileNameOffset = 0l;
    private String broCommand = "/usr/local/bro/bin/bro";


    /**
     * Constructor for all types of bro usage
     * @param nterface
     * @param options
     * @param interval
     * @param outputDir
     */
    public Bromance(String nterface, List<String> options, long interval, String outputDir)
    {
        this.nterface = nterface;
        this.scripts = options;
        this.interval = interval;
        this.outputDir = outputDir;
    }

    /**
     * Constructor with the added ability to determine a non-default location for bro command
     * @param nterface
     * @param options
     * @param interval
     * @param outputDir
     * @param broCommand
     */
    public Bromance(String nterface, List<String> options, long interval, String outputDir, String broCommand)
    {
        this.nterface = nterface;
        this.scripts = options;
        this.interval = interval;
        this.outputDir = outputDir;
        this.broCommand = broCommand;
    }

    /**
     * Runs bro command with appropriate options for the predetermined interval and then moves output to output folder.
     * Have to have execute setRunBro with true and this will cause the bro command to be repeatedly run until the
     * method setRunBro is run with the value false passed.
     */
    @Override
    public void run() {
        System.out.println("Running Bro in the following way:");
        do{
            ProcessBuilder pb = this.buildBroCommand();
            System.out.println(pb.toString());
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = null;
            try {
                p = pb.start();
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(p != null) {
                    p.destroy();
                }
            }
            this.moveLatestBroFiles();
        }
        while(runBro);
    }

    /**
     * This method retrieves all files in the current folder that end with .log, which are most probably bro files
     * @return
     */
    public static List<String> getCurrentBroLogFiles()
    {
        List<String> broLogFiles = new ArrayList<String>();
        File folder = new File("./");
        File[] listOfFiles = folder.listFiles();
        for(File file: listOfFiles)
        {
            if(file.getName().endsWith(".log"))
            {
                broLogFiles.add(file.getName());
            }
        }
        return broLogFiles;
    }


    /**
     * This method takes the basic bro files that are generated each time it is executed and moves it to another
     * location and slightly alters the name.
     * Note that the basis for this method can be found at http://www.mkyong.com/java/how-to-move-file-to-another-directory-in-java/
     * @return the success of copying files to previously determined output folder
     */
    public boolean moveLatestBroFiles()
    {
        boolean success = true;
        InputStream inStream = null;
        OutputStream outStream = null;
        try{
            List<String> broLogFiles = Bromance.getCurrentBroLogFiles();
            for(String broOutputFileName: broLogFiles) {
                if(broOutputFileName != null) {
                    File broOutputFile = new File(broOutputFileName);
                    if (broOutputFile.exists()) {
                        int lastIndexOfLog = broOutputFileName.lastIndexOf(".log");
                        String broOutputFileNameWithoutExtension = broOutputFileName.substring(0, lastIndexOfLog);
                        File broNewOutputFile = new File(this.outputDir + File.separator + broOutputFileNameWithoutExtension + "_" + fileNameOffset + ".log");
                        inStream = new FileInputStream(broOutputFile);
                        outStream = new FileOutputStream(broNewOutputFile);
                        byte[] buffer = new byte[1024];

                        int length;
                        //copy the file content in bytes
                        while ((length = inStream.read(buffer)) > 0) {

                            outStream.write(buffer, 0, length);

                        }

                        inStream.close();
                        outStream.close();

                        //delete the original file
                        broOutputFile.delete();
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
            success = false;
        }
        this.fileNameOffset++;
        return success;
    }

    /**
     * Builds up a ProcessBuilder object based on object properties
     * @return a ProcessBuilder object for running bro command
     */
    public ProcessBuilder buildBroCommand() {
        ProcessBuilder pb = new ProcessBuilder();
        List<String> commandStrings = new ArrayList<String>();
        commandStrings.add(broCommand);
        if (nterface != null) {
            commandStrings.add("-i");
            commandStrings.add(nterface);
        }
        if (scripts != null && scripts.size() > 0) {
            for (String script : scripts) {
                commandStrings.add(script);
            }
        }
        pb.command(commandStrings);
        return pb;
    }

    /**
     *
     * @return
     */
    public String getNterface() {
        return nterface;
    }

    /**
     *
     * @param nterface
     */
    public void setNterface(String nterface) {
        this.nterface = nterface;
    }

    /**
     *
     * @return
     */
    public List<String> getScripts() {
        return scripts;
    }

    /**
     *
     * @param scripts
     */
    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    /**
     *
     * @return
     */
    public long getInterval() {
        return interval;
    }

    /**
     *
     * @param interval
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     *
     * @return
     */
    public boolean isRunBro() {
        return runBro;
    }

    /**
     *
     * @param runBro
     */
    public void setRunBro(boolean runBro)
    {
        this.runBro = runBro;
    }

    /**
     *
     * @return
     */
    public String getBroCommand() {
        return broCommand;
    }

    /**
     *
     * @param broCommand
     */
    public void setBroCommand(String broCommand) {
        this.broCommand = broCommand;
    }

    /**
     *
     * @param args
     */
    public static void main(String args[])
    {
        System.out.println("Starting Bromance -- a simple Java program for using Bro.");
        if(args == null || args.length < 1)
        {
            System.out.println("Bromance execution pattern: java -jar Bromance.jar <networking-interface> <output-dir>" +
                    " <polling-interval-seconds> <list of Bro-based options>");
            System.exit(1);
        } else {
            String nterface = args[0];
            List<String> broOptions = null;
            long interval = 60000l;
            if(args.length > 1) {
                interval = Long.parseLong(args[1])*1000;
                if (args.length > 2) {
                    broOptions = Arrays.asList(args);
                    broOptions = broOptions.subList(2, broOptions.size());
                }
            }
            Bromance bromance = new Bromance(nterface,broOptions,interval,"output");
            bromance.setRunBro(true);
            bromance.run();
            System.out.println("Exit Bromance -- 'May the Schwartz be with you!'");
            System.exit(0);
        }
    }

}
