# Audioutils
Audioutils-音频录制和音频播放工具<br>  
### 音频录制有两个对象：
MediaRecorderUtils对象是针对com.kailashdabhi:om-recorder框架封装，并且内部已经实现转换成wav格式。<br> 
MediaRecorderUtilsAmr对象是针对原生API封装，并且内部已经实现转换成amr格式<br> 
### 音频播放对象：
MediaPlayerUtils对象是针对原生API封装的。
## 效果图
![效果图](./image/GIF.gif)
## 功能介绍
音频录制，音频暂停录制，音频重新录制，音频播放。
## 注意事项
每次只能录制一段音频！第二次录制成功的音频文件会把第一次录制成功的文件覆盖掉。如果点击重新录制，那么就会把上一次录制成功的音频删掉，恢复到未录制音频前的状态。
### 如有发现问题，请联系QQ：543945827
