/**
 * Copyright 2010 Tobias Teunissen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package de.teunito.android.cyclelife.heartbeat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * @author teunito
 *
 */
public interface IBluetoothConnection {

	/**
	 * Return the current connection state. */
	public int getState();

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * @param device  The BluetoothDevice to connect
	 */
	public void connect(BluetoothDevice device, IHeartbeatObserver observer);

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * @param socket  The BluetoothSocket on which the connection was made
	 * @param device  The BluetoothDevice that has been connected
	 */
	public void connected(BluetoothSocket socket, BluetoothDevice device);

	/**
	 * Stop all threads
	 */
	public void stop();

}