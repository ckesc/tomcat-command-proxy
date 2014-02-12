package ru.ckesc.tomcatCommandProxy;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    //Program-wide params
    public static boolean silentMode = false;
    public static boolean verboseMode = false;

    public static void main(String[] args) {
        OptionParser optionParser = new OptionParser();

        OptionSpec<String> usernameOpt = optionParser.acceptsAll(Arrays.asList("username", "u"), "username of tomcat user, who have 'manager-script' role").withRequiredArg().required();
        OptionSpec<String> passwordOpt = optionParser.acceptsAll(Arrays.asList("password", "p"), "password of tomcat user. See help for username").withRequiredArg().required();
        OptionSpec<String> hostOpt = optionParser.accepts("host", "host name or ip of server").withRequiredArg().required();
        OptionSpec<Integer> portOpt = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(8080);
        OptionSpec<String> commandOpt = optionParser.acceptsAll(Arrays.asList("command", "c", "cmd"), "command to perform").withRequiredArg().required();
        OptionSpec<String> parametersOpt = optionParser.acceptsAll(Arrays.asList("parameters", "param"), "parameter for given command").withRequiredArg();

        OptionSpec<Void> helpOpt = optionParser.acceptsAll(Arrays.asList("help", "?"), "show help").forHelp();
        OptionSpec<Void> useHttpsOpt = optionParser.accepts("useHttps", "connect via http over ssl");
        OptionSpec<Void> silentOpt = optionParser.accepts("silent", "don`t show any messages");
        OptionSpec<Void> verboseOpt = optionParser.accepts("verbose", "display additional messages");

        OptionSet optionSet;
        try {
            optionSet = optionParser.parse(args);
        } catch (OptionException e) {
            System.err.println(e.getLocalizedMessage());
            showHelp(optionParser);
            return;
        }

        if (optionSet.has(helpOpt)) {
            showHelp(optionParser);
            return;
        }

        silentMode = optionSet.has(silentOpt);
        verboseMode = optionSet.has(verboseOpt);

        try {
            CmdProxy cmdProxy = new CmdProxy(
                    optionSet.valueOf(usernameOpt),
                    optionSet.valueOf(passwordOpt),
                    optionSet.valueOf(hostOpt),
                    optionSet.valueOf(portOpt),
                    optionSet.has(useHttpsOpt)
            );

            String response = cmdProxy.performCommand(
                    optionSet.valueOf(commandOpt),
                    optionSet.valueOf(parametersOpt));

            if (verboseMode) {
                println("Server response:");
            }
            print(response);

        } catch (ClassCastException e) {
            showException(e);
        } catch (OptionException e) {
            showException(e);
        } catch (IOException e) {
            showException(e);
        }
    }

    private static void showException(Exception e) {
        if (silentMode) {
            return;
        }
        System.err.println(e.getLocalizedMessage());
        if (verboseMode) {
            e.printStackTrace(System.err);
        }
    }

    public static void showHelp(OptionParser parser) {
        println(
                "-= TomCat 7/8 command proxy tool =-\t\t\t\t\t by CkEsc\r\n" +
                        "Description: tool executes commands from specified user with tomcat manager application.\r\n" +
                        "Tool uses & requires installed tomcat manager app!\r\n");
        try {
            parser.printHelpOn(System.out);
        } catch (IOException e) {
            if (!silentMode) {
                e.printStackTrace();
            }
        }
    }

    public static void println(String message) {
        if (!silentMode) {
            System.out.println(message);
        }
    }

    public static void print(String message) {
        if (!silentMode) {
            System.out.print(message);
        }
    }
}
