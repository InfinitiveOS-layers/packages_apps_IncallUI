/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.incallui;

import android.telecomm.CallCameraCapabilities;
import android.telecomm.VideoCallProfile;
import android.telecomm.CallVideoClient;

/**
 * Implements the InCall-UI Call Video client.
 */
public class InCallVideoClient extends CallVideoClient {

    /**
     * The call associated with this {@link InCallVideoClient}.
     */
    private Call mCall;

    /**
     * Creates an instance of the call video client, specifying the call it is related to.
     *
     * @param call The call.
     */
    public InCallVideoClient(Call call) {
        mCall = call;
    }

    /**
     * Handles an incoming session modification request.
     *
     * @param videoCallProfile The requested video call profile.
     */
    @Override
    public void onReceiveSessionModifyRequest(VideoCallProfile videoCallProfile) {
        int previousVideoState = mCall.getVideoState();
        int newVideoState = videoCallProfile.getVideoState();

        boolean wasVideoState = isVideoStateSet(previousVideoState,
                VideoCallProfile.VIDEO_STATE_BIDIRECTIONAL);
        boolean isVideoState = isVideoStateSet(newVideoState,
                VideoCallProfile.VIDEO_STATE_BIDIRECTIONAL);

        boolean wasPaused = isVideoStateSet(previousVideoState, VideoCallProfile.VIDEO_STATE_PAUSED);
        boolean isPaused = isVideoStateSet(newVideoState, VideoCallProfile.VIDEO_STATE_PAUSED);

        // Check for upgrades to video and downgrades to audio.
        if (!wasVideoState && isVideoState) {
            CallVideoClientNotifier.getInstance().upgradeToVideoRequest(mCall);
        } else if (wasVideoState && !isVideoState) {
            CallVideoClientNotifier.getInstance().downgradeToAudio(mCall);
        }

        // Check for pause/un-pause
        if (!wasPaused && isPaused) {
            CallVideoClientNotifier.getInstance().peerPausedStateChanged(mCall, true /* pause */);
        } else {
            CallVideoClientNotifier.getInstance().peerPausedStateChanged(mCall, false /* resume */);
        }
    }

    /**
     * Handles a session modification response.
     *
     * @param status Status of the session modify request.  Valid values are
     *               {@link CallVideoClient#SESSION_MODIFY_REQUEST_SUCCESS},
     *               {@link CallVideoClient#SESSION_MODIFY_REQUEST_FAIL},
     *               {@link CallVideoClient#SESSION_MODIFY_REQUEST_INVALID}
     * @param requestedProfile
     * @param responseProfile The actual profile changes made by the peer device.
     */
    @Override
    public void onReceiveSessionModifyResponse(int status, VideoCallProfile requestedProfile,
            VideoCallProfile responseProfile) {
    }

    /**
     * Handles a call session event.
     *
     * @param event The event.
     */
    @Override
    public void onHandleCallSessionEvent(int event) {
    }

    /**
     * Handles a change to the peer video dimensions.
     *
     * @param width  The updated peer video width.
     * @param height The updated peer video height.
     */
    @Override
    public void onUpdatePeerDimensions(int width, int height) {
        CallVideoClientNotifier.getInstance().peerDimensionsChanged(mCall, width, height);
    }

    /**
     * Handles a change to the call data usage.  No implementation as the in-call UI does not
     * display data usage.
     *
     * @param dataUsage The updated data usage.
     */
    @Override
    public void onUpdateCallDataUsage(int dataUsage) {
    }

    /**
     * Handles changes to the camera capabilities.  No implementation as the in-call UI does not
     * make use of camera capabilities.
     *
     * @param callCameraCapabilities The changed camera capabilities.
     */
    @Override
    public void onHandleCameraCapabilitiesChange(CallCameraCapabilities callCameraCapabilities) {
    }

    /**
     * Determines if a specified state is set in a videoState bit-mask.
     *
     * @param videoState The video state bit-mask.
     * @param state The state to check.
     * @return {@code True} if the state is set.
     */
    private boolean isVideoStateSet(int videoState, int state) {
        return (videoState & state) == state;
    }
}