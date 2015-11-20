package concurrency;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.SwingWorker;

@SuppressWarnings("serial")
public class Flipper extends JFrame implements ActionListener {
	private static GridBagConstraints constraints;
	private JTextField[] textArray = new JTextField[5];
	private final Border border = BorderFactory.createLoweredBevelBorder();
	private final JButton startButton, pauseButton, clearAllButton;
	private JLabel[] labelArray = new JLabel[5];
	private final DecimalFormat df = new DecimalFormat("0.0000000000");
	private FlipTask flipTask;
	public static long heads = 0;
	public static long total = 0;
	public int trial = 0, fair = 0;
	public double result = 0;
	public String preCmd = null;

	private JLabel makeLabel(String content) {
		JLabel l = new JLabel(content, JLabel.LEFT);
		l.setVisible(true);
		getContentPane().add(l, constraints);
		return l;
	}

	private JTextField makeText(String tipString) {
		JTextField t = new JTextField(20);
		if(tipString.equals("Within which will be considered fair"))
			t.setEditable(true);
		else
			t.setEditable(false);
		t.setHorizontalAlignment(JTextField.RIGHT);
		t.setBorder(border);
		t.setToolTipText(tipString);
		getContentPane().add(t, constraints);
		return t;
	}

	private JButton makeButton(String caption, boolean enabled) {
		JButton b = new JButton(caption);
		b.setActionCommand(caption);
		b.addActionListener(this);
		b.setEnabled(enabled);
		getContentPane().add(b, constraints);
		return b;
	}

	public Flipper() {
		super("Flipper");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Make layout
		getContentPane().setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(3, 10, 3, 10);
		//make Labels and textFields
		String[] textLabel = { "Head", "Total", "Deviation", "Threshold",
				"Fair rate" };
		String[] toolTipKit = { "count coins head", "count total tosses",
				"percentage of heads - 50%",
				"Within which will be considered fair",
				"the propotion of trials that considered fair" };
		for(int i = 0 ;i<5;i++){
			constraints.gridy = i;
			constraints.gridwidth = 1;
			labelArray[i] = makeLabel(textLabel[i]);
			constraints.gridwidth = 2;
			textArray[i] = makeText(toolTipKit[i]);
		}
		// Make buttons
		constraints.gridy = 5;
		constraints.gridwidth = 1;
		startButton = makeButton("Start", true);
		pauseButton = makeButton("Pause", false);
		clearAllButton = makeButton("Clear-All", false);
		// Make guidance
		constraints.gridy = 6;
		constraints.gridwidth = 3;
		JTextArea instruction = new JTextArea(
				"Function: Testing the fairness of random generation\n"
						+ "Principle: Simulating a toss and analysising the deviation");
		instruction.setEditable(false);
		instruction.setBackground(new Color(236, 236, 236));
		instruction.setVisible(true);
		this.add(instruction, constraints);
		// Display the window.
		pack();
		setVisible(true);
	}

	private static class FlipPair {
		private final long heads, total;

		FlipPair(long heads, long total) {
			this.heads = heads;
			this.total = total;
		}
	}

	private class FlipTask extends SwingWorker<Void, FlipPair> {
		long heads, total;

		protected Void doInBackground() {
			heads = Flipper.heads;
			total = Flipper.total;
			Random random = new Random();
			while (!isCancelled()) {
				total++;
				if (random.nextBoolean()) {
					heads++;
				}
				publish(new FlipPair(heads, total));
			}
			return null;
		}

		@Override
		protected void process(List<FlipPair> pairs) {
			FlipPair pair = pairs.get(pairs.size() - 1);
			textArray[0].setText(String.format("%d", pair.heads));
			textArray[1].setText(String.format("%d", pair.total));
			textArray[2].setText(String.format("%.10g", ((double) pair.heads)
					/ ((double) pair.total) - 0.5));
			heads = pair.heads;
			total = pair.total;
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("Start" == e.getActionCommand()) {
			startButton.setActionCommand("Stop");
			startButton.setText("Stop");
			pauseButton.setEnabled(true);
			clearAllButton.setEnabled(false);
			iniText3();
			preCmd = "Start";
			(flipTask = new FlipTask()).execute();
		} else if ("Stop" == e.getActionCommand()) {
			startButton.setActionCommand("Start");
			startButton.setText("Start");
			pauseButton.setActionCommand("Pause");
			pauseButton.setText("Pause");
			pauseButton.setEnabled(false);
			clearAllButton.setEnabled(true);
			if (preCmd == "Resume" || preCmd == "Pause") {
			} else {
				calresult(flipTask.heads, flipTask.total);
			}
			iniText3();
			preCmd = "Stop";
			flipTask.cancel(true);
			flipTask = null;
		} else if ("Pause" == e.getActionCommand()) {
			pauseButton.setActionCommand("Resume");
			pauseButton.setText("Resume");
			clearAllButton.setEnabled(true);
			Flipper.heads = flipTask.heads;
			Flipper.total = flipTask.total;
			calresult(flipTask.heads, flipTask.total);
			preCmd = "Pause";
			flipTask.cancel(true);
		} else if ("Resume" == e.getActionCommand()) {
			pauseButton.setActionCommand("Pause");
			pauseButton.setText("Pause");
			clearAllButton.setEnabled(false);
			preCmd = "Resume";
			(flipTask = new FlipTask()).execute();
		} else if ("Clear-All" == e.getActionCommand()) {
			startButton.setActionCommand("Start");
			startButton.setText("Start");
			pauseButton.setActionCommand("Pause");
			pauseButton.setText("Pause");
			initialize();
		}
	}
	
	public void initialize(){
		if (flipTask != null) {
			flipTask.cancel(true);
		}
		trial = 0;
		fair = 0;
		heads = 0;
		total = 0;
		for(int i = 0;i<5;i++){
			textArray[i].setText(null);
		}
		textArray[3].setEditable(true);
		textArray[4].setToolTipText("the propotion that considered fair");
		textArray[4].setBackground(new Color(236, 236, 236));
		pauseButton.setEnabled(false);
		clearAllButton.setEnabled(false);
	}
	
	public void iniText3(){
		Flipper.heads = 0;
		Flipper.total = 0;
		textArray[3].setEditable(true);
	}

	public void calresult(long heads, long total) {
		String dev = df
				.format(Math.abs((Double.parseDouble(textArray[0].getText()))
						/ (Double.parseDouble(textArray[1].getText())) - 0.5));
		trial++;
		if (dev.compareTo(textArray[3].getText()) < 0) {
			fair++;
			textArray[4].setBackground(Color.green);
		} else {
			textArray[4].setBackground(Color.red);
		}
		textArray[4].setText(String.format("%1$d%%", 100 * fair / trial));
		textArray[4].setToolTipText(fair + " fairs with " + trial + " trial");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Flipper();
			}
		});
	}
}