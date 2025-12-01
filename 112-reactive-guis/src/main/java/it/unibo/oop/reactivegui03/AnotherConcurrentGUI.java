package it.unibo.oop.reactivegui03;

import it.unibo.oop.JFrameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This is a first example on how to realize a reactive GUI.
 * This shows an alternative solutions using lambdas
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final int TIME_TO_WAIT = 10_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    private final transient Agent agent = new Agent();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        new Thread(agent).start();
        final SentinelAgent agent2 = new SentinelAgent();
        new Thread(agent2).start();
        up.addActionListener(e -> agent.upCounting());
        down.addActionListener(e -> agent.downCounting());
        /*
         * Register a listener that stops it
         */
        stop.addActionListener(e -> {
            agent.stopCounting();
            stop.setEnabled(false);
            up.setEnabled(false);
            down.setEnabled(false);
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        private volatile boolean stop;
        private volatile boolean direction = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (direction) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void upCounting() {
            this.direction = true;
        }

        public void downCounting() {
            this.direction = false;
        }
    }

    private final class SentinelAgent implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(TIME_TO_WAIT);
                 SwingUtilities.invokeLater(() -> {
                    agent.stopCounting();
                    stop.setEnabled(false);
                    up.setEnabled(false);
                    down.setEnabled(false);
                 });
            } catch (final InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
}

