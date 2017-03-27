# 模仿 AmpMe 将数台 Android 设备组合成一套环绕音响系统
[toc]

- 前言：
偶尔在某知名软件站看到 AmpMe 这个 APP ，当时没有下载使用过，但是敬佩作者的新奇想法。（后来发现三星自家也有个类似的 APP ，懒得考究谁先谁后，总之相当有趣。）适逢上完班会，在班主任打了鸡血之下，抱着试试做的心态，拉上舍长开始这个 APP 项目。

## （一） 研究模型
### 1. 自我构思
在安装了 AmpMe 的机器中，负责选择曲目和分享的机器自动打开 无线热点 ，一边播放，一边传输音频数据。其他机器自动连接 WIFI ，接收音频数据并播放。
### 2. 实际体验
AmpMe 并不会自动打开 无线热点 和 自动连接 WIFI 。音乐并非边传边播放（流媒体），而是在播放前把歌曲先完整发送，然后同步时间。

## （二） 解决问题
### 1. 已解决问题
- 解决了在 `Android 6.0` 以上版本无法开启 `Wireless Hotspot` 的问题。
- ~~解决了在开启 `Wireless Hotspot` 后无法发送 `UDP组播` 的问题。~~[^1]

[^1]: 使用UDP进行PCM数据流传输的构思已弃用。

### 2. 未解决问题

## （三） 项目状态
未完工。