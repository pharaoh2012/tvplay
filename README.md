# Tvbox

电视家不能用后，为方便自家老人看电视，基于网页播放电视台的直播。

启动后直接进直播，方便老年人看电视。

## 原理

浏览网页，根据域名运行自定义的js脚本，脚本实现解析播放地址，将播放窗口全屏等。

## 内容介绍

- 软件启动直接进直播，每个频道播放内容可用自定义。
- 支持遥控器按键
  - 支持频道上下键换台
  - 支持数字1~9切换到对应台
  - 菜单键进入设置页面
- 频道配置文件，只需要一个文件即可
- 可用根据域名自定义脚本。

## 配置

使用遥控器的菜单键进入设置(手机上长按界面上半部分即可)，输入配置地址即可,例如: `https://tvplay-config.pharaoh.cf/`

默认配置文件项目: <https://github.com/pharaoh2012/tvplay-config>，大家可以Fork项目后修改部署。

在此地址下创建`config.txt`文件，里面放入频道地址，每行一个，第几行对应遥控器的1~9频道。

例如:

```txt
https://tv.cctv.com/live/cctv1/m/
https://tv.cctv.com/live/cctv2/m/
https://tv.cctv.com/live/cctv3/m/
```

在此地址下创建`<域名>.js`文件，浏览网页时自动根据域名运行对应的脚本。
例如`tv.cctv.com.js`文件:

```js
document.getElementsByTagName('video')[0].style.position = 'fixed'
document.querySelector(".nav_wrapper_bg").style.display = 'none'
document.querySelector(".header_nav").style.display = 'none'
document.querySelector(".video_right").style.display = 'none'
```

## 安装方法

- 在电视上安装apk
- 运行软件
- 使用遥控器`菜单`按键，打开设置界面
- 输入配置的地址

## 电视端使用方法

- 打开软件
- 使用数字1~9键切换到对应台
- 或者使用频道上下键换台

## 手机端操作

- 长按界面上半部分，显示设置界面。
- 长按左下部分，切换为上一频道。
- 长按右下部分，切换下一频道。
