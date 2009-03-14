/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Prints an <code>AnalyzedCluster</code> to a <code>PrintWriter</code>.
 *
 * @author  AO Industries, Inc.
 */
public class AnalyzedClusterConfigurationPrinter {

    /**
     * Make no instances.
     */
    private AnalyzedClusterConfigurationPrinter() {}

    private static void println(int indent, String label, Object value, String alertLevel, PrintWriter out) {
        out.print("| ");
        int lineWidth = 2;
        for(int c=0; c<indent; c++) {
            out.print("    ");
            lineWidth += 4;
        }
        if(label!=null) {
            out.print(label);
            lineWidth += label.length();
        }
        while(lineWidth<60) {
            out.print(' ');
            lineWidth++;
        }
        out.print(" | ");
        lineWidth += 3;
        if(value!=null) {
            String valueString = value.toString();
            // Right-align value
            for(int c=7 - valueString.length(); c>0; c--) {
                out.print(' ');
                lineWidth++;
            }
            out.print(valueString);
            lineWidth += valueString.length();
        }
        while(lineWidth<70) {
            out.print(' ');
            lineWidth++;
        }
        out.print(" | ");
        lineWidth += 3;
        if(alertLevel!=null) {
            out.print(alertLevel);
            lineWidth += alertLevel.toString().length();
        }
        while(lineWidth<85) {
            out.print(' ');
            lineWidth++;
        }
        out.println('|');
    }

    private static void println(int indent, Result result, PrintWriter out) {
        println(indent, result.getLabel(), result.getValue(), result.getAlertLevel().toString(), out);
    }

    static class ResultPrinter implements ResultHandler<Object> {

        private final int indent;
        private final PrintWriter out;

        ResultPrinter(int indent, PrintWriter out) {
            this.indent = indent;
            this.out = out;
        }

        public boolean handleResult(Result<?> result) {
            println(indent, result, out);
            return true;
        }
    }

    static class SortedResultPrinter implements ResultHandler<Object> {

        private final List<Result<?>> results = new ArrayList<Result<?>>();
        private final int indent;
        private final PrintWriter out;

        SortedResultPrinter(int indent, PrintWriter out) {
            this.indent = indent;
            this.out = out;
        }

        public boolean handleResult(Result<?> result) {
            results.add(result);
            return true;
        }

        void sortAndPrint() {
            Collections.sort(results);
            for(Result result : results) println(indent, result, out);
            results.clear();
        }
    }

    public static void print(Collection<AnalyzedClusterConfiguration> analyzedClusters, PrintWriter out, AlertLevel minimumAlertLevel) {
        final ResultPrinter resultPrinter2 = new ResultPrinter(2, out);
        final ResultPrinter resultPrinter4 = new ResultPrinter(4, out);
        final SortedResultPrinter capturer3 = new SortedResultPrinter(3, out);

        out.println("+------------------------------------------------------------+---------+-------------+");
        out.println("|                          Resource                          |  Value  | Alert Level |");
        out.println("+------------------------------------------------------------+---------+-------------+");
        for(AnalyzedClusterConfiguration analyzedCluster : analyzedClusters) {
            // TODO: Add DomU Groups
            println(0, analyzedCluster.getClusterConfiguration().getCluster().getName(), null, null, out);
            for(AnalyzedDom0Configuration dom0 : analyzedCluster.getAnalyzedDom0Configurations()) {
                // Overall
                println(1, dom0.getDom0().getHostname(), null, null, out);

                // RAM
                dom0.getAvailableRamResult(resultPrinter2, minimumAlertLevel);
                println(2, "Secondary RAM", null, null, out);
                dom0.getAllocatedSecondaryRamResults(capturer3, minimumAlertLevel);
                capturer3.sortAndPrint();

                // Processor type
                println(2, "Processor Type", dom0.getDom0().getProcessorType(), null, out);
                dom0.getProcessorTypeResults(capturer3, minimumAlertLevel);
                capturer3.sortAndPrint();

                // Processor architecture
                println(2, "Processor Architecture", dom0.getDom0().getProcessorArchitecture(), null, out);
                dom0.getProcessorArchitectureResults(capturer3, minimumAlertLevel);
                capturer3.sortAndPrint();

                // Processor speed
                println(2, "Processor Speed", Integer.toString(dom0.getDom0().getProcessorSpeed()), null, out);
                dom0.getProcessorSpeedResults(capturer3, minimumAlertLevel);
                capturer3.sortAndPrint();

                // Processor cores
                println(2, "Processor Cores", Integer.toString(dom0.getDom0().getProcessorCores()), null, out);
                dom0.getProcessorCoresResults(capturer3, minimumAlertLevel);
                capturer3.sortAndPrint();

                // Processor weights
                dom0.getAvailableProcessorWeightResult(resultPrinter2, minimumAlertLevel);

                // Supports HVM
                println(2, "Hardware Virtualization", Boolean.toString(dom0.getDom0().getSupportsHvm()), null, out);
                dom0.getRequiresHvmResults(capturer3, minimumAlertLevel);
                capturer3.sortAndPrint();

                // Dom0Disks
                println(2, "Disks", null, null, out);
                List<AnalyzedDom0DiskConfiguration> dom0Disks = dom0.getDom0Disks();
                Collections.sort(dom0Disks);
                for(AnalyzedDom0DiskConfiguration dom0Disk : dom0Disks) {
                    println(3, dom0Disk.getDom0Disk().getDevice(), null, null, out);
                    dom0Disk.getAvailableWeightResult(resultPrinter4, minimumAlertLevel);
                    List<AnalyzedDomUDiskResults> domUDisks = dom0Disk.getModifiableDomUDiskResults();
                    Collections.sort(domUDisks);
                    for(AnalyzedDomUDiskResults domUDisk : domUDisks) {
                        println(4, domUDisk.getDomUDisk().getDomUHostname() + ":" + domUDisk.getDomUDisk().getDevice(), null, null, out);
                        println(5, domUDisk.getRaidTypeResult(), out);
                        println(5, domUDisk.getDiskTypeResult(), out);
                        println(5, domUDisk.getDiskSpeedResult(), out);
                    }
                }
            }
        }
        out.println("+------------------------------------------------------------+---------+-------------+");
    }
}
