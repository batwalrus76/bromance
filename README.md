bromance
========

Java-based framework for enacting Bro via the command line

Instances of the Bromance object are passed a few simple values.  These are network interface, interval for recording from interface, output folder, any Bro options, and optionally the location of the bro command (if not in /usr/local/bro/bin/bro). You will need to execute the setRunBro method with the value true and then either execute run or create a Java thread and then start the thread.
