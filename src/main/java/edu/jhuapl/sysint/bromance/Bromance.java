package edu.jhuapl.sysint.bromance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.ProcessBuilder;

/**
 * Created by pascact1 on 12/23/14.
 */
public class Bromance implements Runnable{

    private String nterface = null;
    private List<String> scripts = null;
    private long interval;
    private boolean runBro = false;
    private String outputDir = null;
    private long fileNameOffset = 0l;

    public static final String[] BRO_OUTPUT_FILES_NAMES = {
        "conn.log", "dns.log", "packet_filter.log",
        "reporter.log", "weird.log"};

    public static final String BRO_COMMAND = "/usr/local/bro/bin/bro";

    public Bromance(String nterface, List<String> scripts, long interval, String outputDir)
    {
        this.nterface = nterface;
        this.scripts = scripts;
        this.interval = interval;
        this.outputDir = outputDir;
    }

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

        }
        while(runBro);
    }

    public boolean moveLatestBroFiles()
    {
        boolean success = true;
        for(String broOutputFileName; BRO_OUTPUT_FILES_NAMES)
        {
            File broOutputFile =
        }
        return success;
    }

    public void setRunBro(boolean runBro)
    {
        this.runBro = runBro;
    }

    public ProcessBuilder buildBroCommand() {
        ProcessBuilder pb = new ProcessBuilder();
        List<String> commandStrings = new ArrayList<String>();
        commandStrings.add(BRO_COMMAND);
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

    public static void main(String args[])
    {
        System.out.println("Starting Bromance -- a simple Java program for using Bro.");
        if(args == null || args.length < 1)
        {
            System.out.println("Bromance execution pattern: java -jar Bromance.jar <networking-interface> <output-dir> <polling-interval-seconds> <list of Bro-based scripts>");
            System.exit(1);
        } else {
            String nterface = args[0];
            List<String> broScripts = null;
            long interval = 60000l;
            if(args.length > 1) {
                interval = Long.parseLong(args[1]);
                if (args.length > 2) {
                    broScripts = Arrays.asList(args);
                    broScripts = broScripts.subList(2, broScripts.size());
                }
            }
            Bromance bromance = new Bromance(nterface,broScripts,interval);
            bromance.setRunBro(true);
            bromance.run();
            System.out.println("Exit Bromance -- 'May the Schwartz be with you!'");
            System.exit(0);
        }
    }
}
