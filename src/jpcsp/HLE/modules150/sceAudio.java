/* This autogenerated file is part of jpcsp. */
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

package jpcsp.HLE.modules150;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.ThreadMan;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

public class sceAudio implements HLEModule {
    private class pspChannelInfo {

        public boolean reserved;
        public int allocatedSamples;
        public int format;
        public int leftVolume;
        public int rightVolume;
        public SourceDataLine outputDataLine;

        public pspChannelInfo()
        {
            reserved=false;
            allocatedSamples = 0;
            format = 0;
            leftVolume = 0x8000;
            rightVolume = 0x8000;
            outputDataLine = null;
        }
    }

    static final int PSP_AUDIO_VOLUME_MAX = 0x8000;
    static final int PSP_AUDIO_CHANNEL_MAX = 8;
    static final int PSP_AUDIO_NEXT_CHANNEL = (-1);
    static final int PSP_AUDIO_SAMPLE_MIN = 64;
    static final int PSP_AUDIO_SAMPLE_MAX = 65472;

    static final int PSP_AUDIO_FORMAT_STEREO = 0;
    static final int PSP_AUDIO_FORMAT_MONO = 0x10;

    static final int PSP_AUDIO_FREQ_44K = 44100;
    static final int PSP_AUDIO_FREQ_48K = 48000;

    private pspChannelInfo[] pspchannels; // psp channels
    private int sampleRate;

    @Override
    public String getName() { return "sceAudio"; }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(sceAudioOutputFunction, 0x8C1009B2);
            mm.addFunction(sceAudioOutputBlockingFunction, 0x136CAF51);
            mm.addFunction(sceAudioOutputPannedFunction, 0xE2D56B2D);
            mm.addFunction(sceAudioOutputPannedBlockingFunction, 0x13F592BC);
            mm.addFunction(sceAudioChReserveFunction, 0x5EC81C55);
            mm.addFunction(sceAudioOneshotOutputFunction, 0x41EFADE7);
            mm.addFunction(sceAudioChReleaseFunction, 0x6FC46853);
            mm.addFunction(sceAudioGetChannelRestLengthFunction, 0xB011922F);
            mm.addFunction(sceAudioSetChannelDataLenFunction, 0xCB2E439E);
            mm.addFunction(sceAudioChangeChannelConfigFunction, 0x95FD0C2D);
            mm.addFunction(sceAudioChangeChannelVolumeFunction, 0xB7E1D8E7);
            mm.addFunction(sceAudioSRCChReserveFunction, 0x38553111);
            mm.addFunction(sceAudioSRCChReleaseFunction, 0x5C37C0AE);
            mm.addFunction(sceAudioSRCOutputBlockingFunction, 0xE0727056);
            mm.addFunction(sceAudioInputBlockingFunction, 0x086E5895);
            mm.addFunction(sceAudioInputFunction, 0x6D4BEC68);
            mm.addFunction(sceAudioGetInputLengthFunction, 0xA708C6A6);
            mm.addFunction(sceAudioWaitInputEndFunction, 0x87B2E651);
            mm.addFunction(sceAudioInputInitFunction, 0x7DE61688);
            mm.addFunction(sceAudioInputInitExFunction, 0xE926D3FB);
            mm.addFunction(sceAudioPollInputEndFunction, 0xA633048E);
            mm.addFunction(sceAudioGetChannelRestLenFunction, 0xE9D97901);

            /* From sceAudio_driver */
            mm.addFunction(sceAudioInitFunction, 0x80F1F7E0);
            mm.addFunction(sceAudioEndFunction, 0x210567F7);
            mm.addFunction(sceAudioSetFrequencyFunction, 0xA2BEAA6C);
            mm.addFunction(sceAudioLoopbackTestFunction, 0xB61595C0);
            mm.addFunction(sceAudioSetVolumeOffsetFunction, 0x927AC32B);

            pspchannels = new pspChannelInfo[8];
            for (int i = 0; i < 8; i++)
            {
                pspchannels[i] = new pspChannelInfo();
            }

            sampleRate = 48000;
        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceAudioOutputFunction);
            mm.removeFunction(sceAudioOutputBlockingFunction);
            mm.removeFunction(sceAudioOutputPannedFunction);
            mm.removeFunction(sceAudioOutputPannedBlockingFunction);
            mm.removeFunction(sceAudioChReserveFunction);
            mm.removeFunction(sceAudioOneshotOutputFunction);
            mm.removeFunction(sceAudioChReleaseFunction);
            mm.removeFunction(sceAudioGetChannelRestLengthFunction);
            mm.removeFunction(sceAudioSetChannelDataLenFunction);
            mm.removeFunction(sceAudioChangeChannelConfigFunction);
            mm.removeFunction(sceAudioChangeChannelVolumeFunction);
            mm.removeFunction(sceAudioSRCChReserveFunction);
            mm.removeFunction(sceAudioSRCChReleaseFunction);
            mm.removeFunction(sceAudioSRCOutputBlockingFunction);
            mm.removeFunction(sceAudioInputBlockingFunction);
            mm.removeFunction(sceAudioInputFunction);
            mm.removeFunction(sceAudioGetInputLengthFunction);
            mm.removeFunction(sceAudioWaitInputEndFunction);
            mm.removeFunction(sceAudioInputInitFunction);
            mm.removeFunction(sceAudioInputInitExFunction);
            mm.removeFunction(sceAudioPollInputEndFunction);
            mm.removeFunction(sceAudioGetChannelRestLenFunction);

            /* From sceAudio_driver */
            mm.removeFunction(sceAudioInitFunction);
            mm.removeFunction(sceAudioEndFunction);
            mm.removeFunction(sceAudioSetFrequencyFunction);
            mm.removeFunction(sceAudioLoopbackTestFunction);
            mm.removeFunction(sceAudioSetVolumeOffsetFunction);
        }
    }

    private boolean enabled;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private int doAudioOutput (int channel, int pvoid_buf)
    {
        int ret = -1;

        if(pspchannels[channel].reserved)
        {
            if(pspchannels[channel].outputDataLine == null) // if not yet initialized, do it now.
            {
                try {
                    pspchannels[channel].outputDataLine = AudioSystem.getSourceDataLine(new AudioFormat(sampleRate, 16, 2, true, false));
                    sceAudioChangeChannelVolume(channel,pspchannels[channel].leftVolume,pspchannels[channel].rightVolume);
                }
                catch(LineUnavailableException e)
                {
                    System.out.println("Exception trying to create channel output line. Channel " + channel + " will be silent.");
                    pspchannels[channel].outputDataLine = null;
                    channel=-1;
                }
            }

            if(pspchannels[channel].outputDataLine != null) // if we couldn't initialize the audio line, just ignore the audio output.
            {
                int channels = ((pspchannels[channel].format&0x10)==0x10)?1:2;
                int bytespersample = 4; //2*channels;

                int bytes = bytespersample * pspchannels[channel].allocatedSamples;

                byte[] data = new byte[bytes];

                /*
                for(int i=0;i<bytes;i++)
                {
                    data[i] = (byte)Memory.getInstance().read8(pvoid_buf+i);
                }
                 */

                // process audio volumes ourselves for now
                if(channels == 1)
                {
                    int nsamples = pspchannels[channel].allocatedSamples;
                    for(int i=0;i<nsamples;i++)
                    {
                        short lval = (short)Memory.getInstance().read16(pvoid_buf+i);
                        short rval = lval;

                        lval = (short)((((int)lval)*pspchannels[channel].leftVolume)>>16);
                        rval = (short)((((int)rval)*pspchannels[channel].rightVolume)>>16);

                        data[i*4+0] = (byte)(lval);
                        data[i*4+1] = (byte)(lval>>8);
                        data[i*4+2] = (byte)(rval);
                        data[i*4+3] = (byte)(rval>>8);
                    }
                }
                else
                {
                    int nsamples = pspchannels[channel].allocatedSamples;
                    for(int i=0;i<nsamples;i++)
                    {
                        short lval = (short)Memory.getInstance().read16(pvoid_buf+i*2);
                        short rval = (short)Memory.getInstance().read16(pvoid_buf+i*2+1);

                        lval = (short)((((int)lval)*pspchannels[channel].leftVolume)>>16);
                        rval = (short)((((int)rval)*pspchannels[channel].rightVolume)>>16);

                        data[i*4+0] = (byte)(lval);
                        data[i*4+1] = (byte)(lval>>8);
                        data[i*4+2] = (byte)(rval);
                        data[i*4+3] = (byte)(rval>>8);
                    }
                }

                pspchannels[channel].outputDataLine.write(data,0,data.length);
                pspchannels[channel].outputDataLine.start();

                ret = 0;
            }
        }

        return ret; //just return the first channel
    }

    private int doAudioFlush(int channel)
    {
        if(pspchannels[channel].outputDataLine != null)
        {
            pspchannels[channel].outputDataLine.drain();
            return 0;
        }
        return -1;
    }

    private int sceAudioChangeChannelVolume (int channel, int leftvol, int rightvol)
    {
        int ret = -1;

        if(pspchannels[channel].reserved)
        {
            /* doing the audio processing myself on doAudioOutput for now

            if(pspchannels[channel].outputDataLine != null)
            {

                FloatControl cvolume  = (FloatControl)pspchannels[channel].outputDataLine.getControl(FloatControl.Type.VOLUME);
                FloatControl cpanning;

                if((pspchannels[channel].format&0x10)==0x10)
                     cpanning = (FloatControl)pspchannels[channel].outputDataLine.getControl(FloatControl.Type.PAN);
                else
                     cpanning = (FloatControl)pspchannels[channel].outputDataLine.getControl(FloatControl.Type.BALANCE);

                float vvolume  = Math.max(leftvol,rightvol);
                float balance = (rightvol-leftvol)/vvolume;

                cvolume.setValue(vvolume/32768.0f);
                cpanning.setValue(balance);

                ret = 0;
            }
            else*/
            {
                pspchannels[channel].leftVolume = leftvol;
                pspchannels[channel].rightVolume = rightvol;
                ret = 0;
            }
        }
        return ret; //just return the first channel
    }

    public void sceAudioInit(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioInit [0x80F1F7E0]");

        cpu.gpr[2] = 0xDEADC0DE;

        // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceAudioEnd(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioEnd [0x210567F7]");

        cpu.gpr[2] = 0xDEADC0DE;

        // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceAudioSetFrequency(Processor processor) {
        CpuState cpu = processor.cpu;

        int frequency = cpu.gpr[4];

        int ret = -1;
        if(frequency == 44100 || frequency == 48000)
        {
            sampleRate = frequency;
            ret = 0;
        }
        cpu.gpr[2] = ret; //just return the first channel
    }

    public void sceAudioLoopbackTest(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioLoopbackTest [0xB61595C0]");

        cpu.gpr[2] = 0xDEADC0DE;

        // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceAudioSetVolumeOffset(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioSetVolumeOffset [0x927AC32B]");

        cpu.gpr[2] = 0xDEADC0DE;

        // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceAudioOutput(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = processor.memory;

        int channel = cpu.gpr[4], vol = cpu.gpr[5], pvoid_buf = cpu.gpr[6];

        if (!mem.isAddressGood(pvoid_buf)) {
            Modules.log.warn("sceAudioOutput bad pointer");
            cpu.gpr[2] = -1;
        } else {
            sceAudioChangeChannelVolume(channel, vol, vol);
            cpu.gpr[2] = doAudioOutput(channel, pvoid_buf);
        }
    }

    public void sceAudioOutputBlocking(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = processor.memory;

        int channel = cpu.gpr[4], vol = cpu.gpr[5], pvoid_buf = cpu.gpr[6];

        if (!mem.isAddressGood(pvoid_buf)) {
            Modules.log.warn("sceAudioOutputBlocking bad pointer");
            cpu.gpr[2] = -1;
        } else {
            int ret = -1;

            sceAudioChangeChannelVolume(channel, vol, vol);
            ret = doAudioOutput(channel, pvoid_buf);
            if (ret>=0) {
                ret = doAudioFlush(channel);
            }

            cpu.gpr[2] = ret;
            ThreadMan.getInstance().yieldCurrentThread();
        }
    }

    public void sceAudioOutputPanned(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = processor.memory;

        int channel = cpu.gpr[4], leftvol = cpu.gpr[5], rightvol = cpu.gpr[6], pvoid_buf = cpu.gpr[7];
        int ret = -1;

        if (!mem.isAddressGood(pvoid_buf)) {
            Modules.log.warn("sceAudioOutputPanned bad pointer");
            cpu.gpr[2] = -1;
        } else {
            sceAudioChangeChannelVolume(channel, leftvol, rightvol);
            ret = doAudioOutput(channel,pvoid_buf);
            cpu.gpr[2] = ret;
        }
    }

    public void sceAudioOutputPannedBlocking(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = processor.memory;

        int channel = cpu.gpr[4], leftvol = cpu.gpr[5], rightvol = cpu.gpr[6], pvoid_buf = cpu.gpr[7];
        int ret = -1;

        if (!mem.isAddressGood(pvoid_buf)) {
            Modules.log.warn("sceAudioOutputPannedBlocking bad pointer");
            cpu.gpr[2] = -1;
        } else {
            sceAudioChangeChannelVolume(channel, leftvol, rightvol);
            ret = doAudioOutput(channel,pvoid_buf);
            if (ret>=0) ret = doAudioFlush(channel);
            cpu.gpr[2] = ret;
            ThreadMan.getInstance().yieldCurrentThread();
        }
    }

    public void sceAudioChReserve(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4], samplecount = cpu.gpr[5], format = cpu.gpr[6];

        if (!enabled)
        {
            Modules.log.warn("IGNORED sceAudioChReserve channel= " + channel + " samplecount = " + samplecount + " format = " + format);
            cpu.gpr[2] = -1;
        }
        else
        {
            Modules.log.debug("sceAudioChReserve channel= " + channel + " samplecount = " + samplecount + " format = " + format);

            if(channel!=-1) // use specified channel, if available
            {
                if(pspchannels[channel].reserved)
                {
                    channel = -1;
                }
            }
            else // find first free channel
            {
                for(int i=0;i<8;i++)
                {
                    if(!pspchannels[i].reserved)
                    {
                        channel=i;
                        break;
                    }
                }

            }

            if(channel!=-1) // if channel == -1 here, it means we couldn't use any.
            {
                pspchannels[channel].reserved = true;
                pspchannels[channel].outputDataLine = null; // delay creation until first use.
                pspchannels[channel].allocatedSamples = samplecount;
                pspchannels[channel].format = format;
            }
            cpu.gpr[2] = channel;
        }
    }

    public void sceAudioOneshotOutput(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioOneshotOutput [0x41EFADE7]");

        cpu.gpr[2] = 0xDEADC0DE;

        // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceAudioChRelease(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int ret = -1;

        if(pspchannels[channel].reserved)
        {
            if(pspchannels[channel].outputDataLine!=null)
            {
                pspchannels[channel].outputDataLine.stop();
                pspchannels[channel].outputDataLine.close();
            }
            pspchannels[channel].outputDataLine=null;
            pspchannels[channel].reserved=false;
            ret = 0;
        }
        cpu.gpr[2] = ret; //just return the first channel
    }

    public void sceAudioGetChannelRestLength(Processor processor) {
        sceAudioGetChannelRestLen(processor);
    }

    public void sceAudioSetChannelDataLen(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4], samplecount = cpu.gpr[5];

        pspchannels[channel].allocatedSamples = samplecount;
        cpu.gpr[2] = 0;
    }

    public void sceAudioChangeChannelConfig(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4], format = cpu.gpr[5];

        pspchannels[channel].format = format;
        cpu.gpr[2] = 0; //just return the first channel
    }

    public void sceAudioChangeChannelVolume(Processor processor) {
        CpuState cpu = processor.cpu;

        cpu.gpr[2] = sceAudioChangeChannelVolume(cpu.gpr[4], cpu.gpr[5], cpu.gpr[6]);
    }

    public void sceAudioSRCChReserve(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioSRCChReserve [0x38553111]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioSRCChRelease(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioSRCChRelease [0x5C37C0AE]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioSRCOutputBlocking(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioSRCOutputBlocking [0xE0727056]");

        cpu.gpr[2] = -1;
        ThreadMan.getInstance().yieldCurrentThread();
    }

    public void sceAudioInputBlocking(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioInputBlocking [0x086E5895]");

        cpu.gpr[2] = -1;
        ThreadMan.getInstance().yieldCurrentThread();
    }

    public void sceAudioInput(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioInput [0x6D4BEC68]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioGetInputLength(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioGetInputLength [0xA708C6A6]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioWaitInputEnd(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioWaitInputEnd [0x87B2E651]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioInputInit(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioInputInit [0x7DE61688]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioInputInitEx(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioInputInitEx [0xE926D3FB]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioPollInputEnd(Processor processor) {
        CpuState cpu = processor.cpu;

        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceAudioPollInputEnd [0xA633048E]");

        cpu.gpr[2] = -1;
    }

    public void sceAudioGetChannelRestLen(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int ret = -1;
        if(pspchannels[channel].outputDataLine != null)
        {
            int bytespersample = 4;

            if((pspchannels[channel].format&0x10)==0x10) bytespersample=2;

            ret = pspchannels[channel].outputDataLine.available() / (bytespersample);
        }

        cpu.gpr[2] = ret;
    }

    public final HLEModuleFunction sceAudioInitFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioInit") {
        @Override
        public final void execute(Processor processor) {
            sceAudioInit(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudio_driverModule.sceAudioInit(processor);";
        }
    };

    public final HLEModuleFunction sceAudioEndFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioEnd") {
        @Override
        public final void execute(Processor processor) {
            sceAudioEnd(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudio_driverModule.sceAudioEnd(processor);";
        }
    };

    public final HLEModuleFunction sceAudioSetFrequencyFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSetFrequency") {
        @Override
        public final void execute(Processor processor) {
            sceAudioSetFrequency(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudio_driverModule.sceAudioSetFrequency(processor);";
        }
    };

    public final HLEModuleFunction sceAudioLoopbackTestFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioLoopbackTest") {
        @Override
        public final void execute(Processor processor) {
            sceAudioLoopbackTest(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudio_driverModule.sceAudioLoopbackTest(processor);";
        }
    };

    public final HLEModuleFunction sceAudioSetVolumeOffsetFunction = new HLEModuleFunction("sceAudio_driver", "sceAudioSetVolumeOffset") {
        @Override
        public final void execute(Processor processor) {
            sceAudioSetVolumeOffset(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudio_driverModule.sceAudioSetVolumeOffset(processor);";
        }
    };

    public final HLEModuleFunction sceAudioOutputFunction = new HLEModuleFunction("sceAudio", "sceAudioOutput") {
        @Override
        public final void execute(Processor processor) {
            sceAudioOutput(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioOutput(processor);";
        }
    };

    public final HLEModuleFunction sceAudioOutputBlockingFunction = new HLEModuleFunction("sceAudio", "sceAudioOutputBlocking") {
        @Override
        public final void execute(Processor processor) {
            sceAudioOutputBlocking(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioOutputBlocking(processor);";
        }
    };

    public final HLEModuleFunction sceAudioOutputPannedFunction = new HLEModuleFunction("sceAudio", "sceAudioOutputPanned") {
        @Override
        public final void execute(Processor processor) {
            sceAudioOutputPanned(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioOutputPanned(processor);";
        }
    };

    public final HLEModuleFunction sceAudioOutputPannedBlockingFunction = new HLEModuleFunction("sceAudio", "sceAudioOutputPannedBlocking") {
        @Override
        public final void execute(Processor processor) {
            sceAudioOutputPannedBlocking(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioOutputPannedBlocking(processor);";
        }
    };

    public final HLEModuleFunction sceAudioChReserveFunction = new HLEModuleFunction("sceAudio", "sceAudioChReserve") {
        @Override
        public final void execute(Processor processor) {
            sceAudioChReserve(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioChReserve(processor);";
        }
    };

    public final HLEModuleFunction sceAudioOneshotOutputFunction = new HLEModuleFunction("sceAudio", "sceAudioOneshotOutput") {
        @Override
        public final void execute(Processor processor) {
            sceAudioOneshotOutput(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioOneshotOutput(processor);";
        }
    };

    public final HLEModuleFunction sceAudioChReleaseFunction = new HLEModuleFunction("sceAudio", "sceAudioChRelease") {
        @Override
        public final void execute(Processor processor) {
            sceAudioChRelease(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioChRelease(processor);";
        }
    };

    public final HLEModuleFunction sceAudioGetChannelRestLengthFunction = new HLEModuleFunction("sceAudio", "sceAudioGetChannelRestLength") {
        @Override
        public final void execute(Processor processor) {
            sceAudioGetChannelRestLength(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioGetChannelRestLength(processor);";
        }
    };

    public final HLEModuleFunction sceAudioSetChannelDataLenFunction = new HLEModuleFunction("sceAudio", "sceAudioSetChannelDataLen") {
        @Override
        public final void execute(Processor processor) {
            sceAudioSetChannelDataLen(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioSetChannelDataLen(processor);";
        }
    };

    public final HLEModuleFunction sceAudioChangeChannelConfigFunction = new HLEModuleFunction("sceAudio", "sceAudioChangeChannelConfig") {
        @Override
        public final void execute(Processor processor) {
            sceAudioChangeChannelConfig(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioChangeChannelConfig(processor);";
        }
    };

    public final HLEModuleFunction sceAudioChangeChannelVolumeFunction = new HLEModuleFunction("sceAudio", "sceAudioChangeChannelVolume") {
        @Override
        public final void execute(Processor processor) {
            sceAudioChangeChannelVolume(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioChangeChannelVolume(processor);";
        }
    };

    public final HLEModuleFunction sceAudioSRCChReserveFunction = new HLEModuleFunction("sceAudio", "sceAudioSRCChReserve") {
        @Override
        public final void execute(Processor processor) {
            sceAudioSRCChReserve(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioSRCChReserve(processor);";
        }
    };

    public final HLEModuleFunction sceAudioSRCChReleaseFunction = new HLEModuleFunction("sceAudio", "sceAudioSRCChRelease") {
        @Override
        public final void execute(Processor processor) {
            sceAudioSRCChRelease(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioSRCChRelease(processor);";
        }
    };

    public final HLEModuleFunction sceAudioSRCOutputBlockingFunction = new HLEModuleFunction("sceAudio", "sceAudioSRCOutputBlocking") {
        @Override
        public final void execute(Processor processor) {
            sceAudioSRCOutputBlocking(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioSRCOutputBlocking(processor);";
        }
    };

    public final HLEModuleFunction sceAudioInputBlockingFunction = new HLEModuleFunction("sceAudio", "sceAudioInputBlocking") {
        @Override
        public final void execute(Processor processor) {
            sceAudioInputBlocking(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioInputBlocking(processor);";
        }
    };

    public final HLEModuleFunction sceAudioInputFunction = new HLEModuleFunction("sceAudio", "sceAudioInput") {
        @Override
        public final void execute(Processor processor) {
            sceAudioInput(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioInput(processor);";
        }
    };

    public final HLEModuleFunction sceAudioGetInputLengthFunction = new HLEModuleFunction("sceAudio", "sceAudioGetInputLength") {
        @Override
        public final void execute(Processor processor) {
            sceAudioGetInputLength(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioGetInputLength(processor);";
        }
    };

    public final HLEModuleFunction sceAudioWaitInputEndFunction = new HLEModuleFunction("sceAudio", "sceAudioWaitInputEnd") {
        @Override
        public final void execute(Processor processor) {
            sceAudioWaitInputEnd(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioWaitInputEnd(processor);";
        }
    };

    public final HLEModuleFunction sceAudioInputInitFunction = new HLEModuleFunction("sceAudio", "sceAudioInputInit") {
        @Override
        public final void execute(Processor processor) {
            sceAudioInputInit(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioInputInit(processor);";
        }
    };

    public final HLEModuleFunction sceAudioInputInitExFunction = new HLEModuleFunction("sceAudio", "sceAudioInputInitEx") {
        @Override
        public final void execute(Processor processor) {
            sceAudioInputInitEx(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioInputInitEx(processor);";
        }
    };

    public final HLEModuleFunction sceAudioPollInputEndFunction = new HLEModuleFunction("sceAudio", "sceAudioPollInputEnd") {
        @Override
        public final void execute(Processor processor) {
            sceAudioPollInputEnd(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioPollInputEnd(processor);";
        }
    };

    public final HLEModuleFunction sceAudioGetChannelRestLenFunction = new HLEModuleFunction("sceAudio", "sceAudioGetChannelRestLen") {
        @Override
        public final void execute(Processor processor) {
            sceAudioGetChannelRestLen(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAudioModule.sceAudioGetChannelRestLen(processor);";
        }
    };

};
