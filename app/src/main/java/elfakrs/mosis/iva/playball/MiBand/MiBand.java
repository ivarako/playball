package elfakrs.mosis.iva.playball.MiBand;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MiBand {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;
    private ArrayList<Observer> observers;
    private Context mContext;


    public MiBand(Context context){
        observers = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
    }

    public void connect(){
        if(searchDevice()) {
            bluetoothGatt = bluetoothDevice.connectGatt(mContext, true, bluetoothGattCallback);
            updateObservers(1, null);
        }

    }

    private boolean searchDevice() {
        Set<BluetoothDevice> pairDevices = bluetoothAdapter.getBondedDevices();
        if (pairDevices.size() > 0) {
            for (BluetoothDevice device : pairDevices) {
                if (device.getName().contains("MI")) {
                    bluetoothDevice = device;
                    break;
                }
            }
        }
        if (bluetoothDevice == null)
            return false;
        else
           return true;
    }

    private void stateConnected() {
        bluetoothGatt.discoverServices();
    }

    private void stateDisconnected() {
        bluetoothGatt.disconnect();
    }

    public boolean getSteps() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(MiBandUUIDs.Basic.service)
                .getCharacteristic(MiBandUUIDs.Basic.stepsCharacteristic);
        if (bluetoothGatt.readCharacteristic(bchar))
            return true;
        return false;
    }

    public boolean startScanHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(MiBandUUIDs.HeartRate.service).getCharacteristic(MiBandUUIDs.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[]{21, 2, 1});
        if(bluetoothGatt.writeCharacteristic(bchar))
            return true;
        return false;
    }

    private void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(MiBandUUIDs.HeartRate.service).getCharacteristic(MiBandUUIDs.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(MiBandUUIDs.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    public boolean startVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(MiBandUUIDs.AlertNotification.service).getCharacteristic(MiBandUUIDs.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{2});
        if (bluetoothGatt.writeCharacteristic(bchar))
            return true;
        return false;
    }

    public boolean stopVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(MiBandUUIDs.AlertNotification.service).getCharacteristic(MiBandUUIDs.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{0});
        if (bluetoothGatt.writeCharacteristic(bchar))
            return true;
        return false;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            setupData(characteristic.getUuid(), characteristic.getValue());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            setupData(characteristic.getUuid(), characteristic.getValue());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };

    private void setupData(UUID chUUID, byte[] data){

        if(chUUID.equals(MiBandUUIDs.Basic.stepsCharacteristic)){
            byte[] tmp = {data[2], data[1]};
            int steps = new BigInteger(tmp).intValue();
            updateObservers(3, steps);
        }

        if(chUUID.equals(MiBandUUIDs.HeartRate.measurementCharacteristic)){
            int heartbeat = data[1];
            updateObservers(2, heartbeat);
        }
    }

    public void addObserver (Observer observer){
        observers.add(observer);
    }

    public void removeObserver(Observer observer){
        observers.remove(observer);
    }

    private void updateObservers(int mode, Object o){
        for(int i = 0; i<observers.size(); i++)
            observers.get(i).updateObserver(mode, o);
    }
}
