### OTTLivePlayer_vlc

This is a live media player for android ott box based on [vlc-android](https://code.videolan.org/videolan/vlc-android) 

### Compile from vlc git

1. <https://wiki.videolan.org/AndroidCompile>
2. <http://www.xugaoxiang.com/post/85>

### How to use in your app

![jniso](images/jniso.png)



![jniso2](images/jniso_2.png)



![vlcjava](images/vlcjava.png)



### How to create a MediaPlayer

```
ArrayList<String> options = new ArrayList<>();
options.add("-vvv");
libvlc = new LibVLC(this, options);

mediaPlayer = new MediaPlayer(libvlc);

IVLCVout ivlcVout = mediaPlayer.getVLCVout();
ivlcVout.setVideoView(surfaceView);
ivlcVout.attachViews();

Media media = new Media(libvlc, Uri.parse("udp://@225.0.0.1:9000"));
mediaPlayer.setMedia(media);
mediaPlayer.play();
```

### Troubleshooting

##### Error:(466, 73) 错误: -source 1.6 中不支持 diamond 运算符 (请使用 -source 7 或更高版本以启用 diamond 运算符)

Android studio --> File --> Project structure... --> app --> Source Compatibility

![q1](images/q1.png)

##### Android studio 3.0.1版本问题

<http://xugaoxiang.com/post/111>

### Screenshots

![1](images/screencap_1.png)



![2](images/screencap_2.png)



![3](images/screencap_3.png)



![4](images/screencap_4.png)



![5](images/screencap_5.png)

### License

```
Copyright <2017> <COPYRIGHT djstava@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```