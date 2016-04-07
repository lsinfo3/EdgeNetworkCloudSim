package org.cloudbus.cloudsim.ext;

import java.util.List;
import java.util.Objects;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.ext.delay.ConstantVMBootDelay;
import org.cloudbus.cloudsim.ext.delay.IVMBootDelayDistribution;

/**
 * Extended datacenter, that allows for VM booting delays to be modeled.
 * 
 * @author nikolay.grozev
 * 
 */
public class DatacenterEX extends Datacenter {

    private IVMBootDelayDistribution delayDistribution = new ConstantVMBootDelay(0);

    public DatacenterEX(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    public DatacenterEX(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList, double schedulingInterval, IVMBootDelayDistribution delayDistribution)
            throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.delayDistribution = delayDistribution;
    }

    public IVMBootDelayDistribution getDelayDistribution() {
        return delayDistribution;
    }

    public void setDelayDistribution(IVMBootDelayDistribution delayDistribution) {
        this.delayDistribution = delayDistribution;
    }

    @Override
    protected void processOtherEvent(final SimEvent ev) {
        switch (ev.getTag()) {
        case CloudSimTagsExt.DATACENTER_BOOT_VM_TAG:
            Vm vm = (Vm) ev.getData();
            if (vm.isBeingInstantiated()) {
                vm.setBeingInstantiated(false);
            }
            break;
        default:
            super.processOtherEvent(ev);
            break;
        }
    }

    /*
     * Copied and modified from the superclass as we wanted to avoid setting the
     * beingInstantiated flag, before the boot time.
     */
    @Override
    protected void processVmCreate(final SimEvent ev, final boolean ack) {
        Vm vm = (Vm) ev.getData();

        boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

        double delay = delayDistribution.getDelay(vm);
        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = vm.getId();
            data[2] = result ? CloudSimTags.TRUE : CloudSimTags.FALSE;
            send(vm.getUserId(), delay, CloudSimTags.VM_CREATE_ACK, data);
        }

        if (result) {
            send(getId(), delay, CloudSimTagsExt.DATACENTER_BOOT_VM_TAG, vm);

            getVmList().add(vm);

            // if (vm.isBeingInstantiated()) {
            // vm.setBeingInstantiated(false);
            // }

            vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
                    .getAllocatedMipsForVm(vm));
        }

    }

    @Override
    public String toString() {
        return String.format("DC(%s,%d)", Objects.toString(getName(), "N/A"), getId());
    }
}
