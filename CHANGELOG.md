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