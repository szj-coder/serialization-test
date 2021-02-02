package com.keyhunter.test.serialization;

import com.keyhunter.test.serialization.bean.ComplexObjectGenerator;
import com.keyhunter.test.serialization.bean.SimpleObject;
import com.keyhunter.test.serialization.proto.Complex;
import com.keyhunter.test.serialization.proto.Simple;
import com.keyhunter.test.serialization.protobuffer.ProtoBufferSerializer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Application entry point
 *
 * @auther yingren
 * Created on 2017/2/23.
 */
public class Application {

	private List<Serializer> serializers = new ArrayList<>();

	private List<Statistics> statisticsList;
	private final Config config;

	private StatisticsCollecter statisticsCollecter;

	/**
	 * main method to the run a application
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Application application;

		//simple
		Config config = new Config();
		config.setLoopSize(10000);
		config.setNameSuffix("Simple");
		// 普通序列化对象 todo 选择序列化内容
//		config.setTargetObject(buildSimpleObject());
		// protobuf序列化对象
		config.setTargetObject(buildSimpleProtoObject());
		application = new Application(config);
		application.init();
		application.run();

		//complex
		config.setLoopSize(10000);
		config.setNameSuffix("Complex");
		// 普通序列化对象 todo 选择序列化内容
//    final ComplexObject generate = new ComplexObjectGenerator().generate();
//    config.setTargetObject(generate);
		// protobuf序列化对象
		config.setTargetObject(buildComplexProtoObject());
		application = new Application(config);
		application.init();
		application.run();
	}

	private static Complex.ComplexObject buildComplexProtoObject() {
		Complex.ComplexObject.Builder builder = Complex.ComplexObject.newBuilder();
		Class<? extends Complex.ComplexObject.Builder> clazz = builder.getClass();
		try {
			for (int i = 0; i < ComplexObjectGenerator.LOOP_SIZE; i++) {
				Method setJustAName = clazz.getDeclaredMethod("setJustAName" + i, String.class);
				setJustAName.setAccessible(true);
				setJustAName.invoke(builder, "this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,this is just a name,");
				Method setJustANumber = clazz.getDeclaredMethod("setJustANumber" + i, int.class);
				setJustANumber.setAccessible(true);
				setJustANumber.invoke(builder, Integer.valueOf(243242322));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return builder.build();
	}

	private static Simple.SimpleObject buildSimpleProtoObject() {
		SimpleObject simpleObject = buildSimpleObject();
		Simple.SimpleObject.Builder builder = Simple.SimpleObject.newBuilder()
				.setName(simpleObject.getName())
				.setValue(simpleObject.getValue())
				.setAge(simpleObject.getAge())
				.setMail(simpleObject.getValue())
				.addParents(simpleObject.getParents().get(0))
				.addParents(simpleObject.getParents().get(1))
				.setSchool(simpleObject.getSchool())
				.setTeacher(simpleObject.getTeacher())
				.setScore(simpleObject.getScore())
				.setHeight(simpleObject.getHeight())
				.setWeight(simpleObject.getWeight())
				.setDesc(simpleObject.getDesc());
		return builder.build();
	}

	private static SimpleObject buildSimpleObject() {
		SimpleObject simpleObject = new SimpleObject();
		simpleObject.setName("simple object");
		simpleObject.setValue("i'm a simple object");
		simpleObject.setAge(22);
		simpleObject.setMail("xiaoming@qq.com");
		ArrayList<String> parents = new ArrayList<>();
		parents.add("mother");
		parents.add("father");
		simpleObject.setParents(parents);
		simpleObject.setSchool("Star School");
		simpleObject.setTeacher("James Lee");
		simpleObject.setScore(32.8);
		simpleObject.setHeight(180);
		simpleObject.setWeight(66);
		simpleObject.setDesc("It's my pleasure to introduce my self..well");
		return simpleObject;
	}

	private void run() {
		statistics();
		print();
	}

	public Application(Config config) {
		this.config = config;
	}

	public void init() {
//    register(new JdkSerializer());
//    register(new FastJSONSerializer());
//    register(new ProtoStuffSerializer());
		register(new ProtoBufferSerializer());
//    register(new XMLSerializer());
//    register(new JackJSONSerializer());
//    register(new KryoSerializer());
		statisticsCollecter = new StatisticsCollecter(config);
	}

	public void register(Serializer serializer) {
		serializers.add(serializer);
	}

	public void print() {
		sortStatistics();
		System.out.println("\n--------------------------------------------------------------------------------------------");
		System.out.println(Statistics.header());
		for (Statistics statistics : statisticsList) {
			System.out.println(statistics.toString());
		}
	}

	private void sortStatistics() {
		Collections.sort(statisticsList, (o1, o2) -> {
			long avgCostTime1 = o1.getAvgSerializeCostTime() + o1.getAvgDeserializeCostTime();
			long avgCostTime2 = o2.getAvgSerializeCostTime() + o2.getAvgDeserializeCostTime();
			if (avgCostTime1 > avgCostTime2) {
				return 1;
			} else if (avgCostTime1 == avgCostTime2) {
				return 0;
			} else {
				return -1;
			}
		});
	}

	public void statistics() {
		final List<Statistics> statisticsList = new ArrayList<>();

		final CountDownLatch countDownLatch = new CountDownLatch(serializers.size());
		for (final Serializer serializer : serializers) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Statistics statistics = statisticsCollecter.collect(serializer, config.getTargetObject());
					statistics.setName(statistics.getName() + "-" + config.getNameSuffix());
					statisticsList.add(statistics);
					countDownLatch.countDown();
				}
			});
			thread.start();

		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.statisticsList = statisticsList;
	}
}
