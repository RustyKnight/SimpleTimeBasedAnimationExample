/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpletimebasedanimationexample;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.Instant;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;

public class SimpleTimeBasedAnimationExample {

    public static void main(String[] args) {
        new SimpleTimeBasedAnimationExample();
    }

    public SimpleTimeBasedAnimationExample() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    protected class TestPane extends JPanel {

        private AnimationPanel animationPane;

        public TestPane() {
            setLayout(new BorderLayout());
            animationPane = new AnimationPanel();
            add(animationPane);

            JToggleButton pauseButton = new JToggleButton("Run");
            pauseButton.setSelected(true);
            pauseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pauseButton.isSelected()) {
                        pauseButton.setText("Run");
                    } else {
                        pauseButton.setText("Pause");
                    }
                    animationPane.setPaused(pauseButton.isSelected());
                }
            });

            //            JButton fasterButton = new JButton("Faster");
            //            fasterButton.addActionListener(new ActionListener() {
            //                @Override
            //                public void actionPerformed(ActionEvent e) {
            //                    animationPane.setSpeed(animationPane.getSpeed() * 2);
            //                }
            //            });
            //            JButton slowerButton = new JButton("Slower");
            //            slowerButton.addActionListener(new ActionListener() {
            //                @Override
            //                public void actionPerformed(ActionEvent e) {
            //                    animationPane.setSpeed(animationPane.getSpeed() / 2);
            //                }
            //            });
            JToggleButton horizontalButton = new JToggleButton("Horizontal");
            JToggleButton verticalButton = new JToggleButton("Vertical");

            horizontalButton.setSelected(true);

            ButtonGroup bg = new ButtonGroup();
            bg.add(horizontalButton);
            bg.add(verticalButton);

            //            horizontalButton.addActionListener(new ActionListener() {
            //                @Override
            //                public void actionPerformed(ActionEvent e) {
            //                    animationPane.setLeftToRight(horizontalButton.isSelected());
            //                }
            //            });
            //
            //            verticalButton.addActionListener(new ActionListener() {
            //                @Override
            //                public void actionPerformed(ActionEvent e) {
            //                    animationPane.setLeftToRight(!verticalButton.isSelected());
            //                }
            //            });
            JPanel actionPane = new JPanel();
            actionPane.add(pauseButton);
            //            actionPane.add(slowerButton);
            //            actionPane.add(fasterButton);
            //            actionPane.add(horizontalButton);
            //            actionPane.add(verticalButton);

            add(actionPane, BorderLayout.SOUTH);

        }

    }

    public class AnimationPanel extends JPanel {

        public enum Direction {
            VERTICAL, HORIZONTAL
        }

        private Direction direction = Direction.HORIZONTAL;

        private Point2D origin = new Point2D.Double(200, 200);

        private Animator animator;

        private Range<Double> range;

        private Duration duration = Duration.ofSeconds(5);

        private Ellipse2D dot = new Ellipse2D.Double(0, 0, 20, 20);

        public AnimationPanel() {
            animator = new Animator(new Animator.Observer() {
                @Override
                public void animatorDidTick(Animator animator, double progress) {
                    double nextValue = range.valueAt(progress);
                    if (direction == Direction.HORIZONTAL) {
                        origin.setLocation(nextValue, origin.getY());
                    }
                    repaint();
                }

                @Override
                public void animatorDidComplete(Animator animator) {
                    double targetPoint = range.getTo();
                    if (direction == Direction.HORIZONTAL) {
                        range = getDotHorizontalRange();
                        if (targetPoint != range.getFrom()) {
                            range.reverse();
                        }
                    }
                    animator.setDuration(duration);
                    resume();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 400);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(Color.BLUE);

            g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g2d.draw(new Line2D.Double(0, getHeight() / 2, getWidth(), getHeight() / 2));
            g2d.draw(new Line2D.Double(getWidth() / 2, 0, getWidth() / 2, getHeight()));

            g2d.translate(origin.getX() - (dot.getWidth() / 2d), origin.getY() - (dot.getHeight() / 2d));
            g2d.fill(dot);
            g2d.dispose();
        }

        protected Range<Double> getDotHorizontalRange() {
            return new DoubleRange(dot.getWidth() / 2, getWidth() - (dot.getWidth() / 2));
        }

        protected double getHorizontalRangeDistance() {
            return ((DoubleRange) getDotHorizontalRange()).getDistance();
        }

        public void setPaused(boolean paused) {
            if (paused) {
                animator.pause();
            } else {
                if (range == null) {
                    initialiseRange();
                }
                animator.resume();
            }
        }

        protected void resume() {
            if (range == null) {
                // Try and force a restart...
                setPaused(false);
            }
            animator.resume();
        }

        protected void initialiseRange() {
            if (direction == Direction.HORIZONTAL) {
                double currentX = origin.getX();
                // Assume a positive intial direction
                double avaliableRange = Math.abs(getHorizontalRangeDistance());
                double distance = avaliableRange - currentX;
                int remainingTime = (int) (duration.toMillis() * (distance / avaliableRange));
                animator.setDuration(Duration.ofMillis(remainingTime));
                range = new DoubleRange((double) currentX, getDotHorizontalRange().getTo());
            }
        }

    }

    public abstract class Range<T> {

        private T from;
        private T to;

        public Range(T from, T to) {
            this.from = from;
            this.to = to;
        }

        public T getFrom() {
            return from;
        }

        public T getTo() {
            return to;
        }

        @Override
        public String toString() {
            return "From " + getFrom() + " to " + getTo();
        }

        public abstract T valueAt(double progress);

        public void reverse() {
            T nextFrom = to;
            to = from;
            from = nextFrom;
        }

    }

    public class DoubleRange extends Range<Double> {

        public DoubleRange(Double from, Double to) {
            super(from, to);
        }

        public Double getDistance() {
            return getTo() - getFrom();
        }

        @Override
        public Double valueAt(double progress) {
            double distance = getDistance();
            double value = distance * progress;
            value += getFrom();
            return value;
        }

    }

    public class Animator {

        public enum State {
            STOP, PAUSE, RUN
        }

        public interface Observer {

            public void animatorDidTick(Animator animator, double progress);

            public void animatorDidComplete(Animator animator);
        }

        private Duration duration = Duration.ofSeconds(5);
        // Used to manage pause support.  This will be
        // added onto the "live" runtime when the
        // animator is running
        private Duration previousRuntime = Duration.ZERO;

        private Instant epoch;

        private Observer observer;

        private State state = State.STOP;

        // This is actually used to manage the "ticks"
        private Timer ticker = new Timer(5, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (epoch == null) {
                    epoch = Instant.now();
                }
                double progress = getProgressAtCurrentTime();
                observer.animatorDidTick(Animator.this, Math.max(0, Math.min(1.0, progress)));
                if (progress >= 1.0) {
                    progress = 1.0;
                    stop();
                    observer.animatorDidComplete(Animator.this);
                }
            }
        });

        public Animator(Observer observer) {
            this.observer = observer;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public boolean isPaused() {
            return state == State.PAUSE;
        }

        public boolean isRunning() {
            return state == State.RUN;
        }

        public boolean isStopped() {
            return state == State.STOP;
        }

        public void pause() {
            ticker.stop();
            if (epoch != null) {
                Duration runtime = Duration.between(epoch, Instant.now());
                previousRuntime = previousRuntime.plus(runtime);
                state = State.PAUSE;
            }
            epoch = null;
        }

        public void resume() {
            state = State.RUN;
            ticker.start();
        }

        protected double getProgressAtCurrentTime() {
            Duration runtime = Duration.ZERO;
            if (epoch != null) {
                // The delta time between when we started and now
                runtime = Duration.between(epoch, Instant.now());
            }

            // Plus any additonal time which was recored
            runtime = runtime.plus(previousRuntime);

            return runtime.toMillis() / (double) duration.toMillis();
        }

        // This is for internal reset purposes
        protected void stop() {
            ticker.stop();
            state = State.STOP;
            previousRuntime = Duration.ZERO;
            epoch = null;
        }
    }
}
