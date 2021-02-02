## serialization-test

Java serialization test.

FastJSON Jackson XML JDK ProtoBuffer Kyro 几种序列化方式的 demo 和简单的性能测试及对比。

## 序列化简介

![image](https://cloud.githubusercontent.com/assets/7654175/23894440/169d21e8-08dd-11e7-964a-aeffd5bb7211.png)

对象是由行为和状态，序列化代表我们可以将一个对象的状态转化成数据流，可以通过网络传输，也可以存储成一个文件或者数据库中，同样可以把数据流反序列化成对象。
参见：http://blog.csdn.net/connect_me/article/details/62439743
## 文本序列化

- 优点：文本型序列化的优点在于跨平台跨语言，简单易读易调试，扩展性强
- 缺点：占空间，不过可以压缩，会有一小部分性能损耗
- JSON：比较常用，简单，常用的库有 Gson，Jackson，FastJson
- XML：主要用于基于 SOAP 协议的应用，序列化后内容太多

## 二进制序列化

- 优点：省空间，一般用于系统内部序列化
- 缺点：扩展性较差，不能够很好的跨语言
- ProtoBuffer：Google 提供的跨语言跨平台的序列化框架
- Thirft：是由 Facebook 为 “大规模跨语言服务开发” 而开发的，它被用来定义和创建跨语言的服务，包含一部分二进制的序列化功能
- kryo：序列化大对象时性能较好

## 优缺点

- Kryo：使用 Direct ByteBuffer，对于复杂的对象，序列化快，占用容量小
- Proto Buffer：简单对象的序列化比较快
- JSON：一般用于 web 服务，可读性较强，建议使用 fastjson
- JAVA：开箱即用，不需要依赖第三方包

## 使用介绍

1. 可以在`main`函数中选择执行的测试类型
1. 设置`Application.init()`中需要测试的序列化类型
1. 运行`Application.java`执行序列化测试


## 测试结果

```
简单对象测试:
--------------------------------------------------------------------------------------------
                  name    avgSer    minSer    maxSer  avgDeser  minDeser  maxDeser      size
     ProtoStuff-Simple     17046       700  91962900     21797       800 151908700       167
    ProtoBuffer-Simple      3275       500   5544700      7674       500  16391100       171
           Kryo-Simple     20857      1200 126308700     26969      1500 114516800       181
       FastJSON-Simple     16840      2600  82678900     54146      2300 169683700       254
       JackJSON-Simple     63640      2000 176485900     28913      2600 107764700       258
            Jdk-Simple     32379      3500  93950400    111837     12800 117740300       454
            XML-Simple   1491812    939400 136007600    468711    190500 323252400      1104
复杂对象测试: 
--------------------------------------------------------------------------------------------
                  name    avgSer    minSer    maxSer  avgDeser  minDeser  maxDeser      size
          Kryo-Complex     18133      4500  19233700     31186      4900  90632500      1283
    ProtoStuff-Complex    188440     55300  64674300    119845     42800  58609600     58485
   ProtoBuffer-Complex     54821     24500  10718900     97838     42600  11946800     58485
           Jdk-Complex     60064     23100  56048700    252456    108900  61574600      7146
      FastJSON-Complex    439399    235800 245405200    438605    168200 360547200     63531
      JackJSON-Complex    411030    161300 266387600    491689    168200 188725200     63531
           XML-Complex   8435807   6672800 107200800   2061636   1566100  78898400     76915

```