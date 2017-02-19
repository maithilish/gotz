package in.m.picks.appender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileAppender extends Appender {

	private BlockingQueue<Object> queue;

	public FileAppender() {
		queue = new ArrayBlockingQueue<Object>(1024);
	}

	@Override
	public void run() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File("target/output.txt"));
			for (;;) {
				Object item = queue.take();
				if (item == Marker.EOF) {
					break;
				}
				String str = item.toString();
				writer.println(str);
			}
		} catch (FileNotFoundException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

	@Override
	public void append(Object object) throws InterruptedException {
		queue.put(object);
	}
}
