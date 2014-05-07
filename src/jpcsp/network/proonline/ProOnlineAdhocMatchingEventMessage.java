/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.network.proonline;

import static jpcsp.HLE.modules150.sceNet.convertMacAddressToString;

import org.apache.log4j.Logger;

import jpcsp.network.adhoc.AdhocMatchingEventMessage;
import jpcsp.network.adhoc.MatchingObject;

/**
 * @author gid15
 *
 * A generic ProOnlineAdhocMatchingEventMessage is consisting of:
 * - 1 byte for the event
 * - 4 bytes for the message data length
 * - n bytes for the message data
 */
public class ProOnlineAdhocMatchingEventMessage extends AdhocMatchingEventMessage {
	protected static Logger log = ProOnlineNetworkAdapter.log;
	protected static final int HEADER_SIZE = 1 + 4;
	private int packetOpcode;

	public ProOnlineAdhocMatchingEventMessage(MatchingObject matchingObject, int event, int packetOpcode) {
		super(matchingObject, event);
		this.packetOpcode = packetOpcode;
	}

	public ProOnlineAdhocMatchingEventMessage(MatchingObject matchingObject, int event, int packetOpcode, int address, int length, byte[] toMacAddress) {
		super(matchingObject, event, address, length, toMacAddress);
		this.packetOpcode = packetOpcode;
	}

	public ProOnlineAdhocMatchingEventMessage(MatchingObject matchingObject, int event, byte[] message, int length) {
		super(matchingObject, message, length);
		setEvent(event);
	}

	@Override
	public byte[] getMessage() {
		byte[] message = new byte[getMessageLength()];
		offset = 0;
		addToBytes(message, (byte) getPacketOpcode());
		addInt32ToBytes(message, getDataLength());
		addToBytes(message, data);

		return message;
	}

	@Override
	public void setMessage(byte[] message, int length) {
		if (length >= HEADER_SIZE) {
			offset = 0;
			setPacketOpcode(copyByteFromBytes(message));
			int dataLength = copyInt32FromBytes(message);
			data = new byte[Math.min(dataLength, length - HEADER_SIZE)];
			copyFromBytes(message, data);
		}
	}

	protected int getPacketOpcode() {
		return packetOpcode;
	}

	protected void setPacketOpcode(int packetOpcode) {
		this.packetOpcode = packetOpcode;
	}

	@Override
	public int getMessageLength() {
		return super.getMessageLength() + HEADER_SIZE;
	}

	@Override
	public String toString() {
		return String.format("%s[fromMacAddress=%s, toMacAddress=%s, dataLength=%d, event=%d, packetOpcode=%d]", getClass().getSimpleName(), convertMacAddressToString(fromMacAddress), convertMacAddressToString(toMacAddress), getDataLength(), getEvent(), getPacketOpcode());
	}
}
