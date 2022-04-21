package ru.sieg.view.threadview;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SvgThreadVizualizer implements Closeable {

    private static final int MARGIN_LEFT = 32;
    private static final int MARGIN_TOP = 32;
    private static final int MARGIN_TEXT_TOP = 3;
    private static final int HEIGHT_THREAD = 8;
    private static final String COLOR_BACKGROUND = "#525257";
    private static final String COLOR_EMPTY = "#FFFFFF";
    private static final String COLOR_WORKING = "#4CBB17";
    private static final String COLOR_TEXT = "#FFFFFF";

    private final BufferedOutputStream bs;
    private final StringBuffer sb = new StringBuffer();
    private final StringBuffer whiteBoxes = new StringBuffer();

    private final Map<String, Integer> threads = new ConcurrentHashMap<>();

    private final AtomicLong firstStartTime = new AtomicLong();
    private final AtomicLong lastFinishTime = new AtomicLong();

    private final ThreadLocal<Long> currentThreadTimeStart = new ThreadLocal<>();
    private final ThreadLocal<Long> currentThreadTimeEnd = new ThreadLocal<>();
    private final ThreadLocal<Integer> threadY = ThreadLocal.withInitial(() -> MARGIN_TOP + MARGIN_TEXT_TOP);

    public SvgThreadVizualizer(final String filename) {
        try {
            bs = new BufferedOutputStream(new FileOutputStream(filename));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        sb.append("<svg width=\"WIDTH_TOTAL\" height=\"HEIGHT_TOTAL\" viewbox=\"0 0 WIDTH_TOTAL HEIGHT_TOTAL\" fill=\"none\" ")
                .append("xmlns=\"http://www.w3.org/2000/svg\">\n");
        sb.append("<style>\n   .common { font: regular 12pt inter; fill: ").append(COLOR_TEXT).append("; }\n</style>\n");
        sb.append("<rect width=\"WIDTH_TOTAL\" height=\"HEIGHT_TOTAL\" fill=\"").append(COLOR_BACKGROUND).append("\"/>");
        sb.append("WHITE_BOXES");
    }

    /**
     * Thread-local
     */
    public void threadStart(final Thread t) {
        currentThreadTimeStart.set(System.currentTimeMillis());
        firstStartTime.compareAndSet(0, currentThreadTimeStart.get());

        threads.computeIfAbsent(t.getName(), (thread) -> {

            final int nextThreadY = MARGIN_TOP + MARGIN_TEXT_TOP + threads.size() * (HEIGHT_THREAD + MARGIN_TOP);

            whiteBoxes.append("<text x=\"").append(MARGIN_LEFT)
                    .append("\" y=\"").append(nextThreadY - MARGIN_TEXT_TOP)
                    .append("\" class=\"common\">").append(t.getName())
                    .append("</text>\n");

            whiteBoxes.append("<rect x=\"").append(MARGIN_LEFT)
                    .append("\" y=\"").append(nextThreadY)
                    .append("\" width=\"WIDTH_THREAD\" height=\"").append(HEIGHT_THREAD)
                    .append("\" fill=\"").append(COLOR_EMPTY)
                    .append("\"/>\n");

            return nextThreadY;
        });
    }

    public void threadFinish(final Thread t) {
        currentThreadTimeEnd.set(System.currentTimeMillis());
        lastFinishTime.set(currentThreadTimeEnd.get());

        sb.append("<rect x=\"").append(MARGIN_LEFT + currentThreadTimeStart.get() - firstStartTime.get())
                .append("\" y=\"").append(threads.get(t.getName()))
                .append("\" width=\"").append(currentThreadTimeEnd.get() - currentThreadTimeStart.get())
                .append("\" height=\"").append(HEIGHT_THREAD)
                .append("\" fill=\"").append(COLOR_WORKING)
                .append("\"/>\n");
    }

    public void close() {
        sb.append("</svg>");

        final long widthThread = lastFinishTime.get() - firstStartTime.get();
        final long widthTotal = widthThread + MARGIN_LEFT * 2;
        final int heightTotal = Collections.max(threads.values()) + HEIGHT_THREAD + MARGIN_TOP;

        final String text = sb.toString()
                .replaceAll("WIDTH_TOTAL", String.valueOf(widthTotal))
                .replaceAll("HEIGHT_TOTAL", String.valueOf(heightTotal))
                .replaceAll("WHITE_BOXES", whiteBoxes.toString())
                .replaceAll("WIDTH_THREAD", String.valueOf(widthThread));
        try {
            bs.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                bs.close();
            } catch (final IOException e2) {
                throw new RuntimeException(e2);
            }
        }
    }
}
