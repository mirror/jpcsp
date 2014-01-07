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
package jpcsp.sound;

import static jpcsp.HLE.modules.sceSasCore.PSP_SAS_PITCH_BASE;
import static jpcsp.HLE.modules.sceSasCore.getSasADSRCurveTypeName;
import static jpcsp.HLE.modules150.sceSasCore.PSP_SAS_ADSR_CURVE_MODE_LINEAR_DECREASE;
import static jpcsp.HLE.modules150.sceSasCore.PSP_SAS_ADSR_CURVE_MODE_LINEAR_INCREASE;
import static jpcsp.hardware.Audio.PSP_AUDIO_VOLUME_MAX;
import jpcsp.HLE.modules150.sceAtrac3plus.AtracID;

public class SoundVoice {
	private boolean changed;
	private int leftVolume;
	private int rightVolume;
	private int sampleRate;
	private int vagAddress;
	private int vagSize;
	private AtracID atracId;
	private int pcmAddress;
	private int pcmSize;
    private int loopMode;
    private int pitch;
    private int noise;
    private boolean playing;
    private boolean paused;
    private boolean on;
    private VoiceADSREnvelope envelope;
    private int playSample;
    private int index;

    public class VoiceADSREnvelope {
        public int AttackRate;
        public int DecayRate;
        public int SustainRate;
        public int ReleaseRate;
        public int AttackCurveType;
        public int DecayCurveType;
        public int SustainCurveType;
        public int ReleaseCurveType;
        public int SustainLevel;
        public int height;

        public VoiceADSREnvelope() {
        	// Default values (like on PSP)
        	AttackRate = 0;
        	DecayRate = 0;
        	SustainRate = 0;
        	ReleaseRate = 0;
            AttackCurveType = PSP_SAS_ADSR_CURVE_MODE_LINEAR_INCREASE;
            DecayCurveType = PSP_SAS_ADSR_CURVE_MODE_LINEAR_DECREASE;
            SustainCurveType = PSP_SAS_ADSR_CURVE_MODE_LINEAR_DECREASE;
            ReleaseCurveType = PSP_SAS_ADSR_CURVE_MODE_LINEAR_DECREASE;
            SustainLevel = 0;
            height = 0;
        }

		@Override
		public String toString() {
			return String.format("VoiceADSREnvelope[AR 0x%08X(%s), DR 0x%08X(%s), SR 0x%08X(%s, SL 0x%08X), RR 0x%08X(%s)]", AttackRate, getSasADSRCurveTypeName(AttackCurveType), DecayRate, getSasADSRCurveTypeName(DecayCurveType), SustainRate, getSasADSRCurveTypeName(SustainCurveType), SustainLevel, ReleaseRate, getSasADSRCurveTypeName(ReleaseCurveType));
		}
    }

	public SoundVoice(int index) {
		this.index = index;
		changed = true;
        leftVolume = PSP_AUDIO_VOLUME_MAX;
        rightVolume = PSP_AUDIO_VOLUME_MAX;
		vagAddress = 0;
		vagSize = 0;
		pcmAddress = 0;
		pcmSize = 0;
        loopMode = 0;
        pitch = PSP_SAS_PITCH_BASE;
        noise = 0;
        playing = false;
        paused = false;
        on = false;
        envelope = new VoiceADSREnvelope();
        playSample = 0;
        atracId = null;
	}

    private void onVoiceChanged() {
    	changed = true;
    	if (isOn() && !isPlaying()) {
    		// A parameter was changed while the voice was ON but no longer playing.
    		// Restart playing.
    		setPlaying(true);
    	}
    }

	public int getLeftVolume() {
		return leftVolume;
	}

	public void setLeftVolume(int leftVolume) {
		this.leftVolume = leftVolume;
	}

	public int getRightVolume() {
		return rightVolume;
	}

	public void setRightVolume(int rightVolume) {
		this.rightVolume = rightVolume;
	}

    public void on() {
        on = true;
        setPlaying(true);
	}

    public void off() {
        on = false;
    }

    public VoiceADSREnvelope getEnvelope() {
    	return envelope;
    }

    public boolean isOn() {
		return on;
	}

    public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		playSample = 0;
		if (!playing) {
			envelope.height = 0;
			off();
		}
		this.playing = playing;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public void setVAG(int address, int size) {
		vagAddress = address;
		vagSize = size;
		atracId = null;
		pcmAddress = 0;
		onVoiceChanged();
	}

	public int getVAGAddress() {
		return vagAddress;
	}

	public int getVAGSize() {
		return vagSize;
	}

	public int getLoopMode() {
		return loopMode;
	}

	public void setLoopMode(int loopMode) {
		if (this.loopMode != loopMode) {
			this.loopMode = loopMode;
			onVoiceChanged();
		}
	}

	public int getPitch() {
		return pitch;
	}

	public void setPitch(int pitch) {
		if (this.pitch != pitch) {
			this.pitch = pitch;
			onVoiceChanged();
		}
	}

	public int getNoise() {
		return noise;
	}

	public void setNoise(int noise) {
		if (this.noise != noise) {
			this.noise = noise;
			onVoiceChanged();
		}
	}

	public int getPlaySample() {
		return playSample;
	}

	public void setPlaySample(int playSample) {
		this.playSample = playSample;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public boolean isEnded() {
		return !isPlaying();
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return String.format("SoundVoice #%d", index);
	}

	public AtracID getAtracId() {
		return atracId;
	}

	public void setAtracId(AtracID atracId) {
		this.atracId = atracId;
		pcmAddress = 0;
		vagAddress = 0;
		onVoiceChanged();
	}

	public void setPCM(int address, int size) {
		pcmAddress = address;
		pcmSize = size;
		atracId = null;
		vagAddress = 0;
		onVoiceChanged();
	}

	public int getPcmAddress() {
		return pcmAddress;
	}

	public int getPcmSize() {
		return pcmSize;
	}
}