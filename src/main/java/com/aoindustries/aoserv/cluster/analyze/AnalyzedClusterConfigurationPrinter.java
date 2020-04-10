/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-cluster.
 *
 * aoserv-cluster is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-cluster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-cluster.  If not, see <http://www.gnu.org/licenses/>.
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

	private static void println(int indent, String label, Object value, Object maxValue, String alertLevel, PrintWriter out) {
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
		if(maxValue!=null) {
			String maxValueString = maxValue.toString();
			// Right-align max value
			for(int c=7 - maxValueString.length(); c>0; c--) {
				out.print(' ');
				lineWidth++;
			}
			out.print(maxValueString);
			lineWidth += maxValueString.length();
		}
		while(lineWidth<80) {
			out.print(' ');
			lineWidth++;
		}
		out.print(" | ");
		lineWidth += 3;
		if(alertLevel!=null) {
			out.print(alertLevel);
			lineWidth += alertLevel.length();
		}
		while(lineWidth<95) {
			out.print(' ');
			lineWidth++;
		}
		out.println('|');
	}

	private static void println(int indent, Result<?> result, PrintWriter out) {
		println(indent, result.getLabel(), result.getValue(), result.getMaxValue(), result.getAlertLevel().toString(), out);
	}

	static class ResultPrinter implements ResultHandler<Object> {

		private final int indent;
		private final PrintWriter out;

		ResultPrinter(int indent, PrintWriter out) {
			this.indent = indent;
			this.out = out;
		}

		@Override
		public boolean handleResult(Result<?> result) {
			println(indent, result, out);
			return true;
		}
	}

	static class SortedResultPrinter implements ResultHandler<Object> {

		private final List<Result<?>> results = new ArrayList<>();
		private final int indent;
		private final PrintWriter out;

		SortedResultPrinter(int indent, PrintWriter out) {
			this.indent = indent;
			this.out = out;
		}

		@Override
		public boolean handleResult(Result<?> result) {
			results.add(result);
			return true;
		}

		void sortAndPrint() {
			Collections.sort(results);
			for(Result<?> result : results) println(indent, result, out);
			results.clear();
		}
	}

	public static void print(Collection<AnalyzedClusterConfiguration> analyzedClusters, PrintWriter out, AlertLevel minimumAlertLevel) {
		final ResultPrinter resultPrinter2 = new ResultPrinter(2, out);
		final ResultPrinter resultPrinter4 = new ResultPrinter(4, out);
		final SortedResultPrinter capturer3 = new SortedResultPrinter(3, out);
		final SortedResultPrinter capturer5 = new SortedResultPrinter(5, out);

		out.println("+------------------------------------------------------------+---------+---------+-------------+");
		out.println("|                          Resource                          |  Value  | Max Val | Alert Level |");
		out.println("+------------------------------------------------------------+---------+---------+-------------+");
		for(AnalyzedClusterConfiguration analyzedCluster : analyzedClusters) {
			// TODO: Add DomU Groups
			println(0, analyzedCluster.getClusterConfiguration().getCluster().getName(), null, null, null, out);
			for(AnalyzedDom0Configuration dom0 : analyzedCluster.getAnalyzedDom0Configurations()) {
				// Overall
				println(1, dom0.getDom0().getHostname(), null, null, null, out);

				// RAM
				dom0.getPrimaryRamResult(resultPrinter2, minimumAlertLevel);
				println(2, "Secondary RAM", null, null, null, out);
				dom0.getSecondaryRamResults(capturer3, minimumAlertLevel);
				capturer3.sortAndPrint();

				// Processor weights
				dom0.getPrimaryProcessorWeightResult(resultPrinter2, minimumAlertLevel);

				// Processor type
				println(2, "Processor Type", dom0.getDom0().getProcessorType(), null, null, out);
				dom0.getProcessorTypeResults(capturer3, minimumAlertLevel);
				capturer3.sortAndPrint();

				// Processor architecture
				println(2, "Processor Architecture", dom0.getDom0().getProcessorArchitecture(), null, null, out);
				dom0.getProcessorArchitectureResults(capturer3, minimumAlertLevel);
				capturer3.sortAndPrint();

				// Processor speed
				println(2, "Processor Speed", Integer.toString(dom0.getDom0().getProcessorSpeed()), null, null, out);
				dom0.getProcessorSpeedResults(capturer3, minimumAlertLevel);
				capturer3.sortAndPrint();

				// Processor cores
				println(2, "Processor Cores", Integer.toString(dom0.getDom0().getProcessorCores()), null, null, out);
				dom0.getProcessorCoresResults(capturer3, minimumAlertLevel);
				capturer3.sortAndPrint();

				// Supports HVM
				println(2, "Hardware Virtualization", Boolean.toString(dom0.getDom0().getSupportsHvm()), null, null, out);
				dom0.getRequiresHvmResults(capturer3, minimumAlertLevel);
				capturer3.sortAndPrint();

				// Dom0Disks
				println(2, "Disks", null, null, null, out);
				List<AnalyzedDom0DiskConfiguration> dom0Disks = new ArrayList<>(dom0.getDom0Disks());
				Collections.sort(dom0Disks);
				for(AnalyzedDom0DiskConfiguration dom0Disk : dom0Disks) {
					assert dom0Disk!=null : "AnalyzedClusterConfigurationPrinter.print: dom0Disk is null";
					println(3, dom0Disk.getDom0Disk().getDevice(), null, null, null, out);
					dom0Disk.getAllocatedWeightResult(resultPrinter4, minimumAlertLevel);

					println(4, "Disk Speed", Integer.toString(dom0Disk.getDom0Disk().getDiskSpeed()), null, null, out);
					dom0Disk.getDiskSpeedResults(capturer5, minimumAlertLevel);
					capturer5.sortAndPrint();
				}
			}
		}
		out.println("+------------------------------------------------------------+---------+---------+-------------+");
	}
}
