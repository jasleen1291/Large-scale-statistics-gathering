/*================================================================================
Copyright (c) 2008 VMware, Inc. All Rights Reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

 * Neither the name of VMware, Inc. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior 
written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.
================================================================================*/

package com.vmware.vim25.mo.samples;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.rmi.RemoteException;



import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * http://vijava.sf.net
 * @author Steve Jin
 */

public class RealtimePerfMonitor_Host 
{
	public static void main(String[] args) throws Exception
	{



		try
		{
			String filename= "MyFile.txt";
			FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			///fw.write("add a line\n");//appends the string to the file
			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		}

		URL url1 = new URL("https://130.65.133.50/sdk");

		ServiceInstance si = new ServiceInstance(url1, "administrator", "12!@qwQW", true);

		Folder rootFolder = si.getRootFolder();
		ManagedEntity host=new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", "130.65.133.53");

		ManagedEntity mes = new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine","VM_Ubuntu_Vaijayanthi");


		if(mes==null )
		{
			return;
		}
		if(host==null)

		{
			return;
		}

		//VirtualMachine vm = (VirtualMachine) mes;
		HostSystem vm=(HostSystem) host;
		String vmname=vm.getName();




		/*if(vm==null)
		{
			System.out.println("Virtual Machine " + vmname + " cannot be found.");

			try
			{
				String filename= "MyFile.txt";
				FileWriter fw = new FileWriter(filename,true); //the true will append the new data
				fw.write("Virtual Machine " + vmname + " cannot be found.");//appends the string to the file
				fw.close();
			}
			catch(IOException ioe)
			{
				System.err.println("IOException: " + ioe.getMessage());
			}

			si.getServerConnection().logout();
			return;
		}*/

		PerformanceManager perfMgr = si.getPerformanceManager();

		// find out the refresh rate for the virtual machine
		PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(vm);
		int refreshRate = pps.getRefreshRate().intValue();

		//System.out.println("\nHence, the refresh rate for this VM is :"+ refreshRate);


		// retrieve all the available perf metrics for vm
		PerfMetricId[] pmis = perfMgr.queryAvailablePerfMetric(
				vm, null, null, refreshRate);

		PerfQuerySpec qSpec = createPerfQuerySpec(
				vm, pmis, 3, refreshRate);

		while(true) 
		{
			PerfEntityMetricBase[] pValues = perfMgr.queryPerf(
					new PerfQuerySpec[] {qSpec});



			if(pValues != null)
			{
				displayValues(pValues, perfMgr);
			}
			System.out.println("Sleeping 60 seconds...");

			try
			{
				String filename= "MyFile.txt";
				FileWriter fw = new FileWriter(filename,true); //the true will append the new data
				//fw.write("\nSleeping 60 seconds!");//appends the string to the file
				fw.close();
			}
			catch(IOException ioe)
			{
				System.err.println("IOException: " + ioe.getMessage());
			}

			Thread.sleep(refreshRate*3*1000);
		}


	}

	static PerfQuerySpec createPerfQuerySpec(ManagedEntity me, 
			PerfMetricId[] metricIds, int maxSample, int interval)
	{
		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(me.getMOR());
		// set the maximum of metrics to be return
		// only appropriate in real-time performance collecting
		qSpec.setMaxSample(new Integer(maxSample));
		//    qSpec.setMetricId(metricIds);
		// optionally you can set format as "normal"
		//qSpec.setFormat("csv");
		qSpec.setFormat("normal");
		// set the interval to the refresh rate for the entity
		qSpec.setIntervalId(new Integer(interval));

		return qSpec;
	}

	static void displayValues(PerfEntityMetricBase[] values, PerformanceManager perf ) throws RuntimeFault, RemoteException
	{
		for(int i=0; i<values.length; ++i) 
		{
			String entityDesc = values[i].getEntity().getType() 
					+ ":" + values[i].getEntity().get_value();
			System.out.println("Entity:" + entityDesc);

			try
			{
				String filename= "MyFile.txt";
				FileWriter fw = new FileWriter(filename,true); //the true will append the new data
				fw.write("\nEntity: "+ entityDesc);//appends the string to the file
				fw.close();
			}
			catch(IOException ioe)
			{
				System.err.println("IOException: " + ioe.getMessage());
			}

			if(values[i] instanceof PerfEntityMetric)
			{
				printPerfMetric((PerfEntityMetric)values[i], perf);
			}
			else if(values[i] instanceof PerfEntityMetricCSV)
			{
				printPerfMetricCSV((PerfEntityMetricCSV)values[i]);
			}
			else
			{
				System.out.println("UnExpected sub-type of " +
						"PerfEntityMetricBase.");

				try
				{
					String filename= "MyFile.txt";
					FileWriter fw = new FileWriter(filename,true); //the true will append the new data
					fw.write("UnExpected sub-type of PerfEntityMetricBase. ");//appends the string to the file
					fw.close();
				}
				catch(IOException ioe)
				{
					System.err.println("IOException: " + ioe.getMessage());
				}

			}
		}
	}

	static void printPerfMetric(PerfEntityMetric pem, PerformanceManager perf) throws RuntimeFault, RemoteException
	{
		PerfMetricSeries[] vals = pem.getValue();
		PerfSampleInfo[]  infos = pem.getSampleInfo();
		int[] counterIds;

		System.out.println("Sampling Times and Intervals:");

		try
		{
			String filename= "MyFile.txt";
			FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			//fw.write("\tSampling Times and Intervals:");//appends the string to the file
			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		}

		for(int i=0; infos!=null && i <infos.length; i++)
		{
			//System.out.println("Sample time: " 
					//+ infos[i].getTimestamp().getTime());
			//System.out.println("Sample interval (sec):" 
					//+ infos[i].getInterval());


			try
			{
				String filename= "MyFile.txt";
				FileWriter fw = new FileWriter(filename,true); //the true will append the new data
				//fw.write("\tSample time: "+infos[i].getTimestamp().getTime());//appends the string to the file
				//fw.write("\tSample interval: "+infos[i].getInterval());//appends the string to the file
				fw.close();
			}
			catch(IOException ioe)
			{
				System.err.println("IOException: " + ioe.getMessage());
			}

		}
		//System.out.println("Sample values:");

		try
		{
			String filename= "MyFile.txt";
			FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			//fw.write("\tSample Values: ");
			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		}
		int[] array_ids = new int[]{90,29,57,430,450,335,190,130,245,12,2,16};

		for(int j=0; vals!=null && j<vals.length; ++j)
		{
			for(int i=0;i<array_ids.length;i++)
			{
				if(vals[j].getId().getCounterId()==array_ids[i])
				{
					System.out.println("Perf counter ID:" + vals[j].getId().getCounterId());
					System.out.println("Device instance ID:" + vals[j].getId().getInstance());



					try
					{
						String filename= "MyFile.txt";
						FileWriter fw = new FileWriter(filename,true); //the true will append the new data
						fw.write("\tPerf Counter ID: "+vals[j].getId().getCounterId());//appends the string to the file
						fw.write("\tDevice Instance ID: "+vals[j].getId().getInstance());//appends the string to the file
						fw.close();
					}
					catch(IOException ioe)
					{
						System.err.println("IOException: " + ioe.getMessage());
					}


					counterIds = new int[]{vals[j].getId().getCounterId()};
					//printPerfCounters(perf.queryPerfCounter(counterIds));
					//System.out.println("\n");

					if(vals[j] instanceof PerfMetricIntSeries)
					{
						PerfMetricIntSeries val = (PerfMetricIntSeries) vals[j];
						long[] longs = val.getValue();
						for(int k=0; k<longs.length; k++) 
						{
							System.out.print("\n Sample time: "+infos[k].getTimestamp().getTime());
							//printPerfCounters(perf.queryPerfCounter(counterIds));
							System.out.print("\t" + longs[k] + " ");

							try
							{
								String filename= "MyFile.txt";
								FileWriter fw = new FileWriter(filename,true); //the true will append the new data
								fw.write("\t Sample time: "+infos[k].getTimestamp().getTime());
								printPerfCounters(perf.queryPerfCounter(counterIds));
								fw.write("\tValue: "+longs[k] + " ");//appends the string to the file
								//fw.write("\nSample interval: "+vals[j].getId().getInstance());//appends the string to the file
								fw.close();
							}
							catch(IOException ioe)
							{
								System.err.println("IOException: " + ioe.getMessage());
							}



						}
						//System.out.println("Total:"+longs.length);

						try
						{
							String filename= "MyFile.txt";
							FileWriter fw = new FileWriter(filename,true); //the true will append the new data
							//fw.write("\tTotal:" + longs.length);//appends the string to the file
							// fw.write("\nSample interval: "+vals[j].getId().getInstance());//appends the string to the file
							fw.close();
						}
						catch(IOException ioe)
						{
							System.err.println("IOException: " + ioe.getMessage());
						}
					}
					else if(vals[j] instanceof PerfMetricSeriesCSV)
					{ // it is not likely coming here...
						PerfMetricSeriesCSV val = (PerfMetricSeriesCSV) vals[j];
						System.out.println("CSV value:" + val.getValue());
					}
				}
			}
		}
	}

	static void printPerfCounters(PerfCounterInfo[] pcis)
	{
		for(int i=0; pcis!=null && i<pcis.length; i++)
		{
			System.out.print("\nKey:" + pcis[i].getKey());
			 java.util.Date date= new java.util.Date();
			 //System.out.println(new Timestamp(date.getTime()));
			String perfCounter = pcis[i].getGroupInfo().getKey() + "."+ pcis[i].getNameInfo().getKey() + "."+ pcis[i].getRollupType();
			//System.out.println("PerfCounter:" + perfCounter);
			System.out.print("\tDescription: "+pcis[i].getNameInfo().getSummary());
//			System.out.println("Level:" + pcis[i].getLevel());
//			System.out.println("StatsType:" + pcis[i].getStatsType());
//			System.out.println("UnitInfo:"+ pcis[i].getUnitInfo().getKey());


			try
			{
				String filename= "MyFile.txt";
				FileWriter fw = new FileWriter(filename,true); //the true will append the new data
				fw.write("\nKey:" + pcis[i].getKey());//appends the string to the file
//				fw.write("\nPerfCounter:" + perfCounter);//appends the string to the file
//
			fw.write("\tDescription: "+pcis[i].getNameInfo().getSummary());
//				fw.write("\nLevel:" + pcis[i].getLevel());
//				fw.write("\nStatsType:" + pcis[i].getStatsType());
//				fw.write("\nUnitInfo:"+ pcis[i].getUnitInfo().getKey());
				fw.close();
			}
			catch(IOException ioe)
			{
				System.err.println("IOException: " + ioe.getMessage());
			}
		}
	}

	static void printPerfMetricCSV(PerfEntityMetricCSV pems)
	{
		System.out.println("SampleInfoCSV:" 
				+ pems.getSampleInfoCSV());
		PerfMetricSeriesCSV[] csvs = pems.getValue();
		for(int i=0; i<csvs.length; i++)
		{
			System.out.println("PerfCounterId:" 
					+ csvs[i].getId().getCounterId());
			System.out.println("CSV sample values:" 
					+ csvs[i].getValue());
		}
	}
}