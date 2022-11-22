package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class AnotherConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent agent = new Agent();
        new Thread(agent).start();

        final JButton up = new JButton("UP");
        final JButton down = new JButton("DOWN");
        panel.add(up);
        panel.add(down);

        up.addActionListener((e) -> agent.upgrade());
        down.addActionListener((e) -> agent.downgrade());
        stop.addActionListener((e) -> {agent.stopCounting(); up.setEnabled(false); down.setEnabled(false); stop.setEnabled(false);});

        Thread thread_stop = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                agent.stopCounting();
            }
        });
        thread_stop.start();
    }

    private class Agent implements Runnable {
        private volatile boolean stop;
        private volatile boolean increment;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    
                    if (increment) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {

                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void upgrade() {
            this.increment = true;
        }

        public void downgrade() {
            this.increment = false;
        }
    }
}

