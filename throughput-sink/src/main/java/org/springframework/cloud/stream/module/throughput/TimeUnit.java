package org.springframework.cloud.stream.module.throughput;

/**
 * Helps with conversion between units of time. Similar in spirit to
 * {@link java.util.concurrent.TimeUnit}, but the conversion method
 * uses double and does not truncate.
 *
 * @author Eric Bottard
 */
public enum TimeUnit {
	ns(1),
	ms(1000),
	s(1000L * 1000),
	m(1000L * 1000 * 60),
	h(1000L * 1000 * 60 * 60);

	private final long nanos;

	TimeUnit(long nanos) {
		this.nanos = nanos;
	}

	public double convert(long howMany, TimeUnit original) {
		return (double) howMany * original.nanos / this.nanos;
	}

	public static void main(String[] args) {
		System.out.println(String.format("%7.2f%n", 123.44));
	}

}
