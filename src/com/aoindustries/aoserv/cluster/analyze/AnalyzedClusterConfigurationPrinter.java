/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.ProcessorArchitecture;
import com.aoindustries.aoserv.cluster.ProcessorType;
import java.io.PrintWriter;
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

    public static void print(Collection<AnalyzedClusterConfiguration> analyzedClusters, PrintWriter out) {
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
                println(2, dom0.getAvailableRamResult(), out);
                println(2, "Secondary RAM", null, null, out);
                List<Result<Integer>> availableRamResults = dom0.getModifiableAllocatedSecondaryRamResults(false);
                Collections.sort(availableRamResults);
                for(Result result : availableRamResults) println(3, result, out);

                // Processor type
                println(2, "Processor Type", dom0.getDom0().getProcessorType(), null, out);
                List<Result<ProcessorType>> processorTypeResults = dom0.getModifiableProcessorTypeResults(false);
                Collections.sort(processorTypeResults);
                for(Result result : processorTypeResults) println(3, result, out);

                // Processor architecture
                println(2, "Processor Architecture", dom0.getDom0().getProcessorArchitecture(), null, out);
                List<Result<ProcessorArchitecture>> processorArchitectureResults = dom0.getModifiableProcessorArchitectureResults(false);
                Collections.sort(processorArchitectureResults);
                for(Result result : processorArchitectureResults) println(3, result, out);

                // Processor speed
                println(2, "Processor Speed", Integer.toString(dom0.getDom0().getProcessorSpeed()), null, out);
                List<Result<Integer>> processorSpeedResults = dom0.getModifiableProcessorSpeedResults(false);
                Collections.sort(processorSpeedResults);
                for(Result result : processorSpeedResults) println(3, result, out);

                // Processor cores
                println(2, "Processor Cores", Integer.toString(dom0.getDom0().getProcessorCores()), null, out);
                List<Result<Integer>> processorCoresResults = dom0.getModifiableProcessorCoresResults(false);
                Collections.sort(processorCoresResults);
                for(Result result : processorCoresResults) println(3, result, out);

                // Processor weights
                println(2, dom0.getAvailableProcessorWeightResult(), out);
                /*println(2, "Secondary Processor Weights", null, null, out);
                List<Result<Integer>> secondaryProcessorWeightResults = dom0.getModifiableAllocatedSecondaryProcessorWeightResults();
                Collections.sort(secondaryProcessorWeightResults);
                for(Result result : secondaryProcessorWeightResults) println(3, result, out);*/

                // Supports HVM
                println(2, "Hardware Virtualization", Boolean.toString(dom0.getDom0().getSupportsHvm()), null, out);
                List<Result<Boolean>> requiresHvmResults = dom0.getModifiableRequiresHvmResults(false);
                Collections.sort(requiresHvmResults);
                for(Result result : requiresHvmResults) println(3, result, out);

                // Dom0Disks
                println(2, "Disks", null, null, out);
                List<AnalyzedDom0DiskConfiguration> dom0Disks = dom0.getModifiableDom0Disks();
                Collections.sort(dom0Disks);
                for(AnalyzedDom0DiskConfiguration dom0Disk : dom0Disks) {
                    println(3, dom0Disk.getDom0Disk().getDevice(), null, null, out);
                    println(4, dom0Disk.getAvailableWeightResult(), out);
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
