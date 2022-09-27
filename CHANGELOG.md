**1.14.0 (9/27/2022)**
- Update ExoPlayer to 2.18.1

**1.13.0 (6/29/2022)**
- Update exoplayer to 2.18.0
- Bug fixes with Android Media Session logic that was causing issues with IJKPlayer

**1.12.0 (6/18/2022)**
- Update exoplayer to 2.17.1
- Bug fixes with Android Media Session logic that was causing random crashes
- Added firebase Crashlytics Reporting (Only works on google devices)
- Added configuration item to allow for disabling Crashlytics reporting
- Added configuration item to allow for setting username in Crashlytics reports

**1.11.0 (4/27/2022)**
- Made exoplayer playback error message more descriptive
- Added retry on recoverable playback error
- Disabled remux settings when stream mode is not fixed
- Changed the method for getting version name and version code
- Modified Jenkins local copy to publish changelist and name to directory
- Added code for a Subtitle/Closed Caption selection list from the Navigation window
- Rewrote the Android Media Session logic so that it can control playback for ExoPlayer and IJKPlayer
- Added framework to the Jenkins build to do local deployments with ADB

**1.10.1**
- Fixed a bug where push/pull formats were not begin properly determined.

**1.10.0 (1/6/2021)**
- Added configuration screens for container and codec support that allows users to customize support
- Updated ExoPlayer to version 2.16.1 and fixed all associated code that broke.  SimpleExoPlayer was removed in this release
- Added a new build of the FFmpeg ext for version 2.16.1

**1.9.3 (2021-11-2)**
- Updated to 2.15.1 of ExoPlayer and FFmpeg extension
- Cleaned up a number of deprecated calls in ExoPlayer implementation
- Added VP8, VP9, H.263 as valid video codecs
- Removed MPEG4 as a valid codec, because Android TV support is very limited.  All non H.264/AVC content will be transcoded
 
**1.9.1 (2021-08-26)**
- Fixed issue in PushBufferDataSource that was causing transcoding and live tv transitions to freeze
- Updated build to use local libs directory instead of MavenLocal.  Simplify build process for support libraries
- Fixed logic bugs on how to tell SageTV when to transcode/remux

**1.9.0 (2021-08-09)**
- Moved project to Androidx
- Update GDX library version to 1.9.14
- Update ExoPlayer to 2.14.2  
- Moved IJKPlayer over to normal repository instead of local
- Multiple build.gradle files cleaned up
- Fixed error generating client ID

**1.8.1 (6/18/2021)**
- Updated to ExoPlayer 2.14.0
- Updated FFmpeg extension to 2.14.0
- Some minor bug fixes

**1.8.0 (5/14/2021) - Brought to you by user cncb**
- Support external links to open videos in other apps directly (Netflix/Amazon).
- Allow display of embedded PGS subtitles in ExoPlayer.
- Add option to enable system sleep.

**1.7.1 (2021-02-11)**
- Redesigned some of the settings screens.  Broke out the settings for ExoPlayer and IJKPlayer
- Upgrade to ExoPlayer 2.12.3
- Added additional codecs to the ExoPlaye Codec Debug Screen (DIVX, AC4, OPUS, etc...)
- Removed AVI as a supported container format for ExoPlayer.  This should go thru the transcoder

**1.7.0 (2020-11-12)**
- Playing back active recording TV seeking issue with ExoPlayer: Issue #96
- Frame Advance & Slow Motion Advance enhancement: Issue #91
- Upgrade to ExoPlayer 2.12.1 enhancement: Issue #95
- Full-Screen On Screen Navigation enhancement: Issue #42
- Issue with keyboard and favorites on the FireTV: Issue #92
- Error connecting to server when using fixed Transcoding/Streaming: Issue #97

**1.6.3 (2020-10-8)**
- jvl711: Rearchitected how playback position was retrieved.  Switched to a UI thread that updates every 500ms.
- jvl711: Fixed an issue with seeking after resume from pause.  The state was not getting properly updated
- jvl711: Allowing proper seek when playback is paused.  Allow the position to get reported to sage server on pause
- jvl711: Added the blue icons that I made for FireTV to Android project

**1.6.2 (2020-09-10) **
- jvl711: Fixed an issue with Android 11 where images were not rendering
- jvl711: Upgraded ExoPlayer to 2.11.8
- jvl711: Fixed the volume up/down keys.  You need to map them to NONE for them to work properly
- jvl711: Volume up/down mapped to NONE by default

**1.6.1 (2020-8-31)**
- jvl711: Missed a default value on one of the properties which caused a null pointer exception on launch of connection

**1.6.0 (2020-8-29)**
- jvl711: Added fixed transcoding settings to allow for HD transcoding from the server to MKV container
- jvl711: Added the ability for Exoplayer to filter the Video codecs that are not supported.  This will allow SageTV to transcode on unsupported formats

**1.5.2 (2020-7-09)**
- jvl711: Attempted to add a workaround for the FireTV where you can not close the system keyboard.  Next/Previous buttons should now close it.

**1.5.1 (2020-6-30)**
- jvl711: Update ExoPlayer FFmpeg extension to 2.11.5
- jvl711: Added a setting for ffmpeg exoplayer extension, to turn it on, off or prefer
- jvl711: Updated some deprecated code in the constructing on the SimpleExoPlayer instance

**1.5.0 (2020-6-26)**
- jvl711: Moving to new Play Store app
- jvl711: Updated to exoplayer 2.11.5
- jvl711: Added HEVC codec to list of codecs sent to sage durring connection
- jvl711: Attempted to clean up the current playback time, and the seek
- jvl711: Turn off rendering cues if by default
- jvl711: Add error message toast on ExoPlayer2 Error
- jvl711: Fixed issues with seeking when using exoplayer and push mediasource
- jvl711: Added some basic support for subtitles in ExoPlayer
- jvl711: Fixed a bug where it would not seek to the SageTV supplied start position

**1.4.4 (2018-01-19)**
- sls: feat: Added ability to remap Page UP/DOWN

**1.4.3 (2018-01-12)**
- sls: feat: Added more touch mappings
- sls: feat: Added more key mappings
- sls: feat: NAV OSD and KEYBOARD are able to be mapped in key/touch mappings
- sls: feat: Added Hotpot mappings (useful for mouse/touch ie, ChromeOS/Tablet) 
- sls: refactor: OpenGL and GDX now share 90% same code
- sls: fix: OpenGL transparency issue with shapes
- sls: feat: Added debug keys and commands view
- sls: feat: Added httpls streaming support (debug/test only)

**1.4.2 (2018-12-31)**
- sls: fix: Back when playing video should not stop the video
- sls: feat: Added ability to map insert key

**1.4.1 (2018-12-30)**
- sls: fix: Amazon Fire TV and MiBox could not use OpenGL renderer due to using some invalid GL calls for those devices.



**1.4.0 (2018-12-30)**
- sls: new UI renderer
- sls: configure key repeat speeds
- sls: smart remote for Plugin List (right/left navigates top tabs)
- sls: smart remote for Guide (long press right/left page right/left)
- sls: long press 'back' goes home 
- sls: option to exit MiniClient when it goes into Standby mode
- sls: key sound effects (can be turned off in settings)

**1.3.5 (2018-12-16)**
- sls: fix: ExoPlayer seek issues after upgrading ExoPlayer
- sls: refactor: removed log to sdcard
- sls: (fix): Handling Subtitle request cased NPE in IJKPlayer
- sls: (enh): Set defaults for smart remote preferences
- sls: (fix): Added more checking around setting audio tracks

**1.3.4 (2018-10-08)**
- sls: Fixed crash when showing Help

**1.3.3 (2018-10-05)**
- sls: Fixed issues listed in crash reports
- sls: removed logging on startup that can cause App to hang on some hardware
- sls: removed code that was designed for pre android 21 devices
- sls: Fixed NPE bug in the key listener
- sls: Upgraded ExoPlayer to 2.8.4

**1.3.2 (2018-09-24)**
- jvl711: Fixed bug in Audio Track selection that would cause player to crash (sometimes)

**1.3.1 (2018-09-23)**
- jvl711: Add support for SageTV server to signal Subtitle track changes.
- jvl711: Added audio track change support to ExoPlayer.
- jvl711: Added the ability to switch audio tracks.
- jvl711: Allowed unmapped keys to allow system actions to occur.

**1.3.0 (2018-08-04)**
- jvl711: Added configurable remote mapping ability from within the application 

**1.2.13 (2018-07-01)**
- Upgraded ExoPlayer and IJKPlaer
- Work Around for Shield (and other 8.x versions) where UI gets completely messed up.


**1.2.12 (2017-11-11)**
- Fixes to Skip ahead for ExoPlayer
- Fixed Paused Issue (ie, timeline changes when paused)
- Fixed issue where pause/play would be intercepted by background app


**1.2.9 (2017-11-01)**
- updated IJKPlayer to 0.8.4
- updated ExoPlayer to r2.5.4
- Enhanced ExoPlayer (Seeking in .ts files seems OK for everyday use)
- Build Tools to 26.0.2
- Fixed lots of NullPointerExceptions

**1.2.8 (2017-05-28)**
- Upgraded to IJKPlayer 0.8.0
- Upgraded to ExoPlayer r2.4.0
- Upgraded to LibGDX 1.9.6
- Add "Duplicate" connection action
- Each connection have a different client id
- Each connection can set stateful remote on/off (useful for testing Phoenix)
- Aspect Ratio update for larger non 16/9 screens
- Added support for GUIDE and INFO keys
- Added support for F1-F12 keys

**1.2.6 (2017-01-08)**
- Fixed UI rendering issue that affected SageMC

**1.2.5 (2016-12-23)**
- Fixed regression issue when Animations are disabled

**1.2.4 (2016-12-22)**
- Added Ability to change/set the amount of memory used for image caching
- Another aspect ratio fix for 4/3 content

**1.2.3 (2016-12-19)**
- AR Fixes

**1.2.1 (2016-12-18)**
- Fixed: When animations disabled, the screen shows junk
- Fixed: 16/9 stretch does a shrink instead

**1.2.0 (2016-12-17)**
- Fixed #62 - Implemented SageTV AR Mode Changing (Source,Stretch,Zoom)
- Fixed #64 - When Animations are disabled Video overlay doesn't render correctly
- Fixed #3 - video not showing in preview window
- Removed need to "disable animations" property
- OpenGL: Fixed "CLEAR_RECT" implementation
- OpenGL: Fixed Clear Screen issues

**1.1.0 (2016-12-04)**
- Upgraded IJKPlayer
- IJK: Added configuration options to disable hardware decoding
- IJK: Added ability to disable some codecs
- Upgraded ExoPlayer
- EXO: Added ability to use ffmpeg for audio decoding (ARM only)

**1.0.11 (2016-11-06)**
- Fixed FLIRC keyboard handling for HOME (MOVE_HOME)

**1.0.10 (2016-11-06)**
- Fixed FLIRC keyboard handling for CTRL/ALT/SHIFT

**1.0.7 (2016-09-24)**
- Upgraded LibGDX to 1.9.4
- Upgraded IJKPlayer to 0.6.2
- Upgraded ExoPlayer to 1.5.11
- Added support 'numpad_enter' for select/enter

**1.0.6**
- Added option "Animations Disabled?" which is default "false" but when true it will work around the issue where the screen does not clear.  This is a temporary work around until I find a permanent solution.

**1.0.5**
- Animation updates (less flicker)
- Fixed crash on Android N
- Updated libaries (ExoPlayer, IJKPlayer, and LibGDX) to latest versions
