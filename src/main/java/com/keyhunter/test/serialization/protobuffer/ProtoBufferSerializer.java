package com.keyhunter.test.serialization.protobuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.keyhunter.test.serialization.Serializer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoBufferSerializer implements Serializer {

	private static Map<String, Method> pbParserMethodCache = new ConcurrentHashMap<String, Method>();

	@Override
	public <T> byte[] serialize(T object) {
		if (object instanceof Message) {
			return ((Message) object).toByteArray();
		} else {
			throw new RuntimeException(String.format("class <%s> is not protobuf", object.getClass().getName()));
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		try {
			if (clazz.getSuperclass() == GeneratedMessageV3.class) {
				Method method = getPbParserMethod(clazz);
				Parser<GeneratedMessageV3> parser = (Parser<GeneratedMessageV3>) method.invoke(null);
				return (T) parser.parseFrom(ByteString.copyFrom(bytes));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param clazz
	 * @return
	 * @throws NoSuchMethodException
	 */
	private static Method getPbParserMethod(Class<?> clazz) throws NoSuchMethodException {
		if (!pbParserMethodCache.containsKey(clazz.getName())) {
			pbParserMethodCache.put(clazz.getName(), clazz.getDeclaredMethod("parser"));
		}
		return pbParserMethodCache.get(clazz.getName());
	}
}
