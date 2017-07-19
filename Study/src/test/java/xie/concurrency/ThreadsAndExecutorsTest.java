package xie.concurrency;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Test;

public class ThreadsAndExecutorsTest {

	@Test
	public void test1() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			String threadName = Thread.currentThread().getName();
			System.out.println("Hello " + threadName);
		});
	}

	@Test
	public void test2() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			String threadName = Thread.currentThread().getName();
			System.out.println("Hello " + threadName);
		});

		try {
			System.out.println("attempt to shutdown executor");
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("tasks interrupted");
		} finally {
			if (!executor.isTerminated()) {
				System.err.println("cancel non-finished tasks");
			}
			executor.shutdownNow();
			System.out.println("shutdown finished");
		}
	}

	@Test
	public void test3() throws InterruptedException, ExecutionException {
		Callable<Integer> task = () -> {
			try {
				for (int i = 0; i < 3; i++) {
					System.out.println("task1 sleep " + (i + 1));
					TimeUnit.SECONDS.sleep(1);
				}
				return 111;
			} catch (InterruptedException e) {
				throw new IllegalStateException("task interrupted", e);
			}
		};
		Callable<Integer> task2 = () -> {
			try {
				for (int i = 0; i < 3; i++) {
					System.out.println("task2 sleep " + (i + 1));
					TimeUnit.SECONDS.sleep(1);
				}
				return 222;
			} catch (InterruptedException e) {
				throw new IllegalStateException("task interrupted", e);
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<Integer> future = executor.submit(task);
		Future<Integer> future2 = executor.submit(task2);

		while (true) {
			System.out.println("future done? " + future.isDone());
			System.out.println("future2 done? " + future2.isDone());

			if (future.isDone()) {
				System.out.println("future done? " + future.get());
			}
			if (future2.isDone()) {

				System.out.println("future done? " + future2.get());
			}
			if (future.isDone() && future2.isDone()) {
				break;
			}
			TimeUnit.MILLISECONDS.sleep(900);
		}

		System.out.println("future done? " + future.isDone());

		Integer result = future.get();
		System.out.println("future done? " + future.isDone());
		System.out.println("result: " + result);

		System.out.println("future2 done? " + future2.isDone());
		Integer result2 = future2.get();

		System.out.println("future done? " + future.isDone());
		System.out.println("result: " + result);
		System.out.println("future2 done? " + future2.isDone());
		System.out.println("result2: " + result2);
	}

	@Test
	public void test4() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newWorkStealingPool();

		List<Callable<String>> callables = Arrays.asList(
				() -> "task1",
				() -> "task2",
				() -> "task3");

		executor.invokeAll(callables)
				.stream()
				.map(future -> {
					try {
						return future.get();
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				})
				.forEach(System.out::println);
	}

	Callable<String> callable(String result, long sleepSeconds) {
		return () -> {
			TimeUnit.SECONDS.sleep(sleepSeconds);
			return result;
		};
	}

	AtomicInteger count = new AtomicInteger(0);

	void increment() {
		// count.incrementAndGet();
		// count.getAndSet(count.get() + 1);
		count.addAndGet(1);
	}

	synchronized void incrementSync() {
		// count.incrementAndGet();
		// count.getAndSet(count.get() + 1);
		count.addAndGet(1);
		count.accumulateAndGet(1, (n, m) -> m + n);
	}

	@Test
	public void test6() throws InterruptedException, ExecutionException {
		int aaa = count.accumulateAndGet(5, (n, m) -> n);
		System.out.println(aaa);
		aaa = count.accumulateAndGet(5, (n, m) -> n);
		System.out.println(aaa);
		aaa = count.accumulateAndGet(1, (n, m) -> n);
		System.out.println(aaa);
		System.out.println(ForkJoinPool.getCommonPoolParallelism());
		Map daaa;
		ConcurrentHashMap concurrentHashMap;
	}

	@Test
	public void test5() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(3);

		IntStream.range(0, 100000)
				.forEach(i -> executor.submit(this::increment));

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);
		System.out.println("XXXXXXXXXX_" + count); // 9965

		ExecutorService executor2 = Executors.newFixedThreadPool(3);
		IntStream.range(0, 100000)
				.forEach(i -> executor2.submit(this::incrementSync));

		executor2.shutdown();

		executor2.awaitTermination(1, TimeUnit.MINUTES);
		System.out.println("XXXXXXXXXX_" + count); // 9965
	}

	@Test
	public void test7() throws InterruptedException, ExecutionException {

		ConcurrentHashMap<String, Integer> concurrentHashMap = new ConcurrentHashMap<>();
		concurrentHashMap.put("1", 1);
		concurrentHashMap.put("2", 2);
		concurrentHashMap.put("3", 3);
		concurrentHashMap.put("4", 4);
		concurrentHashMap.put("5", 5);

		Integer s = concurrentHashMap.reduce(8, (v1, v2) -> {
			return v2;
		}, (v1, v2) -> {
			System.out.println(String.format("%d %d %d ", v2, v1, v2-v1));
			return v2 - v1;
		});

		System.out.println(s);
	}
}
