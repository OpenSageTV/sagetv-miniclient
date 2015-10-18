/*****************************************************************************
 * VLCInstance.java
 * ****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package sagex.miniclient.android.video.vlc;

import android.content.Context;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;

public class VLCInstance {
    public static final Logger log = LoggerFactory.getLogger(VLCInstance.class);
    public final static String TAG = "VLC/Util/VLCInstance";

    private static LibVLC sLibVLC = null;

    /** A set of utility functions for the VLC application */
    public synchronized static LibVLC get(final Context context) throws IllegalStateException {
        if (sLibVLC == null) {
            Thread.setDefaultUncaughtExceptionHandler(new VLCCrashHandler());

            if (!VLCUtil.hasCompatibleCPU(context)) {
                Log.e(TAG, VLCUtil.getErrorMsg());
                throw new IllegalStateException("LibVLC initialisation failed: " + VLCUtil.getErrorMsg());
            }

            sLibVLC = new LibVLC(VLCOptions.getLibOptions(context));
            LibVLC.setOnNativeCrashListener(new LibVLC.OnNativeCrashListener() {
                @Override
                public void onNativeCrash() {
                    log.error("VLC Crashed!!");
                }
            });
        }
        return sLibVLC;
    }

    public static synchronized void restart(Context context) throws IllegalStateException {
        if (sLibVLC != null) {
            sLibVLC.release();
            sLibVLC = new LibVLC(VLCOptions.getLibOptions(context));
        }
    }

    public static synchronized void release(Context context) throws IllegalStateException {
        if (sLibVLC != null) {
            sLibVLC.release();
            sLibVLC = null;
        }
    }

}
