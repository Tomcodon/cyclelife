/*******************************************************************************
 * Copyright 2010 Tobias Teunissen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.teunito.android.cyclelife.heartbeat;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;
import de.teunito.android.cyclelife.model.HeartbeatData;
public class ZephyrHxmProtocol implements IHeartbeatSource {
  /**
   *  constant  
   */
  private static final int STX =  0x02;

  private static final int ETX =  0x03;

  private static final int MSG_ID =  0x26;
  
  private static final boolean D = true;


  /**
   *  States 
   */
  private static final int STATE_WAITING_FOR_STX =  0;

  private static final int STATE_STX =  1;

  private static final int STATE_MSGID =  2;

  private static final int STATE_PAYLOAD =  3;

  private static final String TAG = "ZephyrHxmProtocol";

  private InputStream inputStream;

  private volatile boolean isStopped =  false;

  private byte[] datagramPayload;

  private int currentState =  STATE_WAITING_FOR_STX;

  private IHeartbeatObserver observer;

  public ZephyrHxmProtocol(java.io.InputStream inputStream) {
		this.inputStream = inputStream;
  }

  private int calculateCRC(byte[] buffer, int offset, int length) {
		int crc = 0;
		for (int i = offset; i < offset + length; i++) {
			int unsignedByte = readUnsignedByte(buffer[i]);
			crc = crc ^ unsignedByte;
			for (int j = 0; j < 8; j++) {
				if ((crc & 0x01) > 0) {
					crc = (crc >> 1) ^ 0x8C;
				} else {
					crc = (crc >> 1);
				}
			}
		}
		return crc;
  }

  int error = 0;
	int ok = 0;
	int total = 0;
  
	private void processProtocolByte() throws IOException {

		int b = this.inputStream.read();
		switch (currentState) {
		case STATE_WAITING_FOR_STX:
			if (b == STX) {
				currentState = STATE_STX;
			}
			break;

		case STATE_STX:
			if (b == MSG_ID) {
				currentState = STATE_MSGID;
			} else {
				reset();
			}
			break;

		case STATE_MSGID:
			// b+1 um auch die CRC zu bekommen
			int dataLength = b + 1;
			if (dataLength > 0) {
				currentState = STATE_PAYLOAD;
				this.datagramPayload = new byte[dataLength];
				this.inputStream.read(this.datagramPayload);
			} else {
				this.reset();
			}
			break;

		case STATE_PAYLOAD:
			if (b == ETX) {
				if(D) total++;
				int payloadLength = this.datagramPayload.length;
				int recCRC = readUnsignedByte(this.datagramPayload[payloadLength - 1]);
				int calcCRC = this.calculateCRC(this.datagramPayload, 0,
						payloadLength - 1);
				if (recCRC == calcCRC) {
					if (D) ok++;
					HeartbeatData data = new HeartbeatData();
					data.currentPulse = readUnsignedByte(this.datagramPayload[9]);
					data.cadence = readCadence(datagramPayload);
					data.batteryCharge = readUnsignedByte(this.datagramPayload[8]);
					/* TODO: Datagramm weiter auswerten */

					IHeartbeatObserver target = this.observer;
					if (target != null) {
						target.pushHeartbeatInfo(data);
					}
				} else {
					if(D) error++;					
					reset();
				}
			}
			reset();
			break;
		}
		if(D) Log.i(TAG, "total:"+total +" ok:" + ok + "error:" +error);
  }

  /**
   * get 2 unsigned bytes and combine them into a signed int
   * cadence is  mesured in increments of 1/16 of a stride/minute --> divide by 16 to get rpm
   * @param payload
   * @return cadence
   */
  private int readCadence(byte[] payload) {
		return (readUnsignedByte(payload[54]) << 8 | readUnsignedByte(payload[53]))/16;
  }

  /**
   * shifts unsigned shorts into an signed int
   * @param b
   * @return signed int
   */
  public int readUnsignedByte(byte b) {
		return (((int)b) & 0xFF);
  }

  private void reset() {
		currentState = STATE_WAITING_FOR_STX;
  }

  @Override
  public void setHeartBeatListener(IHeartbeatObserver observer) {
		this.observer = observer;
  }

  public void start() throws IOException {
		isStopped = false;
		while (!isStopped) {
			try {
				this.processProtocolByte();
			} catch (IOException ex) {
				IHeartbeatObserver target = this.observer;
				if (target != null) {
					target.timeout();
				}
				this.reset();
			}
		}
  }

  public void stop() {
		isStopped = true;
  }

}
