# Video Players

This document serves as a guide for other developers that may want to integrate additional video players into the MiniClient.

Currently the MiniClient supports ExoPlayer (Google's player) and IJKPlayer (A ffmpeg based player).  IJKPlayer is no longer actively developed.

In general, integrating a new player is not that hard.  You basically include the native libraries for that player, and you create a Java wrapper around the player tha manages seeking, pause, play, stop etc.  Sounds easy, right?  That part is easy, but, the challenge is that most players support opening a `stream` or a `file`, and SageTV provides neither of those, so, you need to bridge the SageTV protocols into the player, and this part is non-trivial.

Before we get into that, let's talk about the PUSH and PULL protocols in SageTV.

## SageTV Video Protocols

### PULL
A `Pull` protocol (or `Datasource`) is a socket based streaming protocol, proprietary to SageTV.  In short it has a few simple commands, `OPEN`, `SIZE`, `READ`, and `CLOSE`.  It's not hard to implement, and the [SimplePullDataSource.java](core/src/main/java/sagex/miniclient/net/SimplePullDataSource.java) is a complete implementation for that protocol.  In many ways, this protocol feels like a File or URL type of data source in that the primary `READ` function is responsible for reading chunks of the stream.

### PUSH
A `PUSH` protocol (or `Datasource`) is another socket based streaming protocol, proprietary to SageTV, that is a lot more complex to implement, and, as the name suggests, the stream chunks are simply pushed to you, and you need to feed that data into a player.  SageTV will use its socket connections that are created when a client connects and it will PUSH data to the client, and the client needs to buffer that data.  When a user seeks to a new location, sagetv will send a command telling the client to `FLUSH`, meaning, discard your content in the player and wait for new content to arrive.  So the player never really knows about seeking.

The obvious question is `why` do these protocols exists, and why not just use `HTTPLS`?

First off, SageTV has been around a long time, way before HTTPLS was a thing, and SageTV needed a way to solve the Live TV problem that many players have.  ie, when streaming a live tv channel and you suddenly skip to the end of the stream, the player will generally just stop.  Years later HTTPLS solves that problem, but, HTTPLS is a complex streaming protocol (requiring both a competent server and client that understands it), and, as I said, SageTV needed to solve it many before HTTPLS was a thing.

SageTV also had to deal with the fact that not all streams are seekable and instead of forcing a player to implement the complex logic of how to seek in non-seekable streams, all this logic could be done on the server, and the `PUSH` protocol could be used in these players and they never need to manage how to seek.  From an implementation point of view, a Player that supports `PUSH` is a simple player.  It really only has to manage pause, play, stop, and flushing its media buffer.  SageTV manages seeking, whether it's a live stream or not.  Using the PUSH protocol is one of the reasons why SageTV can seek faster than any other media client that exists today.

The MiniClient also has a complete implementation for the [PushBufferDataSource](core/src/main/java/sagex/miniclient/net/PushBufferDataSource.java) that manages buffering the data, reading it, and flushing it.

## Add a Video Player

The key to adding a new player is whether or not the Player supports the concept of a custom data source, in Android/Java.   ExoPlayer has the [DataSource](https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/upstream/DataSource.html) interface, and IJKPlayer (by request of me) has the [IMediaDataSource](https://github.com/Bilibili/ijkplayer/blob/master/android/ijkplayer/ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/IMediaDataSource.java) interface.  Both of these very simple interfaces and it was quite easy to create the MiniClient PUSH and PULL data sources using our existing PUSH and PULL implementations and feed those into the players.

If a player does not support the concept of a custom data source, then, it just got a whole lot harder to bridge a player to SageTV.  Now, almost all players support at the Native layer this concept of a custom data source.  In fact IJKPlayer didn't originally support it in the Java layer, and I requested that they create a java to native bridge for adding custom data sources and they did.  To my knowledge, no other players in Android support custom data sources in the Java layer.

There are 2 other full featured video players for Android; [mpv](https://github.com/mpv-android/mpv-android) and [vlc](https://github.com/videolan/vlc-android).  While both of these players support custom data sources, natively (ie, in c code), they have not exposed this functionality to Java/Android.  But, if someone did write a JNI bridge to expose this functionality (maybe look at what IJKPlayer did), then it would be almost trivial to utilize these players in the Android MiniClient.

The most complex part of the video player is actually implementing the method `getMediaTimeMillis()` that returns the current time of the player in milliseconds.  Because of the PUSH and PULL datasources and how a player manages its time, you get some very odd results when you implement this method.

In most players the timeline starts at 0.  When you start a show from the beginning and seek around, things are good.  The challenge is when you are watching live tv or resuming a show.  In those cases you might be 15 minutes into a show, but the times being returned from the player is actually 0 to start.  So, resuming shows, you have to use the resume time + the player time.   The other challenge is that when you resume and then you start seeking back to before your resume time, the player will not generally return a negative time.  In some cases the player will simply return 0.  In some cases the player might return a really large number.  ExoPlayer and IJKPlayer behave very differently for resume times.  In fact, ExoPlayer will reset the player time to 0 with every PUSH seek, whereas IJK will treat when the player first started as 0 and then everything else is relative to that time.

`getMediaTimeMillis()` is an extremely important api call that has to be implemented correctly, IF, you want sagetv to render the time bar correctly, and if you want sagetv to remember the last play position for resuming, etc.

## MiniClient and the Future of Players

As noted, IJKPlayer, which works fairly well, because it is a ffmpeg based player, is no longer being actively developed.  This means that at some point it's likely to just stop working.  IJKPlayer also doesn't support audio pass-through, so, people that want 5.1 audio are not getting it via this player.

ExoPlayer is Google's actively developed player, but, it lacks software decoding, which is problematic if the hardware decoders on devices are defective, which happens more than you would think.  But, ExoPlayer does support audio pass-through.

Both IJK and Exo suffer glitches, sometimes based on resuming video, of even general timebar tracking, generally due to the complex `getMediaTimeMillis()` logic and the player's inconsistent ways of how it manages 'PUSH' streams and seeking.

`MPV` and `VLC` are both actively developed players on Android today.  But, out of the two, only VLC support audio pass-through.  My suggestion, is that someone should look at adding IMediaDataSource support to VLC/Java and pull in VLC as the default player.

## Supporting HTTPLS

Ironically, most players today support HTTPLS fairly well.  SageTV even has a rudimentary HTTPLS server built in.  The challenge is how you integrate that with the Player integration.  Keep in mind, SageTV actions happen on the server, and from there the UI is updated.  So when you open a file to play it, SageTV simply send updates to UI reflecting that you are playing a video.  When you seek, the seek happens on the server, and the server will command to the client telling it seek, etc.  So, because the UI is basically on the server, it does make integrating HTTPLS streams on the client a little tricky and would likely require changes to the server and client communication to work effectively.

If you want to play with HTTPLS support in SageTV (not in the MiniClient but other clients, like vlc, etc), you can do the following.

Open a URL like
```url
    http://localhost:31099/iosstream_CLIENTID_MEDIAFILEID_SEGMENT#_list.m3u8
    
    ie,
    http://localhost:31099/iosstream_51:56:4f:54:49:53_139_0_list.m3u8
    would open mediafileid 139 and return the bitrate lists to the player
```

NOTE: by default sagetv might fail the request complaining about the client, if so, you can disable client id check, by setting
```properties
httpls_require_client_connection=false
```    
In Sage.properties

NOTE: the default bit rates are pretty bad, but you can change those in the Sage.properties as well.

```properties
httpls_bandwidth_options=160,320,864,64
```

Lastly, the Sage transcoding resolution for this is stuck at `480x272`.  At some point we should make this configurable in the SageTV `FFMpegTranscoder.java`, and use a property check with default of `480x272`.

In testing I've manually updated FFMpegTranscoder.java to use 1280x720 and then used bit rates of 4000 (ie, httpls_bandwidth_options) and I've gotten very good video results coming back on a desktop.