import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class Plagiarism {

	static class Line implements Comparable<Line> {
		String src;
		String str, strProcessed;

		Line(String src, String str) {
			this.src = src;
			this.str = str.replaceAll("\\s+", " ");

			// remove comments
			int cindex = str.indexOf("//");
			if (cindex > 0)
				str = str.substring(0, cindex);
			strProcessed = str;
			// remove white spaces
			strProcessed = strProcessed.replaceAll("\\s+", "");
			// remove case differences
			strProcessed = strProcessed.toLowerCase();
		}

		@Override
		public int compareTo(Line o) {
			int srccompare = src.compareTo(o.src);
			return 0 == srccompare ? str.compareTo(o.str) : srccompare;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Line) {
				Line l = (Line) o;
				return strProcessed.equals(l.strProcessed);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return strProcessed.hashCode();
		}

		@Override
		public String toString() {
			return src + ": " + str;
		}
	}

	static interface LineChecker {
		boolean check(Line s);
	}

	static class MyFrame extends JFrame {
		private static final int LABEL_WIDTH = 150;
		private static final long serialVersionUID = 1L;

		JPanel contentPanel, subPanel;

		JCheckBox fullreportCheckBox = new JCheckBox("report which lines in which files are the same");

		JTextField reportField = new JTextField();
		JLabel reportLabel = new JLabel("report will be saved to");
		JButton runButton = new JButton("run checker");
		JButton studentsChoose = new JButton("choose");
		JTextField studentsField = new JTextField();

		JLabel studentsLabel = new JLabel("directory of students");
		JButton templateChoose = new JButton("choose");
		JTextField templateField = new JTextField();

		JLabel templateLabel = new JLabel("directory of template");
		JLabel thresholdLineCountLabel = new JLabel("threshold (line count)");

		JSpinner thresholdLineCountSpinner = createSpinner();
		JLabel thresholdPercentLabel = new JLabel("threshold (percent)");

		JSpinner thresholdPercentSpinner = createSpinner();

		public MyFrame() {
			setBounds(0, 0, 600, 200);
			setMinimumSize(new Dimension(600, 200));
			setMaximumSize(new Dimension(999, 200));

			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setTitle("Plagiarism Checker");

			contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
			contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			setContentPane(contentPanel);

			studentsLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 0));
			templateLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 0));
			reportLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 0));
			thresholdPercentLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 0));
			thresholdLineCountLabel.setPreferredSize(new Dimension(LABEL_WIDTH, 0));

			// students
			subPanel = new JPanel();
			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));
			subPanel.add(studentsLabel);
			subPanel.add(studentsField);
			subPanel.add(studentsChoose);
			contentPanel.add(subPanel);

			// template
			subPanel = new JPanel();
			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));
			subPanel.add(templateLabel);
			subPanel.add(templateField);
			subPanel.add(templateChoose);
			contentPanel.add(subPanel);

			// report
			subPanel = new JPanel();
			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));
			subPanel.add(reportLabel);
			subPanel.add(reportField);
			reportField.setText(System.getProperty("user.dir") + File.separator + "report.txt");
			contentPanel.add(subPanel);

			// threshold (line count)
			subPanel = new JPanel();
			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));
			subPanel.add(thresholdLineCountLabel);
			subPanel.add(thresholdLineCountSpinner);
			contentPanel.add(subPanel);

			// threshold (percent)
			subPanel = new JPanel();
			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));
			subPanel.add(thresholdPercentLabel);
			subPanel.add(thresholdPercentSpinner);
			contentPanel.add(subPanel);

			// run
			subPanel = new JPanel();
			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.LINE_AXIS));
			subPanel.add(Box.createHorizontalGlue());
			subPanel.add(fullreportCheckBox);
			subPanel.add(runButton);
			contentPanel.add(subPanel);

			studentsChoose.addActionListener((e) -> studentsField.setText(getDirectory()));
			templateChoose.addActionListener((e) -> templateField.setText(getDirectory()));
			runButton.addActionListener((e) -> runChecker());

			// show
			setVisible(true);
		}

		JSpinner createSpinner() {
			SpinnerModel model = new SpinnerNumberModel(50, 0, 100, 1);
			return new JSpinner(model);
		}

	}

	static class Report implements Comparable<Report> {
		String fullreport = "", report = "";
		int percent, count, total;
		Student s, t;

		Report(Student s, Student t, int count, int total) {
			this.s = s;
			this.t = t;
			this.count = count;
			this.total = total;
			this.percent = (int) Math.round(count * 100.0 / total);

			fullreport += "Check: " + s.name + " against " + t.name + endl;
			fullreport += "Found: " + percent + "% identical lines (" + count + " of " + total + ")" + endl;

			report = fullreport; // short report

			ArrayList<Line> suspectList = new ArrayList<>(s.suspectSet);
			Collections.sort(suspectList);
			for (Line suspect : suspectList)
				fullreport += " - " + suspect + endl;
		}

		@Override
		public int compareTo(Report o) {
			return Integer.compare(o.count, count);
		}

		public String report(boolean full) {
			return full ? fullreport : report;
		}
	}

	static class Student {
		String name;
		ArrayList<Report> reports = new ArrayList<>();

		HashSet<Line> studentSet = new HashSet<>();
		HashSet<Line> suspectSet = new HashSet<>();

		Student(File file) {
			name = file.getName();
			readFile(file);
		}

		/**
		 * @return max count of identical lines
		 */
		int max() {
			int max = 0;
			for (Report r : reports)
				max = Math.max(max, r.count);
			return max;
		}

		void readFile(File file) {
			Plagiarism.readFile(file, studentSet, (s) -> {
				if (s.src.endsWith("Test.java"))
					return false;
				if (baseSet.contains(s))
					return false;
				if (s.str.startsWith("/"))
					return false;
				if (s.str.startsWith("*"))
					return false;
				if (s.str.startsWith("#"))
					return false;
				return true;
			});
		}

		String summary() {
			String s = Util.parseName(name) + " (max=" + max() + ")" + endl;
			Collections.sort(reports);
			for (Report r : reports)
				s += " - (" + r.percent + "%) -> " + Util.parseName(r.t.name) + endl;

			return s;
		}
	}

	static class Util {
		static final String[] extensions = { ".c", ".cpp", ".h", ".java" };

		/**
		 * @return <code>name</code> with only the initial letter capitalized. e.g. JACK -> Jack
		 */
		static String cap(String name) {
			if (name.isEmpty())
				return "(empty name)";
			name = name.toLowerCase();
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			return name;
		}

		/**
		 * @return <code>true</code> if name ends with common code extensions
		 */
		static boolean checkExtension(String name) {
			for (String extension : extensions)
				if (name.endsWith(extension))
					return true;
			return false;
		}

		static String parseName(String name) {
			String[] arr = name.split("_|\\.");
			if (arr.length > 1)
				name = cap(arr[0]) + " " + cap(arr[1]);
			return name;
		}
	}

	static final HashSet<Line> baseSet = new HashSet<>();
	static final String endl = System.lineSeparator();

	static MyFrame myFrame;

	static PrintWriter pw = null;

	static final String report = "report.txt";

	static final ArrayList<Report> reports = new ArrayList<>();

	static void compare(Student s, Student t) {
		if (s == null || t == null)
			return;
		if (s.studentSet.isEmpty() || t.studentSet.isEmpty())
			return;
		int total = s.studentSet.size();
		s.suspectSet.clear();
		for (Line line : s.studentSet) {
			if (t.studentSet.contains(line)) {
				s.suspectSet.add(line);
			}
		}
		int count = s.suspectSet.size();
		int percent = (int) Math.round(count * 100.0 / total);
		if (count >= (Integer) myFrame.thresholdLineCountSpinner.getValue() && percent >= (Integer) myFrame.thresholdPercentSpinner.getValue()) {
			reports.add(new Report(s, t, count, total));
		}
	}

	static String getDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("~"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

	public static void main(String[] args) {
		myFrame = new MyFrame();
	}

	static void println() {
		println("");
	}

	static void println(String s) {
		System.out.println(s);
		if (pw == null) {
			try {
				pw = new PrintWriter(report);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		pw.println(s);
	}

	static void readFile(File file, HashSet<Line> set, LineChecker checker) {
		String name = file.getName();
		if (file.isDirectory()) {
			for (File f : file.listFiles())
				readFile(f, set, checker);
		} else if (Util.checkExtension(name)) {
			if (name.startsWith("."))
				return; // bad file
			Scanner sc = null;
			try {
				sc = new Scanner(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			sc.useDelimiter("\\{|\\}|;\\s*\\n");
			while (sc.hasNext()) {
				Line line = new Line(name, sc.next().trim());
				if (checker == null || checker.check(line))
					set.add(line);
			}
		}
	}

	static void runChecker() {
		reports.clear();

		File studentsDir = new File(myFrame.studentsField.getText());
		if (!studentsDir.exists()) {
			JOptionPane.showMessageDialog(myFrame, "students path does not exist");
			return;
		}
		File templateDir = new File(myFrame.templateField.getText());

		// Load base
		if (templateDir.exists()) {
			for (File f : templateDir.listFiles()) {
				if (f.getName().startsWith("."))
					continue;
				readFile(f, baseSet, null);
				println("Load " + f.getName());
			}
		}
		println();
		println("Base size: " + baseSet.size());
		println();

		// Load students
		Student[] students = new Student[studentsDir.listFiles().length];
		for (int i = 0; i < students.length; i++) {
			File f = studentsDir.listFiles()[i];
			if (f.getName().startsWith("."))
				continue;
			if (f.isDirectory()) {
				students[i] = new Student(studentsDir.listFiles()[i]);
				println("Load " + students[i].name);
			}
		}
		println();

		for (int i = 0; i < students.length; i++)
			for (int j = 0; j < students.length; j++)
				if (i != j)
					compare(students[i], students[j]);

		HashSet<Student> summary = new HashSet<>();

		Collections.sort(reports);
		int id = 0;
		for (Report report : reports) {
			println("Report ID: " + ++id + endl + report.report(myFrame.fullreportCheckBox.isSelected()));

			report.s.reports.add(report);
			summary.add(report.s);
		}

		println("=======");
		println("SUMMARY");
		println("=======");
		println();

		Student[] arr = new Student[summary.size()];
		summary.toArray(arr);
		Arrays.sort(arr, new Comparator<Student>() {
			@Override
			public int compare(Student s, Student t) {
				return t.max() - s.max();
			}
		});

		for (Student s : arr)
			println(s.summary());

		println("Total: " + arr.length);

		pw.close();
	}
}
