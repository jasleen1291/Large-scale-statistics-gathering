package lab3;

import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.Timedout;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoadBalance {

    ServiceInstance si = null;
    ArrayList<MyHost> host;
    ArrayList<MyVM> vm;
    String url = "https://192.168.143.138/sdk";
    String username = "administrator@vsphere.local";
    String password = "12!@qwQW";
    Folder vmFolder = null, hostFolder = null;
    String extraHost = "192.168.143.142";
    String hostPassword = "1234567";
    String hostUsername = "root";
    String templatename = "Ubuntu1";
    Datacenter dc = null;
    Thread input, background;

    public LoadBalance() {
        try {
            si = new ServiceInstance(new URL(url),
                    username, password, true);

            String dcName = "130.65.133.50";

            Folder rootFolderadmin = si.getRootFolder();
            dc = (Datacenter) new InventoryNavigator(rootFolderadmin)
                    .searchManagedEntity("Datacenter", dcName);

            vmFolder = dc.getVmFolder();

            hostFolder = dc.getHostFolder();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    void refesh() {
        try {
            host = new ArrayList<>();

            vm = new ArrayList<>();

            ManagedEntity templates[] = new InventoryNavigator(vmFolder)
                    .searchManagedEntities("VirtualMachine");
            PerformanceManager perfMgr = si.getPerformanceManager();
            for (int i = 0; i < templates.length; i++) {
                VirtualMachine vm = (VirtualMachine) templates[i];
                if (vm.getResourcePool() != null) {

                    PerfProviderSummary pps = perfMgr
                            .queryPerfProviderSummary(vm);
                    int refreshRate = pps.getRefreshRate().intValue();
                    PerfMetricId[] pmis = perfMgr.queryAvailablePerfMetric(vm,
                            null, null, refreshRate);

                    PerfQuerySpec qSpec = createPerfQuerySpec(vm, pmis, 3,
                            refreshRate);

                    PerfEntityMetricBase[] pValues = perfMgr
                            .queryPerf(new PerfQuerySpec[]{qSpec});

                    if (pValues != null) {
                        displayValues(pValues, perfMgr, new MyVM((vm)));
                    }
                }
            }

            ManagedEntity host[] = new InventoryNavigator(hostFolder)
                    .searchManagedEntities("HostSystem");
            for (ManagedEntity host1 : host) {
                HostSystem vm = (HostSystem) host1;

                PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(vm);
                int refreshRate = pps.getRefreshRate().intValue();
                PerfMetricId[] pmis = perfMgr.queryAvailablePerfMetric(vm,
                        null, null, refreshRate);
                if (pmis != null) {
                    try {
                        PerfQuerySpec qSpec = createPerfQuerySpec(vm, pmis, 3,
                                refreshRate);
               // System.out.println(pmis);
                        //  vm.powerDownHostToStandBy(refreshRate, true);

                        PerfEntityMetricBase[] pValues = perfMgr
                                .queryPerf(new PerfQuerySpec[]{qSpec});
                        if (pValues != null) {
                            displayValues(pValues, perfMgr, new MyHost(vm));
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

            }

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    boolean loadBal() {
        boolean sol = false;
        Collections.sort((List<MyVM>) vm);
        //Collections.reverse(vmvals);
        System.out.println(Arrays.asList(vm));
        for (MyHost e : host) {
            System.out.println(e.usage);

        }
        if (host.size() > 1) {
            long capacity = host.get(0).getTotalCapacity();
            long threshold = (long) (0.5 * capacity);

            Hashtable<MyHost, Long> currload = new Hashtable<MyHost, Long>();

            Hashtable<MyHost, ArrayList<MyVM>> solution = new Hashtable<MyHost, ArrayList<MyVM>>();
            //System.out.println(host.size() + "\t" + (Collections.max(host).getUsage() + "\t" + Collections.min(host).getUsage()));
            System.out.println("Difference\t" + (Collections.max(host).getUsage() - Collections.min(host).getUsage()));
            System.out.println("Threshold\t" + 0.2 * host.get(0).getTotalCapacity());

            if ((Collections.max(host).getUsage() - Collections.min(host).getUsage()) > 0.2 * host.get(0).getTotalCapacity()) {
                while (!sol) {
                    int size = 0;
                    for (int i = 0; i < host.size(); i++) {
                        currload.put(host.get(i), (long) 0);
                        solution.put(host.get(i), new ArrayList<MyVM>());
                    }
                    for (int i = 0; i < vm.size(); i++) {
                        for (int j = 0; j < host.size(); j++) {
                            if ((currload.get(host.get(j)) + vm.get(i).getUsage()) < threshold) {
                                currload.put(host.get(j), (currload.get(host.get(j)) + vm.get(i).getUsage()));
                                solution.get(host.get(j)).add(vm.get(i));
                                size = size + 1;
                                break;
                            }

                        }
                    }

                    if (size != vm.size()) {
                        sol = false;
                    } else {
                        sol = true;
                    }

                    threshold = (long) (threshold * 1.05);
                    if (threshold >= ((long) (1.8 * capacity))) {
                        break;
                    }
                    // System.out.println(threshold);
                }
            }

            if (sol) {
                System.out.println("An Optimal solution to load balance the hosts has been found, Beginning migrations");
                Iterator<Map.Entry<MyHost, ArrayList<MyVM>>> it = solution.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<MyHost, ArrayList<MyVM>> entry = it.next();
                    String hostname = entry.getKey().host.getName();
// System.out.println(entry.getKey() + "\t" + Arrays.asList(entry.getValue()) + "\t" + currload.get(entry.getKey()));
                    for (MyVM vm : entry.getValue()) {
                        if (vm.vm.getName().contains(hostname)) {
                        } else {
                            try {
                                migrateVM(si, vm.vm, vm.vm.getName(), hostname);
                            } catch (Exception ex) {
                                Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }
        else if(host.size()==1)
        {
        long sum=0;
        for(MyVM vms:vm)
        {
        sum=sum+vms.usage;
        }
        if(sum>host.get(0).getTotalCapacity())
        {
        return false;
        }
        else
        {
            for(MyVM vms:vm)
        {
        try {
                                migrateVM(si, vms.vm, vms.vm.getName(), host.get(0).host.getName());
                            } catch (Exception ex) {
                                Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
                            }
        }
        return true;
        }
        }
        return sol;
    }
    static LoadBalance lb;

    public static void main(String args[]) {
        lb = new LoadBalance();
        lb.background = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        lb.refesh();
                        lb.loadBal();
                        System.out.println("Load Balanced Sleeping for 5 mins");
                        Thread.sleep(300000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        lb.input = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    int choice;
                    System.out.println("Choose your option");
                    System.out.println("1 . Create VM");
                    System.out.println("2 . Create host and load balance");
                    System.out.println("3 . DPM");
                    String input = br.readLine();
                    choice = Integer.parseInt(input);
                    switch (choice) {
                        case 1:
                            lb.createNewVM();
                            break;
                        case 2:
                            lb.addHost(lb.extraHost);
                            break;
                        case 3:
                            lb.DPM();
                            break;
                        default:
                            break;
                    }
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

        });
       // lb.background.start();
        lb.input.start();
    }

    PerfQuerySpec createPerfQuerySpec(ManagedEntity me,
            PerfMetricId[] metricIds, int maxSample, int interval) {
        PerfQuerySpec qSpec = new PerfQuerySpec();
        qSpec.setEntity(me.getMOR());

        qSpec.setMaxSample(maxSample);

        qSpec.setFormat("normal");

        qSpec.setIntervalId(interval);

        return qSpec;
    }

    void displayValues(PerfEntityMetricBase[] values,
            PerformanceManager perf, MyHost hos) throws RuntimeFault, RemoteException {
        for (int i = 0; i < values.length; ++i) {

            if (values[i] instanceof PerfEntityMetric) {
                PerfMetricSeries[] vals = ((PerfEntityMetric) values[i]).getValue();
                int[] counterIds;

                for (int j = 0; vals != null && j < vals.length; ++j) {

                    counterIds = new int[]{vals[j].getId().getCounterId()};

                    if (vals[j] instanceof PerfMetricIntSeries) {

                        PerfMetricIntSeries val = (PerfMetricIntSeries) vals[j];
                        long[] longs = val.getValue();

                        for (int k = 0; k < longs.length; k++) {

                            try {

                                PerfCounterInfo pcis[] = perf
                                        .queryPerfCounter(counterIds);
                                for (int p = 0; pcis != null && p < pcis.length; p++) {

                                    String perfCounter = pcis[p].getGroupInfo()
                                            .getKey()
                                            + "."
                                            + pcis[p].getNameInfo().getKey()
                                            + "."
                                            + pcis[p].getRollupType();

                                    if ((perfCounter.equals("cpu.usagemhz.average"))) {

                                       // System.out.println(hos + "cpu.usagemhz.average" + longs[k]);
                                        hos.setUsage(longs[k]);
                                    } else if ((perfCounter.equals("cpu.totalCapacity.average"))) {

                                        //System.out.println(hos + "cpu.totalCapacity.average" + longs[k]);
                                        hos.setTotalCapacity(longs[k]);
                                    }
                                    if (hos.getUsage() != -1) {
                                        if (hos.getTotalCapacity() != -1) {
                                            host.add(hos);
                                            return;
                                        }
                                    }
                                }

                            } catch (IOException ioe) {
                                System.err.println("IOException: " + ioe.getMessage());
                            }
                        }
                    }
                }
            }

        }
    }

    void displayValues(PerfEntityMetricBase[] values,
            PerformanceManager perf, MyVM hos) throws RuntimeFault, RemoteException {
        for (int i = 0; i < values.length; ++i) {

            if (values[i] instanceof PerfEntityMetric) {
                PerfMetricSeries[] vals = ((PerfEntityMetric) values[i]).getValue();
                int[] counterIds;

                for (int j = 0; vals != null && j < vals.length; ++j) {

                    counterIds = new int[]{vals[j].getId().getCounterId()};

                    if (vals[j] instanceof PerfMetricIntSeries) {

                        PerfMetricIntSeries val = (PerfMetricIntSeries) vals[j];
                        long[] longs = val.getValue();

                        for (int k = 0; k < longs.length; k++) {

                            try {

                                PerfCounterInfo pcis[] = perf
                                        .queryPerfCounter(counterIds);
                                for (int p = 0; pcis != null && p < pcis.length; p++) {

                                    String perfCounter = pcis[p].getGroupInfo()
                                            .getKey()
                                            + "."
                                            + pcis[p].getNameInfo().getKey()
                                            + "."
                                            + pcis[p].getRollupType();

                                    if ((perfCounter.equals("cpu.usagemhz.average"))) {

                                        //System.out.println(hos + "cpu.usagemhz.average" + longs[k]);
                                        hos.setUsage(longs[k]);
                                        vm.add(hos);
                                        return;
                                    }

                                }

                            } catch (IOException ioe) {
                                System.err.println("IOException: " + ioe.getMessage());
                            }
                        }
                    }
                }
            }

        }
    }

    private void createNewVM() {
        try {
          //  lb.background.wait();
            lb.refesh();
            lb.loadBal();
            lb.createNewVM(Collections.min(host).host);

            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (Exception ex) {
            Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //lb.notify();
        }
    }

    private void DPM() {
        System.out.println("Staring DPM");
        
        refesh();
        loadBal();
        System.out.println("Size"+host.size());
//  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        boolean result = false;
        if (host.size() > 1) {
            System.out.println("Staring DPM");
            if (Collections.max(host).getUsage() < (1.8 * Collections.max(host).getTotalCapacity())) {
                try {

                    MyHost targetHost = Collections.min(host);
                    host.remove(targetHost);
                    result = loadBal();
                    if (result) {
                        targetHost.shutDown();
                    } else {
                        host.add(targetHost);
                        System.out.println("Host is too overloaded");
                        // break;
                    }
                } // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                catch (Exception ex) {
                    Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
        //            background.notify();
                }
            }
        } else {
            System.out.println("Only one host found");
        }
        if (!result) {
            System.out.println("DPM unsuccessful");
        }
    }

    class MyHost implements Comparable<MyHost>, Serializable {

        HostSystem host;

        public MyHost(HostSystem host) {
            this.host = host;
        }

        Long totalCapacity = -1L, usage = -1L;

        public long getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(Long totalCapacity) {
            this.totalCapacity = totalCapacity;
        }

        public Long getUsage() {
            return usage;
        }

        public void setUsage(Long usage) {
            // System.out.println("Setting usage " + usage);
            this.usage = usage;
        }

        @Override
        public int compareTo(MyHost t) {

            return getUsage().compareTo(t.usage); //To change body of generated methods, choose Tools | Templates.
        }

        public void shutDown() {
            try {
                Task task = host.shutdownHost_Task(true);
                if (task.waitForMe() == Task.SUCCESS) {
                    System.out.println("Host Powered Off");
                }
            } catch (Timedout ex) {
                Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidState ex) {
                Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RuntimeFault ex) {
                Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    class MyVM implements Comparable<MyVM>, Serializable {

        VirtualMachine vm;

        public MyVM(VirtualMachine vm) {
            this.vm = vm;
        }

        Long usage;

        public Long getUsage() {
            return usage;
        }

        public void setUsage(Long usage) {
            this.usage = usage;
        }

        @Override
        public int compareTo(MyVM t) {
            return getUsage().compareTo(t.getUsage()); //To change body of generated methods, choose Tools | Templates.
        }

    }

    private void migrateVM(ServiceInstance si, VirtualMachine vm, String vmname, String newhostname)
            throws Exception {
        Folder rootFolder = si.getRootFolder();
        HostSystem newHost = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", newhostname);
        ComputeResource cr = (ComputeResource) newHost.getParent();

        String[] checks = new String[]{"cpu", "software"};
        HostVMotionCompatibility[] vmcs
                = si.queryVMotionCompatibility(vm, new HostSystem[]{newHost}, checks);

        String[] comps = vmcs[0].getCompatibility();
        if (checks.length != comps.length) {
            System.out.println("CPU/software NOT compatible. Exit.");

            return;
        }
        rename(vm, newhostname);
        Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,
                VirtualMachineMovePriority.highPriority,
                VirtualMachinePowerState.poweredOn);

        if (task.waitForMe() == Task.SUCCESS) {
            System.out.println("VMotioned!");
        } else {
            System.out.println("VMotion failed!");
            TaskInfo info = task.getTaskInfo();
            System.out.println(info.getError().getFault());
        }

    }

    private void rename(VirtualMachine vm, String newHost) {
        String oldhost = vm.getName().substring(vm.getName().lastIndexOf("_") + 1);
        String newname = vm.getName().replaceAll(oldhost, newHost);
        System.out.println(vm.getName() + "\t" + newname);
        try {
            Task task = vm.rename_Task(newname);
            task.waitForMe();
        } catch (DuplicateName ex) {
            Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RuntimeFault ex) {
            Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(LoadBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String getSSLCertForHost(String host, int port) throws Exception {
        String sslThumbprint = null;
        TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                    String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                    String authType) {
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{trm}, null);
        SSLSocketFactory factory = sc.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();
        SSLSession session = socket.getSession();
        java.security.cert.Certificate[] servercerts = session
                .getPeerCertificates();
        for (int i = 0; i < servercerts.length; i++) {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(servercerts[i].getEncoded());
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < result.length; j++) {

                sb.append(Integer.toString((result[j] & 0xff) + 0x100, 16)
                        .substring(1));
                if (j != result.length - 1) {
                    sb.append(":");
                }

            }
            sslThumbprint = sb.toString();
            sb.substring(sb.lastIndexOf(":") - 1);
            sslThumbprint = sb.toString();

        }
        socket.close();
        return sslThumbprint;
    }

    public boolean addHost(String host) throws Exception {

        HostConnectSpec spec = new HostConnectSpec();
        spec.setHostName(host);
        spec.setUserName(hostUsername);
        spec.setPassword(hostPassword);
        String sslThumbprint = null;
        try {
            sslThumbprint = getSSLCertForHost(host, 443);
        } catch (Exception e) {
            System.out.println("Could not retrieve SSL certificate. Now Exiting");
            System.exit(0);
            e.printStackTrace();
        }

        spec.setSslThumbprint(sslThumbprint);
        ComputeResourceConfigSpec compResSpec = new ComputeResourceConfigSpec();
        Task task = null;
        String result = null;

        task = hostFolder.addStandaloneHost_Task(spec, compResSpec, true);
        result = task.waitForMe();

        if (result == Task.SUCCESS) {
            System.out.println("Host Added Successfully");
            return true;
        } else {
            System.out.println("Host Could not be added");
            return false;
        }

    }

    private void createNewVM(HostSystem hs) throws Exception {

        Random r = new Random();
        int clonenumber = r.nextInt(100);

        ResourcePool rp = null;

		//ServiceInstance siadmin = si;
        //Folder rootFolderadmin = siadmin.getRootFolder();
        //Datacenter dc = (Datacenter) new InventoryNavigator(rootFolderadmin).searchManagedEntity("Datacenter", dcName);
        VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        //Folder vmFolder = dc.getVmFolder();
        ManagedEntity templates = new InventoryNavigator(vmFolder).searchManagedEntity("VirtualMachine", templatename);
        System.out.println(templates.getName());
        VirtualMachine temp = (VirtualMachine) templates;

        rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntity("ResourcePool", "ResourcePool");

        VirtualMachineRelocateSpec relSpec = new VirtualMachineRelocateSpec();

		//System.out.println(hs.getDatastores()[1].getName());
        //System.out.println(hs.getDatastores()[0].getMOR());
        relSpec.setDatastore((hs.getDatastores()[1].getMOR()));

        relSpec.setHost(hs.getMOR());

        relSpec.setPool(rp.getMOR());

        cloneSpec.setLocation(relSpec);

        cloneSpec.setPowerOn(true);

        cloneSpec.setTemplate(false);

        cloneSpec.setLocation(relSpec);

        Task task = temp.cloneVM_Task((Folder) temp.getParent(),
                "VM_Ubuntu" + clonenumber + "_" + hs.getName(), cloneSpec);
        System.out.println("Launching the VM clone task. "
                + "Please wait ...");

        String status = task.waitForTask();
        if (status == Task.SUCCESS) {
            System.out.println("VM got cloned successfully.");
        } else {
            System.out.println("Failure -: VM cannot be cloned");
        }

        /*temp.powerOnVM_Task(null);
         if(task.waitForMe()==Task.SUCCESS)
         {
         System.out.println(temp + " powered on*********************************");
         }*/
    }

}
