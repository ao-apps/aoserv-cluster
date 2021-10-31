/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020, 2021  AO Industries, Inc.
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
 * along with aoserv-cluster.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.cluster.analyze;

import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.Dom0Disk;
import com.aoindustries.aoserv.cluster.DomU;
import com.aoindustries.aoserv.cluster.DomUConfiguration;
import com.aoindustries.aoserv.cluster.ProcessorArchitecture;
import com.aoindustries.aoserv.cluster.ProcessorType;
import com.aoindustries.aoserv.cluster.UnmodifiableArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes a single Dom0 to find anything that is not optimal.
 * 
 * @author  AO Industries, Inc.
 */
public class AnalyzedDom0Configuration {

	private final ClusterConfiguration clusterConfiguration;
	private final Dom0 dom0;

	public AnalyzedDom0Configuration(ClusterConfiguration clusterConfiguration, Dom0 dom0) {
		this.clusterConfiguration = clusterConfiguration;
		this.dom0 = dom0;
	}

	public ClusterConfiguration getClusterConfiguration() {
		return clusterConfiguration;
	}

	public Dom0 getDom0() {
		return dom0;
	}

	/**
	 * Gets the results for primary RAM allocation.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getPrimaryRamResult(ResultHandler<? super Integer> resultHandler, AlertLevel minimumAlertLevel) {
		int allocatedPrimaryRam = 0;
		for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
			if(domUConfiguration.getPrimaryDom0()==dom0) allocatedPrimaryRam+=domUConfiguration.getDomU().getPrimaryRam();
		}
		int totalRam = dom0.getRam();
		int overcommittedRam = allocatedPrimaryRam - totalRam;
		AlertLevel alertLevel = overcommittedRam>0 ? AlertLevel.CRITICAL : AlertLevel.NONE;
		if(alertLevel.compareTo(minimumAlertLevel)>=0) {
			return resultHandler.handleResult(
				new IntResult(
					"Primary RAM",
					allocatedPrimaryRam,
					totalRam,
					((double)overcommittedRam / (double)totalRam),
					alertLevel
				)
			);
		} else return true;
	}

	/**
	 * Gets the secondary RAM allocation results.  It has a separate
	 * entry for each Dom0 that has any secondary resource on this dom0.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getSecondaryRamResults(ResultHandler<? super Integer> resultHandler, AlertLevel minimumAlertLevel) {
		if(minimumAlertLevel.compareTo(AlertLevel.HIGH)<=0) {
			int allocatedPrimaryRam = 0;
			Map<String, Integer> allocatedSecondaryRams = new HashMap<>();
			for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
				if(domUConfiguration.getPrimaryDom0()==dom0) {
					allocatedPrimaryRam+=domUConfiguration.getDomU().getPrimaryRam();
				} else if(domUConfiguration.getSecondaryDom0()==dom0) {
					int secondaryRam = domUConfiguration.getDomU().getSecondaryRam();
					if(secondaryRam!=-1) {
						String failedHostname = domUConfiguration.getPrimaryDom0().getHostname();
						Integer totalSecondary = allocatedSecondaryRams.get(failedHostname);
						allocatedSecondaryRams.put(
							failedHostname,
							totalSecondary==null ? secondaryRam : (totalSecondary+secondaryRam)
						);
					}
				}
			}
			int totalRam = dom0.getRam();
			int freePrimaryRam = totalRam - allocatedPrimaryRam;

			for(Map.Entry<String, Integer> entry : allocatedSecondaryRams.entrySet()) {
				String failedHostname = entry.getKey();
				int allocatedSecondary = entry.getValue();
				AlertLevel alertLevel = allocatedSecondary>freePrimaryRam ? AlertLevel.HIGH : AlertLevel.NONE;
				if(alertLevel.compareTo(minimumAlertLevel)>=0) {
					if(
						!resultHandler.handleResult(
							new IntResult(
								failedHostname,
								allocatedSecondary,
								freePrimaryRam,
								(double)(allocatedSecondary-freePrimaryRam)/(double)totalRam,
								alertLevel
							)
						)
					) return false;
				}
			}
		}
		return true;
	}

	/**
	 * Gets the unmodifiable set of specific processor type results.  It has a
	 * separate entry for each DomU that is either primary or secondary (with RAM)
	 * on this dom0.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getProcessorTypeResults(ResultHandler<? super ProcessorType> resultHandler, AlertLevel minimumAlertLevel) {
		if(minimumAlertLevel.compareTo(AlertLevel.LOW)<=0) {
			ProcessorType processorType = dom0.getProcessorType();

			List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
			for(DomUConfiguration domUConfiguration : domUConfigurations) {
				DomU domU = domUConfiguration.getDomU();
				if(
					domUConfiguration.getPrimaryDom0()==dom0
					|| (
						domUConfiguration.getSecondaryDom0()==dom0
						&& domU.getSecondaryRam()!=-1
					)
				) {
					ProcessorType minProcessorType = domU.getMinimumProcessorType();
					AlertLevel alertLevel;
					double deviation;
					if(minProcessorType==null) {
						alertLevel = AlertLevel.NONE;
						deviation = 0;
					} else {
						// The further apart the generations, the higher the deviation
						int diff = minProcessorType.ordinal() - processorType.ordinal();
						alertLevel = diff>0 ? AlertLevel.LOW : AlertLevel.NONE;
						deviation = diff;
					}
					if(alertLevel.compareTo(minimumAlertLevel)>=0) {
						if(
							!resultHandler.handleResult(
								new ObjectResult<>(
									domU.getHostname(),
									minProcessorType,
									processorType,
									deviation,
									alertLevel
								)
							)
						) return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Gets the unmodifiable set of specific processor architecture results.  It has a
	 * separate entry for each DomU that is either primary or secondary (with RAM)
	 * on this dom0.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getProcessorArchitectureResults(ResultHandler<? super ProcessorArchitecture> resultHandler, AlertLevel minimumAlertLevel) {
		ProcessorArchitecture processorArchitecture = dom0.getProcessorArchitecture();

		List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
		for(DomUConfiguration domUConfiguration : domUConfigurations) {
			DomU domU = domUConfiguration.getDomU();
			if(domUConfiguration.getPrimaryDom0()==dom0) {
				// Primary is CRITICAL
				ProcessorArchitecture minProcessorArchitecture = domU.getMinimumProcessorArchitecture();
				AlertLevel alertLevel;
				// The further apart the architectures, the higher the deviation
				int diff = minProcessorArchitecture.ordinal() - processorArchitecture.ordinal();
				alertLevel = diff>0 ? AlertLevel.CRITICAL : AlertLevel.NONE;
				if(alertLevel.compareTo(minimumAlertLevel)>=0) {
					if(
						!resultHandler.handleResult(
							new ObjectResult<>(
								domU.getHostname(),
								minProcessorArchitecture,
								processorArchitecture,
								(double)diff,
								alertLevel
							)
						)
					) return false;
				}
			} else if(
				domUConfiguration.getSecondaryDom0()==dom0
				&& domU.getSecondaryRam()!=-1
			) {
				// Secondary is HIGH
				ProcessorArchitecture minProcessorArchitecture = domU.getMinimumProcessorArchitecture();
				AlertLevel alertLevel;
				// The further apart the architectures, the higher the deviation
				int diff = minProcessorArchitecture.ordinal() - processorArchitecture.ordinal();
				alertLevel = diff>0 ? AlertLevel.HIGH : AlertLevel.NONE;
				if(alertLevel.compareTo(minimumAlertLevel)>=0) {
					if(
						!resultHandler.handleResult(
							new ObjectResult<>(
								domU.getHostname(),
								minProcessorArchitecture,
								processorArchitecture,
								(double)diff,
								alertLevel
							)
						)
					) return false;
				}
			}
		}
		return true;
	}

	/**
	 * Gets the unmodifiable set of specific processor speed results.  It has a
	 * separate entry for each DomU that is either primary or secondary (with RAM)
	 * on this dom0.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getProcessorSpeedResults(ResultHandler<? super Integer> resultHandler, AlertLevel minimumAlertLevel) {
		if(minimumAlertLevel.compareTo(AlertLevel.LOW)<=0) {
			int processorSpeed = dom0.getProcessorSpeed();

			List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
			for(DomUConfiguration domUConfiguration : domUConfigurations) {
				DomU domU = domUConfiguration.getDomU();
				if(
					domUConfiguration.getPrimaryDom0()==dom0
					|| (
						domUConfiguration.getSecondaryDom0()==dom0
						&& domU.getSecondaryRam()!=-1
					)
				) {
					int minSpeed = domU.getMinimumProcessorSpeed();
					AlertLevel alertLevel;
					double deviation;
					if(minSpeed==-1) {
						alertLevel = AlertLevel.NONE;
						deviation = 0;
					} else {
						alertLevel = processorSpeed<minSpeed ? AlertLevel.LOW : AlertLevel.NONE;
						deviation = (double)(minSpeed-processorSpeed)/(double)minSpeed;
					}
					if(alertLevel.compareTo(minimumAlertLevel)>=0) {
						if(
							!resultHandler.handleResult(
								new ObjectResult<>(
									domU.getHostname(),
									minSpeed==-1 ? null : minSpeed,
									processorSpeed,
									deviation,
									alertLevel
								)
							)
						) return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Gets the unmodifiable set of specific processor cores results.  It has a
	 * separate entry for each DomU that is either primary or secondary (with RAM)
	 * on this dom0.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getProcessorCoresResults(ResultHandler<? super Integer> resultHandler, AlertLevel minimumAlertLevel) {
		if(minimumAlertLevel.compareTo(AlertLevel.MEDIUM)<=0) {
			int processorCores = dom0.getProcessorCores();

			List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
			for(DomUConfiguration domUConfiguration : domUConfigurations) {
				DomU domU = domUConfiguration.getDomU();
				if(
					domUConfiguration.getPrimaryDom0()==dom0
					|| (
						domUConfiguration.getSecondaryDom0()==dom0
						&& domU.getSecondaryRam()!=-1
					)
				) {
					int minCores = domU.getProcessorCores();
					AlertLevel alertLevel = minCores!=-1 && processorCores<minCores ? AlertLevel.MEDIUM : AlertLevel.NONE;
					if(alertLevel.compareTo(minimumAlertLevel)>=0) {
						if(
							!resultHandler.handleResult(
								new ObjectResult<>(
									domU.getHostname(),
									minCores==-1 ? null : minCores,
									processorCores,
									(double)(minCores-processorCores)/(double)minCores,
									alertLevel
								)
							)
						) return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Gets the free primary processor weight.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getPrimaryProcessorWeightResult(ResultHandler<? super Integer> resultHandler, AlertLevel minimumAlertLevel) {
		if(minimumAlertLevel.compareTo(AlertLevel.MEDIUM)<=0) {
			int allocatedPrimaryWeight = 0;
			for(DomUConfiguration domUConfiguration : clusterConfiguration.getDomUConfigurations()) {
				if(domUConfiguration.getPrimaryDom0()==dom0) {
					DomU domU = domUConfiguration.getDomU();
					allocatedPrimaryWeight += (int)domU.getProcessorCores() * (int)domU.getProcessorWeight();
				}
			}
			int totalWeight = dom0.getProcessorCores() * 1024;
			int overcommittedWeight = allocatedPrimaryWeight - totalWeight;
			AlertLevel alertLevel = overcommittedWeight>0 ? AlertLevel.MEDIUM : AlertLevel.NONE;
			if(alertLevel.compareTo(minimumAlertLevel)>=0) {
				return resultHandler.handleResult(
					new IntResult(
						"Primary Processor Weight",
						allocatedPrimaryWeight,
						totalWeight,
						((double)overcommittedWeight / (double)totalWeight),
						alertLevel
					)
				);
			}
		}
		return true;
	}

	/**
	 * Gets the unmodifiable list of specific requires HVM results.  It has a
	 * separate entry for each DomU that is either primary or secondary (with RAM)
	 * on this dom0.
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getRequiresHvmResults(ResultHandler<? super Boolean> resultHandler, AlertLevel minimumAlertLevel) {
		boolean supportsHvm = dom0.getSupportsHvm();
		List<DomUConfiguration> domUConfigurations = clusterConfiguration.getDomUConfigurations();
		for(DomUConfiguration domUConfiguration : domUConfigurations) {
			DomU domU = domUConfiguration.getDomU();
			if(domUConfiguration.getPrimaryDom0()==dom0) {
				boolean requiresHvm = domU.getRequiresHvm();
				AlertLevel alertLevel;
				double deviation;
				if(requiresHvm) {
					if(supportsHvm) {
						alertLevel = AlertLevel.NONE;
						deviation = 0;
					} else {
						alertLevel = AlertLevel.CRITICAL;
						deviation = 1;
					}
				} else {
					alertLevel = AlertLevel.NONE;
					if(supportsHvm) {
						deviation = -1;
					} else {
						deviation = 0;
					}
				}
				if(alertLevel.compareTo(minimumAlertLevel)>=0) {
					if(
						!resultHandler.handleResult(
							new BooleanResult(
								domU.getHostname(),
								requiresHvm,
								supportsHvm,
								deviation,
								alertLevel
							)
						)
					) return false;
				}
			} else if(
				domUConfiguration.getSecondaryDom0()==dom0
				&& domU.getSecondaryRam()!=-1
			) {
				boolean requiresHvm = domU.getRequiresHvm();
				AlertLevel alertLevel;
				double deviation;
				if(requiresHvm) {
					if(supportsHvm) {
						alertLevel = AlertLevel.NONE;
						deviation = 0;
					} else {
						alertLevel = AlertLevel.HIGH;
						deviation = 1;
					}
				} else {
					alertLevel = AlertLevel.NONE;
					if(supportsHvm) {
						deviation = -1;
					} else {
						deviation = 0;
					}
				}
				if(alertLevel.compareTo(minimumAlertLevel)>=0) {
					if(
						!resultHandler.handleResult(
							new BooleanResult(
								domU.getHostname(),
								requiresHvm,
								supportsHvm,
								deviation,
								alertLevel
							)
						)
					) return false;
				}
			}
		}
		return true;
	}

	/**
	 * Gets the unsorted, unmodifiable list of results for each disk.
	 */
	public List<AnalyzedDom0DiskConfiguration> getDom0Disks() {
		Map<String, Dom0Disk> clusterDom0Disks = dom0.getDom0Disks();
		int size = clusterDom0Disks.size();
		if(size==0) return Collections.emptyList();
		else if(size==1) {
			return Collections.singletonList(
				new AnalyzedDom0DiskConfiguration(clusterConfiguration, clusterDom0Disks.values().iterator().next())
			);
		} else {
			AnalyzedDom0DiskConfiguration[] array = new AnalyzedDom0DiskConfiguration[size];
			int index = 0;
			for(Dom0Disk dom0Disk : clusterDom0Disks.values()) {
				array[index++] = new AnalyzedDom0DiskConfiguration(clusterConfiguration, dom0Disk);
			}
			assert index==size : "index!=size: "+index+"!="+size;
			return new UnmodifiableArrayList<>(array);
		}
	}

	/**
	 * @see AnalyzedClusterConfiguration#getAllResults(com.aoindustries.aoserv.cluster.analyze.ResultHandler, com.aoindustries.aoserv.cluster.analyze.AlertLevel)
	 *
	 * @return true if more results are wanted, or false to receive no more results.
	 */
	public boolean getAllResults(ResultHandler<Object> resultHandler, AlertLevel minimumAlertLevel) {
		if(!getPrimaryRamResult(resultHandler, minimumAlertLevel)) return false;
		if(!getSecondaryRamResults(resultHandler, minimumAlertLevel)) return false;
		if(!getProcessorTypeResults(resultHandler, minimumAlertLevel)) return false;
		if(!getProcessorArchitectureResults(resultHandler, minimumAlertLevel)) return false;
		if(!getProcessorSpeedResults(resultHandler, minimumAlertLevel)) return false;
		if(!getProcessorCoresResults(resultHandler, minimumAlertLevel)) return false;
		if(!getPrimaryProcessorWeightResult(resultHandler, minimumAlertLevel)) return false;
		if(!getRequiresHvmResults(resultHandler, minimumAlertLevel)) return false;
		// The highest alert level for disks is HIGH, avoid ArrayList creation here
		if(minimumAlertLevel.compareTo(AlertLevel.HIGH)<=0) {
			for(AnalyzedDom0DiskConfiguration dom0Disk : getDom0Disks()) {
				if(!dom0Disk.getAllResults(resultHandler, minimumAlertLevel)) return false;
			}
		}
		return true;
	}
}
