package com.keyhunter.test.serialization.protobuffer;

import com.keyhunter.test.serialization.Serializer;
import com.keyhunter.test.serialization.util.SystemPropertyUtil;
import io.protostuff.ByteArrayInput;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffOutput;
import io.protostuff.Schema;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProtoBuffer序列化
 * 用的是ProtoStuff工具包
 *
 * @auther yingren
 * Created on 2016/12/21.
 */
public class ProtoStuffSerializer implements Serializer {


	static {
		// see io.protostuff.runtime.RuntimeEnv

		// If true, the constructor will always be obtained from {@code ReflectionFactory.newConstructorFromSerialization}.
		//
		// Enable this if you intend to avoid deserialize objects whose no-args constructor initializes (unwanted)
		// internal state. This applies to complex/framework objects.
		//
		// If you intend to fill default field values using your default constructor, leave this disabled. This normally
		// applies to java beans/data objects.
		//
		final String always_use_sun_reflection_factory = SystemPropertyUtil.get(
				"rhea.serializer.protostuff.always_use_sun_reflection_factory", "false");
		SystemPropertyUtil.setProperty("protostuff.runtime.always_use_sun_reflection_factory",
				always_use_sun_reflection_factory);

		// Disabled by default.  Writes a sentinel value (uint32) in place of null values.
		//
		// default is false
		final String allow_null_array_element = SystemPropertyUtil.get(
				"rhea.serializer.protostuff.allow_null_array_element", "false");
		SystemPropertyUtil.setProperty("protostuff.runtime.allow_null_array_element", allow_null_array_element);
	}

	private final static DefaultIdStrategy idStrategy = ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY);

	/**
	 * 缓存Schema防止每次解析Schema带来的开销
	 */
	private final static ConcurrentHashMap<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

	@Override
	public <T> byte[] serialize(final T obj) {
		final LinkedBuffer buf = LinkedBuffers.getLinkedBuffer();
		try {
			final Schema schema = RuntimeSchema.getSchema(obj.getClass());
			final ProtostuffOutput output = new ProtostuffOutput(buf);
			schema.writeTo(output, obj);
			return output.toByteArray();
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			LinkedBuffers.resetBuf(buf); // for reuse
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		final Schema<T> schema = RuntimeSchema.getSchema(clazz);
		final T msg = schema.newMessage();
		final ByteArrayInput input = new ByteArrayInput(bytes, 0, bytes.length, true);
		try {
			schema.mergeFrom(input, msg);
			input.checkLastTagWas(0);
		} catch (final IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return msg;
	}

}
